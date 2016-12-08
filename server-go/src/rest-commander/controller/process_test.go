package controller

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"github.com/stretchr/testify/assert"
	"github.com/gorilla/mux"
)

type ProcessRouteInvocation struct {
	HandleAccessDenied bool
	HandleListProcess bool
	HandleStartProcess bool
	HandleStartProcessAdmin bool
	HandleProcessSignal bool
	HandleProcessInput bool
	HandleProcessOutput bool
	HandleProcessStatus bool
}

func (a *ProcessRouteInvocation) accessDenied(w http.ResponseWriter, r *http.Request){
	a.HandleAccessDenied = true
}

func (a *ProcessRouteInvocation) listProcess(w http.ResponseWriter, r *http.Request){
	a.HandleListProcess = true
}

func (a *ProcessRouteInvocation) startProcess(w http.ResponseWriter, r *http.Request){
	a.HandleStartProcess = true
}

func (a *ProcessRouteInvocation) startProcessAdmin(w http.ResponseWriter, r *http.Request){
	a.HandleStartProcessAdmin = true
}

func (a *ProcessRouteInvocation) processSignal(w http.ResponseWriter, r *http.Request){
	a.HandleProcessSignal = true
}

func (a *ProcessRouteInvocation) processInput(w http.ResponseWriter, r *http.Request){
	a.HandleProcessInput = true
}

func (a *ProcessRouteInvocation) processOutput(w http.ResponseWriter, r *http.Request){
	a.HandleProcessOutput = true
}

func (a *ProcessRouteInvocation) processStatus(w http.ResponseWriter, r *http.Request){
	a.HandleProcessStatus = true
}

func setupProcessTest(t *testing.T) (*mux.Router, *ProcessRouteInvocation) {
	router := mux.NewRouter()
	invocations := ProcessRouteInvocation{}

	applyProcessRouter(router, ProcessRoute{
		HandleAccessDenied: invocations.accessDenied,
		HandleListProcess: invocations.listProcess,
		HandleStartProcess: invocations.startProcess,
		HandleStartProcessAdmin: invocations.startProcessAdmin,
		HandleProcessSignal: invocations.processSignal,
		HandleProcessInput: invocations.processInput,
		HandleProcessOutput: invocations.processOutput,
		HandleProcessStatus: invocations.processStatus,

	})

	return router, &invocations
}

func Test_ProcessList(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("GET", "/process", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleListProcess)
}

func Test_ProcessList_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("GET", "/process", nil)
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleListProcess)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_StartProcess(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleStartProcess)
}

func Test_StartProcess_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleStartProcess)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_StartProcess_wrongContentType(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleStartProcess)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_StartProcessAdmin(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/admin", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleStartProcessAdmin)
}

func Test_StartProcessAdmin_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/admin", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleStartProcess)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_StartProcessAdmin_wrongContentType(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/admin", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleStartProcess)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_ProcessSignal(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/13/9", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleProcessSignal)
}

func Test_ProcessSignal_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/13/9", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleProcessSignal)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_ProcessInput(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/13", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleProcessInput)
}

func Test_ProcessInput_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("POST", "/process/13", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleProcessInput)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_ProcessStatus(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("GET", "/process/13", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleProcessStatus)
}

func Test_ProcessStatus_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("GET", "/process/13", nil)
	req.Header.Set("Content-Type", "application/json")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleProcessStatus)
	assert.True(t, invocations.HandleAccessDenied)
}

func Test_ProcessOutput(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("GET", "/process/13/out", nil)
	req.Header.Set("x-auth-token", "t-o-k-e-n")
	req.Header.Set("Range", "0-")
	req.Header.Set("Accept", "application/octet-stream")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.True(t, invocations.HandleProcessOutput)
}

func Test_ProcessOutput_noToken(t *testing.T) {
	router, invocations := setupProcessTest(t)

	req, _ := http.NewRequest("GET", "/process/13/out", nil)
	req.Header.Set("Range", "0-")
	req.Header.Set("Accept", "application/octet-stream")
	router.ServeHTTP(httptest.NewRecorder(), req)

	assert.False(t, invocations.HandleProcessOutput)
	assert.True(t, invocations.HandleAccessDenied)
}