package utils

import (
	"testing"
	"github.com/stretchr/testify/assert"
)

func Test_Set(t *testing.T) {
	set := NewStringSet()
	assert.Equal(t, 0, len(set.Iter()))

	assert.False(t, set.Contains("test"))

	set.Add("test")
	assert.True(t, set.Contains("test"))

	set.Add("test")
	assert.Equal(t, 1, len(set.Iter()))

	set.Remove("test")
	assert.False(t, set.Contains("test"))
	assert.Equal(t, 0, len(set.Iter()))
}

func Test_SetPreFill(t *testing.T) {
	set := NewStringSet("test")
	assert.Equal(t, 1, len(set.Iter()))
	assert.True(t, set.Contains("test"))
}