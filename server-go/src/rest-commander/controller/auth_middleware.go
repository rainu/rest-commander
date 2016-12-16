package controller

import (
	"net/http"
	"strings"
	"rest-commander/store"
	"github.com/gorilla/context"
	"os"
	"os/user"
)

const CONTEXT_AUTH_TOKEN = "Context.Authentication.Token"

type AuthenticationMiddleware struct {
	tokenStore store.TokenStore
}

func (m *AuthenticationMiddleware) AuthenticationFuncMiddleware(delegate func(w http.ResponseWriter, r *http.Request)) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if m.authenticationMiddleware(w, r) {
			delegate(w, r)
		}
	})
}

func (m *AuthenticationMiddleware) authenticationMiddleware(w http.ResponseWriter, r *http.Request) bool {
	if os.Getenv("debug_mode") == "true" {
		currentUser, _ := user.Current()

		context.Set(r, CONTEXT_AUTH_TOKEN, &store.AuthenticationToken{
			Username: currentUser.Username,
		})
		return true
	}

	token := m.extractTokenFromRequest(r)

	if ! m.tokenStore.Contains(token) {
		w.WriteHeader(http.StatusForbidden)
		return false
	}

	context.Set(r, CONTEXT_AUTH_TOKEN, m.tokenStore.Get(token))
	return true
}

func (m *AuthenticationMiddleware) extractTokenFromRequest(r *http.Request) string{
	for h, v := range r.Header {
		if strings.EqualFold(h, HEADER_TOKEN) {
			return v[0]
		}
	}

	return ""
}

func GetAuthtokenFromRequest(r *http.Request) *store.AuthenticationToken {
	return context.Get(r, CONTEXT_AUTH_TOKEN).(*store.AuthenticationToken)
}
