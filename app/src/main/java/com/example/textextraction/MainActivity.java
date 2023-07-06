package com.example.textextraction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;
import java.util.PrimitiveIterator;

public class MainActivity extends AppCompatActivity {

    private MaterialButton classifyBtn;
    private MaterialButton recognizeTextBtn;
    private ImageView inputImageBtn;
    private ShapeableImageView imageIv;
    private EditText recognizedTexEdt;
    FloatingActionButton floatingActionButton;

    //Tag
    private static final String TAG = "MAIN_TAG";
    private Uri imageUri;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=101;
    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        inputImageBtn = findViewById(R.id.inputImageBtn);
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn);
        imageIv = findViewById(R.id.imageIv);
        recognizedTexEdt = findViewById(R.id.recognizedTexEdt);
        floatingActionButton = findViewById(R.id.LanguageBtn);
        classifyBtn = findViewById(R.id.classifyBtn);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        classifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, SpamClassifyActivity.class);
               intent.putExtra("text", recognizedTexEdt.getText().toString());
               startActivity(intent);
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LanguageConverter.class);
                startActivity(intent);
            }
        });

        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputImageDialog();
            }
        });

        recognizeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri==null){
                    Toast.makeText(MainActivity.this, "Pick Image First", Toast.LENGTH_SHORT).show();
                }
                else{
                    recognizeTextFromImage();
                }
            }
        });

    }

    private void recognizeTextFromImage() {
        Log.d(TAG,"recognizeTextFromImage: ");
        progressDialog.setMessage("Processing Image....");
        progressDialog.show();

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageIv.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()){
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder sb = new StringBuilder();

            for (int i =0 ;i<items.size();i++){
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append("\n");
            }
            recognizedTexEdt.setText(sb.toString());
            if(recognizedTexEdt.getText().toString().trim().isEmpty()){
                Toast.makeText(MainActivity.this, "Text not recognized", Toast.LENGTH_SHORT).show();
            }
        }
        progressDialog.dismiss();
    }

    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, inputImageBtn);

        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");

        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id==1){
                    if(checkCameraPermission()){
                        pickImageCamera();
                    }
                    else{
                        requestCameraPermissions();
                    }
                }
                else if(id==2){
                    if(checkStoragePermission()){
                        pickImageGallery();
                    }
                    else{
                        requestStoragePermission();
                    }
                }
                return true;
            }
        });
    }



    private void pickImageGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

//    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if(result.getResultCode()== Activity.RESULT_OK){
//                        //image picked
//                        Intent data = result.getData();
//                        imageUri = data.getData();
//                        imageIv.setImageURI(imageUri);
//                    }
//                    else{
//                        //cancelled
//                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//    );

    private void pickImageCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

//    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if(result.getResultCode()==Activity.RESULT_OK){
//
//                        imageIv.setImageURI(imageUri);
//                    }
//                    else {
//                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//    );

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== (PackageManager.PERMISSION_GRANTED);
        return cameraResult && storageResult;
    }

    private  void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickImageCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera and Storage Permissions are Required", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickImageGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                imageUri = result.getUri();
                imageIv.setImageURI(imageUri);
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}