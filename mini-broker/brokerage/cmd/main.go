package main

import (
	"brokerage/internal/market_data"
	"brokerage/internal/orders"
	"brokerage/pkg/infra"
	"context"
	"log"
	"sync"
)

func main() {
	infra.SetupBrokerInfra()

	ctx := context.Background()
	marketDataConnector := market_data.CreateMarketDataConnector(ctx)
	marketDataConsumer := market_data.CreateMarketDataConsumer(ctx)

	var wg sync.WaitGroup
	wg.Add(2)
	log.Println("initializing market data consumer...")
	go marketDataConnector.Connect()
	go marketDataConsumer.Connect()
	go orders.StartRandomOrderCreation(ctx)
	go orders.ConnectExchangeCreateOrderApi()
	// todo I still need to handle the orders updates from the exchange
	wg.Wait()
}
