package com.rafaelboban.data.event.ws

import com.rafaelboban.utils.Constants.TYPE_ANNOUNCEMENT

data class Announcement(
    val eventId: String,
    val message: String,
    val timestamp: Long,
    val announcementType: Int
) : BaseModel(TYPE_ANNOUNCEMENT) {

    companion object {
        const val TYPE_PLAYER_JOINED = 0
        const val TYPE_PLAYER_LEFT = 1
        const val TYPE_PLAYER_HELP = 2
        const val TYPE_PLAYER_HELP_CLEAR = 3
        const val TYPE_PLAYER_FINISHED = 4
    }
}