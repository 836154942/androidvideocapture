package demo.capture_video.spc.myapplication.util;

/**
 * Created by spc on 2016/8/1.
 */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

import demo.capture_video.spc.myapplication.util.AppLog;


/**
 * Created by spc on 2016/2/18.
 * 视频播放器
 */

public class Player implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        SurfaceHolder.Callback, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    private int mvideoWidth;
    private int mvideoHeight;
    public MediaPlayer mediaPlayer;
    private SurfaceView mSurfaceView;
    private SurfaceHolder surfaceHolder;
    private SeekBar skbProgress;
    private Timer mTimer = new Timer();
    private PlayCallBackListener mCallBack;
    private boolean mIsPrepare;
    private int mVideoHeight;
    private int mVideoWidth;
    private Context mContext;


    //给activity的回掉接口
    public interface PlayCallBackListener {

        public void mediaInitCompletion();//完成初始化

        public void playCompletion();//播放完成

        public void starBufferring();//播放暂停，开始缓冲

        public void endBufferring();//缓冲结束，开始播放

        public void isPrepare(boolean res);//prepare成功

        public void updateTime(int time);

        public void videoError();//prepare失败

        public void haveError();

    }

    /**
     * 视频预览构造
     */
    public Player(SurfaceView surfaceView, PlayCallBackListener callBack, Context context) {
        mCallBack = callBack;
        mSurfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mContext = context;
    }

    /**
     * 定时器更新进度
     **/
    TimerTask mTimerTask = new TimerTask() {

        @Override
        public void run() {
            if (mediaPlayer == null)
                return;
            try {
                if (mIsPrepare && mediaPlayer.isPlaying() && skbProgress.isPressed() == false) {
                    handleProgress.sendEmptyMessage(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Handler handleProgress = new Handler() {

        public void handleMessage(Message msg) {
            if (!mIsPrepare) {
                return;
            }
            if (mediaPlayer != null) {

                int duration = mediaPlayer.getDuration();
                int position = mediaPlayer.getCurrentPosition();
                if (duration > 0) {
                    mCallBack.updateTime(position);
                    long pos = skbProgress.getMax() * position / duration;
                    skbProgress.setProgress((int) pos);
                }
            }
        }
    };


    public void play() {
        if (mIsPrepare) {
            mediaPlayer.start();
        }
    }

    public void playUrl(String videoUrl) {
        //prepare自动播放
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.prepare();
            setVideoSize();
            mCallBack.isPrepare(true);
            mIsPrepare = true;
        } catch (Exception e) {
            e.printStackTrace();
            mCallBack.videoError();
        }
    }

    /**
     * 调整视频播放的大小
     */
    private void setVideoSize() {
        try {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            int screenWidth = outMetrics.widthPixels;
            int screenHeight = outMetrics.heightPixels;
            ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
            layoutParams.width = (screenWidth);
            layoutParams.height = (int) (mediaPlayer.getVideoHeight() * ((screenWidth * 1.0) /
                    mediaPlayer.getVideoWidth()));
            mSurfaceView.setLayoutParams(layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void pause() {
        if (mediaPlayer != null && mIsPrepare) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
        } catch (Exception e) {
            mCallBack.haveError();
            e.printStackTrace();
        }
        mCallBack.mediaInitCompletion();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {//播放完成.出错 退出，都在这释放资源。
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    @Override
    /**
     * 在进入 Prepared 状态 并 开始播放的时候回调;
     */ public void onPrepared(MediaPlayer arg0) {
        mvideoWidth = mediaPlayer.getVideoWidth();
        mvideoHeight = mediaPlayer.getVideoHeight();
        if (mvideoHeight != 0 && mvideoWidth != 0) {
            arg0.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        mediaPlayer.reset();
        mCallBack.playCompletion();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
        skbProgress.setSecondaryProgress(bufferingProgress);
        if (mediaPlayer.getDuration() > 0) {
            int currentProgress = skbProgress.getMax() * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
            AppLog.e(currentProgress + "% play" + bufferingProgress + "% buffer");
        }
    }


    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {//错误
        AppLog.e("onError");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mCallBack.haveError();
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                AppLog.e("开始渲染第一帧");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                AppLog.e("暂停播放开始缓冲更多数据");
                mCallBack.starBufferring();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                AppLog.e("缓冲了足够的数据重新开始播放");
                mCallBack.endBufferring();
                break;
        }
        return false;
    }

}
