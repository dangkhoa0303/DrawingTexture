package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by dangkhoa on 03/07/2017.
 */

public class OpenGLSurface extends GLSurfaceView {

    private GLRenderer mRenderer;

    public OpenGLSurface(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        mRenderer = new GLRenderer(context);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public GLRenderer getRenderer() {
        return mRenderer;
    }
}
