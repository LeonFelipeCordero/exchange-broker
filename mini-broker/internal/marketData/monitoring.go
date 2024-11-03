package marketData

import (
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/metric"
)

var (
	tracer                     = otel.Tracer("mini_broker_market_data")
	meter                      = otel.Meter("mini_broker_market_data")
	instrumentPublishedCounter metric.Int64Counter
	quotePublishedCounter      metric.Int64Counter
	instrumentCapturedCounter  metric.Int64Counter
	quoteCapturedCounter       metric.Int64Counter
)
