package controller

import (
	"github.com/gorilla/mux"
	"net/http"
)

type ProcessRoute struct {
	HandleAccessDenied http.HandlerFunc
	HandleListProcess http.HandlerFunc
	HandleStartProcess http.HandlerFunc
	HandleStartProcessAdmin http.HandlerFunc
	HandleProcessSignal http.HandlerFunc
	HandleProcessInput http.HandlerFunc
	HandleProcessOutput http.HandlerFunc
	HandleProcessStatus http.HandlerFunc
}

func ApplyProcessRouter(router *mux.Router) {
	applyProcessRouter(router, ProcessRoute{
		HandleAccessDenied: HandleAccessDenied,
		HandleListProcess: handleListProcess,
		HandleStartProcess: handleStartProcess,
		HandleStartProcessAdmin: handleStartProcessAdmin,
		HandleProcessSignal: handleProcessSignal,
		HandleProcessInput: handleProcessInput,
		HandleProcessOutput: handleProcessOutput,
		HandleProcessStatus: handleProcessStatus,
	})
}

func applyProcessRouter(router *mux.Router, route ProcessRoute) {
	subRouter := router.
		PathPrefix("/process").
		HeadersRegexp("x-auth-token", ".*").
		Subrouter()

	router.
		Path("/process").
		Methods("GET").
		HeadersRegexp("x-auth-token", ".*").
		HandlerFunc(route.HandleListProcess)

	router.
		Path("/process").
		Methods("POST").
		HeadersRegexp("x-auth-token", ".*").
		Headers("Content-Type", "application/json").
		HandlerFunc(route.HandleStartProcess)

	router.
		PathPrefix("/process").
		HandlerFunc(route.HandleAccessDenied)

	subRouter.
		Path("/admin").
		Methods("POST").
		Headers("Content-Type", "application/json").
		HandlerFunc(route.HandleStartProcessAdmin)

	subRouter.
		Path("/{pid}/{signal}").
		Methods("POST").
		Headers("Content-Type", "application/json").
		HandlerFunc(route.HandleProcessSignal)

	subRouter.
		Path("/{pid}").
		Methods("POST").
		Headers("Content-Type", "application/json").
		HandlerFunc(route.HandleProcessInput)

	subRouter.
		Path("/{pid}").
		Methods("GET").
		HandlerFunc(route.HandleProcessStatus)

	subRouter.
		Path("/{pid}/{stream}").
		Headers("Accept", "application/octet-stream").
		HeadersRegexp("Range", "([0-9]+)-").
		Methods("GET").
		HandlerFunc(route.HandleProcessOutput)
}