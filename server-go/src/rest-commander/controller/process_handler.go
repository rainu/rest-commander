package controller

import (
	"net/http"
	"rest-commander/store"
	"encoding/json"
	"os"
	"os/user"
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

func (t *ProcessRoute) checkAuthToken(w http.ResponseWriter, r *http.Request) (*store.AuthenticationToken, bool) {
	if os.Getenv("debug_mode") == "true" {
		currentUser, _ := user.Current()

		return &store.AuthenticationToken{
			Username: currentUser.Username,
		}, true
	}

	token := ExtractTokenFromRequest(r)
	if ! t.tokenStore.Contains(token) {
		HandleAccessDenied(w, r)
		return nil, false
	}

	return t.tokenStore.Get(token), true
}

func (t *ProcessRoute) checkProcess(w http.ResponseWriter, err error) (bool) {
	if err != nil {
		response := dto.ErrorResponse{Message: err.Error(), }

		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(response)
		return false
	}
	return true
}

func (t* ProcessRoute) HandleListProcess(w http.ResponseWriter, r *http.Request){
	_, auth := t.checkAuthToken(w, r)
	if ! auth {
		return
	}

	processList := t.processManager.ListProcess()
	json.NewEncoder(w).Encode(processList)
}

func (t* ProcessRoute) HandleStartProcess(w http.ResponseWriter, r *http.Request){
	token, auth := t.checkAuthToken(w, r)
	if ! auth {
		return
	}
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
	_, auth := t.checkAuthToken(w, r)
	if ! auth {
		return
	}

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
	_, auth := t.checkAuthToken(w, r)
	if ! auth {
		return
	}

	pid := mux.Vars(r)["pid"]
	process, err := t.processManager.Process(pid);
	if t.checkProcess(w, err) {
		json.NewEncoder(w).Encode(process)
	}
}
