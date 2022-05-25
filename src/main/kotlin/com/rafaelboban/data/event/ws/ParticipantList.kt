package com.rafaelboban.data.event.ws

import com.rafaelboban.data.event.ParticipantData
import com.rafaelboban.data.event.ws.BaseModel
import com.rafaelboban.utils.Constants.TYPE_ANNOUNCEMENT
import com.rafaelboban.utils.Constants.TYPE_PARTICIPANT_LIST

data class ParticipantList(
    val participantData: List<ParticipantData>
) : BaseModel(TYPE_PARTICIPANT_LIST)