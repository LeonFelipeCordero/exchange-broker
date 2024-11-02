package orders

import (
	"brokerage/config"
	"brokerage/pkg/rabbitmq"
	"context"
)

var rabbitmqSession = rabbitmq.CreateRabbitMQConnection()

func StartRandomOrderCreation(ctx context.Context) {
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
	spanCtx, span := tracer.Start(ctx, "order.created")
	orderCreatedCounter.Add(spanCtx, 1)
	rabbitmqSession.PublishTopic(config.BrokerOrdersTopic, config.BrokerOrderCreatedKey, orderBytes)
	span.End()
}
