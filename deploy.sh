#!/bin/bash
bazel build -c opt :neva_backend
cp -f bazel-bin/neva_backend /var/NeVA/

sudo systemctl restart neva

exit 0

