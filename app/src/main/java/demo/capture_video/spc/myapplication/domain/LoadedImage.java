package demo.capture_video.spc.myapplication.domain;

import android.graphics.Bitmap;

/**
 * Created by spc on 2016/8/1.
 */
public class LoadedImage {

    public Bitmap mBitmap;

    public LoadedImage(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}