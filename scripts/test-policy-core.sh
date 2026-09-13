#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
classes="$(mktemp -d)"
trap 'rm -rf "$classes"' EXIT

mapfile -t sources < <(find "$root/extensions/extension/src/main/java/dev/screentime/runtime/core" "$root/extensions/extension/src/test/java/dev/screentime/runtime/core" -name '*.java' -print)
javac -d "$classes" "${sources[@]}"
java -cp "$classes" dev.screentime.runtime.core.PolicyEngineTest
