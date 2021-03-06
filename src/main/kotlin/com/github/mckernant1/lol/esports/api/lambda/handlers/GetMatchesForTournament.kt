package com.github.mckernant1.lol.esports.api.lambda.handlers

import com.amazonaws.services.dynamodbv2.document.ItemUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.github.mckernant1.lol.esports.api.Match
import com.github.mckernant1.lol.esports.api.lambda.MATCHES_TABLE_NAME
import com.github.mckernant1.lol.esports.api.lambda.TOURNAMENTS_TABLE_NAME
import com.github.mckernant1.lol.esports.api.lambda.TOURNAMENT_INDEX
import com.github.mckernant1.lol.esports.api.lambda.ddb
import com.github.mckernant1.lol.esports.api.lambda.models.ErrorResponse
import com.github.mckernant1.lol.esports.api.lambda.models.Response
import com.github.mckernant1.lol.esports.api.lambda.models.SuccessResponse
import com.github.mckernant1.lol.esports.api.lambda.util.mapToObject

class GetMatchesForTournament : AbstractPathVariableRequestHandler() {
    override val pathParamName: String = "tournamentId"

    override fun execute(paramValue: String): Response {

        ddb.query(
            QueryRequest(TOURNAMENTS_TABLE_NAME)
                .withIndexName(TOURNAMENT_INDEX)
                .withKeyConditionExpression("tournamentId = :desiredTourney")
                .withExpressionAttributeValues(
                    mapOf(":desiredTourney" to AttributeValue(paramValue))
                )
        ).items.ifEmpty {
            return ErrorResponse(
                "The tournament with id $paramValue does not exist",
                "NoSuchTournamentException",
                404
            )
        }

        val items = ddb.query(
            QueryRequest(MATCHES_TABLE_NAME)
                .withKeyConditionExpression("tournamentId = :desiredTourney")
                .withExpressionAttributeValues(
                    mapOf(":desiredTourney" to AttributeValue(paramValue))
                )
        ).items.asSequence()
            .map { ItemUtils.toItem(it).asMap() }
            .map { mapToObject(it, Match::class) }
            .toList()

        return SuccessResponse(
            items,
            200
        )
    }
}
