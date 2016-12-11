package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"log"
	"rest-commander/controller"
	"rest-commander/store"
)

func main() {
	router := mux.NewRouter()
	userStore := store.NewUserStore()
	tokenStore := store.NewAuthenticationTokenStore()

	controller.ApplyAuthenticationRouter(router, userStore, tokenStore)
	controller.ApplyProcessRouter(router, userStore)

	log.Fatal(http.ListenAndServe(":8080", router))
}