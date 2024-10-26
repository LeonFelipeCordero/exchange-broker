package storage

import (
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"log"
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
	dsn := "host=localhost user=broker password=broker dbname=broker port=5432 sslmode=disable"
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("Impossible to conect to databse %e", err)
	}
	//Db.AutoMigrate(&OrderEntity{}, &OrderMatchingEntity{}, &OpenOrderEntity{})
	return db
}
