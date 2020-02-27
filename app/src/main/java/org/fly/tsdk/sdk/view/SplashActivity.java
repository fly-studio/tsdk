package org.fly.tsdk.sdk.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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

        setContentView(ResourceHelper.getId(this, "tsdk_splash", ResourceHelper.DefType.LAYOUT));
        splashImage = findViewById(ResourceHelper.getId(this, "tsdk_splash_image", ResourceHelper.DefType.ID));
        splashImage.setImageResource(ResourceHelper.getId(this, getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "tsdk_splash_landscape_image" : "tsdk_splash_portrait_image", ResourceHelper.DefType.DRAWABLE));

        handler.postDelayed(nextImageRunnable, 2000);
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
