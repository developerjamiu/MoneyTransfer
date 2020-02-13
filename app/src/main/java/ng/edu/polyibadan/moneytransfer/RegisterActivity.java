package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import ng.edu.polyibadan.moneytransfer.App.utils.AppConfig;
import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.AppHelper;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText fullName;
    EditText emailAddress;
    EditText password;
    ProgressBar progressBar;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.et_full_name);
        emailAddress = findViewById(R.id.et_email_address);
        password = findViewById(R.id.et_password);

        progressBar = findViewById(R.id.progress);
        sessionManager = new SessionManager(getApplicationContext());

        if(sessionManager.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void registerUser(final String fullName, final String emailAddress, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        progressBar.setVisibility(View.VISIBLE);

        // Post params to be sent to server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("fullName", fullName);
        params.put("emailAddress", emailAddress);
        params.put("password", password);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_REGISTER, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        progressBar.setVisibility(View.GONE);

                        try {
                            JSONObject headers = response.getJSONObject("headers");
                            String token = headers.getString("X-Auth-Token");
                            sessionManager.setToken(token);

                            sessionManager.setLogin(true);

                            JSONObject data = response.getJSONObject("data");

                            String serverId = data.getString("_id");
                            sessionManager.setServerId(serverId);

                            String fullName = data.getString("fullName");
                            String emailAddress = data.getString("emailAddress");
                            double balance = data.getDouble("balance");
                            String createdAt = data.getString("createdAt");

                            Toast.makeText(RegisterActivity.this, "User successfully registered", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("emailAddress", emailAddress);
                            intent.putExtra("balance", balance);
                            intent.putExtra("createdAt", createdAt);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(RegisterActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }){
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("data", new JSONObject(jsonString));
                    jsonResponse.put("headers", new JSONObject(response.headers));

                    return Response.success(jsonResponse,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException | UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void register(View view) {
        String fullNameInput = fullName.getText().toString().trim();
        String emailAddressInput = emailAddress.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if (fullNameInput.isEmpty()) {
            fullName.setError("Full Name is Required");
            fullName.requestFocus();
            return;
        }

        if (!(fullNameInput.length() > 4 && fullNameInput.length() < 50)) {
            fullName.setError("Full Name should have 5 to 50 Characters");
            fullName.requestFocus();
            return;
        }

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

        if (!(passwordInput.length() > 5 && passwordInput.length() < 255)) {
            password.setError("Password should have 5 to 255 Characters");
            password.requestFocus();
        }

        registerUser(fullNameInput, emailAddressInput, passwordInput);
    }

    public void haveAccount(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
    }
}
