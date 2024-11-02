package orders

import (
	"brokerage/config"
	domain "brokerage/domain/model"
	"brokerage/pkg/cache"
	"brokerage/pkg/storage"
	"context"
	"encoding/json"
	"github.com/google/uuid"
	"log"
	"math"
	"math/rand"
	"time"
)

type OrdersProducer struct {
	marketDataRepository storage.MarketDataRepository
	traders              *cache.SliceStore
	ordersChannel        chan []byte
}

func CreateOrderProducer(ordersChannel chan []byte) OrdersProducer {
	traders := cache.NewSliceStore()
	for i := 0; i < config.TradersSize; i++ {
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
loop:
	for {
		select {
		case <-ctx.Done():
			break loop
		case <-config.OrderTicker:
			go o.createOrders()
		}
	}
}

func (o *OrdersProducer) createOrders() {
	ctx, span := tracer.Start(context.Background(), "orders.creation")
  defer span.End()

	quotes := o.marketDataRepository.FindAllQuotes(ctx)

	// change how to randomise the orders
	log.Printf("creating orders for %d instruments", len(quotes))
	for _, quote := range quotes {
		for i := 0; i < 5; i++ {
			random := rand.Intn(config.TradersSize)
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
				Institution: config.InstitutionId,
				Currency:    quote.Currency,
				Timestamp:   time.Now(),
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
