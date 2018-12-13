package com.amitsharma.action.collegeforum;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class DisplayImageActivity extends AppCompatActivity {
    PhotoView photoView;
    boolean doubleTap=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_display_image);

        photoView = (PhotoView) findViewById(R.id.photo_view);
        String image_url=getIntent().getStringExtra("image_url");
        Toast.makeText(DisplayImageActivity.this, "Loading image please wait!", Toast.LENGTH_SHORT).show();

        Picasso.get().load(image_url).into(photoView);
    }

    @Override
    public void onBackPressed() {
        this.finish();
            super.onBackPressed();

    }
}
