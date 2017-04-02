package ru.tander.camerarc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Created by dmitry on 24.03.17.
 */

public class LoadImagesAsync extends AsyncTask<Integer, Void, Bitmap> {
    private ImageView mImageView;
    private String TAG;
    private ImageContentFragment mFragment;
    private int width;
    private int height;

    public LoadImagesAsync(ImageView imageView, String TAG, ImageContentFragment fragment, int width, int height) {
        mImageView = imageView;
        mFragment = fragment;
        this.TAG = TAG;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        Bitmap bitmap = decodeSampledBitmapFromUrl(TAG, width, height);
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
}