package config

import "time"

const (
	MarketDataTopic      = "broker_market_data_topic"
	InstrumentsQueue     = "broker_instrument_updated_queue"
	QuotesQueue          = "broker_quote_updated_queue"
	InstrumentUpdatedKey = "instrument.updated"
	QuoteUpdatedKey      = "quote.updated"

	// exchange orders processor
	ExchangeOrderCreatedTopic   = "exchange_orders_topic"
	ExchangeOrderCreatedKey     = "exchange.orders.created"
	ExchangeOrderProcessorQueue = "exchange_order_updates_queue"

	// exchange orders matcher
	ExchangeOrderMatchingTopic = "exchange_order_matching_topic"
	ExchangeOrderMatchedKey    = "exchange.orders.matched"
	ExchangeOrderUpdatedKey    = "exchange.orders.updated"
	ExchangeOrderMatcherQueue  = "exchange_order_matcher_queue"

	// broker only
	BrokerOrdersTopic     = "broker_orders_topic"
	BrokerOrderCreatedKey = "order.created"
	BrokerOrderQueue      = "broker_orders_queue"

	// InstrumentsSize should be 1000
	InstrumentsSize = 5
	// TradersSize should be 100
	TradersSize = 1
	// InstitutionId Institution ID to add in orders
	InstitutionId = "test_institution_12345"
)

// InstrumentTicker original value is 100ms
var InstrumentTicker = time.Tick(1000 * time.Millisecond)

// QuotesTicker original value is 10ms
var QuotesTicker = time.Tick(1000 * time.Millisecond)

// OrderTicker original value 100ms
var OrderTicker = time.Tick(2000 * time.Millisecond)
