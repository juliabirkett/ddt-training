package com.store.cli

import com.github.michaelbull.result.mapBoth
import com.store.*
import java.io.PrintStream
import java.time.LocalDate
import java.util.*

sealed class Command(open val value: String)
class ManagerLoginCommand(override val value: String) : Command(value)
class CustomerLoginCommand(override val value: String) : Command(value)
class RegisterProductCommand(override val value: String) : Command(value)
class ShowCatalogCommand(override val value: String) : Command(value)

sealed class BuyProductCommand(override val value: String) : Command(value)
class BuyAdultProductCommand(override val value: String) : BuyProductCommand(value)
class BuyNormalProductCommand(override val value: String) : BuyProductCommand(value)

class CommandParser {
    fun parse(command: String): Command = when {
        command.startsWith("manager-login") -> ManagerLoginCommand(command.removePrefix("manager-login "))
        command.startsWith("customer-login") -> CustomerLoginCommand(command.removePrefix("customer-login "))
        command.startsWith("register-product") -> RegisterProductCommand(command.removePrefix("register-product "))
        command.startsWith("show-catalog") -> ShowCatalogCommand(command.removePrefix("show-catalog "))
        command.startsWith("buy") ->
            if (command.removePrefix("buy ").contains(","))
                BuyAdultProductCommand(command.removePrefix("buy "))
            else BuyNormalProductCommand(command.removePrefix("buy "))
        else -> TODO("Could not parse command!")
    }
}

fun app(
    repository: StorageRepository,
    userStorage: UserManagerRepository,
) {
    val managerHub = ManagerAppHub(repository, userStorage)
    val customerHub = CustomerAppHub(repository, userStorage)

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
            is BuyNormalProductCommand -> customerHub.buy(command.value.toInt())
                .mapBoth(
                    success = { "Product bought! ${command.value}" },
                    failure = { it.message }
                )
            is BuyAdultProductCommand -> {
                val commandString = command.value.split(",")

                customerHub
                    .buy(commandString[0].trim().toInt(), commandString[1].trim().toInt())
                    .mapBoth(
                        success = { "Product bought! ${command.value}" },
                        failure = { it.message }
                    )
            }
        }
    }

    PrintStream(System.out).println(output)
    return
}
