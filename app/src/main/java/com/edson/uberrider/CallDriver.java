package com.edson.uberrider;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.edson.uberrider.Common.Common;
import com.edson.uberrider.Model.Rider;
import com.edson.uberrider.Remote.IFCMService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallDriver extends AppCompatActivity {

    Button btn_call_driver, btn_call_driver_phone;
    String driverId;
    Location mLastLocation;
    IFCMService mService;
    private CircleImageView avatarImage;
    private TextView txtName, txtPhone, txtRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        mService = Common.getFCMService();

        //init view
        avatarImage = findViewById(R.id.avatar_image);
        txtName = findViewById(R.id.txt_name);
        txtPhone = findViewById(R.id.txt_phone);
        txtRate = findViewById(R.id.txt_rate);

        btn_call_driver = findViewById(R.id.btn_call_driver);
        btn_call_driver_phone = findViewById(R.id.btn_call_driver_phone);


        btn_call_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here we'll use function sendNotificationToDriver from Home activity


                if (driverId != null && !driverId.isEmpty()) {
                    Common.sendRequestToDriver(Common.driverID, mService, getBaseContext(), mLastLocation);
                }
            }
        });

        btn_call_driver_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + txtPhone.getText().toString()));
                startActivity(intent);

            }
        });

        //get intent
        if (getIntent() != null) {

            driverId = getIntent().getStringExtra("driverId");
            double lat = getIntent().getDoubleExtra("lat", -1.0);
            double lng = getIntent().getDoubleExtra("lng", -1.0);

            mLastLocation = new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);

            loadDriverInfo(driverId);
        }


    }

    private void loadDriverInfo(String driverId) {

        FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl)
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Rider driverUser = dataSnapshot.getValue(Rider.class);

                        if (!driverUser.getAvatarUrl().isEmpty()) {

                            Picasso.get()
                                    .load(driverUser.getAvatarUrl())
                                    .into(avatarImage);
                        }

                        txtName.setText(driverUser.getName());
                        txtPhone.setText(driverUser.getPhone());
                        txtRate.setText(driverUser.getRates());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
