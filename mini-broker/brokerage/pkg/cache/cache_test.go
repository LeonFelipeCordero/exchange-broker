package cache

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestMatrix(t *testing.T) {
	matrix := NewMatrix()
	matrix.Put("1", "1", "a", []byte("11a"))
	matrix.Put("1", "1", "b", []byte("11b"))
	matrix.Put("1", "2", "c", []byte("12c"))
	matrix.Put("2", "2", "d", []byte("22d"))
	matrix.Put("2", "1", "e", []byte("21e"))
	matrix.Put("3", "2", "f", []byte("32f"))
	matrix.Put("3", "3", "g", []byte("33g"))
	matrix.Put("3", "3", "h", []byte("33h"))
	matrix.Put("3", "3", "i", []byte("33i"))
	matrix.Put("3", "3", "j", []byte("33j"))

	t.Run("Matrix should save, delete and update data in cache", func(t *testing.T) {
		var value = matrix.Get("1", "1", "a")
		assert.Equal(t, value, Result{Value: []byte("11a")})
		var values = matrix.GetItems("3", "3")
		assert.Equal(t, values, map[string]Result{"g": {Value: []byte("33g")}, "h": {Value: []byte("33h")}, "i": {Value: []byte("33i")}, "j": {Value: []byte("33j")}})

		matrix.Remove("3", "3", "i")
		values = matrix.GetItems("3", "3")
		assert.Equal(t, values, map[string]Result{"g": {Value: []byte("33g")}, "h": {Value: []byte("33h")}, "j": {Value: []byte("33j")}})

		matrix.Update("3", "3", "h", []byte("33asdf"))
		value = matrix.Get("3", "3", "h")
		assert.Equal(t, value, Result{Value: []byte("33asdf")})
	})
}

func TestCache(t *testing.T) {
	cache := NewCache()
	cache.Put("1", []byte("4"))
	cache.Put("2", []byte("3"))
	cache.Put("3", []byte("2"))
	cache.Put("4", []byte("1"))

	t.Run("Cache should save data in cache", func(t *testing.T) {
		value := cache.Get("1")
		assert.Equal(t, value, []byte("4"))

		values := cache.GetAll()
		assert.ElementsMatch(t, values, [][]byte{[]byte("4"), []byte("3"), []byte("2"), []byte("1")})

		cache.Remove("3")
		values = cache.GetAll()
		assert.Equal(t, values, [][]byte{[]byte("4"), []byte("3"), []byte("1")})
	})
}

func TestSlice(t *testing.T) {
	slice := NewSliceStore()
	slice.Push("1")
	slice.Push("2")

	t.Run("Slice should save data in cache", func(t *testing.T) {
		value := slice.Get(0)
		assert.Equal(t, value, "1")

		values := slice.GetAll()
		assert.Equal(t, values, []string{"1", "2"})
	})
}
