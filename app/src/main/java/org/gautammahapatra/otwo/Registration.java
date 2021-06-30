package org.gautammahapatra.otwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class Registration extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        final TextInputEditText phone = findViewById(R.id.phone);
        final Button register = findViewById(R.id.register_btn);
        register.setOnClickListener(v -> {
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Editable phoneNumber = phone.getText();
            if (phoneNumber != null && phoneNumber.length() == 10) {
                editor.putString(getString(R.string.phone_pref_key), phoneNumber.toString());
                JSONObject data = new JSONObject();
                try {
                    data.put("phone_number", phoneNumber.toString());
                    RequestQueue queue = Volley.newRequestQueue(this);
                    String url = getString(R.string.api_url);
                    JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, data, response -> {
                        if (response.has("data")) {
                            try {
                                if (response.getJSONObject("data").has("vids")) {
                                    JSONArray vids = response.getJSONObject("data").getJSONArray("vids");
                                    Set<String> vids_pref = new HashSet<>();
                                    for (int i = 0; i < vids.length(); i++) {
                                        vids_pref.add(vids.getString(i));
                                    }
                                    editor.putStringSet(getString(R.string.pref_vids_key), vids_pref);
                                    editor.apply();
                                    Intent intent = new Intent(Registration.this, Dashboard.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error getting virtual ID", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error getting virtual ID", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, error -> Toast.makeText(getApplicationContext(), "Error getting virtual ID", Toast.LENGTH_LONG).show());
                    queue.add(req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Enter a valid phone number", Toast.LENGTH_LONG).show();
            }
            editor.apply();
        });


    }


}