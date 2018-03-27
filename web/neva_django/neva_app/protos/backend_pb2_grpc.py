# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
import grpc

from . import backend_pb2 as protos_dot_backend__pb2


class BackendStub(object):
  # missing associated documentation comment in .proto file
  pass

  def __init__(self, channel):
    """Constructor.

    Args:
      channel: A grpc.Channel.
    """
    self.Register = channel.unary_unary(
        '/neva.backend.Backend/Register',
        request_serializer=protos_dot_backend__pb2.RegisterRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.Login = channel.unary_unary(
        '/neva.backend.Backend/Login',
        request_serializer=protos_dot_backend__pb2.LoginRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.LoginReply.FromString,
        )
    self.UpdateUser = channel.unary_unary(
        '/neva.backend.Backend/UpdateUser',
        request_serializer=protos_dot_backend__pb2.UpdateUserRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.GetUser = channel.unary_unary(
        '/neva.backend.Backend/GetUser',
        request_serializer=protos_dot_backend__pb2.GetUserRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GetUserReply.FromString,
        )
    self.SuggestionItemProposition = channel.unary_unary(
        '/neva.backend.Backend/SuggestionItemProposition',
        request_serializer=protos_dot_backend__pb2.SuggestionItemPropositionRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.GetMultipleSuggestions = channel.unary_unary(
        '/neva.backend.Backend/GetMultipleSuggestions',
        request_serializer=protos_dot_backend__pb2.GetMultipleSuggestionsRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GetMultipleSuggestionsReply.FromString,
        )
    self.TagProposition = channel.unary_unary(
        '/neva.backend.Backend/TagProposition',
        request_serializer=protos_dot_backend__pb2.TagPropositionRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.TagValueProposition = channel.unary_unary(
        '/neva.backend.Backend/TagValueProposition',
        request_serializer=protos_dot_backend__pb2.TagValuePropositionRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.GetSuggestionItemList = channel.unary_unary(
        '/neva.backend.Backend/GetSuggestionItemList',
        request_serializer=protos_dot_backend__pb2.GetSuggestionItemListRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GetSuggestionItemListReply.FromString,
        )
    self.InformUserChoice = channel.unary_unary(
        '/neva.backend.Backend/InformUserChoice',
        request_serializer=protos_dot_backend__pb2.InformUserChoiceRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.InformUserChoiceReply.FromString,
        )
    self.FetchUserHistory = channel.unary_unary(
        '/neva.backend.Backend/FetchUserHistory',
        request_serializer=protos_dot_backend__pb2.FetchUserHistoryRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.FetchUserHistoryReply.FromString,
        )
    self.CheckToken = channel.unary_unary(
        '/neva.backend.Backend/CheckToken',
        request_serializer=protos_dot_backend__pb2.CheckTokenRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.RecordFeedback = channel.unary_unary(
        '/neva.backend.Backend/RecordFeedback',
        request_serializer=protos_dot_backend__pb2.RecordFeedbackRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GenericReply.FromString,
        )
    self.GetTags = channel.unary_unary(
        '/neva.backend.Backend/GetTags',
        request_serializer=protos_dot_backend__pb2.GetTagsRequest.SerializeToString,
        response_deserializer=protos_dot_backend__pb2.GetTagsReply.FromString,
        )


class BackendServicer(object):
  # missing associated documentation comment in .proto file
  pass

  def Register(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def Login(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def UpdateUser(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetUser(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def SuggestionItemProposition(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetMultipleSuggestions(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def TagProposition(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def TagValueProposition(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetSuggestionItemList(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def InformUserChoice(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def FetchUserHistory(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def CheckToken(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def RecordFeedback(self, request, context):
    # missing associated documentation comment in .proto file
    pass
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def GetTags(self, request, context):
    """Returns all tags in the database.
    """
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')


def add_BackendServicer_to_server(servicer, server):
  rpc_method_handlers = {
      'Register': grpc.unary_unary_rpc_method_handler(
          servicer.Register,
          request_deserializer=protos_dot_backend__pb2.RegisterRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'Login': grpc.unary_unary_rpc_method_handler(
          servicer.Login,
          request_deserializer=protos_dot_backend__pb2.LoginRequest.FromString,
          response_serializer=protos_dot_backend__pb2.LoginReply.SerializeToString,
      ),
      'UpdateUser': grpc.unary_unary_rpc_method_handler(
          servicer.UpdateUser,
          request_deserializer=protos_dot_backend__pb2.UpdateUserRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'GetUser': grpc.unary_unary_rpc_method_handler(
          servicer.GetUser,
          request_deserializer=protos_dot_backend__pb2.GetUserRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GetUserReply.SerializeToString,
      ),
      'SuggestionItemProposition': grpc.unary_unary_rpc_method_handler(
          servicer.SuggestionItemProposition,
          request_deserializer=protos_dot_backend__pb2.SuggestionItemPropositionRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'GetMultipleSuggestions': grpc.unary_unary_rpc_method_handler(
          servicer.GetMultipleSuggestions,
          request_deserializer=protos_dot_backend__pb2.GetMultipleSuggestionsRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GetMultipleSuggestionsReply.SerializeToString,
      ),
      'TagProposition': grpc.unary_unary_rpc_method_handler(
          servicer.TagProposition,
          request_deserializer=protos_dot_backend__pb2.TagPropositionRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'TagValueProposition': grpc.unary_unary_rpc_method_handler(
          servicer.TagValueProposition,
          request_deserializer=protos_dot_backend__pb2.TagValuePropositionRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'GetSuggestionItemList': grpc.unary_unary_rpc_method_handler(
          servicer.GetSuggestionItemList,
          request_deserializer=protos_dot_backend__pb2.GetSuggestionItemListRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GetSuggestionItemListReply.SerializeToString,
      ),
      'InformUserChoice': grpc.unary_unary_rpc_method_handler(
          servicer.InformUserChoice,
          request_deserializer=protos_dot_backend__pb2.InformUserChoiceRequest.FromString,
          response_serializer=protos_dot_backend__pb2.InformUserChoiceReply.SerializeToString,
      ),
      'FetchUserHistory': grpc.unary_unary_rpc_method_handler(
          servicer.FetchUserHistory,
          request_deserializer=protos_dot_backend__pb2.FetchUserHistoryRequest.FromString,
          response_serializer=protos_dot_backend__pb2.FetchUserHistoryReply.SerializeToString,
      ),
      'CheckToken': grpc.unary_unary_rpc_method_handler(
          servicer.CheckToken,
          request_deserializer=protos_dot_backend__pb2.CheckTokenRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'RecordFeedback': grpc.unary_unary_rpc_method_handler(
          servicer.RecordFeedback,
          request_deserializer=protos_dot_backend__pb2.RecordFeedbackRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GenericReply.SerializeToString,
      ),
      'GetTags': grpc.unary_unary_rpc_method_handler(
          servicer.GetTags,
          request_deserializer=protos_dot_backend__pb2.GetTagsRequest.FromString,
          response_serializer=protos_dot_backend__pb2.GetTagsReply.SerializeToString,
      ),
  }
  generic_handler = grpc.method_handlers_generic_handler(
      'neva.backend.Backend', rpc_method_handlers)
  server.add_generic_rpc_handlers((generic_handler,))