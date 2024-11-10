package orders

import (
	"fmt"
	"log"
	"mini-broker/config"
	"mini-broker/pkg/rabbitmq"
	"os"
	"os/signal"

	"github.com/gorilla/websocket"
	"github.com/spf13/viper"
)

func ConnectExchangeCreateOrderApi(rabbitmqSession *rabbitmq.Rabbitmq) {
	queueChannel := rabbitmqSession.ConsumeQueue(config.BrokerOrderCreatedQueue)

	c := connectToWs("orders/submission")

	for message := range queueChannel {
		err := c.WriteMessage(websocket.TextMessage, message.Body)
		if err != nil {
			log.Println("Error writing message to websocket connection", err)
		}
	}
}

func ConnectExchangeOrderUpdatesApi(rabbitmqSession *rabbitmq.Rabbitmq) {
	//rabbitmqSession := rabbitmq.CreateRabbitMQConnection()
	//queueChannel := rabbitmqSession.ConsumeQueue(config.BrokerOrderQueue)

  institutionId := viper.GetString("application.intitution-id")
	c := connectToWs("orders/update/" + institutionId)

	var messageChannel = make(chan []byte)
	go handleConnection(c, messageChannel)

	//for message := range queueChannel {
	//	err = c.WriteMessage(websocket.TextMessage, message.Body)
	//	if err != nil {
	//		log.Println("Error writing message to websocket connection", err)
	//	}
	//}
}

func connectToWs(endpoint string) *websocket.Conn {
	host := fmt.Sprintf("%s/%s", viper.Get("exchange.websocket"), endpoint)
	log.Printf("connecting to %s", host)

	c, _, err := websocket.DefaultDialer.Dial(host, nil)
	if err != nil {
		log.Fatal("dial:", err)
	}

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)

	return c
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
		log.Printf("received: %s", message)
		messageChannel <- message
	}
}
