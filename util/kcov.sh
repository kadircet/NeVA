#!/bin/bash
me=$(python -c 'import os; print(os.path.realpath("'"$0"'"))')
WORKSPACE=$(dirname $(dirname "$me"))

if [ ! -f "${WORKSPACE}/WORKSPACE" ]; then
  echo "File not found: ${WORKSPACE}/WORKSPACE"
  exit 1
fi

kcov \
    --include-path=$WORKSPACE \
    --verify \
    --exclude-pattern=external,test \
    $WORKSPACE/bazel-kcov \
    --replace-src-path=/proc/self/cwd:$WORKSPACE \
    --debug=0 \
    "$@"
