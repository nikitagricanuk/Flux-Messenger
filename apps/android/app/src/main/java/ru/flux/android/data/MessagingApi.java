package ru.flux.android.data;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.flux.android.MessageResponse;

public interface MessagingApi {

    @GET("messages/chat/{chatId}")
    Call<List<MessageResponse>> getMessages(
            @Path("chatId") UUID chatId,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("messages")
    Call<MessageResponse> sendMessage(@Body SendMessageRequest request);

    @POST("messages/chat/{chatId}/read")
    Call<Void> markAsRead(@Path("chatId") UUID chatId);
}