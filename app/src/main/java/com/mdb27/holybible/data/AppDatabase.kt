package com.mdb27.holybible.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Book::class, Scripture::class, LastRead::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase(){
    abstract val dao: AppDao
}