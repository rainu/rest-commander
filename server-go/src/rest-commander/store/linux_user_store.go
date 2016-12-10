package store

import (
	osUser "os/user"
	"bufio"
	"strings"
	"rest-commander/utils"
	"os"
)

type LinuxUserStore struct {
	systemUserReader
	systemUsers map[string]*systemUser
	allocatedUsers map[string]*User
}

type groupEntry struct {
	id string
	name string
	users []string
}

type passwdEntry struct {
	id string
	name string
	groupId string
}

type systemUser struct {
	id     string
	name   string
	groups *utils.StringSet
}

const ETC_PASSWD = "/etc/passwd"
const ETC_GROUP = "/etc/group"

func NewLinuxUserStore() *LinuxUserStore {
	return &LinuxUserStore{
		systemUserReader: &defaultSystemUserReader{},
		allocatedUsers: make(map[string]*User),
	}
}

func (l *LinuxUserStore) Get(username string) *User {
	if ! l.Contains(username) {
		return nil
	}

	if _, ok := l.allocatedUsers[username]; !ok {
		su := l.getSystemUser()[username]

		user := User{
			Username: su.name,
			Roles: utils.CopyStringSet(su.groups).Add(ROLE_USER),
		}

		osUser, _ := osUser.Current()
		if osUser.Username == su.name || user.Roles.Contains("root") {
			user.Roles.Add(ROLE_ADMIN)
		}

		l.allocatedUsers[username] = &user
	}

	return l.allocatedUsers[username]
}

func (l *LinuxUserStore) Contains(username string) bool {
	_, contains := l.getSystemUser()[username]

	return contains
}

func (l *LinuxUserStore) CheckPassword(username string, password string) bool {
	return false
}

func (l *LinuxUserStore) getSystemUser() map[string]*systemUser {
	if l.systemUsers == nil {
		l.systemUsers = map[string]*systemUser{}
		for _, su := range l.readSystemUsers() {
			l.systemUsers[su.name] = &su
		}
	}

	return l.systemUsers
}

type systemUserReader interface {
	readSystemUsers() []systemUser
}

type defaultSystemUserReader struct {}

func (d *defaultSystemUserReader) readSystemUsers() []systemUser {
	return readSystemUsersFrom(ETC_PASSWD, ETC_GROUP)
}

func readSystemUsersFrom(passwdPath string, groupPath string) []systemUser {
	passwdEntries := parsePasswd(passwdPath)
	groupEntries := parseGroup(groupPath)

	result := make([]systemUser, len(passwdEntries), len(passwdEntries))
	groupMap := map[string]groupEntry{}

	for _, group := range groupEntries {
		groupMap[group.id] = group
	}

	userMap := map[string]*systemUser{}

	for i, user := range passwdEntries {
		result[i] = systemUser{
			id: user.id,
			name: user.name,
			groups: utils.NewStringSet(groupMap[user.groupId].name),
		}

		userMap[user.name] = &result[i]
	}

	for _, group := range groupEntries {
		for _, user := range group.users {
			if su, ok := userMap[user]; ok {
				su.groups.Add(group.name)
			}
		}
	}

	return result
}

func parseGroup(groupPath string) []groupEntry {
	file, err := os.Open(groupPath)
	if err != nil {
		panic("Can not open group file! Something is wrong with linux...")
	}
	defer file.Close()

	result := []groupEntry{}

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		split := strings.Split(line, ":")

		result = append(result, groupEntry{
			id: split[2],
			name: split[0],
			users: strings.Split(split[3], ","),
		})
	}

	return result
}

func parsePasswd(passwdPath string) []passwdEntry {
	file, err := os.Open(passwdPath)
	if err != nil {
		panic("Can not open group file! Something is wrong with linux...")
	}
	defer file.Close()

	result := []passwdEntry{}

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		split := strings.Split(line, ":")

		result = append(result, passwdEntry{
			id: split[2],
			name: split[0],
			groupId: split[3],
		})
	}

	return result
}