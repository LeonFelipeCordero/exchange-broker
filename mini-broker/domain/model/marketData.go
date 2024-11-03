package model

import (
	"time"
)

type Instrument struct {
	Isin            string    `json:"isin"`
	InstrumentState string    `json:"state"`
	Name            string    `json:"name"`
	Currency        string    `json:"currency"`
	Timestamp       time.Time `json:"timestamp"`
}

type Quote struct {
	Isin      string    `json:"isin"`
	Price     float64   `json:"price"`
	Currency  string    `json:"currency"`
	Timestamp time.Time `json:"timestamp"`
}
