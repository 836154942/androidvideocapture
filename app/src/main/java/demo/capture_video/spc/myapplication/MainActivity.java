package demo.capture_video.spc.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.locks.ReadWriteLock;

import demo.capture_video.spc.myapplication.activity.VideoCaptureActivity;

public class MainActivity extends AppCompatActivity {

    public static final int REWQUEST_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.get_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(VideoCaptureActivity.buildIntent(MainActivity.this), REWQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            String uri = data.getStringExtra(VideoCaptureActivity.VIDEOPATH);
            String coveruri = data.getStringExtra(VideoCaptureActivity.VIDEOCOVER);

            Toast.makeText(MainActivity.this, "视频路径是" + uri + "封面路径是" + coveruri, Toast.LENGTH_LONG).show();
        }

    }
}
