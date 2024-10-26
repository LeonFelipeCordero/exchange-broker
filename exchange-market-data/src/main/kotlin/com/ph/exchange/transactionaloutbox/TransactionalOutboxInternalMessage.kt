package com.ph.exchange.transactionaloutbox

import java.time.OffsetDateTime

data class TransactionalOutboxInternalMessage(
    val sequence: Long? = null,
    val event: String,
    val message: String,
    val sent: Boolean = false,
    val sentAt: OffsetDateTime? = null,
)

enum class InternalEventingMessageStatus() {
    OK,
    ERROR_HANDLED
}

//interface TargetInformation {
//    val type: TransactionalOutboxTargetType
//}
//
//data class InternalTarget(
//    override val type: TransactionalOutboxTargetType = TransactionalOutboxTargetType.INTERNAL,
//    val requestName: String,
//) : TargetInformation
//
//data class ExternalTarget(
//    override val type: TransactionalOutboxTargetType = TransactionalOutboxTargetType.EXTERNAL,
//    val resourceName: String,
//    val key: String?,
//) : TargetInformation
//
//enum class TransactionalOutboxTargetType {
//    INTERNAL,
//    EXTERNAL
//}