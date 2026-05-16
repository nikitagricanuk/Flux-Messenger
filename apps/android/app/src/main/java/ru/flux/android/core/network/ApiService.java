package ru.flux.android.core.network;

import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @GET("chats")
    Call<List<ChatResponse>> getChats();

    @GET("chats/favorites")
    Call<List<FavoriteResponse>> getFavorites();

    @POST("chats/favorites")
    Call<FavoriteResponse> addFavorite(@Body AddFavoriteRequest request);

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

    @POST("chats/direct")
    Call<ChatResponse> createDirectChat(@Body CreateChatRequest request);

    @Multipart
    @POST("chats/group")
    Call<ChatResponse> createGroupChat(
            @Part("name") okhttp3.RequestBody name,
            @Part List<MultipartBody.Part> memberIds,
            @Part MultipartBody.Part avatar);

    @DELETE("chats/{id}")
    Call<Void> deleteChat(@Path("id") String id);

    @POST("users/me/contacts")
    Call<Void> addContact(@Body AddContactRequest request);

    @Multipart
    @PATCH("users/me/avatar")
    Call<UserResponse> uploadAvatar(@Part MultipartBody.Part file);

    @GET("messages/chat/{chatId}")
    Call<List<MessageResponse>> getMessages(
            @Path("chatId") java.util.UUID chatId,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("size") int size);

    @POST("messages")
    Call<MessageResponse> sendMessage(@Body SendMessageRequest request);

    @POST("messages/chat/{chatId}/read")
    Call<Void> markAsRead(@Path("chatId") java.util.UUID chatId);

    @PATCH("messages/{messageId}")
    Call<MessageResponse> editMessage(
            @Path("messageId") java.util.UUID messageId,
            @Body SendMessageRequest request);

    @DELETE("messages/{messageId}")
    Call<Void> deleteMessage(@Path("messageId") java.util.UUID messageId);

    @GET("users/search")
    Call<List<UserResponse>> searchUsers(@retrofit2.http.Query("query") String query);
}
