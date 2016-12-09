package controller

import (
	"github.com/gorilla/mux"
	"rest-commander/store"
)

type AuthenticationRoute struct {
	userStore store.UserStore
}

func ApplyAuthenticationRouter(router *mux.Router, userStore store.UserStore) {
	applyAuthenticationRouter(router, &AuthenticationRoute{
		userStore: userStore,
	})
}

func applyAuthenticationRouter(router *mux.Router, controller AuthenticationController) {
	subRouter := router.PathPrefix("/auth").Subrouter()

	subRouter.
		Methods("POST").
		Path("/login").
		HandlerFunc(controller.HandleLogin)

	subRouter.
		Methods("POST").
		HeadersRegexp("x-auth-token", ".*").
		Path("/logout").
		HandlerFunc(controller.HandleLogout)
}