package com.mutissx.napptilusrickandmorty.feature.characters.di

import org.koin.dsl.module

/** Everything the characters feature binds. Needs core:network's module to be loaded too. */
val charactersModule = module {
    includes(dataModule, databaseModule, domainModule, presentationModule)
}
