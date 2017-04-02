package ru.tander.camerarc.Camera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.tander.camerarc.R;

import static java.security.AccessController.getContext;


/**
 * Created by dmitry on 22.03.17.
 */

public class CameraActivity extends Activity {

    private ProgressDialog pd;
    private TextView videoRecIndicator;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private String TAG = "CAMERARCCamModule";
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private Camera mCamera;
    private CameraPreview mPreview;
    private String appName;
    private DrawingView drawingView;
    private FloatingActionButton captureButton;
    private PopupMenu settingsPopupMenu;
    private Camera.Parameters camParams;

    private final int MENU_PHOTO_QUALITY_ID = 100;
    private final int MENU_VIDEO_QUALITY_ID = 101;
    private final int MENU_BRIGHTNESS_QUALITY_ID = 102;
    private final int MENU_CONTRAST_QUALITY_ID = 103;
    private final int MENU_AF_ID = 103;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appName = getApplicationContext().getString(R.string.app_name);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_layout);

        videoRecIndicator = (TextView) findViewById(R.id.video_recording_title);

        // settings menu
        final ImageButton menuBtn = (ImageButton) findViewById(R.id.cam_menu_button);
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
        settingsPopupMenu = new PopupMenu(wrapper, menuBtn, Gravity.LEFT);
        settingsPopupMenu.inflate(R.menu.cam_settings_menu);

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsPopupMenu.show();
            }
        });

        settingsPopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getGroupId()) {
                            case MENU_PHOTO_QUALITY_ID:
                                int cwidth = Integer.parseInt(item.getTitle().toString().split(" : ")[0]);
                                int cheight = Integer.parseInt(item.getTitle().toString().split(" : ")[1]);
                                camParams = mCamera.getParameters();
                                camParams.setPictureSize(cwidth, cheight);
                                mCamera.setParameters(camParams);
                                break;
                            case MENU_VIDEO_QUALITY_ID:
                                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                                profile.videoFrameWidth = Integer.parseInt(item.getTitle().toString().split(" : ")[0]);
                                profile.videoFrameHeight = Integer.parseInt(item.getTitle().toString().split(" : ")[1]);
                                mMediaRecorder.setProfile(profile);
                                break;
                            case MENU_AF_ID:
                                camParams = mCamera.getParameters();
                                camParams.setFocusMode(item.getTitle().toString());
                                mCamera.setParameters(camParams);
                                break;
                        }
                        return true;
                    }
                });

        // и кнопки затвора
        FloatingActionButton captureVideoButton = (FloatingActionButton) findViewById(R.id.capture_video_button);
        captureVideoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording) {
                            videoRecIndicator.setVisibility(View.GONE);
                            try{
                                mMediaRecorder.stop();
                            }catch(RuntimeException stopException){
                                //handle cleanup here todo
                            }
                            releaseMediaRecorder();
                            mCamera.lock();
                            isRecording = false;
                        } else {
                            // initialize video camera
                            if (prepareVideoRecorder()) {
                                mMediaRecorder.start();
                                // inform the user
                                videoRecIndicator.setVisibility(View.VISIBLE);
                                isRecording = true;
                            } else {
                                releaseMediaRecorder();
                            }
                        }
                    }
                }
        );

        captureButton = (FloatingActionButton) findViewById(R.id.capture_photo_button);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            camera.startPreview();
            captureButton.setClickable(true);
        }

    };


    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), appName);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();

        // Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private class PrepareCamAsync extends AsyncTask<Void, Void, Void> {
        Activity activity;

        protected PrepareCamAsync (Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCamera = getCameraInstance();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mCamera != null){
                mPreview = new CameraPreview(activity, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                drawingView = (DrawingView) findViewById(R.id.drawing_surface);
                mPreview.setDrawingView(drawingView);
                fillPopup(settingsPopupMenu);
//                pd.dismiss();
            } else {
                Toast.makeText(activity, "Закройте другие приложения, использующие камеру или вспышку и повторите попытку.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
//            pd = new ProgressDialog(activity);
//            pd.setMessage("Подождите.");
//            pd.setTitle("Запуск камеры.");
//            pd.show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // асинхронно готовим камеру, пока активити запускается
        new PrepareCamAsync(CameraActivity.this).execute();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mCamera != null)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
    }

    private void fillPopup(PopupMenu popup){
        // качечство фото
        Menu menu = popup.getMenu();
        menu.clear();

        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
        Menu sm = menu.addSubMenu("Качество фото");
        for (Camera.Size size: sizes) {
            sm.addSubMenu(MENU_PHOTO_QUALITY_ID, size.hashCode(), Menu.NONE, size.width + " : " + size.height);
        }

        // качечство видео
        List<Camera.Size> vidsizes = mCamera.getParameters().getSupportedVideoSizes();
        Menu smVid = menu.addSubMenu("Качество видео");
        for (Camera.Size vsize: vidsizes) {
            smVid.addSubMenu(MENU_VIDEO_QUALITY_ID, vsize.hashCode(), Menu.NONE, vsize.width + " : " + vsize.height);
        }

        // фокусировка
        List<String> focuses = mCamera.getParameters().getSupportedFocusModes();
        Menu focusSm = menu.addSubMenu("Режим фокусировки");
        for (String focus: focuses) {
            focusSm.addSubMenu(MENU_AF_ID, focus.hashCode(), Menu.NONE, focus.toString());
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        if(item.getItemId() == 1)
//        {
//            //close the Activity
//            this.finish();
//            return true;
//        }
//        return false;
//    }



}