package com.edson.uberrider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.edson.uberrider.Model.Rider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    DatabaseReference users;
    Button btnRegistrar, btnLogar;
    RelativeLayout rootLayout;
    FirebaseAuth auth;
    FirebaseDatabase db;

    private static final int PERMISSION = 1000;

    //adiciona o Calligraphy  ao contexto base
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //config o calligraphy para alterar a font do texto *OBS tem que estar antes do setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        //init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Riders");

        rootLayout= (RelativeLayout) findViewById(R.id.rootLayout);

        btnRegistrar = (Button) findViewById(R.id.btnRegister);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCadastroDialog();
            }
        });

        btnLogar = (Button) findViewById(R.id.btnSignIn);
        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogar.setEnabled(false);
                showLoginDialog();
            }
        });


    }

    private void showLoginDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("LOGIN");
        dialog.setMessage("Por favor use seu e-mail para Logar");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View login_layout = inflater.inflate(R.layout.layout_signin, null);

        final MaterialEditText edtMail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtSenha = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("LOGAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //setar o botão de login como desabilitado enquanto  está carregando
                btnLogar.setEnabled(false);



                //checar validação
                if (TextUtils.isEmpty(edtMail.getText().toString())) {

                    Snackbar.make(rootLayout, "Por favor digite um endereço de email",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtSenha.getText().toString())) {

                    Snackbar.make(rootLayout, "Por favor digite uma senha ",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtSenha.length() < 6) {

                    Snackbar.make(rootLayout, "Por favor digite uma senha com 6 digitos ou mais",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final SpotsDialog carregandoDialog = new SpotsDialog(MainActivity.this);
                carregandoDialog.show();
                //login
                auth.signInWithEmailAndPassword(edtMail.getText().toString(), edtSenha.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                carregandoDialog.dismiss();
                                startActivity(new Intent(MainActivity.this, Home .class));
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        carregandoDialog.dismiss();

                        Snackbar.make(rootLayout, "Login Falhou: " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
//ativando o botão de logar
                        btnLogar.setEnabled(true);
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showCadastroDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("CADASTRO");
        dialog.setMessage("Por favor use seu e-mail para cadastrar");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText edtMail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtSenha = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtNome = register_layout.findViewById(R.id.edtNome);
        final MaterialEditText edtTelefone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        //set button
        dialog.setPositiveButton("CADASTRO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //checar validação

                if (TextUtils.isEmpty(edtMail.getText().toString())) {

                    Snackbar.make(rootLayout, "Por favor digite um endereço de email",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtSenha.getText().toString())) {

                    Snackbar.make(rootLayout, "Por favor digite uma senha",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtSenha.length() < 6) {

                    Snackbar.make(rootLayout, "Por favor digite uma senha com 6 digitos ou mais",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtNome.getText().toString())) {

                    Snackbar.make(rootLayout, "Por favor digite um nome",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtTelefone.getText().toString())) {

                    Snackbar.make(rootLayout, "Por favor digite um telefone",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }


                auth.createUserWithEmailAndPassword(edtMail.getText().toString(), edtSenha.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                //salvar usuario no db
                                Rider rider = new Rider();
                                rider.setEmail(edtMail.getText().toString());
                                rider.setPassword(edtSenha.getText().toString());
                                rider.setName(edtNome.getText().toString());
                                rider.setPhone(edtTelefone.getText().toString());

                                //usando uid do current user do firebase como  chave
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(rider)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout, "Cadastrado com Sucesso!"
                                                        , Snackbar.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout, "Falha ao cadastrar:   " + e.getMessage()
                                                , Snackbar.LENGTH_SHORT).show();

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout, "Falha ao criar usuario:   " + e.getMessage()
                                , Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
