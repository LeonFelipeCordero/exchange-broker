package storage

import (
	domain "brokerage/domain/model"
	"time"
)

func (i *InstrumentEntity) TableName() string {
	return "instruments_timetable"
}

func (q *QuoteEntity) TableName() string {
	return "quotes_timetable"
}

type InstrumentEntity struct {
	Isin            string `gorm:"index"`
	Name            string
	InstrumentState string
	Currency        string
	Timestamp       time.Time `gorm:"index"`
}

func (i *InstrumentEntity) toInstrument() domain.Instrument {
	return domain.Instrument{
		Isin:            i.Isin,
		Name:            i.Name,
		InstrumentState: i.InstrumentState,
		Timestamp:       i.Timestamp,
	}
}

func FromInstrument(instrument domain.Instrument) InstrumentEntity {
	return InstrumentEntity{
		Isin:            instrument.Isin,
		Name:            instrument.Name,
		InstrumentState: instrument.InstrumentState,
		Timestamp:       instrument.Timestamp,
	}
}

type QuoteEntity struct {
	Isin      string `gorm:"index"`
	Currency  string
	Quote     float64
	Timestamp time.Time `gorm:"index"`
}

func (q *QuoteEntity) toQuote() domain.Quote {
	return domain.Quote{
		Isin:      q.Isin,
		Currency:  q.Currency,
		Price:     q.Quote,
		Timestamp: q.Timestamp,
	}
}
func FromQuote(quote domain.Quote) QuoteEntity {
	return QuoteEntity{
		Isin:      quote.Isin,
		Currency:  quote.Currency,
		Quote:     quote.Price,
		Timestamp: quote.Timestamp,
	}
}
