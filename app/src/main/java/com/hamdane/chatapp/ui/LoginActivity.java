package com.hamdane.chatapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hamdane.chatapp.Api;
import com.hamdane.chatapp.MainActivity;
import com.hamdane.chatapp.R;
import com.hamdane.chatapp.listenner.Listener;


public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        if (Api.getCurrentUser() != null) {
            openChatActivity();
        }
    }

    protected void openChatActivity() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        LoginActivity.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Api.initFirebase(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Api.RC_SIGN_IN) {
            try {
                Api.getGoogleSignins(this, data, new Listener() {
                    @Override
                    public void failed() {
                        Toast.makeText(LoginActivity.this, "identification échouée", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void success() {
                        Toast.makeText(LoginActivity.this, "identification succès", Toast.LENGTH_SHORT).show();
                        LoginActivity.this.openChatActivity();
                    }
                });
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void clickLogin(View view) {
        Api.login(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED, null);
        finish();
    }
}