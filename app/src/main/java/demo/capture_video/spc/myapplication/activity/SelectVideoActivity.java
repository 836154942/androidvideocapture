package demo.capture_video.spc.myapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import demo.capture_video.spc.myapplication.domain.AbstructProvider;
import demo.capture_video.spc.myapplication.domain.LoadedImage;
import demo.capture_video.spc.myapplication.R;
import demo.capture_video.spc.myapplication.domain.Video;
import demo.capture_video.spc.myapplication.util.VideoThumbnailUtils;
import demo.capture_video.spc.myapplication.provider.VideoProvider;

public class SelectVideoActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private final static int REQUESTPROVIECE = 2;
    private AbstructProvider mProvider;
    private List<Video> mListVideos;
    private ListView mlistview;
    private Adapter mAdapter;
    public ArrayList<LoadedImage> photos;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_select_video);
        mlistview = (ListView) findViewById(R.id.list);
        findViewById(R.id.ib_title_bar_left).setOnClickListener(this);
        initData();
    }

    private void initData() {
        mProvider = new VideoProvider(this);
        mListVideos = mProvider.getList();
        mAdapter = new Adapter();
        loadImages();
        mlistview.setAdapter(mAdapter);
        mlistview.setOnItemClickListener(this);
    }


    /**
     * Load images.
     */
    private void loadImages() {
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            new LoadImagesFromSDCard().execute();
        } else {
            final LoadedImage[] photos = (LoadedImage[]) data;
            if (photos.length == 0) {
                new LoadImagesFromSDCard().execute();
            }
            for (LoadedImage photo : photos) {
                addImage(photo);
            }
        }
    }

    private void addImage(LoadedImage... value) {
        for (LoadedImage image : value) {
            mAdapter.addPhoto(image);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        final ListView grid = mlistview;
        final int count = grid.getChildCount();
        final LoadedImage[] list = new LoadedImage[count];

        for (int i = 0; i < count; i++) {
            final ImageView v = (ImageView) grid.getChildAt(i);
            list[i] = new LoadedImage(((BitmapDrawable) v.getDrawable()).getBitmap());
        }

        return list;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String videoPath = mListVideos.get(i).getPath();
        startActivityForResult(VideoSelectPreviewAcitivity.buildIntent(this, videoPath), REQUESTPROVIECE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_title_bar_left) {
            finish();
        }
    }

    class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            Bitmap bitmap = null;
            for (int i = 0; i < mListVideos.size(); i++) {
                bitmap = VideoThumbnailUtils.getVideoThumbnail(mListVideos.get(i).getPath());
                if (bitmap != null) {
                    publishProgress(new LoadedImage(bitmap));
                }
            }
            return null;
        }

        @Override
        public void onProgressUpdate(LoadedImage... value) {
            addImage(value);
        }
    }


    class Adapter extends BaseAdapter {

        public Adapter() {
            photos = new ArrayList<LoadedImage>();
        }

        @Override
        public int getCount() {
            return mListVideos.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void addPhoto(LoadedImage image) {
            photos.add(image);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(SelectVideoActivity.this).inflate(R.layout.list_select_video, null);
                holder.img = (ImageView) convertView.findViewById(R.id.video_img);
                holder.title = (TextView) convertView.findViewById(R.id.video_title);
                holder.time = (TextView) convertView.findViewById(R.id.video_time);
                holder.size = (TextView) convertView.findViewById(R.id.video_size);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.size.setText((float) (Math.round((float) mListVideos.get(position).getSize() / 1024 / 1024 * 100))
                    / 100 + "M");
            holder.title.setText(mListVideos.get(position).getTitle());//ms
            long min = mListVideos.get(position).getDuration() / 1000 / 60;
            long sec = mListVideos.get(position).getDuration() / 1000 % 60;


            String showMin = "", showSec = "";
            if (sec < 10) {
                showSec = "0" + sec;
            } else
                showSec = "" + sec;
            if (min < 10) {
                showMin = "0" + min;
            } else
                showMin = "" + min;
            if (!TextUtils.isEmpty(showSec)) {
                holder.time.setText(showMin + ":" + showSec);
            } else {
                holder.time.setText("");
            }
            holder.img.setImageBitmap(null);
            try {
                holder.img.setImageBitmap(photos.get(position).getBitmap());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        public final class ViewHolder {

            public ImageView img;
            public TextView title;
            public TextView time, size;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTPROVIECE) {
                Intent intent = new Intent();
                intent.putExtra(VideoCaptureActivity.VIDEOPATH, data.getStringExtra(VideoCaptureActivity.VIDEOPATH));
                intent.putExtra(VideoCaptureActivity.VIDEOCOVER, data.getStringExtra(VideoCaptureActivity.VIDEOCOVER));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}