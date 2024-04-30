package com.store

import com.github.michaelbull.result.Result
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher

object wasSuccessful: Matcher<Result<Any, ErrorCode>> {
    override val description = "is a successful Result"

    override fun invoke(actual: Result<Any, ErrorCode>): MatchResult =
        if (actual.isOk) MatchResult.Match
        else MatchResult.Mismatch("Result was a failure!")
}
