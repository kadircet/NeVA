GRPC_CLI=/home/kadircet/repos/grpc/bazel-bin/test/cpp/util/grpc_cli
ARGS="--proto_path=protos/ --enable_ssl call neva.0xdeffbeef.com:50051"
TOKEN=$($GRPC_CLI $ARGS Login 'email:"hkn@test.com" password:"asdfASDF" authentication_type: DEFAULT' 2> /dev/null | grep -i "token")

for i in $(seq 0 100)
do
  echo $TOKEN suggestion_category: MEAL | $GRPC_CLI $ARGS \
    "GetMultipleSuggestions" > /dev/null &
done
