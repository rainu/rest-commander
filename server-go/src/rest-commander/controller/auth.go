package controller

import (
	"github.com/gorilla/mux"
	"rest-commander/store"
)

const HEADER_TOKEN = "X-Auth-Token"

type AuthenticationRoute struct {
	userStore store.UserStore
	tokenStore store.TokenStore
}

func ApplyAuthenticationRouter(router *mux.Router, userStore store.UserStore, tokenStore store.TokenStore) {
	applyAuthenticationRouter(router, &AuthenticationRoute{
		userStore: userStore,
		tokenStore: tokenStore,
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
		HeadersRegexp(HEADER_TOKEN, ".*").
		Path("/logout").
		HandlerFunc(controller.HandleLogout)
}