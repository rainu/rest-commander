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
)

type LinuxProcessManager struct {
	procManager procFileManager
	processHandles map[string]*exec.Cmd
}

var PID_PATTERN, _ = regexp.Compile("[0-9]+")
var SIGNAL_PATTERN, _ = regexp.Compile("[0-9]+")

func NewLinuxProcessManager() *LinuxProcessManager {
	return &LinuxProcessManager{
		procManager: &defaultProcFileManager{},
		processHandles: make(map[string]*exec.Cmd),
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
		process.ReturnCode = 1337	//TODO
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

func (p *LinuxProcessManager) StartProcessAsUser(username string, password string, command string, arguments []string, environment map[string]string, workingDirectory string) string {
	return ""
}

func (p *LinuxProcessManager) StartProcess(command string, arguments []string, environment map[string]string, workingDirectory string) (string, error) {
	cmd := exec.Command(command, arguments...)

	if environment != nil && len(environment) > 0 {
		rawEnv := make([]string, len(environment), len(environment))
		i := 0
		for key, value := range environment {
			rawEnv[i] = key + "=" + value
			i++
		}

		cmd.Env = rawEnv
	}
	cmd.Dir = workingDirectory
	err := cmd.Start()
	if err != nil {
		return "", err
	}

	pid := strconv.Itoa(cmd.Process.Pid)
	p.processHandles[pid] = cmd

	return pid, nil
}

func (p *LinuxProcessManager) SendSignal(pid string, signal string) int {
	return -1
}

func (p *LinuxProcessManager) SendInput(pid string, rawInput []byte) {

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