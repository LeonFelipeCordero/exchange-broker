package marketData

import (
	"context"
	"log"
	"mini-broker/config"
	"mini-broker/pkg/rabbitmq"
	"mini-broker/pkg/websocketClient"
	"time"

	"github.com/gorilla/websocket"
	"go.opentelemetry.io/otel/metric"
)

func init() {
	var err error
	instrumentPublishedCounter, err = meter.Int64Counter(
		"instrument.message.published",
		metric.WithDescription("Number Instruments state changes arriving"),
		metric.WithUnit("{count}"),
	)
	if err != nil {
		panic(err)
	}

	quotePublishedCounter, err = meter.Int64Counter(
		"quote.message.published",
		metric.WithDescription("Number of quotes arriving"),
		metric.WithUnit("{count}"),
	)
	if err != nil {
		panic(err)
	}
}

type MarketDataConnector struct {
	rabbitmqSession *rabbitmq.Rabbitmq
	ctx             context.Context
}

func CreateMarketDataConnector(ctx context.Context, rabbitmqSession *rabbitmq.Rabbitmq) MarketDataConnector {
	return MarketDataConnector{
		rabbitmqSession: rabbitmqSession,
		ctx:             ctx,
	}
}

func (m *MarketDataConnector) Connect() {
	instrumentsChannel := m.connectTo("instruments")
	quotesChannel := m.connectTo("quotes")

	go m.consumeInstruments(instrumentsChannel)
	time.Sleep(2 * time.Second)
	go m.consumeQuotes(quotesChannel)
}

func (m *MarketDataConnector) connectTo(endpoint string) chan []byte {
	c := websocketClient.Connect(endpoint)

	var messageChannel = make(chan []byte)
	go handleConnection(c, messageChannel)

	return messageChannel
}

func handleConnection(connection *websocket.Conn, messageChannel chan []byte) {
	for {
		_, message, err := connection.ReadMessage()
		if err != nil {
			log.Println("read:", err)
			return
		}
    // log.Printf("Received: %s", message)
		messageChannel <- message
	}
}

func (m *MarketDataConnector) consumeInstruments(channel chan []byte) {
	for message := range channel {
		ctx, span := tracer.Start(m.ctx, "instrument-published")
		instrumentPublishedCounter.Add(ctx, 1)
		m.rabbitmqSession.PublishTopic(config.MarketDataTopic, config.InstrumentUpdatedKey, message)
		span.End()
	}
}

func (m *MarketDataConnector) consumeQuotes(channel chan []byte) {
	for message := range channel {
		ctx, span := tracer.Start(m.ctx, "quote-published")
		quotePublishedCounter.Add(ctx, 1)
		m.rabbitmqSession.PublishTopic(config.MarketDataTopic, config.QuoteUpdatedKey, message)
		span.End()
	}
}
