package ke.eelaminnovations.kangaishop

import ke.eelaminnovations.kangaishop.domain.model.LedgerTransaction
import ke.eelaminnovations.kangaishop.domain.model.TransactionDirection
import ke.eelaminnovations.kangaishop.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerBalanceTest {

    @Test
    fun testSupplierBalanceCalculation() {
        val txs = listOf(
            LedgerTransaction(
                id = "1",
                personId = "supp1",
                type = TransactionType.MILK_DELIVERY,
                direction = TransactionDirection.DEBIT,
                amount = 1300.0 // shop owes supplier
            ),
            LedgerTransaction(
                id = "2",
                personId = "supp1",
                type = TransactionType.PAYMENT_MPESA,
                direction = TransactionDirection.CREDIT,
                amount = 1000.0 // shop paid supplier
            ),
            LedgerTransaction(
                id = "3",
                personId = "supp1",
                type = TransactionType.MILK_DELIVERY,
                direction = TransactionDirection.DEBIT,
                amount = 600.0 // shop owes supplier
            )
        )

        // Calculate balance
        var balance = 0.0
        txs.forEach { tx ->
            if (tx.direction == TransactionDirection.DEBIT) {
                balance += tx.amount
            } else {
                balance -= tx.amount
            }
        }

        // shop owes: 1300 - 1000 + 600 = 900
        assertEquals(900.0, balance, 0.001)
    }

    @Test
    fun testCustomerBalanceCalculation() {
        val txs = listOf(
            LedgerTransaction(
                id = "1",
                personId = "cust1",
                type = TransactionType.CREDIT_ISSUED,
                direction = TransactionDirection.DEBIT,
                amount = 2500.0 // customer owes shop
            ),
            LedgerTransaction(
                id = "2",
                personId = "cust1",
                type = TransactionType.CUSTOMER_PAYMENT_CASH,
                direction = TransactionDirection.CREDIT,
                amount = 1500.0 // customer paid shop
            )
        )

        var balance = 0.0
        txs.forEach { tx ->
            if (tx.direction == TransactionDirection.DEBIT) {
                balance += tx.amount
            } else {
                balance -= tx.amount
            }
        }

        // customer owes: 2500 - 1500 = 1000
        assertEquals(1000.0, balance, 0.001)
    }

    @Test
    fun testCombinedSupplierCustomerNetBalance() {
        val supplierBalance = 1000.0 // Shop owes supplier
        val customerBalance = 300.0  // Customer owes shop
        
        // Correct calculation:
        val netBalance = supplierBalance - customerBalance
        assertEquals(700.0, netBalance, 0.001)
        
        val onlyCustomerBalance = 300.0
        val onlySupplierBalance = 0.0
        val customerOnlyNetBalance = onlySupplierBalance - onlyCustomerBalance
        assertEquals(-300.0, customerOnlyNetBalance, 0.001)
    }
}
