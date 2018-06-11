package com.example.karchou.uberclonerider_youtube.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface iGoogleAPI {

    @GET
    Call<String> getPath(@Url String url);
    
}
