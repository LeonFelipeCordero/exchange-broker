package orders

import (
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/metric"
)

var (
	tracer              = otel.Tracer("mini_orders")
	meter               = otel.Meter("mini_orders")
	orderCreatedCounter metric.Int64Counter
	orderFilledCounter  metric.Int64Counter
)
