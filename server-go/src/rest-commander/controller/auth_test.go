package controller

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"github.com/gorilla/mux"
	"github.com/golang/mock/gomock"
)

func AuthenticationPassThroughMiddleware(delegate func(w http.ResponseWriter, r *http.Request)) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		delegate(w, r)
	})
}

func AuthenticationDropMiddleware(delegate func(w http.ResponseWriter, r *http.Request)) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {})
}

func setupAuthTest(t *testing.T, middleware HandlerFuncMiddleware) (*mux.Router, *MockAuthenticationController) {
	ctl := gomock.NewController(t)
	defer ctl.Finish()

	router := mux.NewRouter()
	mockController := NewMockAuthenticationController(ctl)

	applyAuthenticationRouter(router, mockController, middleware)

	return router, mockController
}

func Test_AuthLogin_login(t *testing.T) {
	router, controller := setupAuthTest(t, AuthenticationPassThroughMiddleware)
	controller.EXPECT().HandleLogin(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/auth/login", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_AuthLogin_logout(t *testing.T) {
	router, controller := setupAuthTest(t, AuthenticationPassThroughMiddleware)
	controller.EXPECT().HandleLogout(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_AuthLogin_logout_noToken(t *testing.T) {
	router, controller := setupAuthTest(t, AuthenticationDropMiddleware)
	controller.EXPECT().HandleLogout(gomock.Any(), gomock.Any()).Times(0)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)
}