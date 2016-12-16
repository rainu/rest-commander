package controller

import "net/http"

type HandlerMiddleware func(http.Handler) http.Handler
type HandlerFuncMiddleware func(func(http.ResponseWriter, *http.Request)) http.Handler