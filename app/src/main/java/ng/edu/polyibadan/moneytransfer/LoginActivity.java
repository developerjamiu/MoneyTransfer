package ng.edu.polyibadan.moneytransfer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import ng.edu.polyibadan.moneytransfer.App.utils.AppConfig;
import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.AppHelper;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailAddress;
    private EditText password;
    private ProgressBar progressBar;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: starting");

        emailAddress = findViewById(R.id.et_email_address);
        password = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.progress);

        // Session Manager
        sessionManager = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (sessionManager.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void signIn(View view) {
        String emailAddressInput = emailAddress.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if (!emailAddressInput.isEmpty()) {
            if (!AppHelper.validateEmail(emailAddressInput)) {
                emailAddress.setError("Enter a Valid Email Address");
                emailAddress.requestFocus();
                return;
            }
            if (!(emailAddressInput.length() > 4 && emailAddressInput.length() < 255)) {
                emailAddress.setError("Email Address should have 2 to 255 Characters");
                emailAddress.requestFocus();
            }
        } else {
            emailAddress.setError("Email Address is required");
            emailAddress.requestFocus();
        }

        if (passwordInput.isEmpty()) {
            password.setError("Password is Required");
            password.requestFocus();
            return;
        }

        if (!(passwordInput.length() > 4 && passwordInput.length() < 255)) {
            password.setError("Password should have 5 to 255 Characters");
            password.requestFocus();
        }

        loginUser(emailAddressInput, passwordInput);
    }

    private void loginUser(final String emailAddress, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        progressBar.setVisibility(View.VISIBLE);

        // Post params to be sent to server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("emailAddress", emailAddress);
        params.put("password", password);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_LOGIN, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        progressBar.setVisibility(View.GONE);

                        try {
                            String token = response.getString("token");
                            sessionManager.setToken(token);

                            sessionManager.setLogin(true);


                            String serverId = response.getString("_id");
                            sessionManager.setServerId(serverId);

                            String fullName = response.getString("fullName");
                            String emailAddress = response.getString("emailAddress");
                            double balance = response.getDouble("balance");
                            String createdAt = response.getString("createdAt");

                            Toast.makeText(LoginActivity.this, "User authenticated", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("emailAddress", emailAddress);
                            intent.putExtra("balance", balance);
                            intent.putExtra("createdAt", createdAt);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(LoginActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void dontHaveAccount(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
