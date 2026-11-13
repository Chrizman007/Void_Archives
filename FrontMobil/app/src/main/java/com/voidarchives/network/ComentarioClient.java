package com.voidarchives.network;

import com.voidarchives.model.ComentarioRequest;
import com.voidarchives.model.ComentarioResponse;
import com.voidarchives.model.ComentariosListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ComentarioClient {

    @GET("comentarios/{articuloId}")
    Call<ComentariosListResponse> obtenerComentarios(
            @Path("articuloId") String articuloId,
            @Query("limit") int limit,
            @Query("skip") int skip
    );

    @POST("comentarios/{articuloId}")
    Call<ComentarioResponse> crearComentario(
            @Path("articuloId") String articuloId,
            @Header("Authorization") String token,
            @Body ComentarioRequest request
    );

    @DELETE("comentarios/{id}")
    Call<Void> eliminarComentario(
            @Path("id") String idComentario,
            @Header("Authorization") String token
    );
}
