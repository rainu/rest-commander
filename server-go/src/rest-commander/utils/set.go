package utils

type StringSet struct {
	data map[string]bool
}

func NewStringSet(values...string) *StringSet {
	me := StringSet{
		data: make(map[string]bool),
	}

	me.Add(values...)
	return &me
}

func CopyStringSet(set *StringSet) *StringSet {
	return NewStringSet(set.Iter()...)
}

func (ss *StringSet) Contains(value string) bool {
	_, ok := ss.data[value]

	return ok
}

func (ss *StringSet) Add(values...string) {
	for _, v := range values {
		ss.data[v] = true
	}
}

func (ss *StringSet) Remove(values...string) {
	for _, v := range values {
		delete(ss.data, v)
	}
}

func (ss *StringSet) Iter() []string {
	keys := make([]string, 0, len(ss.data))
	for k := range ss.data {
		keys = append(keys, k)
	}

	return keys
}