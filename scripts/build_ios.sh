#!/bin/bash
set -e

ENV="${APP_ENV:-production}"
echo "🍎 [$ENV] Compilando o framework iOS..."

./gradlew :shared:linkReleaseFrameworkIosArm64

if [ "$ENV" = "staging" ]; then
    OUTPUT_ZIP="app-kmp-ios-staging.zip"
else
    OUTPUT_ZIP="app-kmp-ios-production.zip"
fi

# Guarda o caminho absoluto da raiz do projeto para evitar erros de navegação
ROOT_DIR=$(pwd)

cd shared/build/bin/iosArm64/releaseFramework
zip -r "$ROOT_DIR/$OUTPUT_ZIP" .
cd - > /dev/null

echo "✅ Build iOS finalizado com sucesso! Artefato: ./$OUTPUT_ZIP"