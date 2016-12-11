package model

type Process struct {
	Id string
	Parent string
	Commandline string
	User string
	Environment map[string]string
	Running bool
	ReturnCode int
}