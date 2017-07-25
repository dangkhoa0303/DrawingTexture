package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.ImageView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dangkhoa on 03/07/2017.
 */

public class GLRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    // Our screenresolution
    float mScreenWidth = 1280;
    float mScreenHeight = 768;

    private SurfaceTexture mCameraSurfaceTexture;
    private MainActivity delegate;
    private DirectVideo mDirectVideo;

    // used to pass texture id of the camera preview to surface texture defined in MainActivity
    private int mCameraTextureID;

    private RectF base;
    private ImageView sticker;

    public GLRenderer(Context context) {
        mContext = context;
        delegate = (MainActivity) context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        mCameraTextureID = initCameraTexture();
        mDirectVideo = new DirectVideo(mContext, base, sticker);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        Log.d("GL", "" + mScreenHeight + " " + mScreenWidth);

        // call back to start camera preview
        delegate.startCamera(mCameraTextureID, (int) mScreenWidth, (int) mScreenHeight);

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

        // Clear our matrices
        for(int i=0;i<16;i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1.0f, 0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear( GL10.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] mtx = new float[16];
        mCameraSurfaceTexture.updateTexImage();
        mCameraSurfaceTexture.getTransformMatrix(mtx);

        // start drawing
        mDirectVideo.draw(mtrxProjectionAndView);
    }

    // initialise camera texture id
    private int initCameraTexture() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    public void setSurface(SurfaceTexture _surface) {
        mCameraSurfaceTexture = _surface;
    }

    public void setStickerCoordinates(RectF base) {
        this.base = base;
    }

    public void setStickerImageView(ImageView imageView) {
        sticker = imageView;
    }
}
