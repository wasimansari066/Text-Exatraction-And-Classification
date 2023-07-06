package com.example.textextraction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageConverter extends AppCompatActivity {

    private EditText sourcelanguageEt;
    private TextView destlangaugeTv;
    private Button sourceBtn, destinationBtn, translateBtn;

    private TranslatorOptions translatorOptions;
    private Translator translator;

    private ProgressDialog progressDialog;
    private ArrayList<ModelLanguage> languageArrayList;

    private static final String TAG ="MAIN_TAG";

    private String sourcelanguageCode = "en";
    private String sourceLanguagetitle = "English";
    private String destinationLanguageCode = "ur";
    private String destinationLanguageTitle = "Urdu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_converter);

        getSupportActionBar().hide();

        sourcelanguageEt = findViewById(R.id.sourceEt);
        destlangaugeTv = findViewById(R.id.destinationTv);
        sourceBtn = findViewById(R.id.source);
        destinationBtn = findViewById(R.id.destination);
        translateBtn = findViewById(R.id.translate);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait....");
        progressDialog.setCanceledOnTouchOutside(false);

        loadAvailableLanguage();

        sourceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sourceLanguageChoose();
            }
        });

        destinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destinationLanguagechoose();
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String sourceLanguageText="";
    private void validateData() {
        sourceLanguageText=sourcelanguageEt.getText().toString().trim();
        Log.d(TAG, "validateData: sourceLanguageText: "+sourceLanguageText);

        if (sourceLanguageText.isEmpty()){
            Toast.makeText(this, "enter text to translate", Toast.LENGTH_SHORT).show();
        }
        else {
            startTranslations();
        }
    }

    private void startTranslations() {
        progressDialog.setMessage("processing language model....");
        progressDialog.show();
        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(sourcelanguageCode)
                .setTargetLanguage(destinationLanguageCode)
                .build();
        translator = Translation.getClient(translatorOptions);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: model ready, start translating...");
                        progressDialog.setMessage("translating....");

                        translator.translate(sourceLanguageText)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String translatedText) {
                                        Log.d(TAG, "onSuccess: translatedText: "+translatedText);

                                        progressDialog.dismiss();

                                        destlangaugeTv.setText(translatedText);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.d(TAG, "onFailure: ", e);
                                        Toast.makeText(LanguageConverter.this, "Failed to translate due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: ", e);
                        Toast.makeText(LanguageConverter.this, "failed to ready model due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sourceLanguageChoose(){
        PopupMenu popupMenu = new PopupMenu(this,sourceBtn);

        for (int i = 0; i <languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE,i,i, languageArrayList.get(i).languageTitle);
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int position = menuItem.getItemId();

                sourcelanguageCode = languageArrayList.get(position).languageCode;
                sourceLanguagetitle = languageArrayList.get(position).languageTitle;

                sourceBtn.setText(sourceLanguagetitle);
//                sourcelanguageEt.setmint();

                Log.d(TAG, "onMenuItemClick: sourcelanguageCode: "+sourcelanguageCode);
                Log.d(TAG, "onMenuItemClick: sourcelanguageTitle: "+sourceLanguagetitle);
                return false;
            }
        });
    }

    private void destinationLanguagechoose(){
        PopupMenu popupMenu = new PopupMenu(this,destinationBtn);

        for (int i = 0; i <languageArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE,i,i, languageArrayList.get(i).getLanguageTitle());
        }

        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int position = menuItem.getItemId();

                destinationLanguageCode = languageArrayList.get(position).languageCode;
                destinationLanguageTitle = languageArrayList.get(position).languageTitle;

                destinationBtn.setText(destinationLanguageTitle);
//                sourcelanguageEt.setmint();

                Log.d(TAG, "onMenuItemClick: destinationLanguageCode: "+destinationLanguageCode);
                Log.d(TAG, "onMenuItemClick: destinationLanguageTitle: "+destinationLanguageTitle);
                return false;
            }
        });
    }

    private void loadAvailableLanguage() {
        languageArrayList = new ArrayList<>();

        List<String> languageCodelist = TranslateLanguage.getAllLanguages();

        for(String languageCode: languageCodelist){
            String languageTitle = new Locale(languageCode).getDisplayLanguage();
            Log.d(TAG,"loadAvailableLanguage: languageCode: "+languageCode);
            Log.d(TAG, "loadAvailableLanguage: languageTitle: "+languageTitle);
            ModelLanguage modelLanguage = new ModelLanguage(languageCode,languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }
}