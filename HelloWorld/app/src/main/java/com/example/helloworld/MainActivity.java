package com.example.helloworld;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private Executor executor= Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private ListView listView;
    private ImageAdapter adapter;
    private String[] imageArray = new String[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        adapter = new ImageAdapter(this, imageArray);
        listView = findViewById(R.id.listView);
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        findViewById(R.id.loadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImageTask("https://stocksnap.io/");
                //https://stocksnap.io/
                //https://www.istockphoto.com/photos/singapore/
                //https://pixabay.com/
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void displayImageTask(String url) {
        executor.execute(() -> {
            List<String> imageUrls = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(url).get();
                //Log.d("wtf", "DownloadImageTask: " + doc.title());
                Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
                for (Element image : images) {
                    imageUrls.add(image.absUrl("src"));
                    if (imageUrls.size() >= 20) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                for (int i = 0; i < imageUrls.size() && i<imageArray.length; i++) {
                    imageArray[i] = imageUrls.get(i);
                }
                adapter.notifyDataSetChanged();
            });
        });
    }
}