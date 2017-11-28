rm -rf ./NeVA/NeVA/Generated/*
(cd .. && protoc protos/*.proto --swift_out=ios/NeVA/NeVA/Generated/ --swiftgrpc_out=ios/NeVA/NeVA/Generated/)
