package ng.edu.polyibadan.moneytransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ng.edu.polyibadan.moneytransfer.App.utils.AppController;
import ng.edu.polyibadan.moneytransfer.App.utils.SessionManager;

public class SuccessTransferActivity extends AppCompatActivity {

    TextView fullName;
    TextView accountNumber;
    TextView amount;
    TextView bankName;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_transfer);

        fullName = findViewById(R.id.full_name);
        accountNumber = findViewById(R.id.account_number);
        amount = findViewById(R.id.amount);
        bankName = findViewById(R.id.bank_name);

        fullName.setText(getIntent().getStringExtra("accountName"));
        accountNumber.setText(getIntent().getStringExtra("accountNumber"));
        amount.setText(getIntent().getStringExtra("amount"));
        bankName.setText(getIntent().getStringExtra("bankName"));

        sessionManager = new SessionManager(getApplicationContext());
    }

    public void goToDashboard(View view) {
        Intent intent = new Intent(SuccessTransferActivity.this, MainActivity.class);
        intent.putExtra("fullName", fullName.getText());
        intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0) - Double.parseDouble(amount.getText().toString()));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SuccessTransferActivity.this, MainActivity.class);
        intent.putExtra("fullName", fullName.getText());
        intent.putExtra("balance", getIntent().getDoubleExtra("balance", 0.0) - Double.parseDouble(amount.getText().toString()));
        startActivity(intent);
    }
}
