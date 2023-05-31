package com.example.securezip;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class PhoneNumberDialog extends Dialog {
    private TextView phoneNumberTextView;
    private EditText phoneNumberEditText;
    private Button continueButton;
    private String phoneNumber;

    public PhoneNumberDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_dialog);

        // Bind views
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        continueButton = findViewById(R.id.continueButton);

        // Set click listener for the Continue button
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the phone number entered by the user
                phoneNumber = phoneNumberEditText.getText().toString();

                // Dismiss the dialog
                dismiss();
            }
        });
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}