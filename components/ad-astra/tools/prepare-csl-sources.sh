#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../deps/common-storage-lib" && pwd)"
OUT="$ROOT/build/fabric-src"
STITCH="$ROOT/build/stitched"
rm -rf "$OUT" "$STITCH"
mkdir -p "$OUT"
python3 "$(dirname "$0")/stitch_expect.py" \
  "$STITCH" \
  "$ROOT/core/common/src/main/java" \
  "$ROOT/data/common/src/main/java" \
  "$ROOT/lookup/common/src/main/java" \
  "$ROOT/resources/common/src/main/java" \
  -- \
  "$ROOT/core/fabric/src/main/java" \
  "$ROOT/data/fabric/src/main/java" \
  "$ROOT/lookup/fabric/src/main/java" \
  "$ROOT/resources/fabric/src/main/java"
for src in \
  "$ROOT/core/common/src/main/java" \
  "$ROOT/data/common/src/main/java" \
  "$ROOT/lookup/common/src/main/java" \
  "$ROOT/resources/common/src/main/java" \
  "$ROOT/core/fabric/src/main/java" \
  "$ROOT/data/fabric/src/main/java" \
  "$ROOT/lookup/fabric/src/main/java" \
  "$ROOT/resources/fabric/src/main/java"
do
  while IFS= read -r -d '' file; do
    rel="${file#"$src"/}"
    case "$rel" in
      *Actual.java) continue ;;
    esac
    dest="$OUT/$rel"
    if [[ -f "$STITCH/$rel" ]]; then
      mkdir -p "$(dirname "$dest")"
      cp "$STITCH/$rel" "$dest"
    elif [[ ! -f "$dest" ]]; then
      mkdir -p "$(dirname "$dest")"
      cp "$file" "$dest"
    fi
  done < <(find "$src" -name '*.java' -print0)
done
echo "prepared $(find "$OUT" -name '*.java' | wc -l) java files"
