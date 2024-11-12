package orders

import (
	"context"
	"encoding/json"
	"log"
	"math"
	"math/rand"
	domain "mini-broker/domain/model"
	"mini-broker/pkg/cache"
	"mini-broker/pkg/storage"
	"time"

	"github.com/google/uuid"
	"github.com/spf13/viper"
)

type OrdersProducer struct {
	marketDataRepository storage.MarketDataRepository
	traders              *cache.SliceStore
	ordersChannel        chan []byte
}

func CreateOrderProducer(ordersChannel chan []byte) OrdersProducer {
	numberOfTraders := viper.GetInt("application.number-of-traders")
	traders := cache.NewSliceStore()
	for i := 0; i < numberOfTraders; i++ {
		id, _ := uuid.NewUUID()
		traders.Push(id.String())
	}

	return OrdersProducer{
		marketDataRepository: storage.CreateMarketDataRepository(),
		traders:              traders,
		ordersChannel:        ordersChannel,
	}
}

func (o *OrdersProducer) StartStreaming(ctx context.Context) {
	go o.startTicker(ctx)
}

func (o *OrdersProducer) startTicker(ctx context.Context) {
	orderTickerConfig := viper.GetInt("application.order-ticker")
	orderTicker := time.Tick(time.Duration(orderTickerConfig) * time.Millisecond)
loop:
	for {
		select {
		case <-ctx.Done():
			break loop
		case <-orderTicker:
			go o.createOrders()
		}
	}
}

func (o *OrdersProducer) createOrders() {
	ctx, span := tracer.Start(context.Background(), "orders.creation")
	defer span.End()

	quotes := o.marketDataRepository.FindAllQuotes(ctx)

	numberOfTraders := viper.GetInt("application.number-of-traders")
	institutionId := viper.GetString("application.institution-id")

	log.Printf("creating orders for %d instruments", len(quotes))
	for _, quote := range quotes {
		for i := 0; i < 5; i++ {
			random := rand.Intn(numberOfTraders)
			user := o.traders.Get(random)

			price := randomisePrice(quote.Price)
			quantity := randomiseQuantity()
			amount := price * quantity

			order := domain.Order{
				Reference:   uuid.NewString(),
				Instrument:  quote.Isin,
				Nominals:    quantity,
				Price:       price,
				Amount:      amount,
				Type:        randomiseType(),
				Trader:      user,
				Institution: institutionId,
				Currency:    quote.Currency,
				Timestamp:   time.Now(),
				Filled:      false,
			}
			encoded, _ := json.Marshal(order)
			o.ordersChannel <- encoded
		}
	}
}

func randomisePrice(price float64) float64 {
	percentage := rand.Float64()
	upDown := rand.Intn(2)
	change := (price * percentage) / 100
	if upDown == 0 {
		return price - change
	} else {
		return price + change
	}
}

func randomiseType() string {
	upDown := rand.Intn(2)
	if upDown == 0 {
		return "SELL"
	} else {
		return "BUY"
	}
}

func randomiseQuantity() float64 {
	random := rand.Float64()
	if random == 0.0 {
		return randomiseQuantity()
	}
	integer, _ := math.Modf(random * 100)
	return integer
}
