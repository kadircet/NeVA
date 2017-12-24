#!/bin/bash
BINARY_NAME="neva_backend_staging"
FLAGFILE_NAME="stag.flags"
APK_NAME="android_staging.apk"
SERVICE_NAME="neva_staging"

if [ "production" == $1 ];
  BINARY_NAME="neva_backend"
  FLAGFILE_NAME="prod.flags"
  APK_NAME="android.apk"
  SERVICE_NAME="neva"
fi

bazel build -c opt :neva_backend
cp -f bazel-bin/neva_backend /var/NeVA/$BINARY_NAME
cp -f flags.file /var/NeVA/$FLAGFILE_NAME

sudo cp -f $SERVICE_NAME /etc/init.d/$SERVICE_NAME
sudo systemctl daemon-reload
sudo systemctl restart $SERVICE_NAME

sudo cp -f /tmp/neva_android/outputs/apk/debug/app-debug.apk \
  /var/www/neva/$APK_NAME

exit 0

