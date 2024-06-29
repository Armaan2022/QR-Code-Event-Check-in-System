package com.example.qrcheckin.SendNotificationPack;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A class to provide a singleton instance of Retrofit client.
 */
public class Client {
    private static Retrofit retrofit=null;

    /**
     * Gets a singleton instance of Retrofit client.
     * If the instance does not exist, it creates a new one.
     * @param url The base URL for the Retrofit client.
     * @return The singleton instance of Retrofit client.
     */
    public static Retrofit getClient(String url) {
        if(retrofit==null){
            retrofit=new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
