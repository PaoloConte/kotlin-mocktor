package io.paoloconte.mocktor

import io.ktor.http.Parameters

class QueryParamsBuilder {
    private val params = mutableListOf<Pair<String, String>>()
    private val ignoreParams = mutableSetOf<String>()

    fun param(key: String, value: String) {
        params.add(key to value)
    }

    fun ignoreParam(param: String) {
        ignoreParams.add(param)
    }

    fun ignoreParams(vararg params: String) {
        ignoreParams.addAll(params)
    }

    internal fun build(ignoreUnknownParams: Boolean): QueryParams {
        return QueryParams(
            params = params.groupBy({ it.first }, { it.second }),
            ignoreParams = ignoreParams,
            ignoreUnknownParams = ignoreUnknownParams
        )
    }
}

class QueryParams(
    private val params: Map<String, List<String>>,
    private val ignoreParams: Set<String>,
    private val ignoreUnknownParams: Boolean
) {
    fun matches(actualParams: Parameters): MatchResult {
        val expectedFiltered = params.filterKeys { it !in ignoreParams }
        val actualMap = actualParams.entries()
            .associate { it.key to it.value }
            .filterKeys { it !in ignoreParams }

        val missingParams = expectedFiltered.keys - actualMap.keys
        val extraParams = actualMap.keys - expectedFiltered.keys

        if (missingParams.isNotEmpty() || (!ignoreUnknownParams && extraParams.isNotEmpty())) {
            val details = buildList {
                if (missingParams.isNotEmpty()) add("missing: $missingParams")
                if (!ignoreUnknownParams && extraParams.isNotEmpty()) add("extra: $extraParams")
            }.joinToString(", ")
            return MatchResult.Mismatch("Query parameter keys mismatch: $details")
        }

        for ((key, expectedValues) in expectedFiltered) {
            val actualValues = actualMap[key] ?: emptyList()
            if (actualValues != expectedValues) {
                return MatchResult.Mismatch("Query parameter '$key' mismatch: expected $expectedValues but was $actualValues")
            }
        }

        return MatchResult.Match
    }
}