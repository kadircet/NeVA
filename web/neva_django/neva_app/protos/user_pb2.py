# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: protos/user.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from . import util_pb2 as protos_dot_util__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='protos/user.proto',
  package='neva.backend',
  syntax='proto3',
  serialized_pb=_b('\n\x11protos/user.proto\x12\x0cneva.backend\x1a\x11protos/util.proto\"\xa6\x01\n\rLinkedAccount\x12\r\n\x05token\x18\x01 \x01(\t\x12\x46\n\x11social_media_type\x18\x02 \x01(\x0e\x32+.neva.backend.LinkedAccount.SocialMediaType\">\n\x0fSocialMediaType\x12\x1d\n\x19INVALID_SOCIAL_MEDIA_TYPE\x10\x00\x12\x0c\n\x08\x46\x41\x43\x45\x42OOK\x10\x01\"\xc7\x03\n\x04User\x12\x0f\n\x07user_id\x18\x01 \x01(\r\x12\r\n\x05\x65mail\x18\x02 \x01(\t\x12\x10\n\x08password\x18\x03 \x01(\t\x12\x0c\n\x04name\x18\x04 \x01(\t\x12\x33\n\rdate_of_birth\x18\x05 \x01(\x0b\x32\x1c.neva.backend.util.Timestamp\x12)\n\x06gender\x18\x06 \x01(\x0e\x32\x19.neva.backend.User.Gender\x12\x0e\n\x06weight\x18\x07 \x01(\x02\x12\r\n\x05photo\x18\x08 \x01(\t\x12)\n\x06status\x18\t \x01(\x0e\x32\x19.neva.backend.User.Status\x12\x33\n\rregister_date\x18\n \x01(\x0b\x32\x1c.neva.backend.util.Timestamp\x12\x34\n\x0flinked_accounts\x18\x0b \x03(\x0b\x32\x1b.neva.backend.LinkedAccount\"2\n\x06Gender\x12\x12\n\x0eINVALID_GENDER\x10\x00\x12\x08\n\x04MALE\x10\x01\x12\n\n\x06\x46\x45MALE\x10\x02\"6\n\x06Status\x12\x12\n\x0eINVALID_STATUS\x10\x00\x12\x0c\n\x08INACTIVE\x10\x01\x12\n\n\x06\x41\x43TIVE\x10\x02\x62\x06proto3')
  ,
  dependencies=[protos_dot_util__pb2.DESCRIPTOR,])



_LINKEDACCOUNT_SOCIALMEDIATYPE = _descriptor.EnumDescriptor(
  name='SocialMediaType',
  full_name='neva.backend.LinkedAccount.SocialMediaType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='INVALID_SOCIAL_MEDIA_TYPE', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='FACEBOOK', index=1, number=1,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=159,
  serialized_end=221,
)
_sym_db.RegisterEnumDescriptor(_LINKEDACCOUNT_SOCIALMEDIATYPE)

_USER_GENDER = _descriptor.EnumDescriptor(
  name='Gender',
  full_name='neva.backend.User.Gender',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='INVALID_GENDER', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='MALE', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='FEMALE', index=2, number=2,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=573,
  serialized_end=623,
)
_sym_db.RegisterEnumDescriptor(_USER_GENDER)

_USER_STATUS = _descriptor.EnumDescriptor(
  name='Status',
  full_name='neva.backend.User.Status',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='INVALID_STATUS', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='INACTIVE', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='ACTIVE', index=2, number=2,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=625,
  serialized_end=679,
)
_sym_db.RegisterEnumDescriptor(_USER_STATUS)


_LINKEDACCOUNT = _descriptor.Descriptor(
  name='LinkedAccount',
  full_name='neva.backend.LinkedAccount',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='token', full_name='neva.backend.LinkedAccount.token', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='social_media_type', full_name='neva.backend.LinkedAccount.social_media_type', index=1,
      number=2, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _LINKEDACCOUNT_SOCIALMEDIATYPE,
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=55,
  serialized_end=221,
)


_USER = _descriptor.Descriptor(
  name='User',
  full_name='neva.backend.User',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='user_id', full_name='neva.backend.User.user_id', index=0,
      number=1, type=13, cpp_type=3, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='email', full_name='neva.backend.User.email', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='password', full_name='neva.backend.User.password', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='name', full_name='neva.backend.User.name', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='date_of_birth', full_name='neva.backend.User.date_of_birth', index=4,
      number=5, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='gender', full_name='neva.backend.User.gender', index=5,
      number=6, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='weight', full_name='neva.backend.User.weight', index=6,
      number=7, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='photo', full_name='neva.backend.User.photo', index=7,
      number=8, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='status', full_name='neva.backend.User.status', index=8,
      number=9, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='register_date', full_name='neva.backend.User.register_date', index=9,
      number=10, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='linked_accounts', full_name='neva.backend.User.linked_accounts', index=10,
      number=11, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _USER_GENDER,
    _USER_STATUS,
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=224,
  serialized_end=679,
)

_LINKEDACCOUNT.fields_by_name['social_media_type'].enum_type = _LINKEDACCOUNT_SOCIALMEDIATYPE
_LINKEDACCOUNT_SOCIALMEDIATYPE.containing_type = _LINKEDACCOUNT
_USER.fields_by_name['date_of_birth'].message_type = protos_dot_util__pb2._TIMESTAMP
_USER.fields_by_name['gender'].enum_type = _USER_GENDER
_USER.fields_by_name['status'].enum_type = _USER_STATUS
_USER.fields_by_name['register_date'].message_type = protos_dot_util__pb2._TIMESTAMP
_USER.fields_by_name['linked_accounts'].message_type = _LINKEDACCOUNT
_USER_GENDER.containing_type = _USER
_USER_STATUS.containing_type = _USER
DESCRIPTOR.message_types_by_name['LinkedAccount'] = _LINKEDACCOUNT
DESCRIPTOR.message_types_by_name['User'] = _USER
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

LinkedAccount = _reflection.GeneratedProtocolMessageType('LinkedAccount', (_message.Message,), dict(
  DESCRIPTOR = _LINKEDACCOUNT,
  __module__ = 'protos.user_pb2'
  # @@protoc_insertion_point(class_scope:neva.backend.LinkedAccount)
  ))
_sym_db.RegisterMessage(LinkedAccount)

User = _reflection.GeneratedProtocolMessageType('User', (_message.Message,), dict(
  DESCRIPTOR = _USER,
  __module__ = 'protos.user_pb2'
  # @@protoc_insertion_point(class_scope:neva.backend.User)
  ))
_sym_db.RegisterMessage(User)


# @@protoc_insertion_point(module_scope)