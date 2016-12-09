package controller

import (
	"github.com/gorilla/mux"
	"rest-commander/store"
)

type ProcessRoute struct {
	userStore store.UserStore
}

func ApplyProcessRouter(router *mux.Router, userStore store.UserStore) {
	applyProcessRouter(router, &ProcessRoute{
		userStore: userStore,
	}, &AuthenticationRoute{})
}

func applyProcessRouter(router *mux.Router, controller ProcessController, adController AccessDeniedController) {
	subRouter := router.
		PathPrefix("/process").
		HeadersRegexp("x-auth-token", ".*").
		Subrouter()

	router.
		Path("/process").
		Methods("GET").
		HeadersRegexp("x-auth-token", ".*").
		HandlerFunc(controller.HandleListProcess)

	router.
		Path("/process").
		Methods("POST").
		HeadersRegexp("x-auth-token", ".*").
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