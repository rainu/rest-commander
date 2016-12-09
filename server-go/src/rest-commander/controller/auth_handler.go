package controller

import (
	"net/http"
	"strconv"
)

type AuthenticationController interface {
	HandleLogin(w http.ResponseWriter, r *http.Request)
	HandleLogout(w http.ResponseWriter, r *http.Request)
}

type AccessDeniedController interface {
	HandleAccessDenied(w http.ResponseWriter, r *http.Request)
}

func (t *AuthenticationRoute) HandleAccessDenied(w http.ResponseWriter, r *http.Request){
	println("access denied!")
}

func (t *AuthenticationRoute) HandleLogin(w http.ResponseWriter, r *http.Request){
	println(strconv.FormatBool(t.userStore.Contains("Sven")))
}

func (t *AuthenticationRoute) HandleLogout(w http.ResponseWriter, r *http.Request){
	println("logout")
}