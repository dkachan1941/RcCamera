package ru.tander.camerarc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.tander.camerarc.Camera.CameraActivity;


/**
 * Created by dmitry on 22.03.17.
 */

public class CameraContentFragment extends android.support.v4.app.Fragment {

    public static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 1777;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_content_layout, container, false);

        Button newPage = (Button)v.findViewById(R.id.idMakePhoto);
        newPage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent startCameraActivity = new Intent(getActivity(), CameraActivity.class);
                startActivityForResult(startCameraActivity, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
            }
        });

        return v;
    }

}