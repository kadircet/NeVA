GRPC_CLI=/home/kadircet/repos/grpc/bazel-bin/test/cpp/util/grpc_cli
ARGS="--proto_path=protos/ --enable_ssl call neva.0xdeffbeef.com:50051"

LOGIN_EMAIL='email: "hkn@test.com"'
LOGIN_PASSWORD='password: "asdfASDF"'
AUTH_TYPE="authentication_type: DEFAULT"

LOGIN_EMAIL='email: "10156039002563534"'
LOGIN_PASSWORD='password: "EAACZAu8B33nYBAL5YYjoDWqRCXoCpbeyEayoEU6xiUqfX289YqMkdns9p73ZAsoSFouS2jOZAoSGBrXNi2dqJddYaZAN9ymiYEM2bEZCZCB7pZAJyvOIYN9X1ZACKul8zNhrYZAaPSfkdZBBAE8jGMRZBaakNVUxttGg6sZD"'
AUTH_TYPE="authentication_type: FACEBOOK"

TOKEN=$(echo $LOGIN_EMAIL $LOGIN_PASSWORD $AUTH_TYPE | $GRPC_CLI $ARGS "Login" \
  2> /dev/null | grep -i "token")

for i in $(seq 0 100)
do
  echo $TOKEN suggestion_category: MEAL | $GRPC_CLI $ARGS \
    "GetMultipleSuggestions" > /dev/null &
done
