package com.store.cli

import com.github.michaelbull.result.mapBoth
import com.store.CustomerAppHub
import com.store.ManagerAppHub
import com.store.Product
import com.store.StorageRepository
import java.io.PrintStream
import java.time.LocalDate
import java.util.*

sealed class Command(open val value: String)
class ManagerLoginCommand(override val value: String) : Command(value)
class CustomerLoginCommand(override val value: String) : Command(value)
class RegisterProductCommand(override val value: String) : Command(value)
class ShowCatalogCommand(override val value: String) : Command(value)
class BuyProductCommand(override val value: String) : Command(value)

class CommandParser {
    fun parse(command: String): Command = when {
        command.startsWith("manager-login") -> ManagerLoginCommand(command.removePrefix("manager-login "))
        command.startsWith("customer-login") -> CustomerLoginCommand(command.removePrefix("customer-login "))
        command.startsWith("register-product") -> RegisterProductCommand(command.removePrefix("register-product "))
        command.startsWith("show-catalog") -> ShowCatalogCommand(command.removePrefix("show-catalog "))
        command.startsWith("buy") -> BuyProductCommand(command.removePrefix("buy "))
        else -> TODO("Could not parse command!")
    }
}

fun app(
    repository: StorageRepository,
) {
    val managerHub = ManagerAppHub(repository)
    val customerHub = CustomerAppHub(repository)

    val output: String = with(Scanner(System.`in`).nextLine()) {
        when (val command = CommandParser().parse(this)) {
            is ManagerLoginCommand -> managerHub.logIn(command.value)
                .mapBoth(
                    success = { "Logged in successfully!" },
                    failure = { it.message }
                )
            is CustomerLoginCommand -> customerHub.logIn(command.value, LocalDate.now())
                .mapBoth(
                    success = { "Logged in successfully!" },
                    failure = { it.message }
                )
            is RegisterProductCommand -> {
                val productInfo = command.value.split(",")

                managerHub.register(
                    Product(
                        id = productInfo[0].toInt(),
                        description = productInfo[1],
                        quantity = productInfo[2].toInt()
                    )
                ).mapBoth(
                    success = { "Product registered successfully" },
                    failure = { it.message }
                )
            }
            is ShowCatalogCommand -> customerHub.catalog()
                .mapBoth(
                    success = { products -> products.joinToString { it.id.toString() } },
                    failure = { it.message }
                )
            is BuyProductCommand -> customerHub.buy(command.value.toInt())
                .mapBoth(
                    success = { "Product bought! ${command.value}" },
                    failure = { it.message }
                )
        }
    }

    PrintStream(System.out).println(output)
    return
}
