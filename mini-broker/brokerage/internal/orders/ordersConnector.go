package orders

import (
	"brokerage/config"
	"brokerage/pkg/rabbitmq"
	"log"
	"os"
	"os/signal"

	"github.com/gorilla/websocket"
)

func ConnectExchangeCreateOrderApi() {
	rabbitmqSession := rabbitmq.CreateRabbitMQConnection()
	queueChannel := rabbitmqSession.ConsumeQueue(config.BrokerOrderCreatedQueue)

	host := "ws://localhost:8080/ws/orders/submission"
	log.Printf("connecting to %s", host)

	c, _, err := websocket.DefaultDialer.Dial(host, nil)
	if err != nil {
		log.Fatal("dial:", err)
	}

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)

	go handeInterruptSignal(c, interrupt)

	for message := range queueChannel {
		err = c.WriteMessage(websocket.TextMessage, message.Body)
		if err != nil {
			log.Println("Error writing message to websocket connection", err)
		}
	}
}

func ConnectExchangeOrderUpdatesApi() {
	//rabbitmqSession := rabbitmq.CreateRabbitMQConnection()
	//queueChannel := rabbitmqSession.ConsumeQueue(config.BrokerOrderQueue)
	//
	host := "ws://localhost:8080/ws/orders/update/" + config.InstitutionId
	log.Printf("connecting to %s", host)

	c, _, err := websocket.DefaultDialer.Dial(host, nil)
	if err != nil {
		log.Fatal("dial:", err)
	}

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)

	var messageChannel = make(chan []byte)

	go handeInterruptSignal(c, interrupt)
	go handleConnection(c, messageChannel)

	//for message := range queueChannel {
	//	err = c.WriteMessage(websocket.TextMessage, message.Body)
	//	if err != nil {
	//		log.Println("Error writing message to websocket connection", err)
	//	}
	//}
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
		//todo this one has to be be first connected to a consumer, for now only prints the message
		messageChannel <- message
		// log.Printf("received: %s", message)
	}
}
