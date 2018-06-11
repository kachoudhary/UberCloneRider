package com.example.karchou.uberclonerider_youtube.Common;

import com.example.karchou.uberclonerider_youtube.Remote.RetroFitClient;
import com.example.karchou.uberclonerider_youtube.Remote.iGoogleAPI;

public class Common {
    public static final String baseURL="https://maps.googleapis.com";

    public static iGoogleAPI getGoogleAPI() {

        return RetroFitClient.getClient(baseURL).create(iGoogleAPI.class);

    }
}
