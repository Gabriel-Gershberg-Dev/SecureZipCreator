package com.example.mysecureziplib;

import static android.util.Base64.NO_WRAP;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;


import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SecureZip {

    private Context context;
    private ZipFile zipFile;
    private String zipPassword;
    private ArrayList<String> allowedPhoneNumbers;
    private boolean allowedToSeePass;
    public static final String ACCOUNT_SID = "AC74810519b11fec1fa8efa7152fb04cf6";
    public static final String AUTH_TOKEN = "c3ef4f5fd543236110df440a4c3d0c13";
    private String zipFileName;

    public SecureZip(Context context) {
        this.context = context;
        allowedPhoneNumbers = new ArrayList<>();
        zipFileName = "CompressedZip";
    }


    public void createZipWithPassword(List<Uri> urisToAdd) throws IOException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        String password = generatePassword();
        String filePath = "/data/user/0/com.example.securezip/files/" + zipFileName + ".zip";
        File checkIfExists = new File(filePath);
        if (checkIfExists.exists())
            checkIfExists.delete();

        zipFile = new ZipFile(filePath, password.toCharArray());
        List<File> filesToAdd = new ArrayList<>();


        try {
            // Iterate through the selected file URIs
            for (Uri fileUri : urisToAdd) {
                // Create a temporary file for each selected file
                File tempFile = createCopyFileFromUri(fileUri);
                if (tempFile != null) {
                    // Add the temporary file to the list
                    filesToAdd.add(tempFile);

                    ;
                }
                zipFile.addFiles(filesToAdd, zipParameters);
                tempFile.delete();
                this.zipPassword = password;

            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    private File createCopyFileFromUri(Uri uri) throws IOException {
        // Get the file name and extension from the URI
        String fileName = getFileNameFromUri(uri);

        // Create a file in the app's files directory with the same name and extension
        File outputFile = new File(context.getFilesDir(), fileName);

        // Copy the content from the Uri to the output file
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        return outputFile;
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }


    public void sendFile(File file, String messageTitle) {
        String textToMail = "Attached File:\n";
        Uri fileUri = FileProvider.getUriForFile(context, "com.example.securezip.fileprovider", file);
        final Intent messageIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            messageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        messageIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        messageIntent.putExtra(Intent.EXTRA_TEXT, textToMail + "\n\n\nSecuredZip");
        messageIntent.putExtra(Intent.EXTRA_CC, new String[]{""});
        messageIntent.setData(Uri.parse("mailto:")); // or just "mailto:" for blank
        messageIntent.setType("text/html");
        context.startActivity(Intent.createChooser(messageIntent, messageTitle));


    }

    public static String generatePassword() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z') // Set the range from '0' to 'z'
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS) // Include letters and digits
                .build();

        return generator.generate(5);
    }

    public boolean checkFileExists(){
        return zipFile!=null;
    }
    public void sendPasswordBySms(String phoneNumber) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + ACCOUNT_SID + "/Messages";

        String base64EncodedCredentials = "Basic " + Base64.encodeToString((ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("From", "+13156233129") // Replace with your Twilio phone number
                .add("To", phoneNumber)
                .add("Body", "Your secret password is: " + zipPassword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", base64EncodedCredentials)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("SMS sent successfully!");
                    // Handle successful response
                } else {
                    System.out.println("Failed to send SMS: " + response.code() + " - " + response.message());
                    // Handle unsuccessful response
                }
                response.close();
            }
        });

    }


    public void sendCompressedFile(String messageTitle) {
        sendFile(zipFile.getFile(), messageTitle);
    }

    public void setZipFileName(String fileName) {
        this.zipFileName = fileName;
    }

    public void addAllowedPhoneNumber(String phoneNumber) {
        allowedPhoneNumbers.add(phoneNumber);
    }

    public void setNewPassword(String zipPassword) {
        this.zipPassword = zipPassword;
    }

}
