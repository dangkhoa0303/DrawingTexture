package com.example.dangkhoa.drawingtexture;

import android.opengl.GLES20;

/**
 * Created by dangkhoa on 03/07/2017.
 */

public class riGraphicTools {

    public static int loadShader(int type, String shaderCode) {

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
