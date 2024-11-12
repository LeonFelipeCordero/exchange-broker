package orders

import (
	"encoding/json"
	"fmt"
	"log"
	"mini-broker/config"
	domain "mini-broker/domain/model"
	"mini-broker/pkg/rabbitmq"
	"mini-broker/pkg/storage"
	"mini-broker/pkg/websocketClient"

	"github.com/gorilla/websocket"
	"github.com/spf13/viper"
)

func ConnectExchangeCreateOrderApi(rabbitmqSession *rabbitmq.Rabbitmq) {
	queueChannel := rabbitmqSession.ConsumeQueue(config.BrokerOrderCreatedQueue)
	orderRepository := storage.CreateOrderRepository()

	c := websocketClient.Connect("orders/submission")

	for message := range queueChannel {
		var order domain.Order
		json.Unmarshal(message.Body, &order)
		orderRepository.SaveOrder(order)

		err := c.WriteMessage(websocket.TextMessage, message.Body)

		if err != nil {
			log.Println("Error writing message to websocket connection", err)
		}
	}
}

func ConnectExchangeOrderUpdatesApi(rabbitmqSession *rabbitmq.Rabbitmq) {
	endpoint := fmt.Sprintf("orders/update/%s", viper.GetString("application.institution-id"))
	c := websocketClient.Connect(endpoint)

	go handleConnection(c, rabbitmqSession)
}

func handleConnection(connection *websocket.Conn, rabbitmqSession *rabbitmq.Rabbitmq) {
	for {
		_, message, err := connection.ReadMessage()
		if err != nil {
			log.Println("read:", err)
			return
		}
		rabbitmqSession.PublishTopic(config.BrokerOrdersTopic, config.BrokerOrderFilledKey, message)
	}
}

func HanldeOrderFilledEvent(rabbitmqSession *rabbitmq.Rabbitmq) {
	queueChannel := rabbitmqSession.ConsumeQueue(config.BrokerOrderFilledQueue)
	orderRepository := storage.CreateOrderRepository()

	for message := range queueChannel {
		var orderFilledMessage domain.OrderFilledMessage
		json.Unmarshal(message.Body, &orderFilledMessage)

		order := orderRepository.FindOrderByReference(orderFilledMessage.ExternalReference)
		order.Filled = true
		order.FilledTimestamp = &orderFilledMessage.FilledTimestamp

		orderRepository.UpdateOrder(order)
	}
}
