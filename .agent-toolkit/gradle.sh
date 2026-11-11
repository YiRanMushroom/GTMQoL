#!/usr/bin/env bash
# Gradle on one or both worktrees, quiet: only errors, new warnings and the lines that tell how a data run went.
#   .agent-toolkit/gradle.sh <21|20|both> <tasks...>      e.g. both compileJava | 21 runData test | both build
# Runs the branches one after the other (two data runs at once fail now and then). Never clean (rules.working).
# JAVA_HOME is the Gradle JVM, JDK 21 on both branches (build.environment).

set -u
here="$(cd "$(dirname "$0")/.." && pwd)"
which="${1:?21, 20 or both}"
shift
[ $# -gt 0 ] || { echo "no tasks"; exit 2; }
export JAVA_HOME="${GTMQOL_JAVA_HOME:-$HOME/.jdks/openjdk-21.0.1}"

status=0
run() {
  local name="$1" dir="$2" log
  shift 2
  log="$(mktemp)"
  (cd "$dir" && ./gradlew "$@" --console=plain -q) > "$log" 2>&1
  local code=$?
  # the deprecation of BuiltInRegistries is the one accepted warning (1.20 only)
  sed '/BuiltInRegistries has been deprecated/,+2d' "$log" |
    grep -n -A6 "error:\|warning: \[\|FAILED\|Exception in thread\|Caused by:\|What went wrong" | head -60
  grep -h "Mixin targets loaded\|Caching: total files" "$log" | sed 's/^.*\] *//'
  echo "$name: exit $code ($*)   log: $log"
  [ $code -eq 0 ] || status=$code
}

case "$which" in
  21) run 1.21 "$here" "$@" ;;
  20) run 1.20 "$here/../GTMQoL-1.20.1" "$@" ;;
  both) run 1.21 "$here" "$@"; run 1.20 "$here/../GTMQoL-1.20.1" "$@" ;;
  *) echo "first argument: 21, 20 or both"; exit 2 ;;
esac
exit $status
