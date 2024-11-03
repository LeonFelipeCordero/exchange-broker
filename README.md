# Mini Broker

Simple/naive implementation of market data and order placing/matching happens in a exchange. The idea is create a high load in two systems.
1) The exchange system will create a high load of instrumetns and quotes that need to be consumed by it self and by the order broker
2) Broker generates a high load of ordes that are then matched in the exchange and and broadcast back to the borker

By putting the two systems under a high load we could experiment patterns or technologies to solve those problem at a low cost.

```mermaid
flowchart
    subgraph BUS
        MARKET_DATA_TOPIC
        INSTRUMENTS_QUEUE
        QUOTES_QUEUE
    end
    subgraph Exchange
        MarketDataProducerAPI
        InstrumentsCache
        QuotesCache
    end
    
    subgraph BROKER
        MarketDataConnector
        MarketDataConsumer
        InstrumentsTimetable
        QuotesTimetable
        BUS
    end 

    MarketDataProducerAPI -- random_instruments --> InstrumentsCache[(MarketDataCache)]
    MarketDataProducerAPI -- random_quotes --> QuotesCache[(MarketDataCache)]
    MarketDataConnector <-- WSConn . /ws/instruments --> MarketDataProducerAPI
    MarketDataConnector <-- WSConn . /ws/quotes --> MarketDataProducerAPI
    MarketDataConnector -- instrument . updated --> MARKET_DATA_TOPIC
    MarketDataConnector -- quote . updated --> MARKET_DATA_TOPIC
    MARKET_DATA_TOPIC ---> INSTRUMENTS_QUEUE
    MARKET_DATA_TOPIC ---> QUOTES_QUEUE
    QUOTES_QUEUE ---> MarketDataConsumer
    INSTRUMENTS_QUEUE ---> MarketDataConsumer
    MarketDataConsumer -- Saves --> InstrumentsTimetable[(InstrumentsTimetable)]
    MarketDataConsumer -- Saves --> QuotesTimetable[(QuotesTimetable)]
```

```mermaid
flowchart
    subgraph FRONTEND_INTERFACE
        OrdersProducer
    end
    subgraph BROKER
        OrderHandler
        OrderConnector
        BROKER_BUS
    end
    subgraph BROKER_BUS
        BROKER_ORDER_TOPIC
        BROKER_ORDER_CREATED_QUEUE
    end
    subgraph EXCHANGE_BUS
        EXCHANGE_ORDER_TOPIC
        EXCHANGE_ORDER_CREATED_QUEUE
    end
    subgraph EXCHANGE
        OrderSubmissionApi
        OrderUpdatesApi
        EXCHANGE_BUS
        OrdersStorage
        OrderMatcher
        EXCHANGE_ORDER_TOPIC
    end

    OrdersProducer -- random order --> OrderHandler
    OrderHandler -- order . created --> BROKER_ORDER_TOPIC
    BROKER_ORDER_TOPIC ---> BROKER_ORDER_CREATED_QUEUE
    BROKER_ORDER_CREATED_QUEUE ---> OrderConnector
    OrderConnector -- WSConn . /ws/order/submission --> OrderSubmissionApi
    OrderConnector <-- WSConn . /ws/order/update/institution_id --> OrderUpdatesApi
    OrderSubmissionApi <-- order . created --> EXCHANGE_ORDER_TOPIC
    EXCHANGE_ORDER_TOPIC ---> EXCHANGE_ORDER_CREATED_QUEUE
    EXCHANGE_ORDER_CREATED_QUEUE -- Match Order --> OrderMatcher
    OrderMatcher -- Save Orders and Matches --> OrdersStorage[(OrdersStorage)]
    OrderMatcher -- Broadcast order cancellation --> OrderUpdatesApi
    OrdersStorage[(OrdersStorage)] -- Broadcast Filled orders --> OrderUpdatesApi
```

### Current status
The project is not completed and still need tuning. Some basic knowns to take in consideration are:
2) Better query tunning
3) Broker needs to finish the order updates part
4) Order cancellation in both sides
5) Internal bus implementation have issues to complete order fulfilled events

### Setup Infrastructure

```bash
docker compose -f development/docker-compose.yaml up &
```
