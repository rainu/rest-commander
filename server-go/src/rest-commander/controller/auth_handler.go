package controller

import (
	"net/http"
	"encoding/json"
	"rest-commander/store"
	"rest-commander/model/dto"
)

type AuthenticationController interface {
	HandleLogin(w http.ResponseWriter, r *http.Request)
	HandleLogout(w http.ResponseWriter, r *http.Request)
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
	t.userStore.Get(auth.Username).Password = auth.Password

	resp := dto.LoginResponse{
		Token: token.Token,
	}
	json.NewEncoder(w).Encode(resp)
}

func (t *AuthenticationRoute) HandleLogout(w http.ResponseWriter, r *http.Request){
	token := GetAuthtokenFromRequest(r)
	t.tokenStore.Remove(token.Token)
}