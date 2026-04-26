package ru.flux.android.core.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("chats")
    Call<List<ChatResponse>> getChats();

    @GET("users")
    Call<List<UserResponse>> getUsers();

    @GET("users/me")
    Call<UserResponse> getMe();

    @PUT("users/me")
    Call<UserResponse> updateMe(@Body UpdateUserRequest request);

    @DELETE("users/me")
    Call<Void> deleteMe();

    @GET("users/me/contacts")
    Call<List<ContactResponse>> getMyContacts();

    @POST("chats")
    Call<ChatResponse> createChat(@Body CreateChatRequest request);

    @DELETE("chats/{id}")
    Call<Void> deleteChat(@Path("id") String id);
}
