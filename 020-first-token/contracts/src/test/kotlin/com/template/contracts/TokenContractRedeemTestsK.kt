package com.template.contracts

import com.template.contracts.TokenContractK.Companion.TOKEN_CONTRACT_ID
import com.template.states.TokenState
import com.template.states.TokenStateK
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyContract
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class TokenContractRedeemTestsK {
    private val ledgerServices = MockServices()
    private val alice = TestIdentity(CordaX500Name("Alice", "London", "GB"))
    private val bob = TestIdentity(CordaX500Name("Bob", "New York", "US"))
    private val carly = TestIdentity(CordaX500Name("Carly", "New York", "US"))

    @Test
    fun `transaction must include a TokenContract command`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                command(listOf(alice.publicKey, bob.publicKey), DummyContract.Commands.Create())
                `fails with`("Required com.template.contracts.TokenContractK.Commands command")
                command(listOf(alice.publicKey, bob.publicKey), TokenContractK.Commands.Redeem())
                verifies()
            }
        }
    }

    @Test
    fun `Redeem transaction must have no outputs`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                output(TOKEN_CONTRACT_ID, TokenStateK(alice.party, carly.party, 10L))
                command(listOf(alice.publicKey, bob.publicKey), TokenContractK.Commands.Redeem())
                `fails with`("No tokens should be issued when redeeming.")
            }
        }
    }

    @Test
    fun `Redeem transaction must have inputs`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, DummyState())
                command(alice.publicKey, TokenContractK.Commands.Redeem())
                `fails with`("There should be tokens to redeem.")
            }
        }
    }

    @Test
    // Testing this may be redundant as these wrong states would have to be issued first, but the contract would not
    // let that happen.
    fun `Inputs must not have a zero quantity`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 0L))
                command(listOf(alice.publicKey, bob.publicKey), TokenContractK.Commands.Redeem())
                `fails with`("All quantities must be above 0.")
            }
        }
    }

    @Test
    // Testing this may be redundant as these wrong states would have to be issued first, but the contract would not
    // let that happen.
    fun `Inputs must not have negative quantity`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, -1L))
                command(listOf(alice.publicKey, bob.publicKey), TokenContractK.Commands.Redeem())
                `fails with`("All quantities must be above 0.")
            }
        }
    }

    @Test
    fun `Issuer must sign Redeem transaction`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                command(bob.publicKey, TokenContractK.Commands.Redeem())
                `fails with`("The issuers should sign.")
            }
        }
    }

    @Test
    fun `Current holder must sign Redeem transaction`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                command(alice.publicKey, TokenContractK.Commands.Redeem())
                `fails with`("The current holders should sign.")
            }
        }
    }

    @Test
    fun `All issuers must sign Redeem transaction`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                input(TOKEN_CONTRACT_ID, TokenStateK(carly.party, bob.party, 20L))
                command(listOf(alice.publicKey, bob.publicKey), TokenContractK.Commands.Redeem())
                `fails with`("The issuers should sign.")
            }
        }
    }

    @Test
    fun `All current holders must sign Redeem transaction`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                input(TOKEN_CONTRACT_ID, TokenStateK(carly.party, bob.party, 20L))
                command(listOf(alice.publicKey, carly.publicKey), TokenContractK.Commands.Redeem())
                `fails with`("The current holders should sign.")
            }
        }
    }

    @Test
    fun `Can have different issuers in Redeem transaction`() {
        ledgerServices.ledger {
            transaction {
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 10L))
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, alice.party, 20L))
                input(TOKEN_CONTRACT_ID, TokenStateK(alice.party, bob.party, 30L))
                input(TOKEN_CONTRACT_ID, TokenStateK(carly.party, bob.party, 20L))
                input(TOKEN_CONTRACT_ID, TokenStateK(carly.party, alice.party, 20L))
                command(listOf(alice.publicKey, bob.publicKey, carly.publicKey), TokenContractK.Commands.Redeem())
                verifies()
            }
        }
    }

}