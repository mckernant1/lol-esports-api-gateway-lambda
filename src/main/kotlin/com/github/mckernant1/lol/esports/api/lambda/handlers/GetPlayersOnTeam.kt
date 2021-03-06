package com.github.mckernant1.lol.esports.api.lambda.handlers

import com.amazonaws.services.dynamodbv2.document.ItemUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.github.mckernant1.lol.esports.api.Player
import com.github.mckernant1.lol.esports.api.lambda.PLAYERS_TABLE_NAME
import com.github.mckernant1.lol.esports.api.lambda.PLAYERS_TABLE_TEAM_INDEX
import com.github.mckernant1.lol.esports.api.lambda.TEAMS_TABLE_NAME
import com.github.mckernant1.lol.esports.api.lambda.ddb
import com.github.mckernant1.lol.esports.api.lambda.models.ErrorResponse
import com.github.mckernant1.lol.esports.api.lambda.models.Response
import com.github.mckernant1.lol.esports.api.lambda.models.SuccessResponse
import com.github.mckernant1.lol.esports.api.lambda.util.mapToObject

class GetPlayersOnTeam : AbstractPathVariableRequestHandler() {
    override val pathParamName: String = "teamId"

    override fun execute(paramValue: String): Response {

        ddb.getItem(
            TEAMS_TABLE_NAME,
            mapOf(
                "teamId" to AttributeValue(paramValue)
            )
        ).item ?: return ErrorResponse(
            "teamId $paramValue does not exist",
            "NoSuchTeamException",
            404
        )

        val items = ddb.query(
            QueryRequest(PLAYERS_TABLE_NAME)
                .withIndexName(PLAYERS_TABLE_TEAM_INDEX)
                .withKeyConditionExpression("teamId = :desiredTeam")
                .withExpressionAttributeValues(mapOf(":desiredTeam" to AttributeValue(paramValue)))
        ).items.asSequence()
            .map { ItemUtils.toItem(it).asMap() }
            .map { mapToObject(it, Player::class) }
            .toList()

        return SuccessResponse(
            items,
            200
        )
    }
}
