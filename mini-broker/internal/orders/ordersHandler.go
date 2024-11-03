package orders

import (
	"context"
	"mini-broker/config"
	"mini-broker/pkg/rabbitmq"

	"go.opentelemetry.io/otel/metric"
)

var rabbitmqSession *rabbitmq.Rabbitmq

func init() {
	var err error
	orderCreatedCounter, err = meter.Int64Counter(
		"order.created",
		metric.WithDescription("Number orders caputred"),
		metric.WithUnit("{count}"),
	)
	if err != nil {
		panic(err)
	}

	orderFilledCounter, err = meter.Int64Counter(
		"order.filled",
		metric.WithDescription("Number of orders filled"),
		metric.WithUnit("{count}"),
	)
	if err != nil {
		panic(err)
	}

}

func StartRandomOrderCreation(ctx context.Context) {
	rabbitmqSession = rabbitmq.CreateRabbitMQConnection()

	ordersChannel := make(chan []byte)
	ordersProducer := CreateOrderProducer(ordersChannel)
	go ordersProducer.StartStreaming(ctx)

	for {
		select {
		case <-ctx.Done():
			return
		case orderBytes := <-ordersChannel:
			hanldeOrderCreated(ctx, orderBytes)
		}
	}
}

func hanldeOrderCreated(ctx context.Context, orderBytes []byte) {
	_, span := tracer.Start(ctx, "order.created")
	orderCreatedCounter.Add(ctx, 1)
	rabbitmqSession.PublishTopic(config.BrokerOrdersTopic, config.BrokerOrderCreatedKey, orderBytes)
	span.End()
}

