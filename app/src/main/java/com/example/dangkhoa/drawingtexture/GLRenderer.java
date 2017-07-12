package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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

    private SurfaceTexture surfaceTexture;
    private MainActivity delegate;
    private DirectVideo mDirectVideo;

    // used to pass texture id of the camera preview to surface texture defined in MainActivity
    private int texture;

    public GLRenderer(Context context) {
        mContext = context;
        delegate = (MainActivity) context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1f);

        texture = initCameraTexture();
        mDirectVideo = new DirectVideo(mContext);

        // call back to start camera preview
        delegate.startCamera(texture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear( GL10.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] mtx = new float[16];
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mtx);

        // start drawing
        mDirectVideo.draw();

        mDirectVideo.drawImage();
    }

    // initialise camera texture id
    private int initCameraTexture() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        /*int[] fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0], 0);*/

        return texture[0];
    }

    public void setSurface(SurfaceTexture _surface) {
        surfaceTexture = _surface;
    }
}
