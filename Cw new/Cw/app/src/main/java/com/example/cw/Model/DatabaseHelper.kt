package com.example.cw.Model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import kotlin.random.Random
import org.mindrot.jbcrypt.BCrypt

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CwBank.db"
        private const val DATABASE_VERSION = 6

        // Users Table
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_MOBILE_NO = "mobile_no"
        const val COLUMN_PASSWORD = "password"

        private const val CREATE_TABLE_USERS = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_MOBILE_NO TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """

        // Cards Table
        const val TABLE_CARDS = "cards"
        const val COLUMN_CARD_ID = "card_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_CARD_HOLDER_NAME = "card_holder_name"
        const val COLUMN_BANK = "bank"
        const val COLUMN_CARD_NO = "card_no"
        const val COLUMN_CVV = "cvv"
        const val COLUMN_EXPIRE = "expire"
        const val COLUMN_BALANCE = "balance"

        private const val CREATE_TABLE_CARDS = """
            CREATE TABLE $TABLE_CARDS (
                $COLUMN_CARD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_CARD_HOLDER_NAME TEXT NOT NULL,
                $COLUMN_BANK TEXT NOT NULL,
                $COLUMN_CARD_NO TEXT NOT NULL UNIQUE,
                $COLUMN_CVV TEXT NOT NULL,
                $COLUMN_EXPIRE TEXT NOT NULL,
                $COLUMN_BALANCE REAL NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """

        // Wallet Table
        const val TABLE_WALLET = "wallet"
        const val COLUMN_WALLET_ID = "wallet_id"
        const val COLUMN_WALLET_BALANCE = "balance"

        private const val CREATE_TABLE_WALLET = """
            CREATE TABLE $TABLE_WALLET (
                $COLUMN_WALLET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_WALLET_BALANCE REAL NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """

        // Transactions Table
        const val TABLE_TRANSACTIONS = "transactions"
        const val COLUMN_TRANSACTION_ID = "transaction_id"
        const val COLUMN_TRANSACTION_USER_ID = "user_id"
        const val COLUMN_TRANSACTION_CARD_ID = "card_id"
        const val COLUMN_TRANSACTION_TYPE = "transaction_type"
        const val COLUMN_TRANSACTION_AMOUNT = "amount"
        const val COLUMN_TRANSACTION_DATE = "date"

        private const val CREATE_TABLE_TRANSACTIONS = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRANSACTION_USER_ID INTEGER NOT NULL,
                $COLUMN_TRANSACTION_CARD_ID INTEGER,
                $COLUMN_TRANSACTION_TYPE TEXT NOT NULL,
                $COLUMN_TRANSACTION_AMOUNT REAL NOT NULL,
                $COLUMN_TRANSACTION_DATE TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_TRANSACTION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COLUMN_TRANSACTION_CARD_ID) REFERENCES $TABLE_CARDS($COLUMN_CARD_ID) ON DELETE SET NULL
            )
        """
        // Payees Table
        const val TABLE_PAYEES = "payees"
        const val COLUMN_PAYEE_ID = "payee_id"
        const val COLUMN_PAYEE_NAME = "payee_name"
        const val COLUMN_PAYEE_ACCOUNT_NO = "payee_account_no"
        const val COLUMN_PAYEE_BANK = "payee_bank"

        private const val CREATE_TABLE_PAYEES = """
            CREATE TABLE $TABLE_PAYEES (
                $COLUMN_PAYEE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_PAYEE_NAME TEXT NOT NULL,
                $COLUMN_PAYEE_ACCOUNT_NO TEXT NOT NULL UNIQUE,
                $COLUMN_PAYEE_BANK TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """

        // Billers Table
        const val TABLE_BILLERS = "billers"
        const val COLUMN_BILLER_ID = "biller_id"
        const val COLUMN_BILLER_NAME = "biller_name"
        const val COLUMN_BILLER_ACCOUNT_NO = "biller_account_no"

        private const val CREATE_TABLE_BILLERS = """
            CREATE TABLE $TABLE_BILLERS (
                $COLUMN_BILLER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_BILLER_NAME TEXT NOT NULL,
                $COLUMN_BILLER_ACCOUNT_NO TEXT NOT NULL UNIQUE,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_CARDS)
        db.execSQL(CREATE_TABLE_WALLET)
        db.execSQL(CREATE_TABLE_TRANSACTIONS)
        db.execSQL(CREATE_TABLE_PAYEES)
        db.execSQL(CREATE_TABLE_BILLERS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WALLET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYEES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BILLERS")
        onCreate(db)
    }

    fun addTransaction(userId: Int, cardId: Int?, type: String, amount: Double, date: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRANSACTION_USER_ID, userId)
            put(COLUMN_TRANSACTION_CARD_ID, cardId)
            put(COLUMN_TRANSACTION_TYPE, type)
            put(COLUMN_TRANSACTION_AMOUNT, amount)
            put(COLUMN_TRANSACTION_DATE, date)
        }
        val result = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return result != -1L
    }


    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    // registerUser function
    fun registerUser(username: String, email: String, mobileNo: String, password: String): Boolean {
        val hashedPassword = hashPassword(password)
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_MOBILE_NO, mobileNo)
            put(COLUMN_PASSWORD, password)
        }

        val userId = db.insert(TABLE_USERS, null, values)
        if (userId == -1L) {
            db.close()
            return false // User registration failed
        }

        // create a wallet for this user
        val walletValues = ContentValues().apply {
            put(COLUMN_USER_ID, userId.toInt())
            put(COLUMN_USERNAME, username)
            put(COLUMN_WALLET_BALANCE, 0.0)
        }

        val walletResult = db.insert(TABLE_WALLET, null, walletValues)
        db.close()
        return walletResult != -1L
    }

    // register card
    fun registerCard(userId: Int, cardHolderName: String, bank: String, cardNo: String, cvv: String, expire: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_CARD_HOLDER_NAME, cardHolderName)
            put(COLUMN_BANK, bank)
            put(COLUMN_CARD_NO, cardNo)
            put(COLUMN_CVV, cvv)
            put(COLUMN_EXPIRE, expire)
            put(COLUMN_BALANCE, generateRandomBalance())
        }

        val result = db.insert(TABLE_CARDS, null, values)
        db.close()
        return result != -1L
    }

    // get user by username and password
    fun getUserByUsernameAndPassword(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )

        var foundUser: User? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val retrievedUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val retrievedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))

            foundUser = User(id, retrievedUsername, retrievedPassword)
        }

        cursor.close()
        db.close()
        return foundUser
    }

    // get cards by user ID
    fun getCardsByUserId(userId: Int): List<Card> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CARDS,
            arrayOf(COLUMN_CARD_ID, COLUMN_CARD_HOLDER_NAME, COLUMN_BANK, COLUMN_CARD_NO, COLUMN_CVV, COLUMN_EXPIRE, COLUMN_BALANCE),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        val cards = mutableListOf<Card>()
        while (cursor.moveToNext()) {
            val cardId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CARD_ID))
            val holderName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_HOLDER_NAME))
            val branch = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BANK))
            val cardNo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NO))
            val cvv = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CVV))
            val expire = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRE))
            val balance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE))

            cards.add(Card(cardId, userId, holderName, branch, cardNo, cvv, expire, balance))
        }
        cursor.close()
        db.close()
        return cards
    }

    // generate random balance
    private fun generateRandomBalance(): Double {
        return String.format("%.2f", Random.nextDouble(5000.0, 200000.0)).toDouble()  // Limiting to 2 decimal places
    }

    //get username by ID
    fun getUsernameById(userId: Int): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USERNAME),
            "$COLUMN_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        var username: String? = null
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
        }

        cursor.close()
        db.close()
        return username
    }

    // get wallet by user ID
    fun getWalletByUserId(userId: Int): Wallet? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_WALLET,
            arrayOf(COLUMN_WALLET_ID, COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_WALLET_BALANCE),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        var wallet: Wallet? = null
        if (cursor.moveToFirst()) {
            wallet = Wallet(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WALLET_ID)),
                userId,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WALLET_BALANCE))
            )
        }
        cursor.close()
        return wallet
    }

    // create wallet
    fun createWallet(userId: Int, username: String, balance: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_USERNAME, username)
            put(COLUMN_WALLET_BALANCE, balance)
        }

        val result = db.insert(TABLE_WALLET, null, values)
        db.close()
        return result != -1L
    }

    // top up wallet
    fun topUpWallet(userId: Int, amount: Double): Boolean {
        val db = writableDatabase
        val wallet = getWalletByUserId(userId)
        return if (wallet != null) {
            val newBalance = wallet.balance + amount
            val values = ContentValues().apply {
                put(COLUMN_WALLET_BALANCE, newBalance)
            }
            val result = db.update(TABLE_WALLET, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
            db.close()
            result > 0
        } else {
            false
        }
    }
    fun topUpWalletFromCard(userId: Int, cardId: Int, amount: Double): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.query(
                TABLE_CARDS,
                arrayOf(COLUMN_BALANCE),
                "$COLUMN_CARD_ID = ?",
                arrayOf(cardId.toString()),
                null, null, null
            )
            var cardBalance: Double? = null
            if (cursor.moveToFirst()) {
                cardBalance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE))
            }
            cursor.close()
            if (cardBalance == null || cardBalance < amount) {
                return false
            }

            val newCardBalance = cardBalance - amount
            val cardValues = ContentValues().apply {
                put(COLUMN_BALANCE, newCardBalance)
            }
            val cardUpdateResult = db.update(TABLE_CARDS, cardValues, "$COLUMN_CARD_ID = ?", arrayOf(cardId.toString()))

            val wallet = getWalletByUserId(userId) ?: return false
            val newWalletBalance = wallet.balance + amount
            val walletValues = ContentValues().apply {
                put(COLUMN_WALLET_BALANCE, newWalletBalance)
            }
            val walletUpdateResult = db.update(TABLE_WALLET, walletValues, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))

            if (cardUpdateResult > 0 && walletUpdateResult > 0) {
                db.setTransactionSuccessful()
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }
    fun getTransactionsByUserId(userId: Int): List<Transactions> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            arrayOf(COLUMN_TRANSACTION_ID, COLUMN_TRANSACTION_CARD_ID, COLUMN_TRANSACTION_TYPE, COLUMN_TRANSACTION_AMOUNT, COLUMN_TRANSACTION_DATE),
            "$COLUMN_TRANSACTION_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$COLUMN_TRANSACTION_DATE DESC"
        )

        val transactions = mutableListOf<Transactions>()
        while (cursor.moveToNext()) {
            val transactionId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID))
            val cardId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CARD_ID))
            val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE))

            transactions.add(Transactions(transactionId, userId, cardId, type, amount, date))
        }
        cursor.close()
        db.close()
        return transactions
    }
    fun deductFromCard(cardId: Int, amount: Double): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT balance FROM cards WHERE card_id = ?", arrayOf(cardId.toString()))

        if (cursor.moveToFirst()) {
            val currentBalance = cursor.getDouble(0)
            if (currentBalance >= amount) {
                val newBalance = currentBalance - amount
                db.execSQL("UPDATE cards SET balance = ? WHERE card_id = ?", arrayOf(newBalance, cardId))
                cursor.close()
                return true
            }
        }
        cursor.close()
        return false
    }

    fun insertTransaction(userId: Int, cardId: Int, type: String, amount: Double, date: String) {
        val db = writableDatabase
        db.execSQL(
            "INSERT INTO transactions (user_id, card_id, transaction_type, amount, date) VALUES (?, ?, ?, ?, ?)",
            arrayOf(userId, cardId, type, amount, date)
        )
        db.close()
    }
    fun deleteCard(cardId: Int) {
        val db = this.writableDatabase
        db.delete("cards", "card_id = ?", arrayOf(cardId.toString()))
        db.close()
    }
    fun getEmailById(userId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT email FROM users WHERE id=?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val email = cursor.getString(0)
            cursor.close()
            email
        } else {
            cursor.close()
            null
        }
    }

    fun getPhoneNumberById(userId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT mobile_no FROM users WHERE id=?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val phone = cursor.getString(0)
            cursor.close()
            phone
        } else {
            cursor.close()
            null
        }
    }

    fun updateUserDetails(userId: Int, username: String, email: String, phoneNumber: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("email", email)
            put("mobile_no", phoneNumber)
        }
        db.update("users", values, "id=?", arrayOf(userId.toString()))
    }
    fun getPasswordById(userId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT password FROM users WHERE id=?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val password = cursor.getString(0)
            cursor.close()
            password
        } else {
            cursor.close()
            null
        }
    }

    fun updatePassword(userId: Int, newPassword: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("password", newPassword)
        }
        db.update("users", values, "id=?", arrayOf(userId.toString()))
    }
    // add a new payee
    fun addPayee(userId: Int, name: String, accountNo: String, bank: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_PAYEE_NAME, name)
            put(COLUMN_PAYEE_ACCOUNT_NO, accountNo)
            put(COLUMN_PAYEE_BANK, bank)
        }
        val result = db.insert(TABLE_PAYEES, null, values)
        db.close()
        return result != -1L
    }

    // get payees by user ID
    fun getPayeesByUserId(userId: Int): List<Payee> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PAYEES,
            arrayOf(COLUMN_PAYEE_ID, COLUMN_PAYEE_NAME, COLUMN_PAYEE_ACCOUNT_NO, COLUMN_PAYEE_BANK),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        val payees = mutableListOf<Payee>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PAYEE_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYEE_NAME))
            val accountNo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYEE_ACCOUNT_NO))
            val bank = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYEE_BANK))

            payees.add(Payee(id, userId, name, accountNo, bank))
        }
        cursor.close()
        db.close()
        return payees
    }

    // add a new biller
    fun addBiller(userId: Int, name: String, accountNo: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_BILLER_NAME, name)
            put(COLUMN_BILLER_ACCOUNT_NO, accountNo)
        }
        val result = db.insert(TABLE_BILLERS, null, values)
        db.close()
        return result != -1L
    }

    // get billers by user ID
    fun getBillersByUserId(userId: Int): List<Biller> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BILLERS,
            arrayOf(COLUMN_BILLER_ID, COLUMN_BILLER_NAME, COLUMN_BILLER_ACCOUNT_NO),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        val billers = mutableListOf<Biller>()
        while (cursor.moveToNext()) {
            billers.add(
                Biller(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BILLER_ID)),
                    userId,
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BILLER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BILLER_ACCOUNT_NO))
                )
            )
        }
        cursor.close()
        db.close()
        return billers
    }
    fun getTotalCardBalance(userId: Int): Double {
        val db = readableDatabase
        val query = "SELECT SUM(balance) FROM Cards WHERE user_id = ?\n"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        var totalBalance = 0.0
        if (cursor.moveToFirst()) {
            totalBalance = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return totalBalance
    }
    fun withdrawFromWalletToCard(userId: Int, cardId: Int, amount: Double): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val wallet = getWalletByUserId(userId)
            if (wallet == null || wallet.balance < amount) {
                return false
            }

            val cursor = db.query(
                TABLE_CARDS,
                arrayOf(COLUMN_BALANCE),
                "$COLUMN_CARD_ID = ?",
                arrayOf(cardId.toString()),
                null, null, null
            )

            var cardBalance: Double? = null
            if (cursor.moveToFirst()) {
                cardBalance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE))
            }
            cursor.close()

            if (cardBalance == null) {
                return false // Card not found
            }

            // Deduct from wallet
            val newWalletBalance = wallet.balance - amount
            val walletValues = ContentValues().apply {
                put(COLUMN_WALLET_BALANCE, newWalletBalance)
            }
            val walletUpdateResult = db.update(TABLE_WALLET, walletValues, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))

            // Add to card
            val newCardBalance = cardBalance + amount
            val cardValues = ContentValues().apply {
                put(COLUMN_BALANCE, newCardBalance)
            }
            val cardUpdateResult = db.update(TABLE_CARDS, cardValues, "$COLUMN_CARD_ID = ?", arrayOf(cardId.toString()))

            if (walletUpdateResult > 0 && cardUpdateResult > 0) {
                db.setTransactionSuccessful()
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }
}

data class Card(
    val cardId: Int,
    val userId: Int,
    val cardHolderName: String,
    val bank: String,
    val cardNo: String,
    val cvv: String,
    val expire: String,
    val balance: Double
)

data class Wallet(
    val walletId: Int,
    val userId: Int,
    val username: String,
    val balance: Double
)

data class Transactions(
    val transactionId: Int,
    val userId: Int,
    val cardId: Int?,
    val type: String,
    val amount: Double,
    val date: String
)

data class Payee(
    val payeeId: Int,
    val userId: Int,
    val name: String,
    val accountNo: String,
    val bank: String
)

data class Biller(
    val billerId: Int,
    val userId: Int,
    val name: String,
    val accountNo: String
)

