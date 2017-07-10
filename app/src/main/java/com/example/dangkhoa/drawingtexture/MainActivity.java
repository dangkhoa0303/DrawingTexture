package com.example.dangkhoa.drawingtexture;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener {

    private OpenGLSurface openGLSurface;
    private Camera mCamera;
    private SurfaceTexture surfaceTexture;
    private GLRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openGLSurface = new OpenGLSurface(this);
        renderer = openGLSurface.getRenderer();
        setContentView(openGLSurface);
    }

    public void startCamera(int texture) {
        surfaceTexture = new SurfaceTexture(texture);
        surfaceTexture.setOnFrameAvailableListener(this);
        renderer.setSurface(surfaceTexture);
        mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        openGLSurface.requestRender();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
        mCamera.release();
        System.exit(0);
    }
}
