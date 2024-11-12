package storage

import (
	domain "mini-broker/domain/model"
	"time"
)

func (o *OrderEntity) TableName() string {
	return "orders"
}

type OrderEntity struct {
	Reference      string  `gorm:"index"`
	OrderReference *string `gorm:"index"`
	Instrument     string
	Nominals       float64
	Price          float64
	Amount         float64
	Currency       string
	Type           string
	Trader         string
	Institution    string
	Timestamp      time.Time
	Filled         bool
	FilledTimestamp *time.Time
}

func (o *OrderEntity) toOrder() domain.Order {
	return domain.Order{
		Reference:       o.Reference,
		Instrument:      o.Instrument,
		Nominals:        o.Nominals,
		Price:           o.Price,
		Amount:          o.Amount,
		Currency:        o.Currency,
		Type:            o.Type,
		Trader:          o.Trader,
		Institution:     o.Institution,
		Timestamp:       o.Timestamp,
		OrderReference:  o.OrderReference,
		Filled:          o.Filled,
		FilledTimestamp: o.FilledTimestamp,
	}
}

func fromOrder(order domain.Order) OrderEntity {
	return OrderEntity{
		Reference:      order.Reference,
		Instrument:     order.Instrument,
		Nominals:       order.Nominals,
		Price:          order.Price,
		Amount:         order.Amount,
		Currency:       order.Currency,
		Type:           order.Type,
		Trader:         order.Trader,
		Institution:    order.Institution,
		Timestamp:      order.Timestamp,
		OrderReference: order.OrderReference,
		Filled:         order.Filled,
		FilledTimestamp: order.FilledTimestamp,
	}
}
