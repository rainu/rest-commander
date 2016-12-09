package store

type LinuxUserStore struct{}

func (l *LinuxUserStore) Get(username string) *User {
	return nil
}

func (l *LinuxUserStore) Contains(username string) bool {
	return false
}

func (l *LinuxUserStore) CheckPassword(username string, password string) bool {
	return false
}