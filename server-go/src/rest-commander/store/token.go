package store

import "rest-commander/utils"

type AuthenticationToken struct {
	Token string
	Username string
}

func NewAuthenticationToken(username string) *AuthenticationToken {
	rawToken, _ := utils.GenerateRandomString(32)

	return &AuthenticationToken{
		Username: username,
		Token: rawToken,
	}
}

type TokenStore interface {
	Get(token string) *AuthenticationToken
	Contains(token string) bool
	Add(token *AuthenticationToken)
	Remove(token string)
}

type AuthenticationTokenStore struct {
	store map[string]*AuthenticationToken
}

func NewAuthenticationTokenStore() TokenStore {
	return &AuthenticationTokenStore{
		store: make(map[string]*AuthenticationToken),
	}
}

func (s *AuthenticationTokenStore) Get(token string) *AuthenticationToken {
	return s.store[token]
}

func (s *AuthenticationTokenStore) Contains(token string) bool {
	_, contains := s.store[token]

	return contains
}

func (s *AuthenticationTokenStore) Add(token *AuthenticationToken) {
	s.store[token.Token] = token
}

func (s *AuthenticationTokenStore) Remove(token string) {
	delete(s.store, token)
}