package controller

import (
	"github.com/gorilla/context"
	"net/http"
)

func ContextMiddleware(delegate http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer context.Clear(r)

		delegate.ServeHTTP(w, r)
	})
}
