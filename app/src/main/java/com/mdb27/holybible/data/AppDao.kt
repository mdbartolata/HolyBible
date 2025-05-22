package com.mdb27.holybible.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Upsert
    suspend fun upsertBook(book: Book)

    @Upsert
    suspend fun upsertBook(book: List<Book>)

    @Upsert
    suspend fun upsertScripture(scripture: List<Scripture>)

    @Upsert
    suspend fun upsertScripture(scripture: Scripture)

    @Upsert
    suspend fun upsertLastRead(lastRead: LastRead)

    @Upsert
    suspend fun upsertLastRead(lastRead: List<LastRead>)

    @Transaction
    @Query("SELECT COUNT(id) from book")
    fun getBookCount(): Int

    @Transaction
    @Query("SELECT COUNT(id) from scripture")
    fun getScriptureCount() : Int

    @Transaction
    @Query("SELECT * FROM Book")
    fun getBooks(): Flow<List<Book>>

    @Transaction
    @Query("SELECT (COUNT (DISTINCT scripture.chapter)) AS totalChapters, * FROM scripture LEFT JOIN book ON book.id = scripture.bookId GROUP BY book.id")
    fun getBookWithTotalChapters(): Flow<List<BookWithTotalChapters>>

    @Transaction
    @Query("SELECT * FROM BOOK WHERE id = :id")
    fun getCurrentBook(id: Int): Flow<Book>

    @Transaction
    @Query("SELECT * FROM SCRIPTURE WHERE bookId = :id")
    fun getScriptures(id: Int): Flow<List<Scripture>>

    @Transaction
    @Query("SELECT * FROM SCRIPTURE WHERE bookId = :id and isMarked = 1")
    fun getFilteredScriptures(id: Int): Flow<List<Scripture>>

    @Transaction
    @Query("SELECT * FROM lastread where bookId = :bookId")
    fun getLastRead(bookId: Int): Flow<LastRead?>


    @Transaction
    @Query("SELECT * FROM lastread")
    fun getAllLastRead(): List<LastRead>

    @Transaction
    @Query("SELECT * FROM scripture WHERE isMarked = 1")
    fun getAllMarkedScriptures(): List<Scripture>
}