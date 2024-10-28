package orders

import (
	"brokerage/config"
	"brokerage/pkg/rabbitmq"
	"context"
)

func StartRandomOrderCreation(ctx context.Context) {
	ordersChannel := make(chan []byte)
	ordersProducer := CreateOrderProducer(ordersChannel)
	go ordersProducer.StartStreaming(ctx)

	rabbitmqSession := rabbitmq.CreateRabbitMQConnection()

	for {
		select {
		case <-ctx.Done():
			return
		case orderBytes := <-ordersChannel:
			rabbitmqSession.PublishTopic(config.BrokerOrdersTopic, config.BrokerOrderCreatedKey, orderBytes)
		}
	}
}
