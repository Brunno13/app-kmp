#!/bin/bash
set -e

# Se a variável APP_ENV não existir, usamos staging como default para testes
ENV="${APP_ENV:-staging}"

if [ "$ENV" = "staging" ]; then
    echo "🧪 [Staging] Rodando testes unitários..."
    ./gradlew :androidApp:testStagingDebugUnitTest
else
    echo "🧪 [Production] Rodando testes unitários..."
    ./gradlew :androidApp:testProductionDebugUnitTest
fi

echo "✅ Testes concluídos com sucesso."