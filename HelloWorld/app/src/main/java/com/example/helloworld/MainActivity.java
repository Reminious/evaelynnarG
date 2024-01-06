package com.example.helloworld;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private ListView listView;
    private ImageAdapter adapter;
    private String[] imageArray = new String[20];
    private ProgressBar progressBar;
    private boolean isDownloading = false;//not sure if this is still needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        findViewById(R.id.downloadButton).setEnabled(false);//disable download button until 6 images are selected
        progressBar = findViewById(R.id.progressBar);
        adapter = new ImageAdapter(this, imageArray);
        listView = findViewById(R.id.listView);
        EditText editText = findViewById(R.id.urlInput);
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        findViewById(R.id.loadButton).setOnClickListener(new View.OnClickListener() {//display images when load button is clicked
            @Override
            public void onClick(View v) {
                String url = editText.getText().toString();
                executor.execute(() -> {
                    if (isValidUrl(url)) {
                        handler.post(() -> displayImageTask(url));
                    } else {
                        handler.post(() -> Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
        findViewById(R.id.downloadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();//ask for permission to save images
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isValidUrl(String url) {//check if url is valid
        HttpURLConnection connection = null;
        try {
            if(!url.startsWith("https")) {
                return false;
            }
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");//get header
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            //add header to prevent 403 error
            connection.connect();
            return (200<= connection.getResponseCode() && connection.getResponseCode() <= 399);//check if response code is between 200 and 399
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void displayImageTask(String url) {
        executor.execute(() -> {
            List<String> imageUrls = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(url).get();
                Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");//select images with png, jpg, jpeg, or gif extension
                for (Element image : images) {
                    imageUrls.add(image.absUrl("src"));//get absolute url
                    if (imageUrls.size() >= 20) {//only load 20 images
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                for (int i = 0; i < imageUrls.size() && i < imageArray.length; i++) {
                    imageArray[i] = imageUrls.get(i);
                }
                adapter.notifyDataSetChanged();//notify adapter that data has changed
                if (imageUrls.size() < 6) {
                    Toast.makeText(this, "Less than 6 images loaded", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void checkAndRequestPermissions() {//check if permission is granted, if not, ask for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            saveSelectedImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveSelectedImages();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }//if permission is granted, save images

    private void saveSelectedImages() {
        String[] selectedImages = adapter.getSelectedImages();
        int total = selectedImages.length;
        AtomicInteger completeDownloads = new AtomicInteger(0);//count number of images downloaded, AtomicInteger is used because it is thread safe
        isDownloading = true;
        runOnUiThread(() -> {
            findViewById(R.id.downloadButton).setAlpha(0.5f);
            progressBar.setVisibility(View.VISIBLE);//show progress bar
            progressBar.setMax(total);
            progressBar.setProgress(0);
        });
        for (String imageUrl : selectedImages) {
            executor.execute(() -> {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//set input to true, so that we can read from the connection
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    connection.connect();
                    InputStream input = connection.getInputStream();//get input stream from connection, which contains the image
                    Bitmap bitmap = BitmapFactory.decodeStream(input);//decode input stream into bitmap
                    saveImageToMediaStore(bitmap);//save image to media store, so that it can be viewed in gallery
                    input.close();
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(() -> {
                        progressBar.incrementProgressBy(1);//increment progress bar by 1
                        if (completeDownloads.incrementAndGet() == total) {
                            Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(() -> {
                                progressBar.setVisibility(View.INVISIBLE);//hide progress bar after 2 seconds
                                findViewById(R.id.downloadButton).setAlpha(1.0f);
                                adapter.clearSelections();//clear selections, so that user can download again
                                isDownloading = false;
                                adapter.setDownloading(false);
                            }, 2000);
                        }
                    });
                }
            });
        }
    }

    private void saveImageToMediaStore(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            if (uri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                if (outputStream != null) {
                    outputStream.close();
                }
                MediaScannerConnection.scanFile(this, new String[]{uri.getPath()}, new String[]{"image/jpeg"}, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDownloadButton(boolean isEnabled) {
        findViewById(R.id.downloadButton).setEnabled(isEnabled);
    }
}