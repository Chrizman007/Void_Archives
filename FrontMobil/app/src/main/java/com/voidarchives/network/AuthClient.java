package com.voidarchives.network;

import com.voidarchives.model.AuthResponse;
import com.voidarchives.model.LoginRequest;
import com.voidarchives.model.RegistroRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthClient {

    @POST("auth/login")
    Call<AuthResponse> iniciarSesion(@Body LoginRequest loginRequest);

    @POST("auth/registro")
    Call<AuthResponse> registrarUsuario(@Body RegistroRequest registroRequest);
}
