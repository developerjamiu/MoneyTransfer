package ng.edu.polyibadan.moneytransfer.App.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {

    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences preferences;

    SharedPreferences.Editor editor;
    Context context;

    // Shared preference mode
    int PRIVATE_MODE = 0;

    // Shared Preferences File Name
    private static final String PREF_NAME = "SeamLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_BALANCE = "balance";
    private static final String SERVER_ID = "serverId";
    private static final String KEY_TOKEN = "token";

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // Commit changes
        editor.commit();

        Log.d(TAG, "setLogin: User login session modified!");
    }

    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token);

        // Commit changes
        editor.commit();
    }

    public void setBalance(int balance) {
        editor.putInt(KEY_BALANCE, balance);

        // Commit changes
        editor.commit();
    }

    public void setServerId(String serverId) {
        editor.putString(SERVER_ID, serverId);

        // Commit changes
        editor.commit();
    }

    public String getServerId() {
        return preferences.getString(SERVER_ID, "");
    }

    public int getBalance() {
        return preferences.getInt(KEY_BALANCE, 0);
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, "");
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}
