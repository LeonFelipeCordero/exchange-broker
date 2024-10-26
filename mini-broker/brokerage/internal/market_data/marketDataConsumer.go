package market_data

import (
	"brokerage/pkg/storage"
	"commons/config"
	domain "commons/domain/model"
	"commons/rabbitmq"
	"context"
	"encoding/json"
	amqp "github.com/rabbitmq/amqp091-go"
	"log"
	"time"
)

type MarketDataConsumer struct {
	rabbitmqSession      *rabbitmq.Rabbitmq
	marketDataRepository storage.MarketDataRepository
	ctx                  context.Context
}

func CreateMarketDataConsumer(ctx context.Context) MarketDataConsumer {
	return MarketDataConsumer{
		rabbitmqSession:      rabbitmq.CreateRabbitMQConnection(),
		marketDataRepository: storage.CreateMarketDataRepository(),
		ctx:                  ctx,
	}
}

func (m *MarketDataConsumer) Connect() {
	instrumentsChannel := m.rabbitmqSession.ConsumeQueue(config.InstrumentsQueue)
	quotesChannel := m.rabbitmqSession.ConsumeQueue(config.QuotesQueue)

	go m.consumeInstruments(instrumentsChannel)
	time.Sleep(2 * time.Second)
	go m.consumeQuotes(quotesChannel)
}

func (m *MarketDataConsumer) consumeInstruments(channel <-chan amqp.Delivery) {
	for message := range channel {
		body := message.Body
		var instruments []domain.Instrument
		err := json.Unmarshal(body, &instruments)
		if err != nil {
			log.Printf("could not deserialise instruments message")
		}
		go m.marketDataRepository.SaveInstruments(m.ctx, instruments)
	}
}

func (m *MarketDataConsumer) consumeQuotes(channel <-chan amqp.Delivery) {
	for message := range channel {
		body := message.Body
		quote := domain.Quote{}
		err := json.Unmarshal(body, &quote)
		if err != nil {
			log.Printf("could not deserialise quote message")
		}
		go m.marketDataRepository.SaveQuote(m.ctx, quote)
	}
}
