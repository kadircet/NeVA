package mealrecommender.neva.com.neva_android_app;

import com.google.protobuf.ByteString;

import org.junit.Test;

import java.nio.charset.Charset;

import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.UserOuterClass;
import neva.backend.util.Util;

import static org.junit.Assert.assertEquals;

/**
 * Created by hakan on 11/14/17.
 */

public class LoginRequestTest {

    @Test
    public void login_test(){
        try {
            //ManagedChannel mChannel = ManagedChannelBuilder.forAddress("0xdeffbeef.com",50051).build();
            ManagedChannel ch = ManagedChannelBuilder.forAddress("0xdeffbeef.com", 50051).usePlaintext(true).build();
            System.out.println(ch.getState(true));
            BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(ch);
            BackendOuterClass.LoginRequest loginRequest = BackendOuterClass.LoginRequest.newBuilder()
                                                            .setEmail("asdf").setPassword("test")
                                                            .build();
            BackendOuterClass.LoginReply loginReply = blockingStub.login(loginRequest);
            ByteString loginToken = loginReply.getToken();
            System.out.println(loginToken.toString(Charset.defaultCharset()));
        }
        catch (Exception e)
        {
            System.out.println("ERROR-Login Req.");
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void register_test(){
        try{

            ManagedChannel ch = ManagedChannelBuilder.forAddress("0xdeffbeef.com", 50051).usePlaintext(true).build();
            System.out.println(ch.getState(true));
            BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(ch);
            UserOuterClass.User user = UserOuterClass.User.newBuilder().setUserId(5)
                                        .setEmail("deneme@bilemiyorumaltan.com").setPassword("asdf123!")
                                        .setName("testUser").build();
            BackendOuterClass.RegisterRequest registerRequest = BackendOuterClass.RegisterRequest
                                                                .newBuilder()
                                                                .setUser(user).build();
            System.out.println("Sending Register Req");
            BackendOuterClass.GenericReply registerReply = blockingStub.register(registerRequest);
            System.out.println("Sent.");
            System.out.println(registerReply.toString());
        }
        catch (Exception e)
        {
            System.out.println("ERROR-Register Req.");
            System.out.println(e.getMessage());
        }
    }
}


