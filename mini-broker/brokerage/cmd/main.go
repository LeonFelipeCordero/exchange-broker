package main

import (
	"brokerage/internal/market_data"
	"brokerage/internal/orders"
	"brokerage/pkg/infra"
	"context"
	"errors"
	"log"
	"os"
	"os/signal"
	"sync"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt)
  defer stop()

	infra.SetupBrokerInfra()
  otelShutDown, err := infra.SetupTelemetr(ctx)
  if err != nil {
    log.Println("Error setting up telemetry")
    return 
  }
  defer func() {
    err = errors.Join(err, otelShutDown(context.Background()))
  }()

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

