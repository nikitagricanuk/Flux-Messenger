package ru.flux.android;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ru.flux.android.data.CreateChatRequest;

public interface ApiService {
    @GET("chats")
    Call<List<ChatResponse>> getChats();

    @GET("users")
    Call<List<UserResponse>> getUsers();

    @GET("users/me/contacts")
    Call<List<ContactResponse>> getMyContacts();

    @GET("users/search")
    Call<List<UserResponse>> searchUsers(@Query("query") String query);

    @POST("chats")
    Call<ChatResponse> createChat(@Body CreateChatRequest request);
}