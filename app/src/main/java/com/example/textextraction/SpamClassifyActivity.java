package com.example.textextraction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class SpamClassifyActivity extends AppCompatActivity {

    EditText editText;
    Button classify;
    TextView resultText;
//    private static final String MODEL_PATH = "model_spam.tflite";
//    private NLClassifier classifier;
    String text;

    private static final String MODEL_PATH = "text_classification_v2.tflite";
    private static final int SEQ_LENGTH = 128;
    private static final int NUM_CLASSES = 2;
    private Interpreter interpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam_classify);

        editText = findViewById(R.id.idEt);
        classify = findViewById(R.id.idBtn);
        resultText = findViewById(R.id.resultText);

        text = getIntent().getStringExtra("text");
        editText.setText(text);

        // Load the MobileBERT model.
        try {
            interpreter = new Interpreter(loadModelFile(getAssets(), MODEL_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the classify button click listener.
        classify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input text.
                String inputText = editText.getText().toString();

                // Preprocess the input text.
                int[] inputIds = preprocessInputText(inputText);

                // Run inference on the input text.
                float[][] outputScores = new float[8][2];
                interpreter.run(inputIds, outputScores);

                // Get the predicted sentiment.
                float positiveScore = outputScores[0][0];
                float negativeScore = outputScores[0][1];
                String predictedSentiment = positiveScore > negativeScore ? "Positive" : "Negative";

                // Display the predicted sentiment.
                String outputText = "Text Analysis: " + predictedSentiment + "\n" +
                        "Positive score: " + String.format("%.2f", positiveScore) + "\n" +
                        "Negative score: " + String.format("%.2f", negativeScore);
                resultText.setText(outputText);
            }
        });
    }

    private int[] preprocessInputText(String inputText) {
        // TODO: Implement input text preprocessing here.
        // Here, we will simply pad or truncate the input text to the SEQ_LENGTH.
        int[] inputIds = new int[SEQ_LENGTH];
        for (int i = 0; i < SEQ_LENGTH; i++) {
            if (i < inputText.length()) {
                inputIds[i] = (int) inputText.charAt(i);
            } else {
                inputIds[i] = 0;
            }
        }
        return inputIds;
    }

    private MappedByteBuffer loadModelFile(AssetManager assets, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}























//        getTitle();
//        try {
//            classifier = NLClassifier.createFromFile(this, MODEL_PATH);
//        } catch (IOException e) {
//            Log.e(SpamClassifyActivity.class.getSimpleName(), e.getMessage());
//        }
//
//        classify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                runClassification(editText.getText().toString().trim());
////                editText.getText().clear();
//                if(editText.getText().toString().isEmpty()){
//                    Toast.makeText(SpamClassifyActivity.this, "Enter text to classify", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        classifier.close();
//    }
//
//    protected void runClassification(String comment) {
//        List<Category> apiResults = classifier.classify(comment);
//        float score = apiResults.get(1).getScore();
//        if(!editText.getText().toString().trim().isEmpty()){
//            if (score > 0.8f) {
//                outputTextView().setText("Detected as spam.\nSpam score: " + score);
//            } else {
//                outputTextView().setText("Not detected as spam.\nSpam score: " + score);
//            }
//        }
//    }
//
//    protected TextView outputTextView() {
//        return resultText;
//    }
//}
