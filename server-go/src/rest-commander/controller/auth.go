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
	authMiddleware := AuthenticationMiddleware{
		tokenStore: tokenStore,
	}

	applyAuthenticationRouter(router, &AuthenticationRoute{
		userStore: userStore,
		tokenStore: tokenStore,
	},
	authMiddleware.AuthenticationFuncMiddleware)
}

func applyAuthenticationRouter(router *mux.Router, controller AuthenticationController, autMiddleware HandlerFuncMiddleware) {
	subRouter := router.PathPrefix("/auth").Subrouter()

	subRouter.
		Methods("POST").
		Path("/login").
		HandlerFunc(controller.HandleLogin)

	subRouter.
		Methods("POST").
		Path("/logout").
		Handler(autMiddleware(controller.HandleLogout))
}