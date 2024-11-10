package rabbitmq

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/spf13/viper"
)

const maxSize = 10

type Rabbitmq struct {
	channel          *amqp.Channel
	connection       *amqp.Connection
	channels         []*amqp.Channel // todo is it possible to abstract all this o ring patter style?
	channelRingCount int32
	sync.Mutex
}

func CreateRabbitMQConnection() *Rabbitmq {
  uri := fmt.Sprint(viper.Get("rabbitmq.uri"))
	connection, err := amqp.Dial(uri)
	FailOnError(err, "Failed to connect to RabbitMQ")

	channel, err := connection.Channel()
	FailOnError(err, "Failed to open a channel")

	channels := make([]*amqp.Channel, 0, 10)
	for i := 0; i < maxSize; i++ {
		ch, err := connection.Channel()
		FailOnError(err, "Failed to open a channel")

		channels = append(channels, ch)
	}

	return &Rabbitmq{
		connection:       connection,
		channel:          channel,
		channels:         channels,
		channelRingCount: 0,
	}
}

func (r *Rabbitmq) Close() {
	r.Lock()
	defer r.Unlock()
	r.connection.Close()
	r.connection.Close()
}

func (r *Rabbitmq) CreateExchange(name string) {
	r.Lock()
	defer r.Unlock()
	err := r.channel.ExchangeDeclare(
		name,
		"topic",
		true,
		false,
		false,
		false,
		nil,
	)
	FailOnError(err, "Failed to declare an exchange-go")
}

func (r *Rabbitmq) BindQueue(name string, key string, exchange string) {
	r.Lock()
	defer r.Unlock()
	log.Printf("Binding new queue %s to and existing exchange-go %s with key %s", name, exchange, key)
	queue, err := r.channel.QueueDeclare(
		name,
		false,
		false,
		false,
		false,
		nil,
	)
	FailOnError(err, "Failed to declare a queue")

	err = r.channel.QueueBind(
		queue.Name,
		key,
		exchange,
		false,
		nil,
	)
	FailOnError(err, "Failed to declare a queue")
}

func (r *Rabbitmq) ConsumeQueue(name string) <-chan amqp.Delivery {
	r.Lock()
	defer r.Unlock()
	messagesChannel, err := r.channel.Consume(
		name,
		"",
		true,
		false,
		false,
		false,
		nil,
	)
	FailOnError(err, "impossible read messages from topic")

	return messagesChannel
}

func (r *Rabbitmq) PublishTopic(topic string, key string, body []byte) {
	ctx, cancel := context.WithTimeout(context.Background(), 50*time.Millisecond)
	defer cancel()

	channel := r.acquireChannel()

	err := channel.PublishWithContext(
		ctx,
		topic,
		key,
		false,
		false,
		amqp.Publishing{
			ContentType: "text/plain",
			Body:        body,
		})
	LogOnError(err, "Impossible to send message")
}

func (r *Rabbitmq) acquireChannel() *amqp.Channel {
	r.Lock()
	defer r.Unlock()

	r.channelRingCount++

	if r.channelRingCount == maxSize {
		r.channelRingCount = 0
		return r.channels[0]
	}

	return r.channels[r.channelRingCount]
}

func FailOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

func LogOnError(err error, msg string) {
	if err != nil {
		log.Printf("%s: %s", msg, err)
	}
}
