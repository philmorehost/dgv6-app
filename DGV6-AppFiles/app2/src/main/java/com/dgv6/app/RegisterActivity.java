package com.dgv6.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFirst, etLast, etEmail, etPhone, etUser, etPass, etAddress;
    private Button btnRegister;
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFirst = findViewById(R.id.etFirst);
        etLast = findViewById(R.id.etLast);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
        session = new SessionManager(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(session.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String fname = etFirst.getText().toString().trim();
        String lname = etLast.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        JsonObject body = new JsonObject();
        body.addProperty("firstname", fname);
        body.addProperty("lastname", lname);
        body.addProperty("email", email);
        body.addProperty("phone", phone);
        body.addProperty("username", user);
        body.addProperty("password", pass);
        body.addProperty("address", address);

        apiService.register(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsString().equals("success")) {
                    Toast.makeText(RegisterActivity.this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().get("message").getAsString() : "Registration Failed";
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
