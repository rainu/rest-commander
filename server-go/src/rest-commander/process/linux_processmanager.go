package process

import (
	"rest-commander/model"
	"regexp"
	"io/ioutil"
	"os"
	"strings"
	"syscall"
	"os/user"
	"fmt"
)

type LinuxProcessManager struct {
	procManager procFileManager
}

var PID_PATTERN, _ = regexp.Compile("[0-9]+")
var SIGNAL_PATTERN, _ = regexp.Compile("[0-9]+")

func NewLinuxProcessManager() *LinuxProcessManager {
	return &LinuxProcessManager{
		procManager: &defaultProcFileManager{},
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

	process.User = fmt.Sprint(processDir.Sys().(*syscall.Stat_t).Uid)
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

func (p *LinuxProcessManager) Process(pid string) *model.Process {
	return nil
}

func (p *LinuxProcessManager) StartProcessAsUser(username string, password string, command string, arguments []string, environment map[string]string, workingDirectory string) string {
	return ""
}

func (p *LinuxProcessManager) StartProcess(command string, arguments []string, environment map[string]string, workingDirectory string) string {
	return ""
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