package com.miniproject.watermarkgenerator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.miniproject.watermarkgenerator.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView imgGallery;
    TextInputLayout inpwatermark;
    Button downloadBtn;
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;
    Button btnAdd,btnAddinvis;


    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            int result = activityResult.getResultCode();
                            Intent data = activityResult.getData();
                            if (result == RESULT_OK) {
                                imgGallery.setImageURI(data.getData());
                            }
                        }
                    }
            );


    //add image

    //    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgGallery = findViewById(R.id.imgGallery);
        Button btnGallery = findViewById(R.id.btnImagePicker);
        downloadBtn = findViewById(R.id.downloadBtn);

        btnGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent imgGallery = new Intent(Intent.ACTION_PICK);
                imgGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(imgGallery, GALLERY_REQ_CODE);
                activityResultLauncher.launch(imgGallery);

            }
        });


        //add watermark
        btnAdd = findViewById(R.id.btnAdd);
        inpwatermark = findViewById(R.id.inputIcon);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BitmapDrawable drawable = (BitmapDrawable) imgGallery.getDrawable();
                final Bitmap[] bitmap = {drawable.getBitmap()};

                // Create a mutable copy of the Bitmap object
                Bitmap mutableBitmap = bitmap[0].copy(Bitmap.Config.ARGB_8888, true);

                // Create a new Canvas object to draw on the mutable Bitmap
                Canvas canvas = new Canvas(mutableBitmap);

                // Define the text to be added to the image
                String text = inpwatermark.getEditText().getText().toString();

                // Define the font size and color
                int fontSize = 60;
                int fontColor = Color.WHITE;

                // Get the dimensions of the image
                int imageWidth = mutableBitmap.getWidth();
                int imageHeight = mutableBitmap.getHeight();

// Get the dimensions of the text
                Rect textBounds = new Rect();
                Paint paint = new Paint();
                paint.setTextSize(fontSize);
                paint.setColor(fontColor);
                paint.setTextAlign(Paint.Align.CENTER); // add this line
                paint.setAlpha(128);

                paint.getTextBounds(text, 0, text.length(), textBounds);
                int textWidth = textBounds.width();
                int textHeight = textBounds.height();

// Calculate the position of the text
                float x = (imageWidth - textWidth) / 2.0f;
                float y = (imageHeight + textHeight) / 2.0f;

                // Rotate the canvas
                //     canvas.rotate(45, x, y);

                // Translate the canvas to center the text
                //    canvas.translate(x, y);

                // Rotate the text along with the canvas
                //      canvas.rotate(-45, textWidth/2f, textHeight/2f);


                // Create a new Bitmap object for the watermark
                Bitmap watermarkBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                Canvas watermarkCanvas = new Canvas(watermarkBitmap);
                watermarkCanvas.drawText(text, x, y, paint);

                // Draw the original image and the watermark on a new canvas

                Bitmap resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                Canvas resultCanvas = new Canvas(resultBitmap);
                resultCanvas.drawBitmap(mutableBitmap, 0, 0, null);
                resultCanvas.drawBitmap(watermarkBitmap, 0, 0, null);


                // Set the new Bitmap as the image on imgGallery
                imgGallery.setImageBitmap(resultBitmap);


                Toast.makeText(MainActivity.this, "WaterMark Added!!", Toast.LENGTH_SHORT).show();


                imgGallery.setOnTouchListener(new View.OnTouchListener() {
                    float startX, startY, textX, textY;
                    boolean isMoving = false;


                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getX();
                                startY = event.getY();
                                textX = x;
                                textY = y;
                                isMoving = true;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (isMoving) {
                                    float dx = event.getX() - startX;
                                    float dy = event.getY() - startY;
                                    float newX = textX + dx;
                                    float newY = textY + dy;
                                    // Clear the previous watermark
                                    watermarkCanvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);

                                    watermarkCanvas.drawText(text, newX, newY, paint);
                                    resultCanvas.drawBitmap(mutableBitmap, 0, 0, null);
                                    resultCanvas.drawBitmap(watermarkBitmap, 0, 0, null);
                                    resultCanvas.drawBitmap(watermarkBitmap, 0, 0, paint);
                                    imgGallery.setImageBitmap(resultBitmap);


                                    break;
                                }
                        }
                        return true;
                    }
                });


//add watermark
                btnAddinvis=findViewById(R.id.btnAddinvisible);
                inpwatermark=findViewById(R.id.inputIcon);
                btnAddinvis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        BitmapDrawable drawable = (BitmapDrawable) imgGallery.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();

                        // Create a mutable copy of the Bitmap object
                        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        // Create a new Canvas object to draw on the mutable Bitmap
                        Canvas canvas = new Canvas(mutableBitmap);

                        // Define the text to be added to the image
                        String text = inpwatermark.getEditText().getText().toString();

                        // Define the font size and color
                        int fontSize = 50;
                        int fontColor = Color.TRANSPARENT;

                        // Get the dimensions of the image
                        int imageWidth = mutableBitmap.getWidth();
                        int imageHeight = mutableBitmap.getHeight();

// Get the dimensions of the text
                        Rect textBounds = new Rect();
                        Paint paint = new Paint();
                        paint.setTextSize(fontSize);
                        paint.setColor(fontColor);
//                      paint.setTextAlign(Paint.Align.LEFT); // add this line

                        paint.getTextBounds(text, 0, text.length(), textBounds);
                        int textWidth = textBounds.width();
                        int textHeight = textBounds.height();

// Calculate the position of the text
                        int a = (imageWidth - textWidth) / 2;
                        int b =  imageHeight;

// Draw the text on the image






                        canvas.drawText(text, a,b , paint);
//                        canvas.drawText(text, textBounds.bottom, textBounds.right, paint);

                        imgGallery.setImageBitmap(mutableBitmap);

                        Toast.makeText(MainActivity.this, "Watermark added Successfully", Toast.LENGTH_SHORT).show();

                    }
                });


//Download


                downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        bitmapDrawable = (BitmapDrawable) imgGallery.getDrawable();
                        bitmap[0] = bitmapDrawable.getBitmap();
                        FileOutputStream fileOutputStream = null;
                        File sdCard = Environment.getExternalStorageDirectory();
                        File Directory = new File(sdCard.getAbsolutePath() + "/Download");
                        Directory.mkdir();
                        String filename = String.format("%d.jpg", System.currentTimeMillis());
                        File outFile = new File(Directory, filename);
                        Toast.makeText(MainActivity.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                        try {
                            fileOutputStream = new FileOutputStream(outFile);
                            bitmap[0].compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();

                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(outFile));
                            sendBroadcast(intent);


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });}


}
