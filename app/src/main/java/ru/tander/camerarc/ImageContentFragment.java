package ru.tander.camerarc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import ru.tander.camerarc.Camera.CameraActivity;

import static android.app.Activity.RESULT_OK;
import static ru.tander.camerarc.CameraContentFragment.CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE;

/**
 * Created by dmitry on 24.03.17.
 */

public class ImageContentFragment extends Fragment {

    private View mainView;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    public ConcurrentHashMap<String, Bitmap> mMemoryCache = new ConcurrentHashMap<String, Bitmap>();
    private ArrayList<String> mUlrPictures = new ArrayList<String>();
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.recycler_view, container, false);

        swipeContainer = (SwipeRefreshLayout) mainView.findViewById(R.id.swipeContainer);

//        swipeContainer = (SwipeRefreshLayout)inflater.inflate(
//                R.layout.recycler_view, container, false);

        recyclerView = (RecyclerView) mainView.findViewById(R.id.rc_recycler_view); // swipeContainer.getChildAt(1);
        final ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
        recyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRecycleItems(getContext(), adapter);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return mainView;
    }

    public void updateRecycleItems(Context context, ContentAdapter adapter) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
        adapter.clear();
        adapter.addAll(getImageUrlForFolder(mediaStorageDir));
        swipeContainer.setRefreshing(false);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture;
        public TextView name;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_image, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.tile_picture);
            name = (TextView) itemView.findViewById(R.id.tile_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().toString().equals(getResources().getString(R.string.add_item))){

//                        pd = ProgressDialog.show(swipeContainer.getContext(), "", "Отправка данных по исполнителю...");
//                        pd = new ProgressDialog(swipeContainer.getContext());
//                        pd.setTitle("Processing...");
//                        pd.setMessage("Please wait.");
//                        pd.setCancelable(false);
//                        pd.setIndeterminate(true);
//                        pd.show();

//                        pd.setTitle("Processing...");
//                        pd.setMessage("Please wait.");
//                        pd.setCancelable(false);
//                        pd.setIndeterminate(true);
//                        mProgressDialog.show();
//                        pd = new ProgressDialog(getActivity());
//                        pd.setMessage("Please wait a bit.");
//                        pd.setTitle("Launching camera.");
//                        ProgressDialog dialog = ProgressDialog.show(getActivity(), "Launching camera.", "Please wait a bit.", true);
//                        dialog.show();
//                        pd.show();
//                        ((MainActivity) getActivity()).pd.show();
//                        new LongOperation().execute();

//                        ProgressDialog pd;

//                        ProgressDialog pd = new ProgressDialog(getActivity());
//                        new LongOperation(getActivity()).execute();
//                        pd.setMessage("dfdfdf");
//                        pd.setTitle("dfdf");
//                        pd.show();



                        Intent startCameraActivity = new Intent(getActivity(), CameraActivity.class);
                        startActivityForResult(startCameraActivity, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
//                        pb.setVisibility(View.INVISIBLE);

                    } else {
                        if ("android.media.action.IMAGE_CAPTURE".equals(getActivity().getIntent().getAction())) {
                            finishWithResult(name.getText().toString(), getActivity());
                        } else {
                            Intent intent = new Intent(getActivity(), ImageActivity.class);
                            intent.putExtra("source", name.getText());
                            startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    private void finishWithResult(String path, FragmentActivity activity) {
        if (path != null) {
            Uri uri = Uri.fromFile(new File(path));
            activity.setResult(RESULT_OK, new Intent().setData(uri));
            activity.finish();
        } else {
            activity.setResult(activity.RESULT_CANCELED);
            activity.finish();
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {

        public ContentAdapter(Context context) {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
            mUlrPictures = getImageUrlForFolder(mediaStorageDir);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String imUrl = mUlrPictures.get(position % mUlrPictures.size());
            holder.name.setText(mUlrPictures.get(position % mUlrPictures.size()));
            String TAG = String.valueOf(imUrl);
            holder.picture.setTag(TAG);
            if (TAG.equals(getResources().getString(R.string.add_item))){
                holder.picture.setImageDrawable(getResources().getDrawable(R.drawable.add_black));
            } else {
                holder.picture.setImageDrawable(null);
                Bitmap bitmap = getBitmapFromMemCache(TAG);
                if(bitmap == null) {
                    LoadImagesAsync task = new LoadImagesAsync(holder.picture, TAG, ImageContentFragment.this, 100, 100);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else {
                    holder.picture.setImageBitmap(bitmap);
                }
            }

        }

        @Override
        public int getItemCount() {
            return mUlrPictures.size();
        }

        public void clear() {
            mUlrPictures.clear();
            notifyDataSetChanged();
        }

        public void addAll(ArrayList<String> list) {
            mUlrPictures.addAll(list);
            notifyDataSetChanged();
        }

    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public ArrayList<String> getImageUrlForFolder(final File folder) {
        ArrayList<String> result = new ArrayList<String>();
        result.add(getResources().getString(R.string.add_item));
        if (folder.exists()){
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    getImageUrlForFolder(fileEntry);
                } else {
                    if (fileEntry.getName().contains(".jpg")){
                        result.add(String.format("%s/%s", folder, fileEntry.getName()));
                    }
                }
            }
        }
        return result;
    }

//    public class MyTask extends AsyncTask<Void, Void, Void> {
//        ProgressDialog progress;
//        public MyTask(ProgressDialog progress) {
//            this.progress = progress;
//        }
//
//        public void onPreExecute() {
//            progress.show();
//        }
//
//        @Override
//        protected String doInBackground(Void... unused) {
////            Intent startCameraActivity = new Intent(getActivity(), CameraActivity.class);
////            startActivityForResult(startCameraActivity, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
//            return "";
//        }
//
//        public void onPostExecute(Void unused) {
//            progress.dismiss();
//        }
//    }

//    private class LongOperation extends AsyncTask<String, Void, String> {
//
////        ProgressDialog pd;
//        Activity activity;
//
//        public LongOperation(Activity activity) {
//            this.activity = activity;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            Intent startCameraActivity = new Intent(getActivity(), CameraActivity.class);
//            startActivityForResult(startCameraActivity, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
//            return "Executed";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
////            pd.dismiss();
//        }
//
//        @Override
//        protected void onPreExecute() {
//            pd = new ProgressDialog(activity);
//            pd.setMessage("dfdfdf");
//            pd.setTitle("dfdf");
//            pd.show();
//        }
//
//    }

}