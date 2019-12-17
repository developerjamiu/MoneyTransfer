package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ng.edu.polyibadan.moneytransfer.App.utils.AppConfig;
import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.AppHelper;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class TmTransferActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    EditText emailAddress;
    LinearLayout reviewDetails;
    EditText accountName;
    EditText emailAddressFound;
    ProgressBar progressBar;
    String emailAddressResponse;
    String fullNameResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tm_transfer);

        sessionManager = new SessionManager(getApplicationContext());

        progressBar = findViewById(R.id.loading);
        emailAddress = findViewById(R.id.et_email_address);
        reviewDetails = findViewById(R.id.review_details);
        accountName = findViewById(R.id.et_account_name);
        emailAddressFound = findViewById(R.id.et_email_address_found);
    }

    public void fetchRecipient(View view) {
        String emailAddressInput = emailAddress.getText().toString().trim();

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
        tmTransfer(emailAddressInput);
    }

    private void tmTransfer(String emailAddressInput) {
        // Tag used to cancel the request
        String tag_string_req = "req_transfer";
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, AppConfig.URL_FETCH_RECIPIENT + emailAddressInput, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            fullNameResponse = response.getString("fullName");
                            emailAddressResponse = response.getString("emailAddress");

                            reviewDetails.setVisibility(View.VISIBLE);
                            accountName.setText(fullNameResponse);
                            emailAddressFound.setText(emailAddressResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TmTransferActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(TmTransferActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("x-auth-token", sessionManager.getToken());

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void transfer(View view) {
        Intent intent = new Intent(TmTransferActivity.this, MakeTmTransferActivity.class);
        intent.putExtra("fullName", fullNameResponse);
        intent.putExtra("emailAddress", emailAddressResponse);
        startActivity(intent);
    }
}
