package ru.tander.camerarc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = (ImageView) findViewById(R.id.main_imageview);
        String source = getIntent().getStringExtra("source");
        imageView.setImageBitmap(null);
        imageView.setTag(source);
        LoadImageTask lTask = new LoadImageTask(imageView);
        lTask.execute();

    }

    private class LoadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

        ImageView imageView;
        String source;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
            this.source = (String) imageView.getTag();
        }

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(source, options);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }

    }

}
