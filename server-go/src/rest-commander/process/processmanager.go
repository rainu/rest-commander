package process

import (
	"rest-commander/model"
	"runtime"
)

type ProcessManager interface {
	ListProcess() []*model.Process
	Process(pid string) *model.Process
	StartProcessAsUser(username string, password string, command string, arguments []string, environment map[string]string, workingDirectory string) string
	StartProcess(command string, arguments []string, environment map[string]string, workingDirectory string) string
	SendSignal(pid string, signal string) int
	SendInput(pid string, rawInput []byte)
	ReadOutput(pid string, start int64) *Data
	ReadError(pid string, start int64) *Data
}

type Data struct {
	Content []byte
	Read int
}

func NewProcessManager() ProcessManager {
	switch runtime.GOOS {
	case "linux":
		return NewLinuxProcessManager()
	default:
		panic("The current os is not supported!")
	}
}