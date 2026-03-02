package com.dgv6.app;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("site-info.php")
    Call<JsonObject> getSiteInfo();

    @FormUrlEncoded
    @POST("login.php")
    Call<JsonObject> login(@Field("username") String username, @Field("password") String password);

    @POST("register.php")
    Call<JsonObject> register(@Body JsonObject body);

    @POST("profile.php")
    Call<JsonObject> getProfile(@Body JsonObject body);

    @POST("services.php")
    Call<JsonObject> getServices(@Body JsonObject body);

    @POST("airtime.php")
    Call<JsonObject> buyAirtime(@Body JsonObject body);

    @POST("data.php")
    Call<JsonObject> buyData(@Body JsonObject body);

    // --- Bulk SMS ---
    @POST("sms-sender-ids.php")
    Call<JsonObject> getSenderIds(@Body JsonObject body);

    @POST("sms.php")
    Call<JsonObject> sendBulkSms(@Body JsonObject body);

    @POST("submit-sender-id.php")
    Call<JsonObject> submitSenderId(@Body JsonObject body);

    @POST("contacts.php")
    Call<JsonObject> manageContacts(@Body JsonObject body);

    @POST("check-limit.php")
    Call<JsonObject> checkLimit(@Body JsonObject body);

    @POST("identify-network.php")
    Call<JsonObject> identifyNetwork(@Body JsonObject body);

    @POST("batch-status.php")
    Call<JsonObject> getBatchStatus(@Body JsonObject body);

    @POST("biometric.php?action=verify_mobile_login")
    Call<JsonObject> verifyBiometric(@Body JsonObject body);

    // --- Funding Endpoints ---
    @GET("funding-config.php")
    Call<JsonObject> getFundingConfig(@Query("api_key") String apiKey);

    @POST("create-checkout.php")
    Call<JsonObject> createCheckout(@Body JsonObject body);

    @GET("virtual-banks.php")
    Call<JsonObject> getVirtualBanks(@Query("api_key") String apiKey);

    @POST("fund-manual.php")
    Call<JsonObject> submitManualFunding(@Body JsonObject body);
}
