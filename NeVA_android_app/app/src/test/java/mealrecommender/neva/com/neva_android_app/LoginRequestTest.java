package mealrecommender.neva.com.neva_android_app;

import org.junit.Test;

import backend.BackendGrpc;
import backend.BackendOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import static org.junit.Assert.assertEquals;

/**
 * Created by hakan on 11/14/17.
 */

public class LoginRequestTest {

    @Test
    public void login_test(){
        try {
            ManagedChannel mChannel = ManagedChannelBuilder.forAddress("0xdeffbeef.com",50051).build();
            System.out.println(mChannel.getState(true));
            BackendGrpc.BackendBlockingStub blockingStub = BackendGrpc.newBlockingStub(mChannel);
            BackendOuterClass.LoginRequest loginRequest = BackendOuterClass.LoginRequest.newBuilder().setEmail("test_email").setPassword("test_passwd").build();
            BackendOuterClass.LoginReply loginReply = blockingStub.login(loginRequest);
        }
        catch (Exception e)
        {
            System.out.println("ERROR-Login Req.");
            System.out.println(e);
        }

    }
}


