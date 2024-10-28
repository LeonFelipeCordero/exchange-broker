package config

import "time"

const (
	MarketDataTopic      = "broker_market_data_topic"
	InstrumentsQueue     = "broker_instrument_updated_queue"
	QuotesQueue          = "broker_quote_updated_queue"
	InstrumentUpdatedKey = "instrument.updated"
	QuoteUpdatedKey      = "quote.updated"

	BrokerOrdersTopic     = "broker_orders_topic"
	BrokerOrderCreatedKey = "order.created"
	BrokerOrderQueue      = "broker_orders_queue"

	// TradersSize should be 100
	TradersSize = 1
	// InstitutionId Institution ID to add in orders
	InstitutionId = "test_institution_12345"
)

// OrderTicker original value 100ms
var OrderTicker = time.Tick(2000 * time.Millisecond)
