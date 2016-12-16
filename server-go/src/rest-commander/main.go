package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"log"
	"rest-commander/controller"
	"rest-commander/store"
	"rest-commander/process"
)

func main() {
	router := mux.NewRouter()
	userStore := store.NewUserStore()
	tokenStore := store.NewAuthenticationTokenStore()
	processManager := process.NewProcessManager()

	controller.ApplyAuthenticationRouter(router, userStore, tokenStore)
	controller.ApplyProcessRouter(router, userStore, tokenStore, processManager)

	handler := controller.ContextMiddleware(router)

	log.Fatal(http.ListenAndServe(":8080", handler))
}