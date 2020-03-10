package com.edson.uberrider.Common;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.edson.uberrider.Model.DataMessage;
import com.edson.uberrider.Model.FCMResponse;
import com.edson.uberrider.Model.Token;
import com.edson.uberrider.Remote.FCMClient;
import com.edson.uberrider.Remote.GoogleMapAPI;
import com.edson.uberrider.Remote.IFCMService;
import com.edson.uberrider.Remote.IGoogleAPI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {

    public static final  String driver_tbl = "Drivers";
    public static final  String user_driver_tbl = "DriversInformation";
    public static final  String user_rider_tbl = "RiderInformation";
    public static final  String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String fcmURL = "https://fcm.googleapis.com";
    public static final String googleAPIUrl = "https://maps.googleapis.com";
    public static final String user_field = "rider_user"; //we need differente key with Driver App cuz we have the case: One Phone install both Driver and Rider app
    public static final String pwd_field = "rider_pwd";



    public static boolean isDriverFound = false;
    public static String driverID = "";

    public static double base_fare = 2.55;
    public static double time_rate = 0.35;
    public static double distance_rate = 1.75;
    public static String rate_detail_tbl = "RateDetails"; // se não tiver esse nó no Firebase, ele será criado automaticamente


    public static double getPrice(double km, int min) {

        return (base_fare + (time_rate * min) + (distance_rate * km));
    }


    public static IFCMService getFCMService() {

        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleService() {

        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

    public static void sendRequestToDriver(String driverID, IFCMService mService, Context context, Location currentLocation) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            Token token = postSnapShot.getValue(Token.class); //get token object drom database with key

                            //make raw payload - convert latlng to json

                            String riderToken = FirebaseInstanceId.getInstance().getToken(); // possivel erro pois está depreciado
                            Log.d("riderToken", "onDataChange: " + riderToken);
                            /** Caso dê erro utilizar esse método
                             * FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
                             *                 @Override
                             *                 public void onSuccess(InstanceIdResult instanceIdResult) {
                             *                       String deviceToken = instanceIdResult.getToken();
                             *                       // Do whatever you want with your token now
                             *                       // i.e. store it on SharedPreferences or DB
                             *                       // or directly send it to server
                             *                 }
                             * });
                             */
//                            Notification notification = new Notification(riderToken, json_lat_lng); //send it to driver app and we will deserialize it again
//                            Sender content = new Sender(token.getToken(), notification); //send this data to token
                            Map<String, String> content = new HashMap<>();
                            content.put("customer", riderToken);
                            content.put("lat", String.valueOf(currentLocation.getLatitude()));
                            content.put("lng", String.valueOf(currentLocation.getLongitude()));
                            DataMessage dataMessage = new DataMessage(token.getToken(), content);

                            mService.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show();
                                                Log.d("EDS", "onResponse: RESQUEST FOI");
                                            } else
                                                Toast.makeText(context, "Failed !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                                            Log.e("ERROR", t.getMessage());

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
