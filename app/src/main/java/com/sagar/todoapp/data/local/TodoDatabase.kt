package com.sagar.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The Room database for this app.
 * It provides the main access point to the persisted data related to todo items.
 */
@Database(entities = [TodoEntity::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    /**
     * Gets the DAO for interacting with the "todos" table.
     */
    abstract fun todoDao(): TodoDao
}