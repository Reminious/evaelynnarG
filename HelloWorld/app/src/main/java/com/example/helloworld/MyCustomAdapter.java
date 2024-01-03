package com.example.helloworld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class MyCustomAdapter extends ArrayAdapter<Object> {
    private final Context context;
    protected String[] pics,captions;
    public MyCustomAdapter(Context context, String[] pics, String[] captions) {
        super(context, R.layout.row);
        this.context = context;
        this.pics = pics;
        this.captions = captions;
        addAll(new Object[pics.length]);
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view ==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row, parent, false);
        }
        ImageView imageView = view.findViewById(R.id.imageView);
        @SuppressLint("DiscouragedApi") int resId = context.getResources().getIdentifier(pics[position], "drawable", context.getPackageName());
        imageView.setImageResource(resId);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(captions[position]);
        return view;
    }
}
