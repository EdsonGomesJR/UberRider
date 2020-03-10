package com.edson.uberrider.Remote;


import com.edson.uberrider.Model.DataMessage;
import com.edson.uberrider.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAoLqe3jk:APA91bHnQFqKYOLFx1ba-QakNFHegLewPbDxr1voU5lJ6Db2RP3sRwcWqCXZjyY_J4tRio3dKaeUALRkSm0jZp7aZcYvFvzsaXiYVFBO_NWIoLLPAh2CgfuXvH0fg2Pq8LdcYahBSmzp"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}
