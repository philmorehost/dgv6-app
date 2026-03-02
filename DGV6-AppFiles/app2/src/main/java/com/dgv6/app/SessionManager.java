package com.dgv6.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "DGV6Prefs";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_BIO_ENABLED = "bioEnabled";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_SITE_TITLE = "site_title";
    private static final String KEY_PRIMARY_COLOR = "primary_color";
    private static final String KEY_LOGO_URL = "logo_url";
    private static final String KEY_SUPPORT_WHATSAPP = "support_whatsapp";
    private static final String KEY_SUPPORT_EMAIL = "support_email";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUser(String username, String apiKey) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_API_KEY, apiKey);
        editor.apply();
    }

    public void setBaseUrl(String url) {
        if (!url.endsWith("/")) url += "/";
        if (!url.startsWith("http")) url = "https://" + url;
        editor.putString(KEY_BASE_URL, url + "web/api/");
        editor.apply();
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_BASE_URL, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void saveSiteInfo(String title, String color, String logo, String whatsapp, String email) {
        editor.putString(KEY_SITE_TITLE, title);
        editor.putString(KEY_PRIMARY_COLOR, color);
        editor.putString(KEY_LOGO_URL, logo);
        editor.putString(KEY_SUPPORT_WHATSAPP, whatsapp);
        editor.putString(KEY_SUPPORT_EMAIL, email);
        editor.apply();
    }

    public String getSiteTitle() { return prefs.getString(KEY_SITE_TITLE, "VTU Platform"); }
    public String getPrimaryColor() { return prefs.getString(KEY_PRIMARY_COLOR, "#007bff"); }
    public String getLogoUrl() { return prefs.getString(KEY_LOGO_URL, null); }
    public String getSupportWhatsApp() { return prefs.getString(KEY_SUPPORT_WHATSAPP, null); }
    public String getSupportEmail() { return prefs.getString(KEY_SUPPORT_EMAIL, null); }

    public void setBiometricEnabled(boolean enabled) {
        editor.putBoolean(KEY_BIO_ENABLED, enabled);
        editor.apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIO_ENABLED, false);
    }

    public void logout() {
        String baseUrl = getBaseUrl();
        editor.clear();
        editor.putString(KEY_BASE_URL, baseUrl); // Keep the domain setup even after logout
        editor.apply();
    }
}
