package controller

import (
	"net/http"
	"log"
	"rest-commander/process"
	"rest-commander/model/dto"
	"encoding/json"
	"reflect"
)

func PanicMiddleware(delegate http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if rec := recover(); rec != nil {
				if pnfErr, ok := rec.(*process.ProcessNotFoundError); ok {
					response := dto.ErrorResponse{Message: pnfErr.Error(), }

					w.WriteHeader(http.StatusBadRequest)
					json.NewEncoder(w).Encode(response)

					return
				}

				log.Fatalf("PANIC(%v): %v", reflect.TypeOf(rec), rec)
				response := dto.ErrorResponse{Message: "An error occured. See logs for details!", }

				w.WriteHeader(http.StatusInternalServerError)
				json.NewEncoder(w).Encode(response)
			}
		}()

		delegate.ServeHTTP(w, r)
	})
}
