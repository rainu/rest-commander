package store

import (
	"testing"
	"github.com/stretchr/testify/assert"
)

func Test_TokenStore(t *testing.T) {
	store := NewAuthenticationTokenStore()

	assert.False(t, store.Contains("test"))

	store.Add(&AuthenticationToken{
		Token: "test", Username: "user",
	})
	assert.True(t, store.Contains("test"))
	assert.NotNil(t, store.Get("test"))
	assert.Equal(t, "user", store.Get("test").Username)

	store.Remove("test")
	assert.False(t, store.Contains("test"))
}
