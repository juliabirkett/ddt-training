package com.store

data class Product(
    val id: Int,
    val description: String,
    val quantity: Int,
) {
    fun reduceStock(): Product = copy(quantity = quantity - 1)
    fun isForAdultsOnly() = description == "cigarettes" || description == "alcohol"
}
