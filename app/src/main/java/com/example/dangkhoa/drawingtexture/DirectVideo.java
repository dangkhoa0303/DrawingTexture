package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.dangkhoa.drawingtexture.riGraphicTools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.example.dangkhoa.drawingtexture.riGraphicTools.loadShader;

/**
 * Created by chau on 04.03.15.
 */
public class DirectVideo {

    private Context mContext;

    private final String vertexShaderCode =
                    "attribute vec4 position;" +

                    "attribute vec2 inputTextureCoordinate;" +
                    "attribute vec2 inputImgTextureCoordinate;" +

                    "varying vec2 textureCoordinate;" +
                    "varying vec2 imgTextureCoordinate;" +

                    "void main()" +
                    "{"+
                    "   gl_Position = position;"+
                    "   textureCoordinate = inputTextureCoordinate;" +
                    "   imgTextureCoordinate = inputImgTextureCoordinate;" +
                    "}";

    private final String fragmentShaderCode =
                    "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +

                    "varying vec2 textureCoordinate;" +
                    "varying vec2 imgTextureCoordinate;" +

                    "uniform samplerExternalOES s_texture;" +
                    "uniform sampler2D img_texture;" +

                    "void main() {" +
                    "   vec4 camera = texture2D(s_texture, textureCoordinate);" +
                    "   vec4 img = texture2D(img_texture, imgTextureCoordinate);" +
                    "   gl_FragColor = vec4(mix(camera.rgb, img.rgb, img.a*1.0), camera.a);" +
                    //"   gl_FragColor = mix(camera, img, 0.6);" +
                    "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mCameraTextureHandle;
    private int mImageTextureHandle;
    private int mTextureCoordHandle;

    private FloatBuffer imageTextureVerticesBuffer;
    private int mImgTextureCoordHandle;


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    // draw 2 rectangles - one rectangle for camera and one for sticker
    static float squareVertices[] = {
            1.0f, 1.0f,
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,

            /*0.5f, 0.5f,
            -0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f*/

            0.0f, 0.0f,
            -0.5f, 0.0f,
            -0.5f, -0.5f,
            0.0f, 0.0f,
            -0.5f, -0.5f,
            0.0f, -0.5f
    };

    // texture coordinates of camera preview
    float textureVertices[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,

            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f
    };

    // texture coordinates of sticker
    float imageTextureVertices[] = {
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 1.0f,

            /*0.75f, 0.75f,
            0.25f, 0.75f,
            0.25f, 0.25f,
            0.75f, 0.75f,
            0.25f, 0.25f,
            0.75f, 0.25f*/

            0.5f, 0.5f,
            0.25f, 0.5f,
            0.25f, 0.25f,
            0.5f, 0.5f,
            0.25f, 0.25f,
            0.5f, 0.25f
    };

    private short drawOrder[] = {
            0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7
    }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public DirectVideo(Context context)
    {
        mContext = context;

        loadImageTexture();

        ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareVertices);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        ByteBuffer bb3 = ByteBuffer.allocateDirect(imageTextureVertices.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        imageTextureVerticesBuffer = bb3.asFloatBuffer();
        imageTextureVerticesBuffer.put(imageTextureVertices);
        imageTextureVerticesBuffer.position(0);

        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
    }

    private void loadImageTexture() {
        int[] textures = new int[1];

        GLES20.glGenTextures(1, textures, 0);

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();
    }

    public void draw()
    {
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        textureVerticesBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        mImgTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputImgTextureCoordinate");
        imageTextureVerticesBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mImgTextureCoordHandle);
        GLES20.glVertexAttribPointer(mImgTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, imageTextureVerticesBuffer);

        mCameraTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        GLES20.glUniform1i(mCameraTextureHandle, 0);

        mImageTextureHandle = GLES20.glGetUniformLocation(mProgram, "img_texture");
        GLES20.glUniform1i(mImageTextureHandle, 1);

        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        GLES20.glDisableVertexAttribArray(mImgTextureCoordHandle);
    }
}
