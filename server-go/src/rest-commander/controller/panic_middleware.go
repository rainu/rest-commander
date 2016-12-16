package controller

import (
	"net/http"
	"log"
)

func PanicMiddleware(delegate http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if rec := recover(); rec != nil {
				log.Fatalf("PANIC: %v", rec)
			}
		}()

		delegate.ServeHTTP(w, r)
	})
}
