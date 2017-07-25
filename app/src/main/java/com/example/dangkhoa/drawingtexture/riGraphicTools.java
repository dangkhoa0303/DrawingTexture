package com.example.dangkhoa.drawingtexture;

import android.opengl.GLES20;

/**
 * Created by dangkhoa on 03/07/2017.
 */

public class riGraphicTools {

    public static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 position;" +

                    "attribute vec2 inputTextureCoordinate;" +

                    "varying vec2 textureCoordinate;" +

                    "void main()" +
                    "{"+
                    "   gl_Position = uMVPMatrix * position;"+
                    "   textureCoordinate = inputTextureCoordinate;" +
                    "}";

    public static final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +

                    "varying vec2 textureCoordinate;" +

                    "uniform samplerExternalOES s_texture;" +

                    "void main() {" +
                    "   vec4 camera = texture2D(s_texture, textureCoordinate);" +
                    "   gl_FragColor = camera;" +
                    "}";

    public static final String vs_Sticker =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 position;" +

                    "attribute vec2 inputImgTextureCoordinate;" +

                    "varying vec2 imgTextureCoordinate;" +

                    "void main()" +
                    "{"+
                    "   gl_Position = uMVPMatrix * position;"+
                    "   imgTextureCoordinate = inputImgTextureCoordinate;" +
                    "}";

    public static final String fs_Sticker =
                    "precision mediump float;" +

                    "varying vec2 imgTextureCoordinate;" +

                    "uniform sampler2D img_texture;" +

                    "void main() {" +
                    "   vec4 img = texture2D(img_texture, imgTextureCoordinate);" +
                    "   gl_FragColor = img;" +
                    "}";

    public static int loadShader(int type, String shaderCode) {

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
