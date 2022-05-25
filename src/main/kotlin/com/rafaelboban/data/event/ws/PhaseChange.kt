package com.rafaelboban.data.event.ws

import com.rafaelboban.data.event.Event
import com.rafaelboban.utils.Constants.TYPE_PHASE_CHANGE

data class PhaseChange(
    var phase: Event.Phase,
    val eventId: String
) : BaseModel(TYPE_PHASE_CHANGE)