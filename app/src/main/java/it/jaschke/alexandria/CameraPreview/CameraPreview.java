package it.jaschke.alexandria.CameraPreview;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import it.jaschke.alexandria.MainActivity;

/**
 * Created by Kamini on 11/14/2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {


    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PreviewCallback previewCallback;
    private Camera.AutoFocusCallback autoFocusCallback;
    private CameraSource mcameraSource;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    BarcodeDetector detector;
    TextView barcodeInfo;
    // constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    private Context mContext;



    @StringDef({
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_AUTO,
            Camera.Parameters.FOCUS_MODE_EDOF,
            Camera.Parameters.FOCUS_MODE_FIXED,
            Camera.Parameters.FOCUS_MODE_INFINITY,
            Camera.Parameters.FOCUS_MODE_MACRO
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FocusMode {}

    public CameraPreview(Context context, BarcodeDetector mdetector){
        super(context);


       /* //the bitmap we wish to draw

        mbitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logintab_off);*/
        mContext=context;
        //mCamera = camera;
        detector=mdetector;



        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);







    }



    public void surfaceCreated(SurfaceHolder holder) {

        mcameraSource = new CameraSource.Builder(mContext, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(15.0f)
                .build();

        if (mcameraSource != null) {
            try {
                this.start(mcameraSource);
            } catch (IOException e) {
              //  Log.e(MainActivity.LOG_TAG, "Unable to start camera source.", e);
                mcameraSource.release();
                mcameraSource = null;
            }
        }

        cameraFocus(mcameraSource, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);


    }




    public void surfaceDestroyed(SurfaceHolder holder) {



     //   Log.d(MainActivity.LOG_TAG, "surfaceDestroyed (stopPreview) :: " + mCamera);

         mcameraSource.stop();
        mcameraSource.release();
        detector.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
// start preview with new settings
        try {
            mcameraSource.stop();
            mcameraSource = new CameraSource.Builder(mContext, detector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(640, 480)
                    .setRequestedFps(15.0f)
                    .build();

            if (mcameraSource != null) {
                try {
                    this.start(mcameraSource);
                } catch (IOException e) {
                    //  Log.e(MainActivity.LOG_TAG, "Unable to start camera source.", e);
                    mcameraSource.release();
                    mcameraSource = null;
                }
            }

            cameraFocus(mcameraSource, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        } catch (Exception e) {

        }
    }



    public  boolean cameraFocus(@NonNull CameraSource cameraSource, @FocusMode @NonNull String focusMode) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        Camera.Parameters params = camera.getParameters();

                        if (!params.getSupportedFocusModes().contains(focusMode)) {
                            return false;
                        }

                        params.setFocusMode(focusMode);
                        camera.setParameters(params);
                        return true;
                    }

                    return false;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }

        return false;
    }



    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        // mGraphicOverlay = overlay;
        if (cameraSource == null) {
            stop();
        }

        mcameraSource = cameraSource;

        if (mcameraSource != null) {
            mStartRequested = true;
            // startIfReady();
            mcameraSource.start(this.mHolder);
          //  Log.d(MainActivity.LOG_TAG, "I am ready");
        }
       // Log.d(MainActivity.LOG_TAG, "camerasaurce statrted ");
    }

    public void stop() {
        if (mcameraSource != null) {
            mcameraSource.stop();
        }
    }


}
