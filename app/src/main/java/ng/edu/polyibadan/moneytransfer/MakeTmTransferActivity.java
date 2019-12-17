package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class MakeTmTransferActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    ProgressBar progressBar;
    TextView fullName;
    EditText amount;

    String amountInput;
    String emailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_tm_transfer);

        sessionManager = new SessionManager(getApplicationContext());

        progressBar = findViewById(R.id.loading);
        fullName = findViewById(R.id.full_name);
        amount = findViewById(R.id.et_amount);
        Intent intent = getIntent();
        emailAddress = intent.getStringExtra("emailAddress");
        fullName.setText(intent.getStringExtra("fullName"));
    }

    private void tmTransfer() {
        // Tag used to cancel the request
        String tag_string_req = "req_transfer";
        progressBar.setVisibility(View.VISIBLE);

        amountInput = amount.getText().toString();
        // Post params to be sent to server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("emailAddress", emailAddress);
        params.put("amount", amountInput);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, AppConfig.URL_TM_TRANSFER, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            String fullName = response.getString("fullName");
                            String balance = response.getString("balance");

                            Toast.makeText(MakeTmTransferActivity.this, "Transfer Successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MakeTmTransferActivity.this, MainActivity.class);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("balance", Double.parseDouble(balance));
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MakeTmTransferActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(MakeTmTransferActivity.this, jsonError, Toast.LENGTH_SHORT).show();
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

    public void confirmTransfer(View view) {
        tmTransfer();
    }
}
