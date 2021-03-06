package com.edson.uberrider;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edson.uberrider.Common.Common;
import com.edson.uberrider.Remote.IGoogleAPI;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mLocation, mDestination;
    TextView txtCalculate, txtLocation, txtDestination;
    IGoogleAPI mService;

    boolean isTapOnMap;

    public static BottomSheetDialogFragment newInstance(String location, String destination, boolean isTapOnMap) {

        BottomSheetDialogFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("location", location);
        args.putString("destination", destination);
        args.putBoolean("isTapOnMap", isTapOnMap);
        f.setArguments(args);
        return f;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_rider, container, false);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtDestination = view.findViewById(R.id.txtDestination);
        txtCalculate = view.findViewById(R.id.txtCalculate);


        mService = Common.getGoogleService();
        getPrice(mLocation, mDestination);


        //set data
        if (!isTapOnMap) {
            //call this fragment from place autocomplete textView
            txtLocation.setText(mLocation);
            txtDestination.setText(mDestination);
        }

        return view;
    }

    private void getPrice(String mLocation, String mDestination) {

        String requestUrl = null;
        //  https://maps.googleapis.com/maps/api/directions/json?mode=driving&transit_routing_preference=less_driving&origin=-23.204127,%20-45.853337&destination=-23.205229,%20-45.856794&key=AIzaSyDqCIADJSnH1KsyrWD1UPWWagUpWk9t4-U
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&"
                    + "transit_routing_preference=less_driving&"
                    + "origin=" + mLocation + "&"
                    + "destination=" + mDestination + "&"
                    + "key=" + getResources().getString(R.string.google_direction_api);
            Log.e("LINK", requestUrl); // for debug

            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //get object

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        //get distance

                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");

                        //use regex to extract double from string
                        //this regex will remove all text thats not digit
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                        //get time

                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");

                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                        String final_calculate = String.format("%s + %s = $%.2f", distance_text, time_text,
                                Common.getPrice(distance_value, time_value));

                        txtCalculate.setText(final_calculate);

                        if (isTapOnMap) {
                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            txtLocation.setText(start_address);
                            txtDestination.setText(end_address);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    Log.e("ERROR", t.getMessage());

                }
            });
        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }
}
