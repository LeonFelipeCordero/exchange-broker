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
	wg.Add(5)
	log.Println("initializing market data and orders consumers...")
	go marketDataConnector.Connect()
	go marketDataConsumer.Connect()
	go orders.StartRandomOrderCreation(ctx)
	go orders.ConnectExchangeCreateOrderApi()
	go orders.ConnectExchangeOrderUpdatesApi()
	wg.Wait()
}
