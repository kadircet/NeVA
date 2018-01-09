GRPC_CLI=/home/kadircet/repos/grpc/bazel-bin/test/cpp/util/grpc_cli
ARGS="--proto_path=protos/ --enable_ssl call neva.0xdeffbeef.com:50053"
TOKEN=$($GRPC_CLI $ARGS Login 'email:"cbogrqqktr_1511716461@tfbnw.net" password:"EAACZAu8B33nYBAHyARqh8V6tQEF84eRMGC5UNwXEJM79aicwfFcnZCfSZBugZAZBxZCcGZCpHEZBQIlCJinFfzoSjUpY8EvZAtWcHwpQBxftzUDxQO2VKehD5mV83lNwSiAKr0uYEBGYkiZC7cJvFql0K2gGdTlh8atpUTzeUotEhbjVgyYeYCwpWZA" authentication_type: FACEBOOK' 2> /dev/null | grep -i "token")

for i in $(seq 0 100)
do
  echo $TOKEN suggestion_category: MEAL | $GRPC_CLI $ARGS \
    "GetMultipleSuggestions" > /dev/null &
done
