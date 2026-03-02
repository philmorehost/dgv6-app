package com.dgv6.app;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceActivity extends AppCompatActivity {
    private EditText etPhone, etAmount;
    private Button btnPurchase;
    private Spinner spnNetwork, spnPlan;
    private TextView tvTitle, tvLimits, tvPlanLabel;
    private View headerView;
    private ImageView btnBack;
    private String serviceType;
    private SessionManager session;
    private ApiService apiService;
    private List<JsonObject> plansList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        serviceType = getIntent().getStringExtra("service_type");
        etPhone = findViewById(R.id.etPhone);
        etAmount = findViewById(R.id.etAmount);
        btnPurchase = findViewById(R.id.btnPurchase);
        spnNetwork = findViewById(R.id.spnNetwork);
        spnPlan = findViewById(R.id.spnPlan);
        tvTitle = findViewById(R.id.tvTitle);
        tvLimits = findViewById(R.id.tvLimits);
        tvPlanLabel = findViewById(R.id.tvPlanLabel);
        headerView = findViewById(R.id.headerView);
        btnBack = findViewById(R.id.btnBack);

        session = new SessionManager(this);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(session.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        setupUI();
        loadNetworks();

        btnPurchase.setOnClickListener(v -> performPurchase());
        btnBack.setOnClickListener(v -> finish());

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() >= 11) checkLimit(s.toString());
                if (s.length() >= 4) identifyNetwork(s.toString());
            }
        });
    }

    private void setupUI() {
        tvTitle.setText(serviceType.toUpperCase());
        try {
            headerView.setBackgroundColor(Color.parseColor(session.getPrimaryColor()));
        } catch (Exception e) {}

        if (serviceType.equals("data") || serviceType.equals("cable") || serviceType.equals("electric") || serviceType.equals("education")) {
            tvPlanLabel.setVisibility(View.VISIBLE);
            spnPlan.setVisibility(View.VISIBLE);
        }
    }

    private void loadNetworks() {
        String[] networks = {"MTN", "Airtel", "Glo", "9mobile"};
        if (serviceType.equals("cable")) networks = new String[]{"DSTV", "GOtv", "StarTimes"};
        if (serviceType.equals("electric")) networks = new String[]{"Ikeja Electric", "Eko Electric", "Abuja Electric", "Kano Electric"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, networks);
        spnNetwork.setAdapter(adapter);

        spnNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPlans(parent.getItemAtPosition(position).toString().toLowerCase());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadPlans(String network) {
        if (spnPlan.getVisibility() != View.VISIBLE) return;

        JsonObject body = new JsonObject();
        body.addProperty("api_key", session.getApiKey());
        body.addProperty("service", serviceType);
        body.addProperty("network", network);

        apiService.getServices(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsString().equals("success")) {
                    JsonArray plans = response.body().getAsJsonArray("data");
                    plansList.clear();
                    List<String> planNames = new ArrayList<>();
                    for (JsonElement el : plans) {
                        JsonObject p = el.getAsJsonObject();
                        plansList.add(p);
                        planNames.add(p.get("name").getAsString() + " - ₦" + p.get("amount").getAsString());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ServiceActivity.this, android.R.layout.simple_spinner_dropdown_item, planNames);
                    spnPlan.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void checkLimit(String phone) {
        JsonObject body = new JsonObject();
        body.addProperty("api_key", session.getApiKey());
        body.addProperty("phone", phone);
        body.addProperty("type", serviceType);

        apiService.checkLimit(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    tvLimits.setText("Daily Limit Used: " + data.get("used").getAsString() + " / " + data.get("limit").getAsString());
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void identifyNetwork(String phone) {
        if (!serviceType.equals("airtime") && !serviceType.equals("data")) return;

        JsonObject body = new JsonObject();
        body.addProperty("phone", phone);
        apiService.identifyNetwork(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("status").getAsString().equals("success")) {
                    String network = response.body().get("network").getAsString();
                    // Auto select network in spinner
                    for (int i = 0; i < spnNetwork.getCount(); i++) {
                        if (spnNetwork.getItemAtPosition(i).toString().toLowerCase().contains(network.toLowerCase())) {
                            spnNetwork.setSelection(i);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void performPurchase() {
        String phone = etPhone.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();
        String network = spnNetwork.getSelectedItem().toString().toLowerCase();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone/Meter Number required", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("api_key", session.getApiKey());
        body.addProperty("phone", phone);
        body.addProperty("network", network);

        if (spnPlan.getVisibility() == View.VISIBLE && !plansList.isEmpty()) {
            JsonObject selectedPlan = plansList.get(spnPlan.getSelectedItemPosition());
            body.addProperty("plan_id", selectedPlan.get("id").getAsString());
            body.addProperty("amount", selectedPlan.get("amount").getAsString());
        } else {
            if (amount.isEmpty()) {
                Toast.makeText(this, "Amount required", Toast.LENGTH_SHORT).show();
                return;
            }
            body.addProperty("amount", amount);
        }

        Call<JsonObject> call;
        switch(serviceType) {
            case "data": call = apiService.buyData(body); break;
            case "airtime": call = apiService.buyAirtime(body); break;
            case "sms": call = apiService.sendBulkSms(body); break;
            default: call = apiService.buyAirtime(body); break;
        }

        btnPurchase.setEnabled(false);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnPurchase.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ServiceActivity.this, response.body().get("message").getAsString(), Toast.LENGTH_LONG).show();
                    if(response.body().get("status").getAsString().equals("success")) finish();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnPurchase.setEnabled(true);
                Toast.makeText(ServiceActivity.this, "Transaction Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
