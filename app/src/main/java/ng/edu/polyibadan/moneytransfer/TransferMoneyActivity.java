package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ng.edu.polyibadan.moneytransfer.App.utils.AppController;

import static android.view.View.GONE;

public class TransferMoneyActivity extends AppCompatActivity {

    private static final String TAG = "TransferMoneyActivity";
    private String[] banks = {"Select A Bank", "Access Bank", "CitiBank", "Diamond Bank", "EcoBank", "Fidelity Bank", "First Bank of Nigeria", "First City Monument Bank",
            "GTBank Plc", "Heritage Bank", "JAIZ Bank", "Keystone Bank", "Skye Bank", "Stanbic IBTC Bank", "Sterling Bank", "Union Bank", "United Bank for Africa",
            "Unity Bank", "Wema Bank", "Zenith Bank"};
    private Spinner spBank;
    private EditText etAccountNumber;
    private ProgressBar loading;

    private LinearLayout container;
    private EditText accountNameInReview;
    private EditText bankNameInReview;
    private EditText accountNumberInReview;

    private String createUrl;
    private String id;
    private String accountNumberFromResponse;
    private String fullName;
    private String bankName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money);

        createUrl = "https://api.ravepay.co/v2/gpx/transfers/beneficiaries/create";

        container = findViewById(R.id.review_details);
        loading = findViewById(R.id.loading);
        etAccountNumber = findViewById(R.id.et_account_number);
        spBank = findViewById(R.id.sp_bank);
        spBank.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, banks));

        accountNameInReview = findViewById(R.id.et_account_name);
        bankNameInReview = findViewById(R.id.et_bank_name);
        accountNumberInReview = findViewById(R.id.et_account_number_in_review);
    }

    public void createRecipient(View view) {
        createRecipient();
    }

    private void createRecipient() {
        // Tag used to cancel the request
        String tag_string_req = "create_recipient";
        String secretKey = "FLWSECK_TEST-2694f8c4f47b5cfc47a241fc0826bdaa-X";

        loading.setVisibility(View.VISIBLE);

        String bank = spBank.getSelectedItem().toString();
        String accountBank;
        String accountNumber = etAccountNumber.getText().toString();

        switch (bank) {
            case "Access Bank":
                accountBank = "044";
                break;
            case "CitiBank":
                accountBank = "023";
                break;
            case "Diamond Bank":
                accountBank = "063";
                break;
            case "Ecobank Plc":
                accountBank = "050";
                break;
            case "Fidelity Bank":
                accountBank = "070";
                break;
            case "First Bank of Nigeria":
                accountBank = "011";
                break;
            case "First City Monument Bank":
                accountBank = "214";
                break;
            case "GTBank Plc":
                accountBank = "058";
                break;
            case "Heritage Bank":
                accountBank = "030";
                break;
            case "JAIZ Bank":
                accountBank = "301";
                break;
            case "Keystone Bank":
                accountBank = "082";
                break;
            case "Skye Bank":
                accountBank = "076";
                break;
            case "Stanbic IBTC Bank":
                accountBank = "221";
                break;
            case "Sterling Bank":
                accountBank = "232";
                break;
            case "Union Bank":
                accountBank = "032";
                break;
            case "United Bank for Africa":
                accountBank = "033";
                break;
            case "Unity Bank":
                accountBank = "215";
                break;
            case "Wema Bank":
                accountBank = "035";
                break;
            case "Zenith Bank":
                accountBank = "057";
                break;
            default:
                loading.setVisibility(GONE);
                Toast.makeText(this, "Please select a Bank from the list", Toast.LENGTH_SHORT).show();
                return;
        }

        if (!(accountNumber.length() == 10)) {
            etAccountNumber.setError("Account number should be 10 characters");
            etAccountNumber.requestFocus();
            loading.setVisibility(GONE);
            return;
        }

        // Post params to be sent to server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("account_number", accountNumber);
        params.put("account_bank", accountBank);
        params.put("seckey", secretKey);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, createUrl, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        loading.setVisibility(GONE);

                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                JSONObject recipient = response.getJSONObject("data");

                                id = recipient.getString("id");
                                accountNumberFromResponse = recipient.getString("account_number");
                                fullName = recipient.getString("fullname");
                                bankName = recipient.getString("bank_name");

                                container.setVisibility(View.VISIBLE);
                                accountNameInReview.setText(fullName);
                                bankNameInReview.setText(bankName);
                                accountNumberInReview.setText(accountNumberFromResponse);

                                Toast.makeText(TransferMoneyActivity.this, "Account Fetched", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(TransferMoneyActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TransferMoneyActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(TransferMoneyActivity.this, jsonError, Toast.LENGTH_SHORT).show();
                }
                loading.setVisibility(GONE);
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(0,-1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_string_req);
    }

    public void transfer(View view) {
        Intent intent = new Intent(TransferMoneyActivity.this, MakeTransferActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("accountNumber", accountNumberFromResponse);
        intent.putExtra("accountName", fullName);
        intent.putExtra("bankName", bankName);
        intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0));
        startActivity(intent);
    }

    public void goBack(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
