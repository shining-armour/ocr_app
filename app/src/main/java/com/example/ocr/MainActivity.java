package com.example.ocr;

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

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button button1;
    TextView textView;
    public static final int REQUEST_CODE_1=101;
    public static final int REQUEST_CODE_2=102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        button1=findViewById(R.id.button1);
        textView=findViewById(R.id.textView);
    }

    public void captureImage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null)
            startActivityForResult(intent,REQUEST_CODE_1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_1){
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            imageView.setImageBitmap(bitmap);

            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVision firebaseVision = FirebaseVision.getInstance();
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

            task.addOnSuccessListener((new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                   textView.setText(firebaseVisionText.getText());
                }
            }));

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "OCR failed. Retry once more", Toast.LENGTH_LONG).show();
                }
            });

        }

        if (requestCode==REQUEST_CODE_2){
            imageView.setImageURI(data.getData());
            FirebaseVisionImage firebaseVisionImage = null;
            try{
              firebaseVisionImage = FirebaseVisionImage.fromFilePath(getApplicationContext(),data.getData()) ;
            }
            catch(Exception e){

            }
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener((new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    textView.setText(firebaseVisionText.getText());
                }
            })).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "OCR failed. Retry once more", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    public void pickImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (intent.resolveActivity(getPackageManager())!=null)
            startActivityForResult(intent,REQUEST_CODE_2);
    }
}