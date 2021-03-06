package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"github.com/gorilla/handlers"
	"rest-commander/controller"
	"rest-commander/store"
	"rest-commander/process"
	"os"
	"log"
	"io/ioutil"
)

func main() {
	workTempDir, err := ioutil.TempDir(os.TempDir(), "rest-commander_")
	if err != nil {
		panic("Could not create temp directory!" + err.Error())
	}
	defer os.RemoveAll(workTempDir)
	os.Setenv("working.temp", workTempDir)

	userStore := store.NewUserStore()
	tokenStore := store.NewAuthenticationTokenStore()
	processManager := process.NewProcessManager()

	router := mux.NewRouter()
	controller.ApplyAuthenticationRouter(router, userStore, tokenStore)
	controller.ApplyProcessRouter(router, userStore, tokenStore, processManager)

	handlerChain := controller.PanicMiddleware(router)
	handlerChain = controller.ContextMiddleware(handlerChain)
	handlerChain = handlers.LoggingHandler(os.Stdout, handlerChain)

	log.Fatal(http.ListenAndServe(":8080", handlerChain))
}