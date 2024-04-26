package com.store.cli

import com.store.Product
import com.store.StorageRepository
import com.store.StoreAppHub
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.*

fun main() {
    managerCliApp(repository = InMemoryStorageRepository)
}

object InMemoryStorageRepository : StorageRepository {
    private val products = mutableListOf<Product>()

    override fun findAll(): List<Product> = products
    override fun save(product: Product) {
        products.find { it.id == product.id }?.let { existingProduct ->
            products.remove(existingProduct)
        }

        products += product
    }
}

fun customerCliApp(
    inFun: InputStream = System.`in`,
    outFun: OutputStream = System.out,
    repository: StorageRepository,
) {
    val hub = StoreAppHub(repository)

    val catalog = hub.catalog()
        .joinToString("\n") { product ->
            product.id.toString()
        }

    PrintStream(outFun).println(
        """
            ------------ Product catalog ------------ 
            $catalog
            
            What's the ID of the product you want to buy?
        """
    )
    val scanner = Scanner(inFun)

    while (scanner.hasNext()) {
        val id = scanner.nextInt()

        hub.buy(id)
    }
}

fun managerCliApp(
    inFun: InputStream = System.`in`,
    outFun: OutputStream = System.out,
    repository: StorageRepository,
) {
    val hub = StoreAppHub(repository)
    val scanner = Scanner(inFun)

    PrintStream(outFun).println(
        """
             ------------ Log in  ------------
             
             Are you really a manager? Type the secret!
        """
    )

    val passwordString = scanner.nextLine().toString()
    hub.logInAsAManager(passwordString)

    PrintStream(outFun).println(
        """
             ------------ Register product  ------------
             How many products do you want to register?
        """
    )
    val quantityToRegister = scanner.nextLine().toInt()

    PrintStream(outFun).println(
        """
        Please enter the product details in this format:

        > id,description,quantity
        """
    )

    for (i in 0..< quantityToRegister) {
        PrintStream(outFun).println("Registering product #$i:")

        val inputString = scanner.nextLine().toString().split(",")
        val id = inputString[0].toInt()
        val description = inputString[1]
        val quantity = inputString[2].toInt()

        hub.register(Product(id = id, description = description, quantity = quantity))
    }
}
