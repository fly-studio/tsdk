package org.fly.tsdk.sdk.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.fly.tsdk.io.ResourceHelper;

public class SplashActivity extends Activity {
    private ImageView splashImage;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);

        setContentView(ResourceHelper.getId(this, "tsdk_splash", "layout"));
        splashImage = findViewById(ResourceHelper.getId(this, "tsdk_splash_image", "id"));
        splashImage.setImageResource(ResourceHelper.getId(this, getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_LANDSCAPE ? "tsdk_splash_landscape_image" : "tsdk_splash_portrait_image", "drawable"));

        handler.postDelayed(nextImageRunnable, 5000);
    }

    private Runnable nextImageRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(getPackageName());
            SplashActivity.this.startActivity(intent);
            SplashActivity.this.finish();
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



}
