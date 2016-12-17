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
	authMiddleware := AuthenticationMiddleware{
		tokenStore: tokenStore,
	}

	applyProcessRouter(router, &ProcessRoute{
		userStore: userStore,
		tokenStore: tokenStore,
		processManager: processManager,
	},
	authMiddleware.AuthenticationFuncMiddleware)
}

func applyProcessRouter(router *mux.Router, controller ProcessController, authMiddleware HandlerFuncMiddleware) {
	subRouter := router.
		PathPrefix("/process").
		Subrouter()

	router.
		Path("/process").
		Methods("GET").
		Handler(authMiddleware(controller.HandleListProcess))

	router.
		Path("/process").
		Methods("POST").
		Headers("Content-Type", "application/json").
		Handler(authMiddleware(controller.HandleStartProcess))

	subRouter.
		Path("/admin").
		Methods("POST").
		Headers("Content-Type", "application/json").
		Handler(authMiddleware(controller.HandleStartProcessAdmin))

	subRouter.
		Path("/{pid}/{signal}").
		Methods("POST").
		Handler(authMiddleware(controller.HandleProcessSignal))

	subRouter.
		Path("/{pid}").
		Methods("POST").
		Headers("Content-Type", "application/json").
		Handler(authMiddleware(controller.HandleProcessInput))

	subRouter.
		Path("/{pid}").
		Methods("GET").
		Handler(authMiddleware(controller.HandleProcessStatus))

	subRouter.
		Path("/{pid}/{stream}").
		Headers("Accept", "application/octet-stream").
		HeadersRegexp("Range", "([0-9]+)-").
		Methods("GET").
		Handler(authMiddleware(controller.HandleProcessOutput))
}