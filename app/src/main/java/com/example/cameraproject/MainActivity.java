package com.example.cameraproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final int CAMERA_REQ_CODE = 100;
    ImageView imgCamera;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgCamera = findViewById(R.id.imgCamera);
        Button btnCamera = findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(iCamera, CAMERA_REQ_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQ_CODE) {
                // Retrieve the captured image and display it in the ImageView
                Bitmap img = (Bitmap) data.getExtras().get("data");
                imgCamera.setImageBitmap(img);

                // Initialize the 'bitmap' variable with the captured image
                bitmap = img;

                // Process the image
                processImage();
            }
        }
    }

    // Function to decrypt text using the provided mapping
    public static String decryptText(String text) {
        // Define the mapping as two lists with the same length
        char[] originalLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] substitutionLetters = "bkflmnophqrsactiugejdvwxyz".toCharArray();

        // Create a mapping dictionary
        Map<Character, Character> mapping = new HashMap<>();
        for (int i = 0; i < originalLetters.length; i++) {
            mapping.put(originalLetters[i], substitutionLetters[i]);
        }

        // Decrypt the text
        StringBuilder decryptedText = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (mapping.containsKey(ch)) {
                decryptedText.append(mapping.get(ch));
            } else {
                decryptedText.append(ch);
            }
        }
        return decryptedText.toString();
    }

    private void processImage() {
        if (bitmap != null) {
            // Create a FirebaseVisionImage object from the initialized 'bitmap' object
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

            // Get an instance of FirebaseVision
            FirebaseVision firebaseVision = FirebaseVision.getInstance();

            // Create an instance of FirebaseVisionTextRecognizer
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

            // Create a task to process the image
            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

            // Handle the task success and failure
            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    String recognizedText = firebaseVisionText.getText();

                    // Decrypt the recognized text
                    String decryptedText = decryptText(recognizedText);

                    // Display the recognized and decrypted text in your TextView or wherever you want
                    // Make sure you have a TextView with the id 'textView' in your layout
                    TextView textView = findViewById(R.id.textView);
                    textView.setText("Recognized Text: " + recognizedText + "\nDecrypted Text: " + decryptedText);
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
