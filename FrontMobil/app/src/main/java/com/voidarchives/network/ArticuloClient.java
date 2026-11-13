package com.voidarchives.network;

import com.voidarchives.model.ArticuloDTO;
import com.voidarchives.model.ArticuloRequest;
import com.voidarchives.model.ArticuloResponse;
import com.voidarchives.model.ArticulosListResponse;
import com.voidarchives.model.ImagenUploadRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ArticuloClient {

    @POST("articulos")
    Call<ArticuloResponse> crearArticulo(
            @Header("Authorization") String token,
            @Body ArticuloRequest request
    );

    @GET("articulos")
    Call<ArticulosListResponse> obtenerArticulos();

    @GET("articulos/{id}")
    Call<ArticuloDTO> obtenerArticuloPorId(
            @Path("id") String id,
            @Header("Authorization") String token
    );

    @PUT("articulos/{id}")
    Call<ArticuloResponse> actualizarArticulo(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body ArticuloRequest request
    );

    @POST("articulos/{id}/imagen")
    Call<ArticuloResponse> subirImagenArticulo(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body ImagenUploadRequest request
    );
}
