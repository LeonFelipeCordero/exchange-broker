package market_data

import (
	"brokerage/config"
	domain "brokerage/domain/model"
	"brokerage/pkg/rabbitmq"
	"brokerage/pkg/storage"
	"context"
	"encoding/json"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.opentelemetry.io/otel/metric"
)

func init() {
	var err error
	instrumentCapturedCounter, err = meter.Int64Counter(
		"instrument.message.caputred",
		metric.WithDescription("Number Instruments state changes caputred"),
		metric.WithUnit("{count}"),
	)
	if err != nil {
		panic(err)
	}

	quoteCapturedCounter, err = meter.Int64Counter(
		"quote.message.captured",
		metric.WithDescription("Number of quotes captured"),
		metric.WithUnit("{count}"),
	)
	if err != nil {
		panic(err)
	}
}

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
		ctx, span := tracer.Start(m.ctx, "instrument.caputred")
    instrumentCapturedCounter.Add(ctx, 1)
		body := message.Body
		var instruments []domain.Instrument
		err := json.Unmarshal(body, &instruments)
		if err != nil {
			log.Printf("could not deserialise instruments message")
		}
		// go m.marketDataRepository.SaveInstruments(m.ctx, instruments)
		m.marketDataRepository.SaveInstruments(m.ctx, instruments)
    span.End()
	}
}

func (m *MarketDataConsumer) consumeQuotes(channel <-chan amqp.Delivery) {
	for message := range channel {
    ctx, span:= tracer.Start(m.ctx, "quotes.captured")
    quoteCapturedCounter.Add(ctx,1)
		body := message.Body
		quote := domain.Quote{}
		err := json.Unmarshal(body, &quote)
		if err != nil {
			log.Printf("could not deserialise quote message")
		}
		// go m.marketDataRepository.SaveQuote(m.ctx, quote)
		m.marketDataRepository.SaveQuote(m.ctx, quote)
    span.End()
	}
}
