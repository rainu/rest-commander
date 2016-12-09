package controller

import (
	"net/http"
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

func (t* ProcessRoute) HandleListProcess(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleStartProcess(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleStartProcessAdmin(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessSignal(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessInput(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessOutput(w http.ResponseWriter, r *http.Request){
}

func (t* ProcessRoute) HandleProcessStatus(w http.ResponseWriter, r *http.Request){
}
