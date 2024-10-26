package com.ph.exchange.orders.service

import com.ph.exchange.transactionaloutbox.InternalEventingMessageStatus
import io.quarkus.logging.Log
import io.quarkus.vertx.ConsumeEvent
import io.quarkus.websockets.next.OpenConnections
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class OrderFilledEventListener {
//    private final val job = SupervisorJob()
//    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)
    private val ordersConnections = mutableMapOf<String, String>()

    @Inject
    private lateinit var openConnections: OpenConnections

    // todo this needs to broadcast only to the target connection
    @ConsumeEvent("ORDER_FILLED")
    fun consume(message: String): Uni<String> {
        Log.info("Got message $message")
        openConnections
            .filter { ordersConnections.values.contains(it.id()) }
            .forEach {
                Log.info("sending message $message FILLED -> ${it.id()}")
                it.sendTextAndAwait(message)
            }
        return Uni.createFrom().item(InternalEventingMessageStatus.OK.name)
    }

    //    @PostConstruct
//    fun init() {
//        scope.launch {
//             todo make it from the source
//            val ordersChannel = Channel<OrderFilledEvent>()
//            for (event in ordersChannel) {
//                openConnections
//                    .filter { ordersConnections.values.contains(it.id()) }
//                    .forEach {
//                        Log.info("sending message ${event.orderReference} FILLED -> ${it.id()}")
//                        it.sendTextAndAwait(event)
//                    }
//            }
//        }
//    }
//
    fun addConnection(wsConnId: String) {
        ordersConnections[wsConnId] = wsConnId
    }

    fun removeConnection(wsConnId: String) {
        ordersConnections.remove(wsConnId)
    }
}