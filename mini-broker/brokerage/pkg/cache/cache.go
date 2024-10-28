package cache

import (
	"sync"
)

type Result struct {
	Value []byte
	err   error
}

type Cache struct {
	cache map[string]Result
	sync.Mutex
}

func (c *Cache) Get(key string) []byte {
	c.Lock()
	defer c.Unlock()
	item, _ := c.cache[key]

	return item.Value
}

func (c *Cache) Put(key string, value []byte) {
	c.Lock()
	defer c.Unlock()
	c.cache[key] = Result{Value: value}
}

func (c *Cache) GetAll() [][]byte {
	c.Lock()
	defer c.Unlock()
	values := make([][]byte, 0, len(c.cache))
	for _, item := range c.cache {
		value := item.Value
		values = append(values, value)
	}
	return values
}

func (c *Cache) Exist(key string) bool {
	c.Lock()
	defer c.Unlock()
	_, present := c.cache[key]

	return present
}

func (c *Cache) Remove(key string) {
	c.Lock()
	defer c.Unlock()
	delete(c.cache, key)
}

type SliceStore struct {
	cache []string
	sync.Mutex
}

func (c *SliceStore) Push(value string) {
	c.Lock()
	defer c.Unlock()
	c.cache = append(c.cache, value)
}

func (c *SliceStore) Get(index int) string {
	c.Lock()
	defer c.Unlock()
	return c.cache[index]
}

func (c *SliceStore) GetAll() []string {
	c.Lock()
	defer c.Unlock()
	return c.cache
}

type Matrix struct {
	cache map[string]map[string]map[string]Result
	sync.Mutex
}

func (m *Matrix) Put(x, y, z string, value []byte) {
	m.Lock()
	defer m.Unlock()

	_, present := m.cache[x]
	if !present {
		m.cache[x] = make(map[string]map[string]Result)
	}

	_, present = m.cache[x][y]
	if !present {
		m.cache[x][y] = make(map[string]Result)
	}

	m.cache[x][y][z] = Result{Value: value}
}

func (m *Matrix) GetItems(x, y string) map[string]Result {
	m.Lock()
	defer m.Unlock()
	items, _ := m.cache[x][y]

	return items
}

func (m *Matrix) Get(x, y, z string) Result {
	m.Lock()
	defer m.Unlock()
	item, _ := m.cache[x][y][z]

	return item
}

func (m *Matrix) Remove(x, y, z string) {
	m.Lock()
	defer m.Unlock()
	delete(m.cache[x][y], z)
}

func (m *Matrix) Update(x, y, z string, value []byte) {
	m.Lock()
	defer m.Unlock()
	m.cache[x][y][z] = Result{Value: value}
}

func NewCache() *Cache {
	return &Cache{
		cache: make(map[string]Result),
	}
}

func NewSliceStore() *SliceStore {
	return &SliceStore{
		cache: make([]string, 0),
	}
}

func NewMatrix() *Matrix {
	return &Matrix{
		cache: make(map[string]map[string]map[string]Result),
	}
}
