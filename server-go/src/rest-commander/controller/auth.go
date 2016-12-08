package controller

import (
	"github.com/gorilla/mux"
	"net/http"
)

type AuthenticationRoute struct {
	HandleLogin http.HandlerFunc
	HandleLogout http.HandlerFunc
}

func ApplyAuthenticationRouter(router *mux.Router) {
	applyAuthenticationRouter(router, AuthenticationRoute{
		HandleLogin: handleLogin,
		HandleLogout: handleLogout,
	})
}

func applyAuthenticationRouter(router *mux.Router, route AuthenticationRoute) {
	subRouter := router.PathPrefix("/auth").Subrouter()

	subRouter.
		Methods("POST").
		Path("/login").
		HandlerFunc(route.HandleLogin)

	subRouter.
		Methods("POST").
		HeadersRegexp("x-auth-token", ".*").
		Path("/logout").
		HandlerFunc(route.HandleLogout)
}