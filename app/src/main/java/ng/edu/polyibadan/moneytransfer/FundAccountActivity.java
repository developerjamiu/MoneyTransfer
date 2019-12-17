package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ng.edu.polyibadan.moneytransfer.App.utils.AppConfig;
import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class FundAccountActivity extends AppCompatActivity {

    private static final String TAG = "FundAccountActivity";
    private SessionManager sessionManager;

    EditText etAmount;
    EditText etNarration;

    String fullName;
    double amount;
    String narration;
    String email;
    String firstName;
    String lastName;
    String transactionReference;
    String country = "NG";
    String currency = "NGN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fund_account);

        Intent intent = getIntent();

        etAmount = findViewById(R.id.et_amount);
        etNarration = findViewById(R.id.et_narration);

        fullName = intent.getStringExtra("fullName");
        email = intent.getStringExtra("emailAddress");
        sessionManager = new SessionManager(getApplicationContext());

        String[] names = fullName.split(" ");
        firstName = names[0];
        lastName = names[1];
        transactionReference = email +" "+  UUID.randomUUID().toString();
        country = "NG";
        currency = "NGN";
    }

    public void fundAccount(View view) {
        if (etAmount.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etNarration.getText().toString().isEmpty()) {
            narration = "";
        }

        amount = Double.parseDouble(etAmount.getText().toString());
        narration = etNarration.getText().toString();

        new RavePayManager(this).setAmount(amount)
                .setCountry(country)
                .setCurrency(currency)
                .setEmail(email)
                .setfName(firstName)
                .setlName(lastName)
                .setNarration(narration)
                .setPublicKey(AppConfig.publicKey)
                .setEncryptionKey(AppConfig.encryptionKey)
                .setTxRef(transactionReference)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptMpesaPayments(false)
                .acceptGHMobileMoneyPayments(false)
                .onStagingEnv(false)
                .withTheme(R.style.DefaultTheme)
                .initialize();
    }

    private void fund() {
        // Tag used to cancel the request
        String tag_string_req = "req_fund";

        HashMap<String, Double> params = new HashMap<>();
        params.put("amount", amount);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, AppConfig.URL_FUND, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);

                        try {
                            double balance = response.getDouble("balance");

                            Intent intent = new Intent(FundAccountActivity.this, MainActivity.class);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("balance", balance);
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FundAccountActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(FundAccountActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                Toast.makeText(this, "SUCCESS: Payment Successful", Toast.LENGTH_SHORT).show();
                fund();
            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(FundAccountActivity.this, MainActivity.class));
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void goBack(View view) {
        finish();
    }
}
