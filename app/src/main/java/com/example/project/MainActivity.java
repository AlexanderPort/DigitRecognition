package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.icu.text.Edits;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Interpreter tflite;
    Button saveButton;
    Button clearButton;
    Button recognizeButton;
    DrawableView drawableView;
    DatabaseHelper databaseHelper;
    TextView similarTextView;
    TextView predictionTextView;
    TextView probabilityTextView;
    EditText telephoneEditText;
    HashMap<Integer, String> labels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawableView = (DrawableView)findViewById(R.id.input);
        saveButton = (Button)findViewById(R.id.save);
        clearButton = (Button)findViewById(R.id.clear);
        telephoneEditText = (EditText)findViewById(R.id.phone);
        recognizeButton = (Button)findViewById(R.id.recognize);
        similarTextView = (TextView)findViewById(R.id.TheMostSimilar);
        predictionTextView = (TextView)findViewById(R.id.prediction);
        probabilityTextView = (TextView)findViewById(R.id.probability);
        saveButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        recognizeButton.setOnClickListener(this);
        databaseHelper = new DatabaseHelper(getBaseContext());
        initLabels();

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    private TensorBuffer getPrediction() {
        Bitmap bitmap = drawableView.bitmap;
        bitmap = drawableView.preprocess(bitmap);
        if (bitmap != null) {
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(32, 32, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(127.5f, -127.5f))
                    .build();
            TensorBuffer probabilityBuffer = TensorBuffer.
                    createFixedSize(new int[]{1, 47}, DataType.FLOAT32);
            TensorImage tensorImage = TensorImage.fromBitmap(bitmap);
            tensorImage = imageProcessor.process(tensorImage);
            if (tflite != null) {
                tflite.run(tensorImage.getBuffer(), probabilityBuffer.getBuffer());
            }
            return probabilityBuffer;
        }
        return null;
    }
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("model.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    private int argmax(float[] probabilities) {
        int argmax = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[argmax] < probabilities[i]) { argmax = i; }
        }
        return argmax;
    }

    public void onClick(View view) {
        int id = view.getId();
        System.out.println(id);
        if (id == R.id.clear) {
            drawableView.clear();
            drawableView.initBoundingBox();
        } else if (id == R.id.recognize) {
            TensorBuffer probabilityBuffer = getPrediction();
            if (probabilityBuffer != null) {
                float[] probabilities = probabilityBuffer.getFloatArray();
                int argmax = argmax(probabilities);
                String prediction = labels.get(argmax);
                String probability = String.valueOf(probabilities[argmax]);
                predictionTextView.setText("Label: " + prediction);
                probabilityTextView.setText("Confidence: " + probability);

                String telephoneNumber = "";
                Vector vector = new Vector(probabilities);
                HashMap<String, Vector> data = databaseHelper.onSelectAll();
                float maximum = Float.MIN_VALUE;
                for (String key : data.keySet()) {
                    float similarity = Vector.cosineSimilarity(vector, data.get(key));
                    if (similarity > maximum) {
                        maximum = similarity;
                        telephoneNumber = key;
                    }
                }
                similarTextView.setText(telephoneNumber);

            }
        } else if (id == R.id.save) {
            TensorBuffer probabilityBuffer = getPrediction();
            if (probabilityBuffer != null) {
                float[] probabilities = probabilityBuffer.getFloatArray();
                int argmax = argmax(probabilities);
                String prediction = labels.get(argmax);
                String probability = String.valueOf(probabilities[argmax]);
                String telephoneNumber = telephoneEditText.getText().toString();
                if (!telephoneNumber.equals("")) {
                    databaseHelper.onInsert(telephoneNumber, probabilities);
                }
            }
        }
    }
    void initLabels() {
        labels = new HashMap<>();
        labels.put(0, "0");
        labels.put(1, "1");
        labels.put(2, "2");
        labels.put(3, "3");
        labels.put(4, "4");
        labels.put(5, "5");
        labels.put(6, "6");
        labels.put(7, "7");
        labels.put(8, "8");
        labels.put(9, "9");
        labels.put(10, "A");
        labels.put(11, "B");
        labels.put(12, "C");
        labels.put(13, "D");
        labels.put(14, "E");
        labels.put(15, "F");
        labels.put(16, "G");
        labels.put(17, "H");
        labels.put(18, "I");
        labels.put(19, "J");
        labels.put(20, "K");
        labels.put(21, "L");
        labels.put(22, "M");
        labels.put(23, "N");
        labels.put(24, "O");
        labels.put(25, "P");
        labels.put(26, "Q");
        labels.put(27, "R");
        labels.put(28, "S");
        labels.put(29, "T");
        labels.put(30, "U");
        labels.put(31, "V");
        labels.put(32, "W");
        labels.put(33, "X");
        labels.put(34, "Y");
        labels.put(35, "Z");
        labels.put(36, "a");
        labels.put(37, "b");
        labels.put(38, "d");
        labels.put(39, "e");
        labels.put(40, "f");
        labels.put(41, "g");
        labels.put(42, "h");
        labels.put(43, "n");
        labels.put(44, "q");
        labels.put(45, "r");
        labels.put(46, "t");
    }
}