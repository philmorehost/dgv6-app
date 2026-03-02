package com.dgv6.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FundActivity extends AppCompatActivity {
    private EditText etAmount;
    private Button btnPay;
    private TextView tvBankInfo;
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fund);

        etAmount = findViewById(R.id.etAmount);
        btnPay = findViewById(R.id.btnPayOnline);
        tvBankInfo = findViewById(R.id.tvBankInfo);
        session = new SessionManager(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(session.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        btnPay.setOnClickListener(v -> initiateFunding());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnManual).setOnClickListener(v -> Toast.makeText(this, "Manual banks coming soon", Toast.LENGTH_SHORT).show());

        fetchVirtualBanks();
    }

    private void fetchVirtualBanks() {
        apiService.getVirtualBanks(session.getApiKey()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Logic to display bank details
                    tvBankInfo.setText("Wema Bank: 0123456789 (DGV6-" + session.getUsername() + ")");
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void initiateFunding() {
        String amount = etAmount.getText().toString().trim();
        if (amount.isEmpty() || Integer.parseInt(amount) < 100) {
            Toast.makeText(this, "Min funding is ₦100", Toast.LENGTH_SHORT).show();
            return;
        }

        String ref = "MOB-" + System.currentTimeMillis();

        JsonObject body = new JsonObject();
        body.addProperty("api_key", session.getApiKey());
        body.addProperty("reference", ref);
        body.addProperty("amount", amount);

        apiService.createCheckout(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsString().equals("success")) {
                    // In a real app, integrate Monnify/Paystack Mobile SDK here
                    Toast.makeText(FundActivity.this, "Checkout Initialized. Proceed with SDK.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FundActivity.this, "Failed to init checkout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(FundActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
