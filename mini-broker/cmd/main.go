package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"mini-broker/internal/marketData"
	"mini-broker/internal/orders"
	"mini-broker/pkg/infra"
	"os"
	"os/signal"
	"sync"

	"github.com/spf13/viper"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt)
	defer stop()

	setupConfiguration()

	otelShutDown, err := infra.SetupTelemetr(ctx)
	if err != nil {
		log.Println("Error setting up telemetry")
		return
	}
	defer func() {
		err = errors.Join(err, otelShutDown(context.Background()))
	}()

	marketDataConnector := marketData.CreateMarketDataConnector(ctx)
	marketDataConsumer := marketData.CreateMarketDataConsumer(ctx)

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

func setupConfiguration() {
	var env, set = os.LookupEnv("ENVIRONMENT")
	if !set {
		env = "default"
	}

	configFile := fmt.Sprintf("config-%s.yaml", env)

	viper.SetConfigName(configFile)
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	err := viper.ReadInConfig()
	if err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); ok {
			log.Panicf("Configuration file %s not found", configFile)
		} else {
			log.Panicf("Error occured when loading configuration %s, %e", configFile, err)
		}
	}
}

