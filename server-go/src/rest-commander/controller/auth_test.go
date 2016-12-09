package controller

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"github.com/gorilla/mux"
	"github.com/golang/mock/gomock"
)

func setupAuthTest(t *testing.T) (*mux.Router, *MockAuthenticationController) {
	ctl := gomock.NewController(t)
	defer ctl.Finish()

	router := mux.NewRouter()
	mockController := NewMockAuthenticationController(ctl)

	applyAuthenticationRouter(router, mockController)

	return router, mockController
}

func Test_AuthLogin_login(t *testing.T) {
	router, controller := setupAuthTest(t)
	controller.EXPECT().HandleLogin(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/auth/login", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_AuthLogin_logout(t *testing.T) {
	router, controller := setupAuthTest(t)
	controller.EXPECT().HandleLogout(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_AuthLogin_logout_noToken(t *testing.T) {
	router, controller := setupAuthTest(t)
	controller.EXPECT().HandleLogout(gomock.Any(), gomock.Any()).Times(0)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)
}