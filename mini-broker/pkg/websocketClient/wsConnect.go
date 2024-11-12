package websocketClient

import (
	"fmt"
	"log"
	"os"
	"os/signal"

	"github.com/gorilla/websocket"
	"github.com/spf13/viper"
)

func Connect(endpoint string) *websocket.Conn {
	connectionUrl := fmt.Sprintf("%s/%s", viper.Get("exchange.websocket"), endpoint)
	log.Printf("connecting to %s", connectionUrl)

	c, _, err := websocket.DefaultDialer.Dial(connectionUrl, nil)
	if err != nil {
		log.Fatal("dial:", err)
	}

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)
	go handeInterruptSignal(c, interrupt)

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
