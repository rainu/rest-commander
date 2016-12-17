package process

import (
	"rest-commander/model"
	"regexp"
	"io/ioutil"
	"os"
	"strings"
	"syscall"
	"os/user"
	"os/exec"
	"fmt"
	"strconv"
	"errors"
	"bytes"
	"io"
)

type LinuxProcessManager struct {
	procManager procFileManager
	processHandles map[string]*processHandle
}

type processHandle struct {
	pid string
	command *exec.Cmd
	stdIn io.WriteCloser
	stdOut io.ReadCloser
	stdErr io.ReadCloser
}

var PID_PATTERN, _ = regexp.Compile("[0-9]+")
var SIGNAL_PATTERN, _ = regexp.Compile("[0-9]+")

func NewProcessManager() *LinuxProcessManager {
	return &LinuxProcessManager{
		procManager: &defaultProcFileManager{},
		processHandles: make(map[string]*processHandle),
	}
}

func (p *LinuxProcessManager) ListProcess() []*model.Process {
	dirs, err := ioutil.ReadDir(p.procManager.getProcDir())
	if err != nil {
		return []*model.Process{}
	}

	result := make([]*model.Process, 0, len(dirs))
	for _, dir := range dirs {
		if PID_PATTERN.Match([]byte(dir.Name())) {
			result = append(result, p.toProcess(dir.Name()))
		}
	}

	return result
}

func (p *LinuxProcessManager) toProcess(pid string) *model.Process {
	process := model.Process{}
	process.Id = pid
	process.ReturnCode = -1

	processDir, err := os.Stat(p.procManager.getProcDir() + "/" + pid)
	process.Running = err == nil

	if process.Running {
		process.User = fmt.Sprint(processDir.Sys().(*syscall.Stat_t).Uid)
	}

	uidUser, err := user.LookupId(process.User)
	if err == nil {
		process.User = uidUser.Username
	}

	stateFile, err := ioutil.ReadFile(p.procManager.getProcDir() + "/" + pid + "/stat")
	if err == nil {
		process.Parent = strings.Split(string(stateFile), " ")[3]
	}

	commandFile, err := ioutil.ReadFile(p.procManager.getProcDir() + "/" + pid + "/cmdline")
	if err == nil {
		process.Commandline = strings.Replace(string(commandFile), string([]byte{0}), " ", -1)
	}

	envFile, err := ioutil.ReadFile(p.procManager.getProcDir() + "/" + pid + "/environ")
	if err == nil {
		process.Environment = make(map[string]string)
		envSplit := strings.Split(string(envFile), string([]byte{0}))
		for _, env := range envSplit {
			keyVal := strings.Split(env, "=")
			key := keyVal[0]
			val := strings.Join(keyVal[1:], "=")

			process.Environment[key] = val
		}
	}

	return &process
}

func (p *LinuxProcessManager) Process(pid string) (*model.Process, error) {
	err := p.checkPid(pid)
	if err != nil {
		return nil, err
	}

	process := p.toProcess(pid)
	err = p.checkProcess(process)

	if err != nil {
		return nil, err
	}

	if ! process.Running && p.processHandles[pid] != nil {
		// i can only know the return code if i starts the process!
		if p.processHandles[pid].command.ProcessState.Success() {
			process.ReturnCode = 0
		} else {
			// The program has exited with an exit code != 0

			// This works on both Unix and Windows. Although package
			// syscall is generally platform dependent, WaitStatus is
			// defined for both Unix and Windows and in both cases has
			// an ExitStatus() method with the same signature.
			if status, ok := p.processHandles[pid].command.ProcessState.Sys().(syscall.WaitStatus); ok {
				process.ReturnCode = status.ExitStatus()
			}
		}
	}

	return process, nil
}

func (p *LinuxProcessManager) checkPid(pid string) error {
	if(pid == "" || !PID_PATTERN.MatchString(pid)) {
		return &ProcessNotFoundError{pid,}
	}

	return nil
}

func (p *LinuxProcessManager) checkProcess(process *model.Process) error {
	if _, exists := p.processHandles[process.Id]; exists {
		return nil
	}

	if ! process.Running {
		return &ProcessNotFoundError{Pid: process.Id}
	}

	return nil
}

func (p *LinuxProcessManager) StartProcessAsUser(username string, password string, command string, arguments []string, environment map[string]string, workingDirectory string) (string , error){
	if username == "" || password == "" {
		return "", errors.New("Username and password are mandatory!")
	}
	var userCommand bytes.Buffer

	userCommand.WriteString(command)
	for _, arg := range arguments {
		userCommand.WriteString(" " + arg)
	}

	suArgs := []string{
		username, "-c", userCommand.String(),
	}

	handle, err := p.startProcess("su", suArgs, environment, workingDirectory)

	if err != nil {
		return "", err
	}

	_, err = handle.stdIn.Write([]byte(password + "\n"))
	if err != nil {
		return "", err
	}

	return handle.pid, nil
}

func (p *LinuxProcessManager) StartProcess(command string, arguments []string, environment map[string]string, workingDirectory string) (string, error) {
	handle, err := p.startProcess(command, arguments, environment, workingDirectory)

	if err != nil {
		return "", err
	}

	return handle.pid, nil
}

func (p *LinuxProcessManager) startProcess(command string, arguments []string, environment map[string]string, workingDirectory string) (*processHandle, error) {
	handle := &processHandle{}
	handle.command = exec.Command(command, arguments...)

	if environment != nil && len(environment) > 0 {
		rawEnv := make([]string, len(environment), len(environment))
		i := 0
		for key, value := range environment {
			rawEnv[i] = key + "=" + value
			i++
		}

		handle.command.Env = rawEnv
	}
	handle.command.Dir = workingDirectory

	var err error
	handle.stdIn, err = handle.command.StdinPipe()
	if err != nil {
		return nil, err
	}
	handle.stdOut, err = handle.command.StdoutPipe()
	if err != nil {
		return nil, err
	}
	handle.stdErr, err = handle.command.StderrPipe()
	if err != nil {
		return nil, err
	}

	err = handle.command.Start()
	if err != nil {
		return nil, err
	}

	handle.pid = strconv.Itoa(handle.command.Process.Pid)
	p.processHandles[handle.pid] = handle

	return handle, nil
}

func (p *LinuxProcessManager) SendSignal(pid string, signal string) (int, error) {
	err := p.checkPid(pid)
	if err != nil {
		return -1, err
	}

	err = p.checkProcess(p.toProcess(pid))
	if err != nil {
		return -1, err
	}

	killCommand := exec.Command("kill")

	if SIGNAL_PATTERN.MatchString(signal) {
		killCommand.Args = append(killCommand.Args, "-" + signal)
	} else {
		killCommand.Args = append(killCommand.Args,
			"-s" + signal,
			signal,
		)
	}
	killCommand.Args = append(killCommand.Args, pid)

	err = killCommand.Run()
	if err != nil {
		return -1, err
	}

	if status, ok := killCommand.ProcessState.Sys().(syscall.WaitStatus); ok {
		return status.ExitStatus(), nil
	}
	return 0, nil
}

func (p *LinuxProcessManager) SendInput(pid string, rawInput []byte) error {
	err := p.checkPid(pid)
	if err != nil {
		return err
	}

	err = p.checkProcess(p.toProcess(pid))
	if err != nil {
		return err
	}

	processHandle := p.processHandles[pid]
	if processHandle == nil {
		return &ProcessNotFoundError{ Pid: pid, }
	}

	processHandle.stdIn.Write(rawInput)
	return nil
}

func (p *LinuxProcessManager) ReadOutput(pid string, start int64) *Data {
	return nil
}

func (p *LinuxProcessManager) ReadError(pid string, start int64) *Data {
	return nil
}

type procFileManager interface {
	getProcDir() string
}

type defaultProcFileManager struct {}

func (pm *defaultProcFileManager) getProcDir() string {
	return "/proc/"
}

func getProcessDir(procDir string, pid string) string {
	return procDir + "/" + pid
}