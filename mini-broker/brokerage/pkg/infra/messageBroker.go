package infra

import (
	"commons/rabbitmq"
)

func SetupBrokerInfra() {
	rabbitmqSession := rabbitmq.CreateRabbitMQConnection()
	defer rabbitmqSession.Close()

	// market_data
	//rabbitmqSession.CreateExchange(config.MarketDataTopic)
	//rabbitmqSession.BindQueue(config.InstrumentsQueue, config.InstrumentUpdatedKey, config.MarketDataTopic)
	//rabbitmqSession.BindQueue(config.QuotesQueue, config.QuoteUpdatedKey, config.MarketDataTopic)

	//orders
	//rabbitmqSession.CreateExchange(config.BrokerOrdersTopic)
	//rabbitmqSession.BindQueue(config.BrokerOrderQueue, config.BrokerOrderCreatedKey, config.BrokerOrdersTopic)
}
