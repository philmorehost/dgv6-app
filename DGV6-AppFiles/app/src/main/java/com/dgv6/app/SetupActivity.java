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

public class SetupActivity extends AppCompatActivity {
    private EditText etDomain;
    private Button btnConnect;
    private ProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        etDomain = findViewById(R.id.etDomain);
        btnConnect = findViewById(R.id.btnConnect);
        progressBar = findViewById(R.id.progressBar);
        session = new SessionManager(this);

        btnConnect.setOnClickListener(v -> connect());
    }

    private void connect() {
        String domain = etDomain.getText().toString().trim();
        if (domain.isEmpty()) {
            Toast.makeText(this, "Please enter a domain", Toast.LENGTH_SHORT).show();
            return;
        }

        if (domain.startsWith("http://")) {
            domain = domain.substring(7);
        } else if (domain.startsWith("https://")) {
            domain = domain.substring(8);
        }

        // Remove trailing slashes to avoid issues with malformed URLs
        while (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        final String finalUrl = "https://" + domain + "/web/api/";
        final String domainToSave = "https://" + domain;

        btnConnect.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit;
        try {
            retrofit = new Retrofit.Builder()
                .baseUrl(finalUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        } catch (IllegalArgumentException e) {
            progressBar.setVisibility(View.GONE);
            btnConnect.setEnabled(true);
            Toast.makeText(SetupActivity.this, "Invalid domain format. Please enter a valid domain.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService tempApi = retrofit.create(ApiService.class);
        tempApi.getSiteInfo().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);
                btnConnect.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsString().equals("success")) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    session.setBaseUrl(domainToSave);
                    session.saveSiteInfo(
                        data.get("site_title").getAsString(),
                        data.get("primary_color").getAsString(),
                        data.get("logo_url").getAsString(),
                        data.has("whatsapp") ? data.get("whatsapp").getAsString() : null,
                        data.has("email") ? data.get("email").getAsString() : null
                    );
                    Toast.makeText(SetupActivity.this, "Connected to " + data.get("site_title").getAsString(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetupActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SetupActivity.this, "Invalid VTU domain. Please check and try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnConnect.setEnabled(true);
                Toast.makeText(SetupActivity.this, "Connection failed. Check your internet or domain.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
