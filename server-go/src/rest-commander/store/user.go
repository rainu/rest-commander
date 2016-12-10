package store

import (
	"runtime"
	"rest-commander/utils"
)

const ROLE_USER = "__USER__"
const ROLE_ADMIN = "__ADMIN__"

type User struct {
	Username string
	Password string
	Roles *utils.StringSet
}

type UserStore interface {
	Get(username string) *User
	Contains(username string) bool
	CheckPassword(username string, password string) bool
}

func NewUserStore() UserStore {
	switch runtime.GOOS {
	case "linux":
		return NewLinuxUserStore()
	default:
		panic("The current os is not supported!")
	}
}