package com.dgv6.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvBalance, tvWelcome, tvSiteTitle, tvLoyalty;
    private ImageView ivLogo;
    private View headerView;
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvBalance = findViewById(R.id.tvBalance);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSiteTitle = findViewById(R.id.tvSiteTitle);
        ivLogo = findViewById(R.id.ivLogo);
        headerView = findViewById(R.id.headerView);
        tvLoyalty = findViewById(R.id.tvLoyalty);

        findViewById(R.id.btnFund).setOnClickListener(v -> startActivity(new Intent(this, FundActivity.class)));
        findViewById(R.id.btnReferral).setOnClickListener(v -> Toast.makeText(this, "Referral link copied!", Toast.LENGTH_SHORT).show());

        findViewById(R.id.navHome).setOnClickListener(v -> {});
        findViewById(R.id.navHistory).setOnClickListener(v -> Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.navSupport).setOnClickListener(v -> {
            String whatsapp = session.getSupportWhatsApp();
            if(whatsapp != null) {
                // Open WhatsApp logic here
                Toast.makeText(this, "Contacting " + whatsapp, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.cardData).setOnClickListener(v -> openService("data"));
        findViewById(R.id.cardAirtime).setOnClickListener(v -> openService("airtime"));
        findViewById(R.id.cardCable).setOnClickListener(v -> openService("cable"));
        findViewById(R.id.cardPower).setOnClickListener(v -> openService("electric"));
        findViewById(R.id.cardSms).setOnClickListener(v -> openService("sms"));
        findViewById(R.id.cardEducation).setOnClickListener(v -> openService("education"));

        session = new SessionManager(this);
        tvWelcome.setText("Welcome, " + session.getUsername());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(session.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        fetchSiteInfo();
        fetchProfile();
    }

    private void fetchSiteInfo() {
        // Load from Session first for immediate UI update
        updateUI(session.getSiteTitle(), session.getPrimaryColor(), session.getLogoUrl());

        apiService.getSiteInfo().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsString().equals("success")) {
                    JsonObject data = response.body().getAsJsonObject("data");

                    String title = data.get("site_title").getAsString();
                    String primaryColor = data.get("primary_color").getAsString();
                    String logoUrl = data.get("logo_url").getAsString();

                    session.saveSiteInfo(
                        title, primaryColor, logoUrl,
                        data.has("whatsapp") ? data.get("whatsapp").getAsString() : null,
                        data.has("email") ? data.get("email").getAsString() : null
                    );

                    updateUI(title, primaryColor, logoUrl);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void updateUI(String title, String color, String logo) {
        tvSiteTitle.setText(title);
        if (headerView != null) {
            try {
                int parsedColor = Color.parseColor(color);
                headerView.setBackgroundColor(parsedColor);
                // Also update service icons tint if possible
                updateServiceIconsTint(parsedColor);
            } catch (Exception e) {
                headerView.setBackgroundColor(Color.parseColor("#007bff"));
            }
        }
        if (logo != null) {
            Glide.with(DashboardActivity.this).load(logo).placeholder(android.R.drawable.ic_menu_myplaces).into(ivLogo);
        }
    }

    private void updateServiceIconsTint(int color) {
        int[] cardIds = {R.id.cardData, R.id.cardAirtime, R.id.cardCable, R.id.cardPower, R.id.cardSms, R.id.cardEducation};
        for (int id : cardIds) {
            View card = findViewById(id);
            if (card instanceof androidx.cardview.widget.CardView) {
                LinearLayout layout = (LinearLayout) ((androidx.cardview.widget.CardView) card).getChildAt(0);
                ImageView icon = (ImageView) layout.getChildAt(0);
                icon.setColorFilter(color);
            }
        }
    }

    private void openService(String type) {
        Intent intent = new Intent(this, ServiceActivity.class);
        intent.putExtra("service_type", type);
        startActivity(intent);
    }

    private void fetchProfile() {
        JsonObject body = new JsonObject();
        body.addProperty("api_key", session.getApiKey());

        apiService.getProfile(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    if (data != null) {
                        tvBalance.setText("₦" + data.get("balance").getAsString());
                        if(data.has("loyalty_points")) tvLoyalty.setText(data.get("loyalty_points").getAsString() + " Points");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Failed to refresh balance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
