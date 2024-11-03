package storage

import (
	"fmt"
	"log"

	"github.com/spf13/viper"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

type PostgresStorage struct {
	Db *gorm.DB
}

type Tabler interface {
	TableName() string
}

func CreatePostgresStorage() *PostgresStorage {
	return &PostgresStorage{
		Db: connect(),
	}
}

func connect() *gorm.DB {
	host := viper.Get("postgresql.host")
	port := viper.Get("postgresql.port")
	user := viper.Get("postgresql.user")
	password := viper.Get("postgresql.password")
	database := viper.Get("postgresql.database")

	dsn := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=%s sslmode=disable", host, user, password, database, port)
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("Impossible to conect to databse %e", err)
	}
	return db
}
