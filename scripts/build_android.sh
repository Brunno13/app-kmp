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
# release.yml e execução manual sem APP_ENV.
ENV="${APP_ENV:-production}"

case "$ENV" in
    staging)
        echo "🤖 [Staging] Compilando o APK Android..."

        ./gradlew :androidApp:assembleStagingRelease

        APK_DIR="androidApp/build/outputs/apk/staging/release"
        ;;

    production)
        echo "🤖 [Production] Compilando o APK Android..."

        ./gradlew :androidApp:assembleProductionRelease

        APK_DIR="androidApp/build/outputs/apk/production/release"
        ;;

    *)
        echo "❌ ERRO: APP_ENV inválido: $ENV"
        echo "Valores permitidos: staging | production"
        exit 1
        ;;
esac

OUTPUT_NAME="$ARTIFACT_BASENAME-$ENV.apk"
OUTPUT_PATH="$PROJECT_ROOT/$OUTPUT_NAME"

# ============================================================
# FIND APK
# ============================================================

APK_FILE="$(
    find "$APK_DIR" \
        -maxdepth 1 \
        -type f \
        -name '*.apk' \
        -print \
        2>/dev/null \
        | head -n 1 \
        || true
)"

if [ -z "$APK_FILE" ] || [ ! -f "$APK_FILE" ]; then
    echo "❌ ERRO: Nenhum APK encontrado em:"
    echo "   $APK_DIR"

    echo
    echo "===== ANDROID APK OUTPUTS ====="

    find androidApp/build/outputs/apk \
        -type f \
        -print \
        2>/dev/null \
        || true

    exit 1
fi

# ============================================================
# NORMALIZE ARTIFACT NAME
# ============================================================

cp "$APK_FILE" "$OUTPUT_PATH"

if [ ! -s "$OUTPUT_PATH" ]; then
    echo "❌ ERRO: artefato final não foi criado corretamente."
    exit 1
fi

echo
echo "✅ Build Android finalizado com sucesso!"
echo "📦 Artefato disponível na raiz: ./$OUTPUT_NAME"

echo
echo "ANDROID_BUILD_ENV=$ENV"
echo "ANDROID_ARTIFACT_NAME=$OUTPUT_NAME"
echo "ANDROID_ARTIFACT_PATH=./$OUTPUT_NAME"
echo "ANDROID_BUILD=PASS"