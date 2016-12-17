package controller

import (
	"testing"
	"net/http"
	"github.com/stretchr/testify/assert"
	"rest-commander/store"
	"github.com/golang/mock/gomock"
	"os"
	"net/http/httptest"
	"github.com/gorilla/context"
	"os/user"
)

func authMiddlewareTestSetup(t *testing.T) (*store.MockTokenStore, *AuthenticationMiddleware){
	os.Setenv("debug_mode", "false")

	ctl := gomock.NewController(t)
	defer ctl.Finish()

	tokenStore := store.NewMockTokenStore(ctl)

	return tokenStore, &AuthenticationMiddleware{
		tokenStore: tokenStore,
	}
}

func Test_AuthMiddleware_extractTokenFromRequest(t *testing.T) {
	_, toTest := authMiddlewareTestSetup(t)

	req, _ := http.NewRequest("POST", "/auth", nil)
	req.Header.Set(HEADER_TOKEN, "t-o-k-e-n")

	assert.Equal(t, "t-o-k-e-n", toTest.extractTokenFromRequest(req))

	req.Header.Del(HEADER_TOKEN)
	assert.Equal(t, "", toTest.extractTokenFromRequest(req))

	req.Header.Set("x-auTh-ToKeN", "t-o-k-e-n")
	assert.Equal(t, "t-o-k-e-n", toTest.extractTokenFromRequest(req))
}

func Test_AuthMiddleware_adminMode(t *testing.T) {
	tokenStoreMock, toTest := authMiddlewareTestSetup(t)

	os.Setenv("debug_mode", "true")

	tokenStoreMock.EXPECT().Get(gomock.Any()).Times(0)

	req, _ := http.NewRequest("POST", "/auth", nil)
	recorder := httptest.NewRecorder()

	assert.True(t, toTest.authenticationMiddleware(recorder, req))
	assert.NotEmpty(t, context.Get(req, CONTEXT_AUTH_TOKEN))

	user, _ := user.Current()
	expectedToken := store.AuthenticationToken{
		Username: user.Username,
	}
	assert.Equal(t, &expectedToken, context.Get(req, CONTEXT_AUTH_TOKEN))
}

func Test_AuthMiddleware_TokenInvalid(t *testing.T){
	tokenStoreMock, toTest := authMiddlewareTestSetup(t)

	tokenStoreMock.EXPECT().Contains(gomock.Any()).Times(1).Return(false)

	req, _ := http.NewRequest("POST", "/auth", nil)
	recorder := httptest.NewRecorder()

	assert.False(t, toTest.authenticationMiddleware(recorder, req))
	assert.Equal(t, http.StatusForbidden, recorder.Code)
	assert.Empty(t, GetAuthtokenFromRequest(req))
}

func Test_AuthMiddleware_TokenValid(t *testing.T){
	tokenStoreMock, toTest := authMiddlewareTestSetup(t)

	expectedToken := store.NewAuthenticationToken("rainu")
	tokenStoreMock.EXPECT().Contains(gomock.Any()).Times(1).Return(true)
	tokenStoreMock.EXPECT().Get("t-o-k-e-n").Times(1).Return(expectedToken)

	req, _ := http.NewRequest("POST", "/auth", nil)
	req.Header.Set(HEADER_TOKEN, "t-o-k-e-n")
	recorder := httptest.NewRecorder()

	assert.True(t, toTest.authenticationMiddleware(recorder, req))
	assert.Equal(t, expectedToken, GetAuthtokenFromRequest(req))
}