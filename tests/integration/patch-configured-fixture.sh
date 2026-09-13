#!/usr/bin/env bash
set -euo pipefail

: "${SCREENTIME_FIXTURE_APK_URL:?Configure an immutable fixture APK URL.}"
: "${SCREENTIME_PATCH_COMMAND:?Configure the exact patch command.}"
mkdir -p tests/integration/evidence
curl --fail --location --output tests/integration/evidence/fixture.apk "$SCREENTIME_FIXTURE_APK_URL"
sha256sum tests/integration/evidence/fixture.apk | tee tests/integration/evidence/fixture.apk.sha256
eval "$SCREENTIME_PATCH_COMMAND"
