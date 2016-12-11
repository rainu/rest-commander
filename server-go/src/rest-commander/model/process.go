package model

type Process struct {
	Id          string `json:"id"`
	Parent      string `json:"parent"`
	Commandline string `json:"commandline"`
	User        string `json:"user"`
	Environment map[string]string `json:"environment"`
	Running     bool `json:"running"`
	ReturnCode  int `json:"returnCode"`
}