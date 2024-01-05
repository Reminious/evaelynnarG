package com.example.helloworld;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ImageAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] imageUrls;

    public ImageAdapter(Context context, String[] imageUrls) {
        super(context, R.layout.row, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(R.layout.row, parent, false);
        }

        ImageView[] imageViews = new ImageView[4];
        imageViews[0] = row.findViewById(R.id.imageView0);
        imageViews[1] = row.findViewById(R.id.imageView1);
        imageViews[2] = row.findViewById(R.id.imageView2);
        imageViews[3] = row.findViewById(R.id.imageView3);

        for (int i = 0; i < imageViews.length; i++) {
            int imageIndex = position * 4 + i;
            if (imageIndex < imageUrls.length && imageUrls[imageIndex] != null && !imageUrls[imageIndex].isEmpty()) {
                GlideUrl glideUrl = new GlideUrl(imageUrls[imageIndex], new LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .build());
                Glide.with(context)
                        .load(glideUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(R.drawable.haha1)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                Log.e("Glide", "Load failed for " + model);
                                if (e != null) {
                                    e.logRootCauses("Glide");
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                Log.d("Glide", "Load success for " + model);
                                return false;
                            }
                        })
                        .into(imageViews[i]);
                imageViews[i].setVisibility(View.VISIBLE);
            } else {
                imageViews[i].setVisibility(View.INVISIBLE);
            }
        }
        return row;
    }
}
