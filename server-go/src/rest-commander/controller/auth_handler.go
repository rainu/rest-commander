package controller

import (
	"net/http"
)

func HandleAccessDenied(w http.ResponseWriter, r *http.Request){
	println("access denied!")
}

func handleLogin(w http.ResponseWriter, r *http.Request){
	println("login")
}

func handleLogout(w http.ResponseWriter, r *http.Request){
	println("logout")
}