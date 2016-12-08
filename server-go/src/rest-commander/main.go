package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"log"
	"rest-commander/controller"
)

func main() {
	router := mux.NewRouter()
	controller.ApplyAuthenticationRouter(router)
	controller.ApplyProcessRouter(router)

	log.Fatal(http.ListenAndServe(":8080", router))
}