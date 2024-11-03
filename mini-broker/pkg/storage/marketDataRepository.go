package storage

import (
	"context"
	domain "mini-broker/domain/model"
	"time"
)

type MarketDataRepository interface {
	FindAllQuotes(ctx context.Context) []domain.Quote
	SaveInstrument(ctx context.Context, instrument domain.Instrument) domain.Instrument
	SaveInstruments(ctx context.Context, instruments []domain.Instrument)
	SaveQuote(ctx context.Context, quote domain.Quote) domain.Quote
}

type marketDataImpl struct {
	*PostgresStorage
}

func CreateMarketDataRepository() MarketDataRepository {
	return marketDataImpl{
		PostgresStorage: CreatePostgresStorage(),
	}
}

func (m marketDataImpl) FindAllQuotes(ctx context.Context) []domain.Quote {
	var quotesEntity []QuoteEntity
	m.PostgresStorage.Db.Raw(
		"select quote.* from (select quoteGroupIsin.isin as grouppedIsin, max(quoteGroupIsin.timestamp) as groupTimestamp from quotes_timetable quoteGroupIsin group by quoteGroupIsin.isin) as data join quotes_timetable as quote on quote.isin = data.grouppedIsin and quote.timestamp = data.groupTimestamp;",
	).Scan(&quotesEntity)

	var quotes []domain.Quote
	for _, entity := range quotesEntity {
		quotes = append(quotes, entity.toQuote())
	}

	return quotes
}

func (m marketDataImpl) SaveInstrument(ctx context.Context, instrument domain.Instrument) domain.Instrument {
	instrumentEntity := FromInstrument(instrument)
	instrumentEntity.Timestamp = time.Now()
	m.PostgresStorage.Db.Create(instrumentEntity)
	return instrumentEntity.toInstrument()
}

func (m marketDataImpl) SaveInstruments(ctx context.Context, instruments []domain.Instrument) {
	for _, instrument := range instruments {
		instrumentEntity := FromInstrument(instrument)
		instrumentEntity.Timestamp = time.Now()
		m.PostgresStorage.Db.Create(instrumentEntity)
	}
}

func (m marketDataImpl) SaveQuote(ctx context.Context, quote domain.Quote) domain.Quote {
	quoteEntity := FromQuote(quote)
	quoteEntity.Timestamp = time.Now()
	m.PostgresStorage.Db.Create(quoteEntity)
	return quoteEntity.toQuote()
}
