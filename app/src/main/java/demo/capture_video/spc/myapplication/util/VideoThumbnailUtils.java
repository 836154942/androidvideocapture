package demo.capture_video.spc.myapplication.util;

/**
 * Created by spc on 2016/8/1.
 */

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

/**
 * Created by spc on 2016/2/17.
 * 获取视频缩略图
 */
public class VideoThumbnailUtils {

    /**
     * 获取视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 600, 800, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

}