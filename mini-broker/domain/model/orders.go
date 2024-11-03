package model

import (
	"encoding/json"
	"log"
	"time"
)

type Order struct {
	Reference   string    `json:"reference"`
	Instrument  string    `json:"instrument"`
	Nominals    float64   `json:"nominals"`
	Price       float64   `json:"price"`
	Amount      float64   `json:"amount"`
	Currency    string    `json:"currency"`
	Type        string    `json:"type"`
	Trader      string    `json:"trader"`
	Institution string    `json:"institution"`
	Timestamp   time.Time `json:"timestamp"`
}

type OpenOrder struct {
	Reference       string    `json:"reference"`
	Instrument      string    `json:"instrument"`
	Nominals        float64   `json:"nominals"`
	Price           float64   `json:"price"`
	MatchedQuantity float64   `json:"matchedQuantity"`
	Type            string    `json:"type"`
	CreatedAt       time.Time `json:"createdAt"`
	UpdatedAt       time.Time `json:"updatedAt"`
}

type OrderMatching struct {
	Buy             OpenOrder `json:"buy"`
	Sell            OpenOrder `json:"sell"`
	MatchedNominals float64   `json:"matchedNominals"`
	CreatedAt       time.Time `json:"createdAt"`
	UpdatedAt       time.Time `json:"updatedAt"`
}

func (order *Order) ToBytes() []byte {
	bytes, err := json.Marshal(order)
	if err != nil {
		log.Println("Error marshalling orders", err)
		return nil
	}
	return bytes
}

func (order *Order) FromBytes(bytes []byte) {
	err := json.Unmarshal(bytes, order)
	if err != nil {
		log.Println("Error unmarshalling orders", err)
	}
}

func (orderMatching *OrderMatching) ToBytes() []byte {
	bytes, err := json.Marshal(orderMatching)
	if err != nil {
		log.Println("Error marshalling orders matching", err)
		return nil
	}
	return bytes
}

func (orderMatching *OrderMatching) FromBytes(bytes []byte) {
	err := json.Unmarshal(bytes, orderMatching)
	if err != nil {
		log.Println("Error unmarshalling orders matching", err)
	}
}

//func (openOrder *OpenOrder) RemainingQuantity() float64 {
//	return *new(big.Float).Sub(&openOrder.Nominals, &openOrder.MatchedQuantity)
//}

func (openOrder *OpenOrder) ToBytes() []byte {
	bytes, err := json.Marshal(openOrder)
	if err != nil {
		log.Println("Error marshalling open orders", err)
		return nil
	}
	return bytes
}

func (openOrder *OpenOrder) FromBytes(bytes []byte) {
	err := json.Unmarshal(bytes, openOrder)
	if err != nil {
		log.Println("Error unmarshalling orders", err)
	}
}

func (openOrder *OpenOrder) FromString(value string) {
	err := json.Unmarshal([]byte(value), openOrder)
	if err != nil {
		log.Println("Error unmarshalling orders", err)
	}
}
