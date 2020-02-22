package demo;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.fly.tsdk.R;
import org.fly.tsdk.sdk.TsdkApi;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TsdkApi.getInstance().bindMainActivity(this);
    }
}
