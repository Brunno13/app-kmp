#!/bin/bash
set -e

# Se a variável APP_ENV não existir, assume "production" como padrão de segurança
ENV="${APP_ENV:-production}"

if [ "$ENV" = "staging" ]; then
    echo "🤖 [Staging] Compilando o APK Android..."
    ./gradlew :androidApp:assembleStagingRelease

    APK_DIR="androidApp/build/outputs/apk/staging/release"
    OUTPUT_NAME="app-kmp-staging.apk"
else
    echo "🤖 [Production] Compilando o APK Android..."
    ./gradlew :androidApp:assembleProductionRelease

    APK_DIR="androidApp/build/outputs/apk/production/release"
    OUTPUT_NAME="app-kmp-production.apk"
fi

# Procura o primeiro ficheiro .apk dentro do diretório gerado,
# ignorando se tem o sufixo "-unsigned" ou não.
APK_FILE=$(ls $APK_DIR/*.apk 2>/dev/null | head -n 1 || true)

# Verifica se o ficheiro foi encontrado e se realmente existe
if [ -n "$APK_FILE" ] && [ -f "$APK_FILE" ]; then
    # Move o APK para a raiz com o nome esperado pela pipeline
    cp "$APK_FILE" "./$OUTPUT_NAME"
    echo "✅ Build Android finalizado com sucesso!"
    echo "📦 Artefato disponível na raiz: ./$OUTPUT_NAME"
else
    echo "❌ ERRO: Nenhum ficheiro APK encontrado na pasta ($APK_DIR)."
    echo "Listando recursivamente a árvore de outputs para depuração:"
    find androidApp/build/outputs/apk/ -type f || true
    exit 1
fi