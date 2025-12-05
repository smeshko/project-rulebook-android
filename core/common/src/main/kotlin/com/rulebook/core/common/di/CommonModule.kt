package com.rulebook.core.common.di

import org.koin.dsl.module

val commonModule = module {
    // Common utilities are typically stateless and don't need DI registration
    // This module serves as a placeholder for future common dependencies
    // such as dispatchers, date/time utilities, or shared formatters
}
