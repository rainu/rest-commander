package controller

import (
	"net/http"
	"encoding/json"
	"github.com/gorilla/mux"
	"rest-commander/model/dto"
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
}

func (t* ProcessRoute) HandleProcessInput(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessOutput(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessStatus(w http.ResponseWriter, r *http.Request){
	pid := mux.Vars(r)["pid"]
	process, err := t.processManager.Process(pid);
	t.checkProcess(err)

	json.NewEncoder(w).Encode(process)
}
