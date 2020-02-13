package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.Map;

import ng.edu.polyibadan.moneytransfer.App.utils.AppConfig;
import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SessionManager sessionManager;
    TextView accountText;
    TextView amount;
    private ProgressBar progressBar;

    String fullName;
    double balance;

    String fullName2;
    double balance2;
    String emailAddress2;
    ImageView normalTransfer;
    ImageView tmTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountText = findViewById(R.id.account_text);
        amount = findViewById(R.id.amount);
        progressBar = findViewById(R.id.progressBar);
        sessionManager = new SessionManager(getApplicationContext());
        normalTransfer = findViewById(R.id.normal_transfer_button);
        tmTransfer = findViewById(R.id.tm_transfer_button);

        if (!sessionManager.isLoggedIn()) {
            backToLogin();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("fullName")) {
                fullName = extras.getString("fullName", "");
                accountText.setText(String.format("Welcome, %s", fullName));
            }
            if (extras.containsKey("balance")) {
                balance = extras.getDouble("balance", 0.0);
                amount.setText(String.format("₦%s0", balance));
            }
        } else if (sessionManager.getToken().equals("")) {
            backToLogin();
        }else {
            getMyInfo();
        }

        normalTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = getIntent().getExtras();
                Intent intent = new Intent(MainActivity.this, TransferMoneyActivity.class);
                if (extras != null) {
                    intent.putExtra("fullName", getIntent().getStringExtra("fullName"));
                    intent.putExtra("emailAddress", getIntent().getStringExtra("emailAddress"));
                    intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0));
                }else {
                    intent.putExtra("fullName", fullName2);
                    intent.putExtra("emailAddress", emailAddress2);
                    intent.putExtra("balance", balance2);
                }
                startActivity(intent);
            }
        });
        tmTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TmTransferActivity.class));
            }
        });
    }

    private void backToLogin() {
        sessionManager.setLogin(false);

        // Launching the Login Activity
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void getMyInfo() {
        // Tag used to cancel the request
        String tag_string_req = "req_user_info";

        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, AppConfig.URL_ME, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        progressBar.setVisibility(View.GONE);

                        try {
                            fullName2 = response.getString("fullName");
                            balance2 = response.getDouble("balance");
                            emailAddress2 =  response.getString("emailAddress");

                            accountText.setText(String.format("Welcome, %s", fullName2));
                            amount.setText(String.format("₦%s0", balance2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    Toast.makeText(MainActivity.this, jsonError, Toast.LENGTH_SHORT).show();
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

    public void fundAccount(View view) {
        Bundle extras = getIntent().getExtras();
        Intent intent = new Intent(MainActivity.this, FundAccountActivity.class);
        if (extras != null) {
            intent.putExtra("fullName", getIntent().getStringExtra("fullName"));
            intent.putExtra("emailAddress", getIntent().getStringExtra("emailAddress"));
        }else {
            intent.putExtra("fullName", fullName2);
            intent.putExtra("emailAddress", emailAddress2);
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    public void refresh(View view) {
        getMyInfo();
    }

    private void logoutUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Logout?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sessionManager.setLogin(false);

                // Launching the Login Activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.transparent));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    public void logout(View view) {
        logoutUser();
    }
}
