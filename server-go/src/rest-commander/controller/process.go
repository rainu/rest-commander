package controller

import (
	"github.com/gorilla/mux"
	"rest-commander/store"
	"rest-commander/process"
)

type ProcessRoute struct {
	userStore store.UserStore
	tokenStore store.TokenStore
	processManager process.ProcessManager
}

func ApplyProcessRouter(router *mux.Router, userStore store.UserStore, tokenStore store.TokenStore, processManager process.ProcessManager) {
	applyProcessRouter(router, &ProcessRoute{
		userStore: userStore,
		tokenStore: tokenStore,
		processManager: processManager,
	}, &AuthenticationRoute{})
}

func applyProcessRouter(router *mux.Router, controller ProcessController, adController AccessDeniedController) {
	subRouter := router.
		PathPrefix("/process").
		HeadersRegexp(HEADER_TOKEN, ".*").
		Subrouter()

	router.
		Path("/process").
		Methods("GET").
		HeadersRegexp(HEADER_TOKEN, ".*").
		HandlerFunc(controller.HandleListProcess)

	router.
		Path("/process").
		Methods("POST").
		HeadersRegexp(HEADER_TOKEN, ".*").
		Headers("Content-Type", "application/json").
		HandlerFunc(controller.HandleStartProcess)

	router.
		PathPrefix("/process").
		HandlerFunc(adController.HandleAccessDenied)

	subRouter.
		Path("/admin").
		Methods("POST").
		Headers("Content-Type", "application/json").
		HandlerFunc(controller.HandleStartProcessAdmin)

	subRouter.
		Path("/{pid}/{signal}").
		Methods("POST").
		Headers("Content-Type", "application/json").
		HandlerFunc(controller.HandleProcessSignal)

	subRouter.
		Path("/{pid}").
		Methods("POST").
		Headers("Content-Type", "application/json").
		HandlerFunc(controller.HandleProcessInput)

	subRouter.
		Path("/{pid}").
		Methods("GET").
		HandlerFunc(controller.HandleProcessStatus)

	subRouter.
		Path("/{pid}/{stream}").
		Headers("Accept", "application/octet-stream").
		HeadersRegexp("Range", "([0-9]+)-").
		Methods("GET").
		HandlerFunc(controller.HandleProcessOutput)
}