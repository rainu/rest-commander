package store

import "runtime"

type User struct {
	Username string
	Password string
}

type UserStore interface {
	Get(username string) *User
	Contains(username string) bool
	CheckPassword(username string, password string) bool
}

func NewUserStore() UserStore {
	switch runtime.GOOS {
	case "linux":
		return &LinuxUserStore{}
	default:
		panic("The current os is not supported!")
	}
}