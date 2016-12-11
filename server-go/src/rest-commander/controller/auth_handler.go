package controller

import (
	"net/http"
	"encoding/json"
	"rest-commander/store"
	"strings"
	"rest-commander/model/dto"
)

type AuthenticationController interface {
	HandleLogin(w http.ResponseWriter, r *http.Request)
	HandleLogout(w http.ResponseWriter, r *http.Request)
}

type AccessDeniedController interface {
	HandleAccessDenied(w http.ResponseWriter, r *http.Request)
}

func (t *AuthenticationRoute) HandleAccessDenied(w http.ResponseWriter, r *http.Request){
	HandleAccessDenied(w, r)
}

func HandleAccessDenied(w http.ResponseWriter, r *http.Request){
	w.WriteHeader(http.StatusForbidden)
}

func ExtractTokenFromRequest(r *http.Request) string{
	for h, v := range r.Header {
		if strings.EqualFold(h, HEADER_TOKEN) {
			return v[0]
		}
	}

	return ""
}

func (t *AuthenticationRoute) HandleLogin(w http.ResponseWriter, r *http.Request){
	var auth dto.LoginRequest
	err := json.NewDecoder(r.Body).Decode(&auth)

	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	if !t.userStore.CheckPassword(auth.Username, auth.Password) {
		resp := dto.ErrorResponse{
			Message: "Username or password are incorrect!",
		}
		w.WriteHeader(http.StatusBadRequest)
		json.NewEncoder(w).Encode(resp)
		return
	}

	token := store.NewAuthenticationToken(auth.Username)

	t.tokenStore.Add(token)
	resp := dto.LoginResponse{
		Token: token.Token,
	}
	json.NewEncoder(w).Encode(resp)
}

func (t *AuthenticationRoute) HandleLogout(w http.ResponseWriter, r *http.Request){
	token := ExtractTokenFromRequest(r)
	if ! t.tokenStore.Contains(token) {
		t.HandleAccessDenied(w, r)
		return
	}

	t.tokenStore.Remove(token)
}