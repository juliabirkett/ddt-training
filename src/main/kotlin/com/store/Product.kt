package com.store

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

data class Product(
    val id: Int,
    val description: String,
    val quantity: Int,
) {
    fun isForAdultsOnly() = description == "cigarettes" || description == "alcohol"

    fun buy(isLegalAged: () -> Boolean): Result<Product, ErrorCode> = when {
        quantity <= 0 -> Err(ProductIsOutOfStock)
        isForAdultsOnly() && !isLegalAged() -> Err(ProductForAdultsOnly)
        else -> Ok(withReducedStock())
    }

    private fun withReducedStock(): Product = copy(quantity = quantity - 1)
}