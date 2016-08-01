package demo.capture_video.spc.myapplication.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import demo.capture_video.spc.myapplication.util.Player;
import demo.capture_video.spc.myapplication.R;
import demo.capture_video.spc.myapplication.util.VideoThumbnailUtils;

public class VideoSelectPreviewAcitivity extends Activity implements View.OnClickListener {

    public static final String VIDEOPATH = "videopath";
    private String mViewoPath;
    private SurfaceView mSurfaceView;
    private TextView mCancle, mSelect;
    private ImageView mPlayView, mPreview;
    private Player mPlayer;
    private Uri mUri;

    private Handler mhandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == 0x123) {
                if (mUri != null)
                    mPreview.setImageURI(mUri);
            }
        }
    };


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_select_preview_acitivity);
        mViewoPath = getIntent().getStringExtra(VIDEOPATH);
        initView();
        initPlayer();
    }

    private void initPlayer() {
        if (mViewoPath != null || !TextUtils.isEmpty(mViewoPath)) {
            mPlayer = new Player(mSurfaceView, mlistener, this);
        } else {
            Toast.makeText(VideoSelectPreviewAcitivity.this, "视频路径无效", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void initView() {
        mPreview = (ImageView) findViewById(R.id.iv_preview);
        mSelect = (TextView) findViewById(R.id.tv_ok);
        mCancle = (TextView) findViewById(R.id.tv_cancle);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mPlayView = (ImageView) findViewById(R.id.iv_player);
        mPlayView.setOnClickListener(this);
        mCancle.setOnClickListener(this);
        mSelect.setOnClickListener(this);
        new Thread() {

            @Override
            public void run() {
                super.run();
                Bitmap mBitmap = VideoThumbnailUtils.getVideoThumbnail(mViewoPath);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, null, null);
                if (path == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoSelectPreviewAcitivity.this, "视频封面获取失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                    return;
                }
                mUri = Uri.parse(path);
                mhandler.sendEmptyMessage(0x123);
            }
        }.start();
    }

    Player.PlayCallBackListener mlistener = new Player.PlayCallBackListener() {//播放的回调

        @Override
        public void mediaInitCompletion() {
        }

        @Override
        public void playCompletion() {
            mPlayView.setOnClickListener(VideoSelectPreviewAcitivity.this);
        }

        @Override
        public void starBufferring() {
        }

        @Override
        public void endBufferring() {
        }

        @Override
        public void isPrepare(boolean res) {
        }

        @Override
        public void updateTime(int time) {
        }

        @Override
        public void videoError() {
            Toast.makeText(VideoSelectPreviewAcitivity.this, "视频无效", Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void haveError() {
            Toast.makeText(VideoSelectPreviewAcitivity.this, "播放失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    public static Intent buildIntent(Context context, String path) {
        Intent intent = new Intent(context, VideoSelectPreviewAcitivity.class);
        intent.putExtra(VIDEOPATH, path);
        return intent;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_player) {
            mPreview.setVisibility(View.GONE);
            mPlayer.playUrl(mViewoPath);
            mPlayView.setOnClickListener(null);
        } else if (v.getId() == R.id.tv_cancle) {
            finish();
        } else if (v.getId() == R.id.tv_ok) {
            if (mUri != null && mViewoPath != null && !mViewoPath.equals("")) {
                Intent intent = new Intent();
                intent.putExtra(VideoCaptureActivity.VIDEOCOVER, mUri.toString());
                intent.putExtra(VideoCaptureActivity.VIDEOPATH, mViewoPath);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "获取缩略图失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
