package controller

import (
	"testing"
	"github.com/gorilla/mux"
	"rest-commander/store"
	"github.com/golang/mock/gomock"
	"net/http/httptest"
	"net/http"
	"encoding/json"
	"rest-commander/model/dto"
	"bytes"
	"github.com/stretchr/testify/assert"
)

func setupAuthHandlerTest(t *testing.T) (*mux.Router, *store.MockUserStore, *store.MockTokenStore) {
	ctl := gomock.NewController(t)
	defer ctl.Finish()

	router := mux.NewRouter()
	userStore := store.NewMockUserStore(ctl)
	tokenStore := store.NewMockTokenStore(ctl)

	ApplyAuthenticationRouter(router, userStore, tokenStore)

	return router, userStore, tokenStore
}

func Test_AuthLoginHandler_login(t *testing.T) {
	router, mockUserStore, mockTokenStore := setupAuthHandlerTest(t)

	data := dto.LoginRequest{
		Username: "rainu", Password: "password",
	}

	mockUserStore.EXPECT().CheckPassword(data.Username, data.Password).Times(1).Return(true)
	mockTokenStore.EXPECT().Add(gomock.Any()).Times(1)

	jsonData, _ := json.Marshal(data)

	req, _ := http.NewRequest("POST", "/auth/login", bytes.NewBuffer(jsonData))
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, req)

	result := dto.LoginResponse{}
	json.NewDecoder(recorder.Body).Decode(&result)

	assert.NotEmpty(t, result.Token)
}

func Test_AuthLoginHandler_loginInvalid(t *testing.T) {
	router, mockUserStore, mockTokenStore := setupAuthHandlerTest(t)

	data := dto.LoginRequest{
		Username: "rainu", Password: "password",
	}

	mockUserStore.EXPECT().CheckPassword(data.Username, data.Password).Times(1).Return(false)
	mockTokenStore.EXPECT().Add(gomock.Any()).Times(0)

	jsonData, _ := json.Marshal(data)

	req, _ := http.NewRequest("POST", "/auth/login", bytes.NewBuffer(jsonData))
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, req)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)

	result := dto.ErrorResponse{}
	json.NewDecoder(recorder.Body).Decode(&result)

	assert.NotEmpty(t, result.Message)
}

func Test_AuthLoginHandler_logout(t *testing.T) {
	router, _, mockTokenStore := setupAuthHandlerTest(t)

	token := "t-o-k-e-n"

	mockTokenStore.EXPECT().Contains(token).Times(1).Return(true)
	mockTokenStore.EXPECT().Remove(token).Times(1)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	req.Header.Set(HEADER_TOKEN, token)
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, req)

	assert.Equal(t, http.StatusOK, recorder.Code)
}

func Test_AuthLoginHandler_logoutInvalidToken(t *testing.T) {
	router, _, mockTokenStore := setupAuthHandlerTest(t)

	token := "t-o-k-e-n"

	mockTokenStore.EXPECT().Contains(token).Times(1).Return(false)
	mockTokenStore.EXPECT().Remove(token).Times(1)

	req, _ := http.NewRequest("POST", "/auth/logout", nil)
	req.Header.Set(HEADER_TOKEN, token)
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, req)

	assert.Equal(t, http.StatusForbidden, recorder.Code)
}

func Test_ExtractTokenFromRequest(t *testing.T) {
	token := "t-o-k-e-n"

	req, _ := http.NewRequest("POST", "/auth", nil)
	req.Header.Set("x-auth-token", token)

	assert.Equal(t, token, ExtractTokenFromRequest(req))

	req, _ = http.NewRequest("POST", "/auth", nil)
	req.Header.Set("x-AuTh-tOkEn", token)

	assert.Equal(t, token, ExtractTokenFromRequest(req))

	req, _ = http.NewRequest("POST", "/auth", nil)
	req.Header.Set(HEADER_TOKEN, token)

	assert.Equal(t, token, ExtractTokenFromRequest(req))
}