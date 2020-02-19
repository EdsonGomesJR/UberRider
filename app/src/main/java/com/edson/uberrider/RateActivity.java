package com.edson.uberrider;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.edson.uberrider.Common.Common;
import com.edson.uberrider.Model.Rate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateActivity extends AppCompatActivity {

    Button btnSubmit;
    MaterialEditText edtComment;
    MaterialRatingBar ratingBar;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference driverInformationRef;

    double ratingStars = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        //iniciar firebase
        database = FirebaseDatabase.getInstance();
        rateDetailRef = database.getReference(Common.rate_detail_tbl);
        driverInformationRef = database.getReference(Common.user_driver_tbl);

        //Iniciar view
        btnSubmit = findViewById(R.id.btnSubmit);
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);

        //evento

        ratingBar.setOnRatingChangeListener((ratingBar1, rating) -> ratingStars = rating);

       /*   ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
        @Override public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {

        ratingStars = rating;

        }
        });*/

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitRateDetails(Common.driverID);

            }
        });
    }

    private void submitRateDetails(String driverID) {

        AlertDialog alertDialog = new SpotsDialog(this);
        alertDialog.show();

        Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComment(edtComment.getText().toString());


        //update o novo valor para o Firebase
        rateDetailRef.child(driverID)
                .push() //Gera chave unica
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //Se houver sucesso do upload das informações do rate para o firebase, ira calcular a média para  Driver Information
                        rateDetailRef.child(driverID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        double averageStars = 0.0;
                                        int count = 0;

                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                            Rate rate = postSnapshot.getValue(Rate.class);
                                            averageStars += Double.parseDouble(rate.getRates());
                                            count++;
                                        }

                                        double finalAverage = averageStars / count;
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        String valueUpdate = df.format(finalAverage);

                                        //criando o objeto para ser atualizado
                                        Map<String, Object> driverUpdateRate = new HashMap<>();
                                        driverUpdateRate.put("rates", valueUpdate);

                                        driverInformationRef.child(Common.driverID)
                                                .updateChildren(driverUpdateRate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "Thank you for submit", Toast.LENGTH_SHORT).show();
                                                        finish();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                alertDialog.dismiss();
                                                Toast.makeText(RateActivity.this, "Rate updated but can't write to Driver Information", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RateActivity.this, "Rate Failed !", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
