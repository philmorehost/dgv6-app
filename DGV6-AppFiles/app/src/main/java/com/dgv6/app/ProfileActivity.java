package com.dgv6.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvUsername, tvLevel, tvPhone, tvJoined, tvPoints;
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvUsername = findViewById(R.id.tvUsername);
        tvLevel = findViewById(R.id.tvLevel);
        tvPhone = findViewById(R.id.tvPhone);
        tvJoined = findViewById(R.id.tvJoined);
        tvPoints = findViewById(R.id.tvPoints);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        session = new SessionManager(this);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(session.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        fetchProfile();
    }

    private void fetchProfile() {
        JsonObject body = new JsonObject();
        body.addProperty("api_key", session.getApiKey());

        apiService.getProfile(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    tvName.setText(data.get("firstname").getAsString() + " " + data.get("lastname").getAsString());
                    tvEmail.setText(data.get("email").getAsString());
                    tvUsername.setText("@" + data.get("username").getAsString());
                    tvLevel.setText(data.get("level").getAsString());
                    if(data.has("phone")) tvPhone.setText(data.get("phone").getAsString());
                    if(data.has("created_at")) tvJoined.setText(data.get("created_at").getAsString());
                    if(data.has("loyalty_points")) tvPoints.setText(data.get("loyalty_points").getAsString() + " pts");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
