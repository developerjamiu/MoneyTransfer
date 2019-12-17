package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ng.edu.polyibadan.moneytransfer.App.utils.AppConfig;
import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

import static android.view.View.GONE;

public class MakeTransferActivity extends AppCompatActivity {

    private static final String TAG = "MakeTransferActivity";
    ProgressBar loading;

    String recipient;
    String transactionReference;
    TextView fullName;
    TextView accountNumber;
    TextView bankName;

    EditText etAmount;
    EditText etNote;
    String amount;
    double amount2;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_transfer);

        sessionManager = new SessionManager(getApplicationContext());

        loading = findViewById(R.id.loading);
        fullName = findViewById(R.id.full_name);
        accountNumber = findViewById(R.id.account_number);
        bankName = findViewById(R.id.bank_name);

        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);

        Intent intent = getIntent();
        fullName.setText(intent.getStringExtra("accountName"));
        accountNumber.setText(intent.getStringExtra("accountNumber"));
        bankName.setText(intent.getStringExtra("bankName"));
        recipient = intent.getStringExtra("id");
        transactionReference = recipient +" "+  UUID.randomUUID().toString();
    }

    private void initializeTransfer() {
        // Tag used to cancel the request
        String tag_string_req = "create_recipient";
        String secretKey = "FLWSECK_TEST-2694f8c4f47b5cfc47a241fc0826bdaa-X";
        String initializeTransferUrl = "https://api.ravepay.co/v2/gpx/transfers/create";

        loading.setVisibility(View.VISIBLE);

        amount = etAmount.getText().toString();
        amount2 = Double.parseDouble(amount);
        String note = etNote.getText().toString();
        String currency = "NGN";
        String beneficiaryName = getIntent().getStringExtra("accountName");

        if (amount.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            loading.setVisibility(GONE);
            return;
        }

        if (note.isEmpty()) {
            note = "No Note";
        }

        // Post params to be sent to server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("recipient", recipient);
        params.put("amount", amount);
        params.put("seckey", secretKey);
        params.put("narration", note);
        params.put("currency", currency);
        params.put("reference", transactionReference);
        params.put("beneficiary_name", beneficiaryName);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, initializeTransferUrl, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        loading.setVisibility(GONE);

                        try {
                            String status = response.getString("status");

                            if (status.equals("success")) {
                                JSONObject recipient = response.getJSONObject("data");

                                String accountNumberText = recipient.getString("account_number");
                                String bankNameText = recipient.getString("bank_name");
                                String fullNameText = recipient.getString("fullname");
                                String amountText = recipient.getString("amount");

                                transfer();
                                Intent intent = new Intent(MakeTransferActivity.this, SuccessTransferActivity.class);
                                intent.putExtra("amount", amountText);
                                intent.putExtra("accountNumber", accountNumberText);
                                intent.putExtra("accountName", fullNameText);
                                intent.putExtra("bankName", bankNameText);
                                intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0));
                                startActivity(intent);
                            } else {
                                Toast.makeText(MakeTransferActivity.this, "Error: Try Again", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MakeTransferActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(MakeTransferActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
                loading.setVisibility(GONE);
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(0,-1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    private void deleteRecipient(String id) {
        // Tag used to cancel the request
        String tag_string_req = "delete_recipient";
        String secretKey = "FLWSECK_TEST-2694f8c4f47b5cfc47a241fc0826bdaa-X";
        String deleteUrl = "https://api.ravepay.co/v2/gpx/transfers/beneficiaries/delete";
        //loading.setVisibility(View.VISIBLE);

        // Post params to be sent to server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        params.put("seckey", secretKey);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, deleteUrl, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        //loading.setVisibility(GON
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(MakeTransferActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
                //loading.setVisibility(GONE);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void confirmTransfer(View view) {
        initializeTransfer();
        deleteRecipient(recipient);
    }

    public void goBack(View view) {
        Intent intent = new Intent(MakeTransferActivity.this, TransferMoneyActivity.class);
        intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MakeTransferActivity.this, TransferMoneyActivity.class);
        intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0));
        startActivity(intent);
    }

    private void transfer() {
        // Tag used to cancel the request
        String tag_string_req = "req_transfer";

        HashMap<String, Double> params = new HashMap<>();
        params.put("amount", amount2);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, AppConfig.URL_TRANSFER, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(MakeTransferActivity.this, jsonError, Toast.LENGTH_SHORT).show();
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
}
