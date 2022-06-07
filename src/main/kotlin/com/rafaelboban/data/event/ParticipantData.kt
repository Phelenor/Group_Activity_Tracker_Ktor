package com.rafaelboban.data.event

data class ParticipantData(
    val id: String,
    val name: String,
    var lastUpdateTimestamp: Long,
    var status: Status = Status.WAITING,
) {

    enum class Status(val text: String) {
        WAITING("Waiting"), ACTIVE("Active"), FINISHED("Finished"), LEFT("Left")
    }
}
