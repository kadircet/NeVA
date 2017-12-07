#!/bin/bash
bazel build -c opt :neva_backend
cp -f bazel-bin/neva_backend /var/NeVA/
cp -f prod.flags /var/NeVA/prod.flags

sudo systemctl restart neva

exit 0

