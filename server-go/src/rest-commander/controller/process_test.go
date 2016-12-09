package controller

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"github.com/gorilla/mux"
	"github.com/golang/mock/gomock"
)

func setupProcessTest(t *testing.T) (*mux.Router, *MockProcessController, *MockAccessDeniedController) {
	ctl := gomock.NewController(t)
	defer ctl.Finish()

	router := mux.NewRouter()
	mockProcessController := NewMockProcessController(ctl)
	mockAuthController := NewMockAccessDeniedController(ctl)

	applyProcessRouter(router, mockProcessController, mockAuthController)

	return router, mockProcessController, mockAuthController
}

func Test_ProcessList(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleListProcess(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("GET", "/process", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessList_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleListProcess(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("GET", "/process", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_StartProcess(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleStartProcess(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_StartProcess_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleStartProcess(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_StartProcess_wrongContentType(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleStartProcess(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_StartProcessAdmin(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleStartProcessAdmin(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/admin", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_StartProcessAdmin_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleStartProcess(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/admin", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_StartProcessAdmin_wrongContentType(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleStartProcess(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/admin", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessSignal(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleProcessSignal(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/13/9", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessSignal_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleProcessSignal(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/13/9", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessInput(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleProcessInput(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/13", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessInput_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleProcessInput(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("POST", "/process/13", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessStatus(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleProcessStatus(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("GET", "/process/13", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessStatus_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleProcessStatus(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("GET", "/process/13", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessOutput(t *testing.T) {
	router, processController, _ := setupProcessTest(t)
	processController.EXPECT().HandleProcessOutput(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("GET", "/process/13/out", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Range", "0-")
	req.Header.Set("Accept", "application/octet-stream")
	router.ServeHTTP(httptest.NewRecorder(), req)
}

func Test_ProcessOutput_noToken(t *testing.T) {
	router, processController, authController := setupProcessTest(t)
	processController.EXPECT().HandleProcessOutput(gomock.Any(), gomock.Any()).Times(0)
	authController.EXPECT().HandleAccessDenied(gomock.Any(), gomock.Any()).Times(1)

	req, _ := http.NewRequest("GET", "/process/13/out", nil)
	req.Header.Set("Range", "0-")
	req.Header.Set("Accept", "application/octet-stream")
	router.ServeHTTP(httptest.NewRecorder(), req)
}