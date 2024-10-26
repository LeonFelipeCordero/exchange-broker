package market_data

import (
	"commons/config"
	"commons/rabbitmq"
	"context"
	"github.com/gorilla/websocket"
	"log"
	"os"
	"os/signal"
	"time"
)

type MarketDataConnector struct {
	rabbitmqSession *rabbitmq.Rabbitmq
	ctx             context.Context
}

func CreateMarketDataConnector(ctx context.Context) MarketDataConnector {
	return MarketDataConnector{
		rabbitmqSession: rabbitmq.CreateRabbitMQConnection(),
		ctx:             ctx,
	}
}

func (m *MarketDataConnector) Connect() {
	instrumentsChannel := m.connectToWs("instruments")
	quotesChannel := m.connectToWs("quotes")

	go m.consumeInstruments(instrumentsChannel)
	time.Sleep(2 * time.Second)
	go m.consumeQuotes(quotesChannel)
}

func (m *MarketDataConnector) connectToWs(endpoint string) chan []byte {
	host := "ws://localhost:8080/ws/" + endpoint
	log.Printf("connecting to %s", host)

	c, _, err := websocket.DefaultDialer.Dial(host, nil)
	if err != nil {
		log.Fatal("dial:", err)
	}
	// todo I need to handle connections in a way that can be closed when the time comes
	//defer c.Close()

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)

	var messageChannel = make(chan []byte)

	go handeInterruptSignal(c, interrupt)
	go handleConnection(c, messageChannel)

	return messageChannel
}

func handeInterruptSignal(connection *websocket.Conn, interrupt chan os.Signal) {
loop:
	for {
		select {
		case <-interrupt:
			log.Println("Received interrupt signal, closing connection...")
			err := connection.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
			if err != nil {
				log.Println("error closing websocket connection", err)
			}
			break loop
		}
	}
}

func handleConnection(connection *websocket.Conn, messageChannel chan []byte) {
	for {
		_, message, err := connection.ReadMessage()
		if err != nil {
			log.Println("read:", err)
			return
		}
		messageChannel <- message
	}
}

func (m *MarketDataConnector) consumeInstruments(channel chan []byte) {
	for message := range channel {
		m.rabbitmqSession.PublishTopic(config.MarketDataTopic, config.InstrumentUpdatedKey, message)
	}
}

func (m *MarketDataConnector) consumeQuotes(channel chan []byte) {
	for message := range channel {
		m.rabbitmqSession.PublishTopic(config.MarketDataTopic, config.QuoteUpdatedKey, message)
	}
}
