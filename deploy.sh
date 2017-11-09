#!/bin/bash
PIDFILE="/var/run/neva_backend.pid"
LOGFILE="/var/log/neva_backend.log"

bazel build -c opt :neva_backend
cp -f bazel-bin/neva_backend /var/NeVA/

if [ -x $PIDFILE ];
then
  pid=$(cat $PIDFILE)
  kill -0 $pid && kill -9 $pid
fi
/var/NeVA/neva_backend >> $LOGFILE &
echo -n $! > $PIDFILE
