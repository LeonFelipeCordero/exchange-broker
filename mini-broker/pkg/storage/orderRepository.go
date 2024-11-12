package storage

import (
	domain "mini-broker/domain/model"
)

type OrderRepository interface {
	SaveOrder(order domain.Order) domain.Order
  FindOrderByReference(reference string) domain.Order
  UpdateOrder(order domain.Order) domain.Order
}

type OrderRepositoryImpl struct {
	*PostgresStorage
}

func CreateOrderRepository() OrderRepository {
	return OrderRepositoryImpl{
		PostgresStorage: CreatePostgresStorage(),
	}
}

func (o OrderRepositoryImpl) SaveOrder(order domain.Order) domain.Order {
	orderEntity := fromOrder(order)
	o.PostgresStorage.Db.Create(orderEntity)
	return orderEntity.toOrder()
}

func (o OrderRepositoryImpl) FindOrderByReference(reference string) domain.Order {
	var orderEntity OrderEntity
  o.PostgresStorage.Db.First(&orderEntity, "reference = ?", reference)
	return orderEntity.toOrder()
}

func (o OrderRepositoryImpl) UpdateOrder(order domain.Order) domain.Order {
	orderEntity := fromOrder(order)
	o.PostgresStorage.Db.Where("reference = ?", orderEntity.Reference).Updates(orderEntity)
	return orderEntity.toOrder()
}
