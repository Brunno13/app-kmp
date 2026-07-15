#!/bin/bash
set -e

# Se a variável APP_ENV não existir, assume "production" como padrão de segurança
ENV="${APP_ENV:-production}"

if [ "$ENV" = "staging" ]; then
    echo "🤖 [Staging] Compilando o APK Android..."
    # Executa a task gerada pelo flavor "staging"
    ./gradlew :composeApp:assembleStagingRelease

    # O Gradle coloca o APK numa subpasta com o nome do flavor
    APK_PATH="composeApp/build/outputs/apk/staging/release/composeApp-staging-release.apk"
    OUTPUT_NAME="app-kmp-staging.apk"
else
    echo "🤖 [Production] Compilando o APK Android..."
    # Executa a task gerada pelo flavor "production"
    ./gradlew :composeApp:assembleProductionRelease

    APK_PATH="composeApp/build/outputs/apk/production/release/composeApp-production-release.apk"
    OUTPUT_NAME="app-kmp-production.apk"
fi

# Verifica se o APK foi realmente gerado antes de copiar
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "./$OUTPUT_NAME"
    echo "✅ Build Android finalizado com sucesso! Artefato: ./$OUTPUT_NAME"
else
    echo "❌ ERRO: O APK não foi encontrado no caminho esperado ($APK_PATH)."
    exit 1
fi