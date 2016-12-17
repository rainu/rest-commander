package controller

import (
	"net/http"
	"encoding/json"
	"github.com/gorilla/mux"
	"rest-commander/model/dto"
	"rest-commander/store"
	"strings"
	"rest-commander/process"
	"encoding/base64"
)

type ProcessController interface {
	HandleListProcess(w http.ResponseWriter, r *http.Request)
	HandleStartProcess(w http.ResponseWriter, r *http.Request)
	HandleStartProcessAdmin(w http.ResponseWriter, r *http.Request)
	HandleProcessSignal(w http.ResponseWriter, r *http.Request)
	HandleProcessInput(w http.ResponseWriter, r *http.Request)
	HandleProcessOutput(w http.ResponseWriter, r *http.Request)
	HandleProcessStatus(w http.ResponseWriter, r *http.Request)
}

func (t *ProcessRoute) checkProcess(err error) {
	if err != nil {
		panic(err)
	}
}

func (t *ProcessRoute) checkProcessOwner(pid string, user *store.User) {
	if user.Roles.Contains(store.ROLE_ADMIN) {
		return
	}

	p, err := t.processManager.Process(pid)
	if err != nil {
		panic(err)
	}

	if p.User != user.Username {
		//the su command always run as root but the underlying process runs as the user
		if ! strings.HasPrefix(p.Commandline, "su " + user.Username) {
			panic(&process.ProcessNotFoundError{ Pid: pid, })
		}
	}
}

func (t* ProcessRoute) HandleListProcess(w http.ResponseWriter, r *http.Request){
	processList := t.processManager.ListProcess()
	json.NewEncoder(w).Encode(processList)
}

func (t* ProcessRoute) HandleStartProcess(w http.ResponseWriter, r *http.Request){
	token := GetAuthtokenFromRequest(r)
	user := t.userStore.Get(token.Username)

	var processReq dto.ProcessRequest
	json.NewDecoder(r.Body).Decode(&processReq)

	pid, err := t.processManager.StartProcessAsUser(
		user.Username,
		user.Password,
		processReq.Command,
		processReq.Arguments,
		processReq.Environment,
		processReq.WorkingDir);

	res := &dto.ProcessCreateResponse{
		Pid: pid, Created: err == nil,
	}
	json.NewEncoder(w).Encode(res)
}

func (t* ProcessRoute) HandleStartProcessAdmin(w http.ResponseWriter, r *http.Request){
	var processReq dto.ProcessRequest
	json.NewDecoder(r.Body).Decode(&processReq)

	pid, err := t.processManager.StartProcess(
		processReq.Command,
		processReq.Arguments,
		processReq.Environment,
		processReq.WorkingDir);

	res := &dto.ProcessCreateResponse{
		Pid: pid, Created: err == nil,
	}
	json.NewEncoder(w).Encode(res)
}

func (t* ProcessRoute) HandleProcessSignal(w http.ResponseWriter, r *http.Request){
	token := GetAuthtokenFromRequest(r)
	user := t.userStore.Get(token.Username)
	pid := mux.Vars(r)["pid"]
	signal := mux.Vars(r)["signal"]

	t.checkProcessOwner(pid, user)
	returnCode, err := t.processManager.SendSignal(pid, signal)
	if err != nil {
		panic(err)
	}

	res := &dto.ProcessSignalResponse{
		ReturnCode: returnCode,
	}
	json.NewEncoder(w).Encode(res)
}

func (t* ProcessRoute) HandleProcessInput(w http.ResponseWriter, r *http.Request){
	token := GetAuthtokenFromRequest(r)
	user := t.userStore.Get(token.Username)
	pid := mux.Vars(r)["pid"]

	t.checkProcessOwner(pid, user)

	var processInputReq dto.ProcessInputRequest
	json.NewDecoder(r.Body).Decode(&processInputReq)

	var rawInput []byte

	if processInputReq.Raw != "" {
		var err error
		rawInput, err = base64.StdEncoding.DecodeString(processInputReq.Raw)
		if err != nil {
			panic(err)
		}
	} else if processInputReq.Input != "" {
		rawInput = []byte(t.replaceSpecialCharacters(processInputReq.Input))
	} else {
		return
	}

	t.processManager.SendInput(pid, rawInput)
}

func (t* ProcessRoute) replaceSpecialCharacters(input string) string {
	result := strings.Replace(input, "\\\t", "\t", -1)
	result = strings.Replace(result, "\\\b", "\b", -1)
	result = strings.Replace(result, "\\\n", "\n", -1)
	result = strings.Replace(result, "\\\r", "\r", -1)
	result = strings.Replace(result, "\\\f", "\f", -1)

	return result
}

func (t* ProcessRoute) HandleProcessOutput(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessStatus(w http.ResponseWriter, r *http.Request){
	pid := mux.Vars(r)["pid"]
	process, err := t.processManager.Process(pid);
	t.checkProcess(err)

	json.NewEncoder(w).Encode(process)
}
