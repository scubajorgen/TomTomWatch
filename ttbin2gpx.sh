#!/usr/bin/env bash
# Batch convert TomTom .ttbin files using TomTomWatch.
# GPS activities -> .gpx ; Gym/Indoor (HR+time, no GPS) -> .tcx
#
# Usage:
#   ./ttbin2gpx.sh [options] <inputDir> [outputDir]
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="${TOMTOMWATCH_JAR:-$SCRIPT_DIR/target/TomTomWatch.jar}"
PROPS_DEFAULT="$SCRIPT_DIR/target/tomtomwatch.properties"

if [[ ! -f "$JAR" ]]; then
  echo "TomTomWatch.jar not found: $JAR" >&2
  echo "Build first: mvn -Dmaven.test.skip package" >&2
  exit 1
fi

EXTRA_ARGS=()
if [[ -f "$PROPS_DEFAULT" ]]; then
  EXTRA_ARGS+=(--properties "$PROPS_DEFAULT")
fi

exec java -cp "$JAR" net.studioblueplanet.tomtomwatch.TtbinToGpxBatch \
  "${EXTRA_ARGS[@]}" \
  "$@"
