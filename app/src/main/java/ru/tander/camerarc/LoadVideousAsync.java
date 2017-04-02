package ru.tander.camerarc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by dmitry on 24.03.17.
 */

public class LoadVideousAsync extends AsyncTask<Integer, Void, Bitmap> {
    private ImageView mImageView;
    private String TAG;
    private VideoContentFragment mFragment;

    public LoadVideousAsync(ImageView imageView, String TAG, VideoContentFragment fragment) {
        mImageView = imageView;
        this.TAG = TAG;
        mFragment = fragment;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
//        Bitmap bitmap = decodeSampledBitmapFromUrl(TAG, 100, 100);
        Bitmap bitmap = getFrameFromCurrentVideo(1, TAG);
        mFragment.addBitmapToCache(TAG,bitmap);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(mImageView.getTag().toString().equals(TAG)) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap decodeSampledBitmapFromUrl(String url, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(url, options);
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap getFrameFromCurrentVideo(int seconds, String source) {
        Bitmap bitmap = null;
        FileInputStream inputStream = null;
        MediaMetadataRetriever mMediaMetadataRetriever = new MediaMetadataRetriever();;
        try {
            inputStream = new FileInputStream(source);
            mMediaMetadataRetriever.setDataSource(inputStream.getFD());
            bitmap = mMediaMetadataRetriever.getFrameAtTime(seconds * 1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            Log.d("CAMERARC", e.getMessage());
//            e.printStackTrace();
        }

//        if(mMediaMetadataRetriever != null) {
//            bitmap = mMediaMetadataRetriever.getFrameAtTime(seconds * 1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//        }
        mMediaMetadataRetriever.release();
        return bitmap;
    }
}