package com.rulebook.core.database.di

import android.content.Context
import androidx.room.Room
import com.rulebook.core.database.GameDao
import com.rulebook.core.database.RulebookDatabase
import com.rulebook.core.database.RulesDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATABASE_NAME = "rulebook_database"

/**
 * Provides the Room database instance.
 *
 * Uses fallbackToDestructiveMigration for development simplicity.
 * For production with existing users, proper migrations should be implemented.
 */
fun provideDatabase(context: Context): RulebookDatabase {
    return Room.databaseBuilder(
        context,
        RulebookDatabase::class.java,
        DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()
}

fun provideGameDao(database: RulebookDatabase): GameDao {
    return database.gameDao()
}

fun provideRulesDao(database: RulebookDatabase): RulesDao {
    return database.rulesDao()
}

val databaseModule = module {
    single { provideDatabase(androidContext()) }
    single { provideGameDao(get()) }
    single { provideRulesDao(get()) }
}
