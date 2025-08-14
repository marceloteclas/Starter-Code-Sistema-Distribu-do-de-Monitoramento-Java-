package com.example.monitor.grpc;

import com.example.monitor.common.TokenUtil;
import com.example.monitor.grpc.MonitorProto.*;
import io.grpc.stub.StreamObserver;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String token = TokenUtil.issue(request.getUsername());
        boolean ok = token != null;
        
        LoginResponse response = LoginResponse.newBuilder()
            .setToken(ok ? token : "")
            .setOk(ok)
            .setMessage(ok ? "Login successful" : "Login failed")
            .build();
            
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validate(Token request, StreamObserver<ValidateResponse> responseObserver) {
        boolean valid = TokenUtil.validate(request.getToken());
        responseObserver.onNext(ValidateResponse.newBuilder().setValid(valid).build());
        responseObserver.onCompleted();
    }
}