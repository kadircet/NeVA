#!/bin/bash
bazel build -c opt :neva_backend
cp -f bazel-bin/neva_backend /var/NeVA/
cp -f prod.flags /var/NeVA/prod.flags

sudo cp -f neva /etc/init.d/neva
sudo systemctl daemon-reload
sudo systemctl restart neva

sudo cp -f /tmp/neva_android/outputs/apk/debug/app-debug.apk \
  /var/www/neva/android.apk

exit 0

