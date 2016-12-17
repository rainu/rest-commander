package dto

type ErrorResponse struct {
	Message string `json:"message"`
}

type LoginRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

type LoginResponse struct {
	Token string `json:"token"`
}

type ProcessRequest struct {
	Command     string `json:"command"`
	Arguments   []string `json:"arguments"`
	Environment map[string]string `json:"environment"`
	WorkingDir  string `json:"workDirectory"`
}

type ProcessCreateResponse struct {
	Pid     string `json:"pid"`
	Created bool `json:"created"`
}

type ProcessSignalResponse struct {
	ReturnCode     int `json:"returnCode"`
}