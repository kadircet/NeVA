package backend;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: backend.proto")
public final class BackendGrpc {

  private BackendGrpc() {}

  public static final String SERVICE_NAME = "backend.Backend";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<backend.BackendOuterClass.RegisterRequest,
      backend.BackendOuterClass.RegisterReply> METHOD_REGISTER =
      io.grpc.MethodDescriptor.<backend.BackendOuterClass.RegisterRequest, backend.BackendOuterClass.RegisterReply>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "backend.Backend", "Register"))
          .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
              backend.BackendOuterClass.RegisterRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
              backend.BackendOuterClass.RegisterReply.getDefaultInstance()))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<backend.BackendOuterClass.LoginRequest,
      backend.BackendOuterClass.LoginReply> METHOD_LOGIN =
      io.grpc.MethodDescriptor.<backend.BackendOuterClass.LoginRequest, backend.BackendOuterClass.LoginReply>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "backend.Backend", "Login"))
          .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
              backend.BackendOuterClass.LoginRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
              backend.BackendOuterClass.LoginReply.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BackendStub newStub(io.grpc.Channel channel) {
    return new BackendStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BackendBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BackendBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BackendFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BackendFutureStub(channel);
  }

  /**
   */
  public static abstract class BackendImplBase implements io.grpc.BindableService {

    /**
     */
    public void register(backend.BackendOuterClass.RegisterRequest request,
        io.grpc.stub.StreamObserver<backend.BackendOuterClass.RegisterReply> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REGISTER, responseObserver);
    }

    /**
     */
    public void login(backend.BackendOuterClass.LoginRequest request,
        io.grpc.stub.StreamObserver<backend.BackendOuterClass.LoginReply> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LOGIN, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_REGISTER,
            asyncUnaryCall(
              new MethodHandlers<
                backend.BackendOuterClass.RegisterRequest,
                backend.BackendOuterClass.RegisterReply>(
                  this, METHODID_REGISTER)))
          .addMethod(
            METHOD_LOGIN,
            asyncUnaryCall(
              new MethodHandlers<
                backend.BackendOuterClass.LoginRequest,
                backend.BackendOuterClass.LoginReply>(
                  this, METHODID_LOGIN)))
          .build();
    }
  }

  /**
   */
  public static final class BackendStub extends io.grpc.stub.AbstractStub<BackendStub> {
    private BackendStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BackendStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BackendStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BackendStub(channel, callOptions);
    }

    /**
     */
    public void register(backend.BackendOuterClass.RegisterRequest request,
        io.grpc.stub.StreamObserver<backend.BackendOuterClass.RegisterReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REGISTER, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void login(backend.BackendOuterClass.LoginRequest request,
        io.grpc.stub.StreamObserver<backend.BackendOuterClass.LoginReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LOGIN, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BackendBlockingStub extends io.grpc.stub.AbstractStub<BackendBlockingStub> {
    private BackendBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BackendBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BackendBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BackendBlockingStub(channel, callOptions);
    }

    /**
     */
    public backend.BackendOuterClass.RegisterReply register(backend.BackendOuterClass.RegisterRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REGISTER, getCallOptions(), request);
    }

    /**
     */
    public backend.BackendOuterClass.LoginReply login(backend.BackendOuterClass.LoginRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LOGIN, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BackendFutureStub extends io.grpc.stub.AbstractStub<BackendFutureStub> {
    private BackendFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BackendFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BackendFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BackendFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<backend.BackendOuterClass.RegisterReply> register(
        backend.BackendOuterClass.RegisterRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REGISTER, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<backend.BackendOuterClass.LoginReply> login(
        backend.BackendOuterClass.LoginRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LOGIN, getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER = 0;
  private static final int METHODID_LOGIN = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BackendImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BackendImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER:
          serviceImpl.register((backend.BackendOuterClass.RegisterRequest) request,
              (io.grpc.stub.StreamObserver<backend.BackendOuterClass.RegisterReply>) responseObserver);
          break;
        case METHODID_LOGIN:
          serviceImpl.login((backend.BackendOuterClass.LoginRequest) request,
              (io.grpc.stub.StreamObserver<backend.BackendOuterClass.LoginReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BackendGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(METHOD_REGISTER)
              .addMethod(METHOD_LOGIN)
              .build();
        }
      }
    }
    return result;
  }
}
