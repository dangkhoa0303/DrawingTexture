package com.example.dangkhoa.drawingtexture;

import android.opengl.GLES20;

/**
 * Created by dangkhoa on 03/07/2017.
 */

public class riGraphicTools {

    // program variable
    public static int sp_SolidColor;
    public static int sp_Image;

    public static final String vs_SolidColor = "uniform mat4 uMVPMatrix;"
            + "attribute vec4 vPosition;"
            + "void main() {"
            + " gl_Position = vPosition;"
            + "}";

    public static final String fs_SolidColor = "precision mediump float;"
            + "void main() {"
            + " gl_FragColor = vec4(0.5, 0.0, 0.0, 1.0);"
            + "}";

    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fs_Image =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    //"uniform samplerExternalOES s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                    "}";

    public static int loadShader(int type, String shaderCode) {

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
