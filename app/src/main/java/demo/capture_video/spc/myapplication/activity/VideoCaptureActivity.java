package demo.capture_video.spc.myapplication.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import demo.capture_video.spc.myapplication.R;
import demo.capture_video.spc.myapplication.domain.Video;
import demo.capture_video.spc.myapplication.util.VideoThumbnailUtils;
import demo.capture_video.spc.myapplication.provider.VideoProvider;

/**
 * Created by spc on 2016/3/5.
 * 视频录制
 */
public class VideoCaptureActivity extends Activity implements View.OnClickListener, MediaRecorder.OnErrorListener {

    public final static int REQUESTSELECT = 1;
    public final static int REQUESTPROVIEW = 2;
    public final static int REQUESTFEEDVIDEO = 3;
    public final static String VIDEOPATH = "videopath";
    public final static String VIDEOCOVER = "videocover";
    private SurfaceView mCameraPreview;
    private SurfaceHolder mSurfaceHolder;
    private ImageButton mShutter;
    private TextView mMinutePrefix;
    private TextView mMinuteText;
    private TextView mSecondPrefix;
    private TextView mSecondText;
    private ImageView mImageBack, mImageFlash, mImageChange, mAlbumVideo;
    private ProgressBar mProgressBar;

    private Camera mCamera;
    private MediaRecorder mRecorder;
    private int mCameraPosition = 0;//0是后置摄像头，1是前置摄像头

    private boolean mIsRecording = false;
    private boolean mIsSufaceCreated = false;
    private String mVideoPath;
    private int mTime;
    private int mTotalTime = 10;//最长录制时间 10秒
    private VideoProvider mProvider;
    private List<Video> mListVideos;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                updateTimestamp();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_video_capture);
        initView();
        showPreviewImage();
    }

    private void showPreviewImage() {//左下方的相册
        mProvider = new VideoProvider(this);
        mListVideos = mProvider.getList();
        if (mListVideos != null && mListVideos.size() > 0) {
            Bitmap bitmap = VideoThumbnailUtils.getVideoThumbnail(mListVideos.get(0).getPath());
            mAlbumVideo.setImageBitmap(bitmap);
        }
    }


    private void initView() {
        mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        mMinutePrefix = (TextView) findViewById(R.id.timestamp_minute_prefix);
        mMinuteText = (TextView) findViewById(R.id.timestamp_minute_text);
        mSecondPrefix = (TextView) findViewById(R.id.timestamp_second_prefix);
        mSecondText = (TextView) findViewById(R.id.timestamp_second_text);
        mImageBack = (ImageView) findViewById(R.id.iv_back);
        mImageFlash = (ImageView) findViewById(R.id.iv_flash);
        mImageChange = (ImageView) findViewById(R.id.iv_change);
        mAlbumVideo = (ImageView) findViewById(R.id.iv_album_video);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_wait);

        mSurfaceHolder = mCameraPreview.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mShutter = (ImageButton) findViewById(R.id.record_shutter);
        mShutter.setOnClickListener(this);
        mImageBack.setOnClickListener(this);
        mImageFlash.setOnClickListener(this);
        mImageChange.setOnClickListener(this);
        mAlbumVideo.setOnClickListener(this);
        if (Build.BRAND.equals("Meizu")) {//mx4pro打开闪关灯崩溃
            mImageFlash.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsRecording) {
            stopRecording();
        }
        stopPreview();
    }

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsSufaceCreated = false;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIsSufaceCreated = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            startPreview();
        }
    };


    private void startPreview() {    //启动摄像头预览
        if (mCamera != null || !mIsSufaceCreated) {
            return;
        }
        try {
            mCamera = Camera.open(mCameraPosition);
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                mCamera.autoFocus(null);
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            stopPreview();
            e.printStackTrace();
        }

    }

    private void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_album_video:
                //TODO for result
                startActivityForResult(new Intent(this, SelectVideoActivity.class), REQUESTSELECT);
                //                finish();
                break;
            case R.id.iv_back:
                if (mIsRecording && mTime < 1) {
                    return;
                }
                if (mIsRecording) {
                    File f = new File(mVideoPath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
                finish();
                break;
            case R.id.iv_flash:
                if (mCameraPosition == 1)
                    return;
                Camera.Parameters parameter = mCamera.getParameters();
                List<String> flashModes = parameter.getSupportedFlashModes();
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH) && parameter.getFlashMode().equals(Camera
                        .Parameters.FLASH_MODE_OFF))
                    parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                else
                    parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameter);
                break;
            case R.id.iv_change:
                if (mCameraPosition == 0)
                    mCameraPosition = 1;
                else if (mCameraPosition == 1)
                    mCameraPosition = 0;
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                startPreview();
                break;

            case R.id.record_shutter:
                if (mIsRecording) {
                    mHandler.removeMessages(0x123);
                    mHandler = null;
                    stopRecording();
                    stopPreview();
                    //TODO 预览 forresult
                    startActivityForResult(VideoSelectPreviewAcitivity.buildIntent(this, mVideoPath), REQUESTPROVIEW);
                    //                    finish();
                } else {
                    mImageChange.setVisibility(View.INVISIBLE);
                    initMediaRecorder();
                    startRecording();
                    new UpdateThread().start();
                }
                break;
            default:
                break;
        }
    }

    private void initMediaRecorder() {
        mRecorder = new MediaRecorder();
        if (mCamera != null)
            mCamera.unlock();
        mRecorder.setCamera(mCamera);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setVideoSize(640, 480);
//        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoEncodingBitRate(2 * 1024 * 512);
        mRecorder.setOnErrorListener(this);
        if (mCameraPosition == 1)
            mRecorder.setOrientationHint(270);//前置摄像头视频旋转270度
        else
            mRecorder.setOrientationHint(90);//后置摄像头转90
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        String path = getSDPath();
        if (path != null) {
            File dir = new File(path + "/mokavideo");
            if (!dir.exists()) {
                dir.mkdir();
            }
            mVideoPath = dir + "/" + getDate() + ".mp4";
            mRecorder.setOutputFile(mVideoPath);
        }
    }

    private void startRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (Exception e) {
                mIsRecording = false;
                e.printStackTrace();
            }
        }
        mIsRecording = true;
    }

    private void stopRecording() {
        try {


            mIsRecording = false;
            if (mCamera != null) {
                mCamera.lock();
            }
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
            mShutter.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class UpdateThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (mTime <= mTotalTime) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mHandler != null)
                    mHandler.sendEmptyMessage(0x123);
            }
        }
    }


    private void updateTimestamp() {
        if (mTime++ >= mTotalTime) {//到时间停止录制
            if (mHandler != null) {
                mHandler.removeMessages(0x123);
                mHandler = null;
            }
            if (mIsRecording) {
                stopRecording();
            }
            stopPreview();
            //TODO 预览。 forressult
            startActivityForResult(VideoSelectPreviewAcitivity.buildIntent(this, mVideoPath), REQUESTPROVIEW);
            //            finish();
        } else {
            int second = Integer.parseInt(mSecondText.getText().toString());
            int minute = Integer.parseInt(mMinuteText.getText().toString());
            second++;
            if (second < 10) {
                mSecondText.setText(String.valueOf(second));
            } else if (second >= 10 && second < 60) {
                mSecondPrefix.setVisibility(View.GONE);
                mSecondText.setText(String.valueOf(second));
            } else if (second >= 60) {
                mSecondPrefix.setVisibility(View.VISIBLE);
                mSecondText.setText("0");
                minute++;
                mMinuteText.setText(String.valueOf(minute));
            } else if (minute >= 60) {
                mMinutePrefix.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 获取系统时间
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);
        int month = ca.get(Calendar.MONTH);
        int day = ca.get(Calendar.DATE);
        int minute = ca.get(Calendar.MINUTE);
        int hour = ca.get(Calendar.HOUR);
        int second = ca.get(Calendar.SECOND);
        String date = "" + year + (month + 1) + day + hour + minute + second;
        return date;
    }

    /**
     * 获取SD path
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        } else {
            Toast.makeText(VideoCaptureActivity.this, "SD卡不可用", Toast.LENGTH_SHORT).show();
        }
        return null;
    }


    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, VideoCaptureActivity.class);
        return intent;
    }

    @Override
    public void onBackPressed() {
        if (mIsRecording && mTime < 1) {
            return;
        }
        if (mIsRecording) {
            File f = new File(mVideoPath);
            if (f.exists()) {
                f.delete();
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
    }

    //request =1 是选择视频，拿到视频以后 forresult=2 开启预览
    //request 2 返回的都是预览过的，可以直接finish返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTSELECT || requestCode == REQUESTPROVIEW) {
                Intent intent = new Intent();
                intent.putExtra(VIDEOPATH, data.getStringExtra(VIDEOPATH));
                intent.putExtra(VIDEOCOVER, data.getStringExtra(VIDEOCOVER));
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (requestCode == REQUESTPROVIEW) {
            finish();
        }
    }
}