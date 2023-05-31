package com.example.securezip;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mysecureziplib.SecureZip;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    MaterialButton exportBtu;
    MaterialButton sendPasswordSmsBtu;
    private MaterialButton chooseFiles;
    private ArrayList<Uri> arrayListUri;
    private SecureZip secureZip;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();
        findViews();
        secureZip=new SecureZip(this);
      //secureZip.setACCOUNT_SID( "Your account_sid"); provide your Twilio account_sid
      //secureZip.setAUTH_TOKEN("Your token"); provide your Twilio token
        setClickListeners();



    }

    private void setClickListeners() {
        chooseFiles.setOnClickListener(v -> fileChooser());

        sendPasswordSmsBtu.setOnClickListener(v -> {
            if (secureZip.checkFileExists()) {
                PhoneNumberDialog phoneNumberDialog = new PhoneNumberDialog(MainActivity.this);
                phoneNumberDialog.setOnDismissListener(dialog -> {
                    String phoneNumber = phoneNumberDialog.getPhoneNumber();
                    secureZip.sendPasswordBySms(phoneNumber);
                });
                phoneNumberDialog.show();
            } else
                Toast.makeText(this, "Please choose files and create zip first", Toast.LENGTH_LONG).show();

        });

        exportBtu.setOnClickListener(v -> {
            if (secureZip.checkFileExists()) {
                    secureZip.sendCompressedFile("My secured zip file");
            }

        });

    }

    private void fileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Set the MIME type to allow all file types
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // Allow multiple file selection
        someActivityResultLauncher.launch(intent);

    }
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        // Retrieve the selected files

                        ClipData clipData = data.getClipData();
                        arrayListUri=new ArrayList<>();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                // Process the selected file URI (e.g., create a zip file)
                                arrayListUri.add(uri);
                            }


                        }else
                            arrayListUri.add(data.getData());
                        try {
                            secureZip.createZipWithPassword(arrayListUri);
                            Toast.makeText(context,"File created successfully",Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void findViews() {
        exportBtu=findViewById(R.id.export_btu);
        sendPasswordSmsBtu=findViewById(R.id.sendSms_btu);
        chooseFiles =findViewById(R.id.createZip_btu);
    }




}