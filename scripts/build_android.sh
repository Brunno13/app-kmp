#!/bin/bash
set -e

# Se a variável APP_ENV não existir, assume "production" como padrão de segurança
ENV="${APP_ENV:-production}"

if [ "$ENV" = "staging" ]; then
    echo "🤖 [Staging] Compilando o APK Android..."
    ./gradlew :androidApp:assembleStagingRelease

    APK_PATH="androidApp/build/outputs/apk/staging/release/androidApp-staging-release.apk"
    OUTPUT_NAME="app-kmp-staging.apk"
else
    echo "🤖 [Production] Compilando o APK Android..."
    ./gradlew :androidApp:assembleProductionRelease

    APK_PATH="androidApp/build/outputs/apk/production/release/androidApp-production-release.apk"
    OUTPUT_NAME="app-kmp-production.apk"
fi

# Verifica se o APK foi realmente gerado antes de copiar
if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "./$OUTPUT_NAME"
    echo "✅ Build Android finalizado com sucesso! Artefato: ./$OUTPUT_NAME"
else
    echo "❌ ERRO: O APK não foi encontrado no caminho esperado ($APK_PATH)."
    echo "Listando o diretório de outputs para depuração:"
    ls -lah androidApp/build/outputs/apk/ || true
    exit 1
fi