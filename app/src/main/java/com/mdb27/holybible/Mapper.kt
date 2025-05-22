package com.mdb27.holybible

import com.mdb27.holybible.data.Book
import com.mdb27.holybible.data.LastRead
import com.mdb27.holybible.data.Scripture
import com.mdb27.holybible.model.BookModel
import com.mdb27.holybible.model.LastReadModel
import com.mdb27.holybible.model.ScriptureModel

fun interface Mapper<in From, out To> {
    fun map(from: From): To
}

object LastReadEntityToModelMapper: Mapper<LastRead, LastReadModel> {
    override fun map(from: LastRead): LastReadModel {
        return LastReadModel(
            pos = from.pos,
            offset = from.offset
        )
    }
}

data class LastReadModelToEntityMapper(val bookId: Int): Mapper<LastReadModel, LastRead> {
    override fun map(from: LastReadModel): LastRead {
        return LastRead(
            bookId = bookId,
            pos = from.pos,
            offset = from.offset
        )
    }

}

object ScriptureEntityToModelMapper: Mapper<Scripture, ScriptureModel> {
    override fun map(from: Scripture): ScriptureModel {

        return ScriptureModel(
            id = from.id,
            chapter = from.chapter,
            verse = from.verse,
            content = from.content,
            bookId = from.bookId,
            isMarked = from.isMarked
        )
    }
}

object ScriptureModelToEntityMapper: Mapper<ScriptureModel, Scripture> {
    override fun map(from: ScriptureModel): Scripture {
        return Scripture(
            id = from.id,
            chapter = from.chapter,
            verse = from.verse,
            content = from.content,
            bookId = from.bookId,
            isMarked = from.isMarked
        )
    }

}

data class BookEntityToModelMapper(val totalChapters: Int): Mapper<Book, BookModel> {
    override fun map(from: Book): BookModel {
        return BookModel(
            id = from.id,
            title = from.title,
            category = from.category,
            bookOrder = from.bookOrder,
            totalChapters = totalChapters
        )
    }

}


