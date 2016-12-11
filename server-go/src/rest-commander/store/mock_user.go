// Automatically generated by MockGen. DO NOT EDIT!
// Source: ./store/user.go

package store

import (
	gomock "github.com/golang/mock/gomock"
)

// Mock of UserStore interface
type MockUserStore struct {
	ctrl     *gomock.Controller
	recorder *_MockUserStoreRecorder
}

// Recorder for MockUserStore (not exported)
type _MockUserStoreRecorder struct {
	mock *MockUserStore
}

func NewMockUserStore(ctrl *gomock.Controller) *MockUserStore {
	mock := &MockUserStore{ctrl: ctrl}
	mock.recorder = &_MockUserStoreRecorder{mock}
	return mock
}

func (_m *MockUserStore) EXPECT() *_MockUserStoreRecorder {
	return _m.recorder
}

func (_m *MockUserStore) Get(username string) *User {
	ret := _m.ctrl.Call(_m, "Get", username)
	ret0, _ := ret[0].(*User)
	return ret0
}

func (_mr *_MockUserStoreRecorder) Get(arg0 interface{}) *gomock.Call {
	return _mr.mock.ctrl.RecordCall(_mr.mock, "Get", arg0)
}

func (_m *MockUserStore) Contains(username string) bool {
	ret := _m.ctrl.Call(_m, "Contains", username)
	ret0, _ := ret[0].(bool)
	return ret0
}

func (_mr *_MockUserStoreRecorder) Contains(arg0 interface{}) *gomock.Call {
	return _mr.mock.ctrl.RecordCall(_mr.mock, "Contains", arg0)
}

func (_m *MockUserStore) CheckPassword(username string, password string) bool {
	ret := _m.ctrl.Call(_m, "CheckPassword", username, password)
	ret0, _ := ret[0].(bool)
	return ret0
}

func (_mr *_MockUserStoreRecorder) CheckPassword(arg0, arg1 interface{}) *gomock.Call {
	return _mr.mock.ctrl.RecordCall(_mr.mock, "CheckPassword", arg0, arg1)
}