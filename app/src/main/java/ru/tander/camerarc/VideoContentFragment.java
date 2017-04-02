package ru.tander.camerarc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import ru.tander.camerarc.Camera.CameraActivity;

import static ru.tander.camerarc.CameraContentFragment.CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE;

/**
 * Created by dmitry on 21.03.17.
 */

public class VideoContentFragment extends android.support.v4.app.Fragment {

    View mainView;
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

//        swipeContainer = (SwipeRefreshLayout)inflater.inflate(
//                R.layout.recycler_view, container, false);

        mainView = inflater.inflate(R.layout.recycler_view, container, false);

        swipeContainer = (SwipeRefreshLayout) mainView.findViewById(R.id.swipeContainer);

        recyclerView = (RecyclerView) swipeContainer.getChildAt(1);
        final VideoContentFragment.ContentAdapter adapter = new VideoContentFragment.ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

    public void updateRecycleItems(Context context, VideoContentFragment.ContentAdapter adapter) {
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
            super(inflater.inflate(R.layout.item_video, parent, false));
            picture = (ImageView) itemView.findViewById(R.id.imageview_video);
            name = (TextView) itemView.findViewById(R.id.video_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.getText().equals(getResources().getString(R.string.add_item))){
                        Intent startCameraActivity = new Intent(getActivity(), CameraActivity.class);
                        startActivityForResult(startCameraActivity, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
                    } else {
                        if ("android.media.action.IMAGE_CAPTURE".equals(getActivity().getIntent().getAction())) {
                            finishWithResult(name.getText().toString(), getActivity());
                        } else {
                            Intent intent = new Intent(getActivity(),
                                    VideoActivity.class);
                            intent.putExtra("source", name.getText());
                            startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    public class ContentAdapter extends RecyclerView.Adapter<VideoContentFragment.ViewHolder> {

        public ContentAdapter(Context context) {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
            mUlrPictures = getImageUrlForFolder(mediaStorageDir);
        }

        @Override
        public VideoContentFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VideoContentFragment.ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(VideoContentFragment.ViewHolder holder, int position) {
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
                    LoadVideousAsync task = new LoadVideousAsync(holder.picture, TAG, VideoContentFragment.this);
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
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private void finishWithResult(String path, FragmentActivity activity) {
        if (path != null) {
            Uri uri = Uri.fromFile(new File(path));
            activity.setResult(activity.RESULT_OK, new Intent().setData(uri));
            activity.finish();
        } else {
            activity.setResult(activity.RESULT_CANCELED);
            activity.finish();
        }
    }

    public ArrayList<String> getImageUrlForFolder(final File folder) {
        ArrayList<String> result = new ArrayList<String>();
        if (folder.exists()) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    getImageUrlForFolder(fileEntry);
                } else {
                    if (fileEntry.getName().contains(".mp4")) {
                        result.add(String.format("%s/%s", folder, fileEntry.getName()));
                    }
                }
            }
        }
        return result;
    }

}
