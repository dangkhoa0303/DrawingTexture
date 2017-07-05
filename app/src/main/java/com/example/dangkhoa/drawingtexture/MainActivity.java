package com.example.dangkhoa.drawingtexture;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private OpenGLSurface openGLSurface;
    private CameraView cameraView;

    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

        openGLSurface = new OpenGLSurface(this);
        openGLSurface.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
        openGLSurface.getHolder().setFormat( PixelFormat.TRANSLUCENT );

        openGLSurface.setRenderer(new GLRenderer(this));

        setContentView(openGLSurface);

        mCamera = checkDeviceCamera();

        cameraView = new CameraView(this, mCamera);
        // ...and add it, wrapping the full screen size.
        addContentView( cameraView, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT ) );
    }


    private Camera checkDeviceCamera(){
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

}
