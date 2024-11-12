package model

import (
	"encoding/json"
	"log"
	"time"
)

type Order struct {
	Reference       string     `json:"reference"`
	Instrument      string     `json:"instrument"`
	Nominals        float64    `json:"nominals"`
	Price           float64    `json:"price"`
	Amount          float64    `json:"amount"`
	Currency        string     `json:"currency"`
	Type            string     `json:"type"`
	Trader          string     `json:"trader"`
	Institution     string     `json:"institution"`
	Timestamp       time.Time  `json:"timestamp"`
	Filled          bool       `json:"filled"`
	FilledTimestamp *time.Time `json:"filledTimestamp"`
	OrderReference  *string    `json:"orderReference"`
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

type OrderFilledMessage struct {
	OrderReference      string    `json:"orderReference"`
	ExternalReference   string    `json:"externalReference"`
	Instrument          string    `json:"instrument"`
	Nominals            float64   `json:"nominals"`
	OriginalPrice       float64   `json:"originalPrice"`
	Institution         string    `json:"institution"`
	SubmissionTimestamp time.Time `json:"submissionTimestamp"`
	FilledTimestamp     time.Time `json:"filledTimestamp"`
}
