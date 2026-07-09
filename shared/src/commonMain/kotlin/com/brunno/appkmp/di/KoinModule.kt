package com.brunno.appkmp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// Aqui declarar Repositórios, ViewModels e instâncias de Banco/Rede
val appModule = module {
    // Exemplo:
    // single { MeuRepositorio() }
    // factory { MeuViewModel(get()) }
}

// Função para inicializar o Koin (recebe parâmetros nativos quando chamado pelo Android)
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModule)
    }
}

// Função de atalho sem parâmetros (ideal para ser chamada pelo iOS)
fun initKoin() = initKoin {}