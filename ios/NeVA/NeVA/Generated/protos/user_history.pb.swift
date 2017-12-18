// DO NOT EDIT.
//
// Generated by the Swift generator plugin for the protocol buffer compiler.
// Source: protos/user_history.proto
//
// For information on using the generated types, please see the documenation:
//   https://github.com/apple/swift-protobuf/

import Foundation
import SwiftProtobuf

// If the compiler emits an error on this type, it is because this file
// was generated by a version of the `protoc` Swift plug-in that is
// incompatible with the version of SwiftProtobuf to which you are linking.
// Please ensure that your are building against the same version of the API
// that was used to generate this file.
fileprivate struct _GeneratedWithProtocGenSwiftVersion: SwiftProtobuf.ProtobufAPIVersionCheck {
  struct _2: SwiftProtobuf.ProtobufAPIVersion_2 {}
  typealias Version = _2
}

struct Neva_Backend_Choice: SwiftProtobuf.Message {
  static let protoMessageName: String = _protobuf_package + ".Choice"

  /// Which item was selected.
  var suggesteeID: UInt32 {
    get {return _storage._suggesteeID}
    set {_uniqueStorage()._suggesteeID = newValue}
  }

  /// When was the selection performed.
  var timestamp: Neva_Backend_Util_Timestamp {
    get {return _storage._timestamp ?? Neva_Backend_Util_Timestamp()}
    set {_uniqueStorage()._timestamp = newValue}
  }
  /// Returns true if `timestamp` has been explicitly set.
  var hasTimestamp: Bool {return _storage._timestamp != nil}
  /// Clears the value of `timestamp`. Subsequent reads from it will return its default value.
  mutating func clearTimestamp() {_storage._timestamp = nil}

  /// Where was the selection performed.
  var latitude: Double {
    get {return _storage._latitude}
    set {_uniqueStorage()._latitude = newValue}
  }

  var longitude: Double {
    get {return _storage._longitude}
    set {_uniqueStorage()._longitude = newValue}
  }

  /// What is the id of the selection in user_choice_history table.
  var choiceID: UInt32 {
    get {return _storage._choiceID}
    set {_uniqueStorage()._choiceID = newValue}
  }

  var unknownFields = SwiftProtobuf.UnknownStorage()

  init() {}

  /// Used by the decoding initializers in the SwiftProtobuf library, not generally
  /// used directly. `init(serializedData:)`, `init(jsonUTF8Data:)`, and other decoding
  /// initializers are defined in the SwiftProtobuf library. See the Message and
  /// Message+*Additions` files.
  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    _ = _uniqueStorage()
    try withExtendedLifetime(_storage) { (_storage: _StorageClass) in
      while let fieldNumber = try decoder.nextFieldNumber() {
        switch fieldNumber {
        case 1: try decoder.decodeSingularUInt32Field(value: &_storage._suggesteeID)
        case 2: try decoder.decodeSingularMessageField(value: &_storage._timestamp)
        case 3: try decoder.decodeSingularDoubleField(value: &_storage._latitude)
        case 4: try decoder.decodeSingularDoubleField(value: &_storage._longitude)
        case 5: try decoder.decodeSingularUInt32Field(value: &_storage._choiceID)
        default: break
        }
      }
    }
  }

  /// Used by the encoding methods of the SwiftProtobuf library, not generally
  /// used directly. `Message.serializedData()`, `Message.jsonUTF8Data()`, and
  /// other serializer methods are defined in the SwiftProtobuf library. See the
  /// `Message` and `Message+*Additions` files.
  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    try withExtendedLifetime(_storage) { (_storage: _StorageClass) in
      if _storage._suggesteeID != 0 {
        try visitor.visitSingularUInt32Field(value: _storage._suggesteeID, fieldNumber: 1)
      }
      if let v = _storage._timestamp {
        try visitor.visitSingularMessageField(value: v, fieldNumber: 2)
      }
      if _storage._latitude != 0 {
        try visitor.visitSingularDoubleField(value: _storage._latitude, fieldNumber: 3)
      }
      if _storage._longitude != 0 {
        try visitor.visitSingularDoubleField(value: _storage._longitude, fieldNumber: 4)
      }
      if _storage._choiceID != 0 {
        try visitor.visitSingularUInt32Field(value: _storage._choiceID, fieldNumber: 5)
      }
    }
    try unknownFields.traverse(visitor: &visitor)
  }

  fileprivate var _storage = _StorageClass.defaultInstance
}

struct Neva_Backend_UserHistory: SwiftProtobuf.Message {
  static let protoMessageName: String = _protobuf_package + ".UserHistory"

  /// Id of the user to which history entries belongs to.
  var userID: UInt32 = 0

  /// Entries related to user.
  var history: [Neva_Backend_Choice] = []

  var unknownFields = SwiftProtobuf.UnknownStorage()

  init() {}

  /// Used by the decoding initializers in the SwiftProtobuf library, not generally
  /// used directly. `init(serializedData:)`, `init(jsonUTF8Data:)`, and other decoding
  /// initializers are defined in the SwiftProtobuf library. See the Message and
  /// Message+*Additions` files.
  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    while let fieldNumber = try decoder.nextFieldNumber() {
      switch fieldNumber {
      case 1: try decoder.decodeSingularUInt32Field(value: &self.userID)
      case 2: try decoder.decodeRepeatedMessageField(value: &self.history)
      default: break
      }
    }
  }

  /// Used by the encoding methods of the SwiftProtobuf library, not generally
  /// used directly. `Message.serializedData()`, `Message.jsonUTF8Data()`, and
  /// other serializer methods are defined in the SwiftProtobuf library. See the
  /// `Message` and `Message+*Additions` files.
  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    if self.userID != 0 {
      try visitor.visitSingularUInt32Field(value: self.userID, fieldNumber: 1)
    }
    if !self.history.isEmpty {
      try visitor.visitRepeatedMessageField(value: self.history, fieldNumber: 2)
    }
    try unknownFields.traverse(visitor: &visitor)
  }
}

// MARK: - Code below here is support for the SwiftProtobuf runtime.

fileprivate let _protobuf_package = "neva.backend"

extension Neva_Backend_Choice: SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    1: .standard(proto: "suggestee_id"),
    2: .same(proto: "timestamp"),
    3: .same(proto: "latitude"),
    4: .same(proto: "longitude"),
    5: .standard(proto: "choice_id"),
  ]

  fileprivate class _StorageClass {
    var _suggesteeID: UInt32 = 0
    var _timestamp: Neva_Backend_Util_Timestamp? = nil
    var _latitude: Double = 0
    var _longitude: Double = 0
    var _choiceID: UInt32 = 0

    static let defaultInstance = _StorageClass()

    private init() {}

    init(copying source: _StorageClass) {
      _suggesteeID = source._suggesteeID
      _timestamp = source._timestamp
      _latitude = source._latitude
      _longitude = source._longitude
      _choiceID = source._choiceID
    }
  }

  fileprivate mutating func _uniqueStorage() -> _StorageClass {
    if !isKnownUniquelyReferenced(&_storage) {
      _storage = _StorageClass(copying: _storage)
    }
    return _storage
  }

  func _protobuf_generated_isEqualTo(other: Neva_Backend_Choice) -> Bool {
    if _storage !== other._storage {
      let storagesAreEqual: Bool = withExtendedLifetime((_storage, other._storage)) { (_args: (_StorageClass, _StorageClass)) in
        let _storage = _args.0
        let other_storage = _args.1
        if _storage._suggesteeID != other_storage._suggesteeID {return false}
        if _storage._timestamp != other_storage._timestamp {return false}
        if _storage._latitude != other_storage._latitude {return false}
        if _storage._longitude != other_storage._longitude {return false}
        if _storage._choiceID != other_storage._choiceID {return false}
        return true
      }
      if !storagesAreEqual {return false}
    }
    if unknownFields != other.unknownFields {return false}
    return true
  }
}

extension Neva_Backend_UserHistory: SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    1: .standard(proto: "user_id"),
    2: .same(proto: "history"),
  ]

  func _protobuf_generated_isEqualTo(other: Neva_Backend_UserHistory) -> Bool {
    if self.userID != other.userID {return false}
    if self.history != other.history {return false}
    if unknownFields != other.unknownFields {return false}
    return true
  }
}
