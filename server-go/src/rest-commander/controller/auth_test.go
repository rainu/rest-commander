package controller

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"github.com/stretchr/testify/assert"
	"github.com/gorilla/mux"
)

type AuthenticationRouteInvocation struct {
	HandleLogin bool
	HandleLogout bool
}

func (a *AuthenticationRouteInvocation) login(w http.ResponseWriter, r *http.Request){
	a.HandleLogin = true
}

func (a *AuthenticationRouteInvocation) logout(w http.ResponseWriter, r *http.Request){
	a.HandleLogout = true
}

func setupAuthTest(t *testing.T) (*mux.Router, *AuthenticationRouteInvocation) {
	router := mux.NewRouter()
	invocations := AuthenticationRouteInvocation{}

	applyAuthenticationRouter(router, AuthenticationRoute{
		HandleLogin: invocations.login,
		HandleLogout: invocations.logout,
	})

	return router, &invocations
}

func Test_AuthLogin_login(t *testing.T) {
	router, invocations := setupAuthTest(t)

	req, _ := http.NewRequest("POST", "/auth/login", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleLogin)
}

func Test_AuthLogin_logout(t *testing.T) {
	router, invocations := setupAuthTest(t)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleLogout)
}

func Test_AuthLogin_logout_noToken(t *testing.T) {
	router, invocations := setupAuthTest(t)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleLogout)
}