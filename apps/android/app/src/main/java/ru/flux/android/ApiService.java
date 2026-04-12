package ru.flux.android;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("chats")
    Call<List<ChatResponse>> getChats();
}
