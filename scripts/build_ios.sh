#!/usr/bin/env bash
set -eu

# ============================================================
# PROJECT CONFIG
# ============================================================

SCRIPT_DIR="$(
    cd "$(dirname "${BASH_SOURCE[0]}")" &&
    pwd
)"

PROJECT_ROOT="$(
    cd "$SCRIPT_DIR/.." &&
    pwd
)"

CONFIG_FILE="$PROJECT_ROOT/ci/artifacts.env"

if [ ! -r "$CONFIG_FILE" ]; then
    echo "❌ ERRO: arquivo de configuração não encontrado:"
    echo "   $CONFIG_FILE"
    exit 1
fi

set -a
. "$CONFIG_FILE"
set +a

if [ -z "${ARTIFACT_BASENAME:-}" ]; then
    echo "❌ ERRO: ARTIFACT_BASENAME não definido em ci/artifacts.env"
    exit 1
fi

echo "ARTIFACT_CONFIG=PASS"

# ============================================================
# BUILD ENVIRONMENT
# ============================================================

# Mantém production como padrão para compatibilidade com
# release.yml e chamadas antigas.
ENV="${APP_ENV:-production}"

case "$ENV" in
    staging|production)
        ;;
    *)
        echo "❌ ERRO: APP_ENV inválido: $ENV"
        echo "Valores permitidos: staging | production"
        exit 1
        ;;
esac

echo "🍎 [$ENV] Compilando o framework iOS..."

cd "$PROJECT_ROOT"

./gradlew :shared:linkReleaseFrameworkIosArm64

FRAMEWORK_DIR="$PROJECT_ROOT/shared/build/bin/iosArm64/releaseFramework"

if [ ! -d "$FRAMEWORK_DIR" ]; then
    echo "❌ ERRO: framework iOS não encontrado:"
    echo "   $FRAMEWORK_DIR"
    exit 1
fi

OUTPUT_NAME="$ARTIFACT_BASENAME-ios-$ENV.zip"
OUTPUT_PATH="$PROJECT_ROOT/$OUTPUT_NAME"

# ============================================================
# PACKAGE
# ============================================================

rm -f "$OUTPUT_PATH"

cd "$FRAMEWORK_DIR"

zip -r "$OUTPUT_PATH" .

cd "$PROJECT_ROOT"

if [ ! -s "$OUTPUT_PATH" ]; then
    echo "❌ ERRO: artefato ZIP iOS não foi criado corretamente."
    exit 1
fi

echo
echo "✅ Build iOS finalizado com sucesso!"
echo "📦 Artefato disponível na raiz: ./$OUTPUT_NAME"

echo
echo "IOS_BUILD_ENV=$ENV"
echo "IOS_ARTIFACT_NAME=$OUTPUT_NAME"
echo "IOS_ARTIFACT_PATH=./$OUTPUT_NAME"
echo "IOS_BUILD=PASS"
