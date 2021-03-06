package com.jhonghawan.wordpigeon;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import org.tensorflow.lite.Interpreter;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import static android.graphics.Color.BLACK;

public class Classify extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //Lang code for selected language from spinner
    private int langCode;

    // presets for rgb conversion
    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    // options for model interpreter
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    // tflite graph
    private Interpreter tflite;
    // holds all the possible labels for model
    private List<String> labelList;
    // holds the selected image data as bytes
    private ByteBuffer imgData = null;
    // holds the probabilities of each label for non-quantized graphs
    private float[][] labelProbArray = null;
    // holds the probabilities of each label for quantized graphs
    private byte[][] labelProbArrayB = null;
    // array that holds the labels with the highest probabilities
    private String[] topLabels = null;
    // array that holds the highest probabilities
    private String[] topConfidence = null;
    //map set with target language and source language words
    private String[] translations = null;

    // selected classifier information received from extras
    private String chosen;
    private boolean quant;

    // input image dimensions for the Inception Model
    private int DIM_IMG_SIZE_X = 299;
    private int DIM_IMG_SIZE_Y = 299;
    private int DIM_PIXEL_SIZE = 3;

    // used in the make translations method to access translations
    private int transIndex = RESULTS_TO_SHOW - 1;

    // int array to hold image data
    private int[] intValues;

    // activity elements
    private ImageView selected_image;
    private Button classify_button;
    private Button back_button;
    private Button translate;
    private Button print_button;
    private TextView label1;
    private TextView label2;
    private TextView label3;
    private TextView trans1;
    private TextView trans2;
    private TextView trans3;
    private TextView Confidence1;
    private TextView Confidence2;
    private TextView Confidence3;

    // priority queue that will hold the top results from the CNN
    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get all selected classifier data from classifiers
        chosen = (String) getIntent().getStringExtra("chosen");
        quant = (boolean) getIntent().getBooleanExtra("quant", false);

        // initialize array that holds image data
        intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        super.onCreate(savedInstanceState);

        //initilize graph and labels
        try{
            tflite = new Interpreter(loadModelFile(), tfliteOptions);
            labelList = loadLabelList();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        // initialize byte array. The size depends if the input data needs to be quantized or not
        if(quant){
            imgData =
                    ByteBuffer.allocateDirect(
                            DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        } else {
            imgData =
                    ByteBuffer.allocateDirect(
                            4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        }
        imgData.order(ByteOrder.nativeOrder());

        // initialize probabilities array. The datatypes that array holds depends if the input data needs to be quantized or not
        if(quant){
            labelProbArrayB= new byte[1][labelList.size()];
        } else {
            labelProbArray = new float[1][labelList.size()];
        }
        setContentView(R.layout.activity_home_screen);
        // on click for inception float model
        translate = (Button)findViewById(R.id.translate);
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // filename in assets

            }
        });
        setContentView(R.layout.activity_classify);
        Spinner spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    if (position == 1) {
                        langCode = FirebaseTranslateLanguage.ES;
                    } else if (position == 2) {
                        langCode = FirebaseTranslateLanguage.FR;
                    } else if (position == 3) {
                        langCode = FirebaseTranslateLanguage.DE;
                    } else if (position == 4) {
                        langCode = FirebaseTranslateLanguage.IT;
                    } else {
                        langCode = FirebaseTranslateLanguage.JA;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // labels that hold top three results of CNN
        label1 = (TextView) findViewById(R.id.label1);
        label2 = (TextView) findViewById(R.id.label2);
        label3 = (TextView) findViewById(R.id.label3);
        // displays the probabilities of top labels
        Confidence1 = (TextView) findViewById(R.id.Confidence1);
        Confidence2 = (TextView) findViewById(R.id.Confidence2);
        Confidence3 = (TextView) findViewById(R.id.Confidence3);
        // translated labels
        trans1 = (TextView) findViewById(R.id.trans1);
        trans2 = (TextView) findViewById(R.id.trans2);
        trans3 = (TextView) findViewById(R.id.trans3);
        // initialize imageView that displays selected image to the user
        selected_image = (ImageView) findViewById(R.id.selected_image);
        // initialize array to hold translations
        translations = new String[RESULTS_TO_SHOW];
        // initialize array to hold top labels
        topLabels = new String[RESULTS_TO_SHOW];
        // initialize array to hold top probabilities
        topConfidence = new String[RESULTS_TO_SHOW];

        // allows user to go back to activity to select a different image
        back_button = (Button)findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Classify.this, ChooseModel.class);
                startActivity(i);
            }
        });

        // classify current dispalyed image
        classify_button = (Button)findViewById(R.id.classify_image);
        classify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get current bitmap from imageView
                Bitmap bitmap_orig = ((BitmapDrawable)selected_image.getDrawable()).getBitmap();
                // resize the bitmap to the required input size to the CNN
                Bitmap bitmap = getResizedBitmap(bitmap_orig, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y);
                // convert bitmap to byte array
                convertBitmapToByteBuffer(bitmap);
                // pass byte data to the graph
                if(quant){
                    tflite.run(imgData, labelProbArrayB);
                } else {
                    tflite.run(imgData, labelProbArray);
                }

                // display the results
                printTopKLabels();
            }
        });

        //print button
        print_button = (Button)findViewById(R.id.print_button);
        print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrotherPrint brotherPrintManager = new BrotherPrint();
                // Create a list with the labels and their translated pairs.
                List<String> textToPrint = new ArrayList<>();
                for (int i = topLabels.length - 1; i >= 0; i--) {
                    textToPrint.add(topLabels[i]);
                    textToPrint.add(translations[i]);
                }
                Bitmap image = brotherPrintManager.textAsBitmap(textToPrint, 48, BLACK);
                brotherPrintManager.print(image);
            }
        });
        // get image from previous activity to show in the imageView
        Uri uri = (Uri)getIntent().getParcelableExtra("resID_uri");
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            selected_image.setImageBitmap(bitmap);
            // not sure why this happens, but without this the image appears on its side
            selected_image.setRotation(selected_image.getRotation() + 90);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // loads tflite grapg from file
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(chosen);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // converts bitmap to byte array which is passed in the tflite graph
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float
                if(quant){
                    imgData.put((byte) ((val >> 16) & 0xFF));
                    imgData.put((byte) ((val >> 8) & 0xFF));
                    imgData.put((byte) (val & 0xFF));
                } else {
                    imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                }

            }
        }
    }

    // loads the labels from the label txt file in assets into a string array
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    /**
     * TODO: Pass in the language code into this method as a variable.
     */
    // print the top labels and respective confidences
    private void printTopKLabels() {
        // add all results to priority queue
        for (int i = 0; i < labelList.size(); ++i) {
            if(quant){
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), (labelProbArrayB[0][i] & 0xff) / 255.0f));
            } else {
                sortedLabels.add(
                        new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            }
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        // get top results from priority queue
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            topLabels[i] = label.getKey();
            topConfidence[i] = String.format("%.0f%%",label.getValue()*100);
        }

        translateText(langCode); //CANT HAVE TRANSLATE TEXT BEFORE TOP LABELS MADE!!!!!

        // set the corresponding textviews with the results
        label1.setText("1. "+topLabels[2]);
        label2.setText("2. "+topLabels[1]);
        label3.setText("3. "+topLabels[0]);
        Confidence1.setText(topConfidence[2]);
        Confidence2.setText(topConfidence[1]);
        Confidence3.setText(topConfidence[0]);
        if (translations[0] != null) {
            trans1.setText("1. "+translations[2]);
            trans2.setText("2. "+translations[1]);
            trans3.setText("3. "+translations[0]);
        } else {
            trans1.setText("1. "+topLabels[2]);
            trans2.setText("2. "+topLabels[1]);
            trans3.setText("3. "+topLabels[0]);
        }

    }


    // resizes bitmap to given dimensions
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    protected FirebaseTranslatorOptions createTranslator(int source, int target) {
        return new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(source)
                .setTargetLanguage(target)
                .build();
    }

//    private void downloadModel(FirebaseTranslator translatorObj) {
//        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
//                .requireWifi()
//                .build();
//                translatorObj.downloadModelIfNeeded(conditions)
//                .addOnSuccessListener(
//                        new OnSuccessListener<String>() {
//                            @Override
//                            public void onSuccess(String s) {
//                                // Model downloaded successfully. Okay to start translating.
//                                // (Set a flag, unhide the translation UI, etc.)
//
//                                //display the translator
//                            }
//
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Model couldn’t be downloaded or other internal error.
//                                // ...
//                            }
//                        });
//    }

    private void translateText(int langCode) {
        FirebaseTranslatorOptions options = createTranslator(FirebaseTranslateLanguage.EN, langCode);

        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                .getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().
                requireWifi().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>(){
           @Override
           public void onSuccess(Void aVoid) {

               for (int i = topLabels.length - 1; i >= 0; i--) {
                   translator.translate(topLabels[i]).addOnSuccessListener(new OnSuccessListener<String>() {
                       @Override
                       public void onSuccess(String s) {
                           if (transIndex >= 0) {
                               translations[transIndex] = s;
                           }
                           transIndex--;
                       }
                   });
               }
               transIndex = RESULTS_TO_SHOW - 1;
           }

        })
        .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Model couldn’t be downloaded or other internal error.
                        // ...
                        System.out.println(e + "FAILED TO TRANSLATE");
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
