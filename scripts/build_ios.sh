#!/bin/bash
set -e

ENV="${APP_ENV:-production}"

echo "🍎 [$ENV] Compilando o framework iOS..."
# O framework KMP geralmente é compilado a partir do módulo shared
./gradlew :shared:linkReleaseFrameworkIosArm64

# Acessa a pasta onde o framework gerado foi colocado
cd shared/build/bin/iosArm64/releaseFramework

if [ "$ENV" = "staging" ]; then
    OUTPUT_ZIP="app-kmp-ios-staging.zip"
else
    OUTPUT_ZIP="app-kmp-ios-production.zip"
fi

# Volta 6 níveis para guardar o ZIP na raiz do projeto
zip -r "../../../../../../$OUTPUT_ZIP" .
cd - > /dev/null

echo "✅ Build iOS finalizado com sucesso! Artefato: ./$OUTPUT_ZIP"