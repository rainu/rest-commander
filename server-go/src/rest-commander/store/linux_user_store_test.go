package store

import (
	"testing"
	"github.com/stretchr/testify/assert"
	"github.com/golang/mock/gomock"
	"rest-commander/utils"
)

const GROUP_FILE = "_testresource/group"
const PASSWD_FILE = "_testresource/passwd"

func Test_parseGroup(t *testing.T) {
	groups := parseGroup(GROUP_FILE)

	assert.Equal(t, 2, len(groups))

	assert.Equal(t, "1", groups[0].id)
	assert.Equal(t, "root", groups[0].name)
	assert.Equal(t, []string{"rainu", "root"}, groups[0].users)

	assert.Equal(t, "1001", groups[1].id)
	assert.Equal(t, "bin", groups[1].name)
	assert.Equal(t, []string{""}, groups[1].users)
}

func Test_parsePasswd(t *testing.T) {
	users := parsePasswd(PASSWD_FILE)

	assert.Equal(t, 2, len(users))

	assert.Equal(t, "0", users[0].id)
	assert.Equal(t, "root", users[0].name)
	assert.Equal(t, "1", users[0].groupId)

	assert.Equal(t, "1000", users[1].id)
	assert.Equal(t, "rainu", users[1].name)
	assert.Equal(t, "1001", users[1].groupId)
}

func Test_readSystemUsersFrom(t *testing.T) {
	users := readSystemUsersFrom(PASSWD_FILE, GROUP_FILE)

	assert.Equal(t, 2, len(users))

	assert.Equal(t, "0", users[0].id)
	assert.Equal(t, "root", users[0].name)
	assert.Equal(t, utils.NewStringSet("root"), users[0].groups)

	assert.Equal(t, "1000", users[1].id)
	assert.Equal(t, "rainu", users[1].name)
	assert.Equal(t, utils.NewStringSet("bin", "root"), users[1].groups)
}

func Test_Contains(t *testing.T) {
	ctl := gomock.NewController(t)
	defer ctl.Finish()

	mockSystemUserReader := NewMocksystemUserReader(ctl)
	mockSystemUserReader.EXPECT().readSystemUsers().Times(1).Return([]systemUser{
		{name: "rainu", },
	})

	toTest := LinuxUserStore{
		systemUserReader: mockSystemUserReader,
	}
	assert.True(t, toTest.Contains("rainu"))
	assert.False(t, toTest.Contains("root"))
}