package process

import (
	"rest-commander/model"
)

type ProcessManager interface {
	ListProcess() []*model.Process
	Process(pid string) (*model.Process, error)
	StartProcessAsUser(username string, password string, command string, arguments []string, environment map[string]string, workingDirectory string) (string, error)
	StartProcess(command string, arguments []string, environment map[string]string, workingDirectory string) (string, error)
	SendSignal(pid string, signal string) int
	SendInput(pid string, rawInput []byte)
	ReadOutput(pid string, start int64) *Data
	ReadError(pid string, start int64) *Data
}

type Data struct {
	Content []byte
	Read int
}

type ProcessNotFoundError struct {
	Pid string
}

func (p * ProcessNotFoundError) Error() string {
	return "No process found for " + p.Pid
}
