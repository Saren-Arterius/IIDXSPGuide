package net.wtako.bcr.fragments;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.drawable.GradientDrawable;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.MemoryFile;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.wtako.bcr.R;
import net.wtako.bcr.utils.MiscUtils;

import java.io.IOException;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener, SurfaceHolder.Callback {

    @InjectView(R.id.record_button)
    ImageButton recordButton;
    @InjectView(R.id.camera_view)
    SurfaceView cameraView;
    SurfaceHolder holder;

    MediaRecorder recorder;
    MemoryFile memoryFile;
    private boolean isRecording = false;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        recorder = new MediaRecorder();
        initRecorder();
        recordButton.setOnClickListener(this);

        holder = cameraView.getHolder();
        holder.addCallback(this);

        return rootView;
    }

    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_480P);
        recorder.setProfile(cpHigh);
        try {
            memoryFile = new MemoryFile(null, 5000000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.setOutputFile(MiscUtils.getFileDescriptor(memoryFile));
        recorder.setMaxDuration(1000);
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());
        try {
            recorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            // finish();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w("WTF", "surfaceCreated");
        prepareRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isRecording) {
            recorder.stop();
            isRecording = false;
        }
        recorder.release();
    }

    @Override
    public void onClick(View v) {
        final GradientDrawable shape = (GradientDrawable) recordButton.getBackground();
        int targetPx = MiscUtils.dpToPx(getActivity(), 80);
        ObjectAnimator oa = ObjectAnimator.ofFloat(shape, "CornerRadius",
                isRecording ? 0 : targetPx, isRecording ? targetPx : 0);
        oa.setInterpolator(easeOut);
        oa.setDuration(1000);
        oa.start();
        recordButton.setImageResource(isRecording ? R.drawable.ic_videocam_black_24dp :
                R.drawable.ic_videocam_off_black_24dp);
        if (isRecording) {
            recorder.stop();
            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();


            byte[] data = new byte[1024];
            try {
                memoryFile.getInputStream().read(data);
                Log.w("WTF", Arrays.toString(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
            memoryFile.close();

        } else {
            recorder.start();
        }
        isRecording = !isRecording;
    }

    public static final TimeInterpolator easeOut = new TimeInterpolator() {
        private static final float DOMAIN = 1.0f;
        private static final float DURATION = 1.0f;
        private static final float START = 0.0f;

        public float getInterpolation(float input) {
            return DOMAIN * ((input = input / DURATION - 1) * input * input * input * input + 1) + START;
        }
    };
}
