package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by chau on 04.03.15.
 */
public class DirectVideo {

    private Context mContext;

    // variables used for camera preview texture
    private final int mProgram;
    private FloatBuffer vertexBuffer, textureVerticesBuffer;

    // variables used for sticker texture
    private final int sProgram;
    private FloatBuffer stickerSquareVertexBuffer, imageTextureVerticesBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    private static float screen_width = 1080;
    private static float screen_height = 1920;

    private static float sticker_size = 400;
    private static float sticker_top_right_x = 700;
    private static float sticker_top_right_y = 800;

    // draw 2 rectangles - one rectangle for camera and one for sticker
    static float squareVertices[] = {

            // vertices of camera preview rectangle
            screen_width, screen_height,                              // top right
            0f, screen_height,                                        // top left
            0f, 0f,                                                   // bottom left
            screen_width, screen_height,                              // top right
            0f, 0f,                                                   // bottom left
            screen_width, 0f                                          // bottom right
    };

    static float stickerSquareVertices[] = {
            sticker_top_right_x, sticker_top_right_y,                               // top right
            sticker_top_right_x-sticker_size, sticker_top_right_y,                  // top left
            sticker_top_right_x-sticker_size, sticker_top_right_y-sticker_size,     // bottom left
            sticker_top_right_x, sticker_top_right_y,                               // top right
            sticker_top_right_x-sticker_size, sticker_top_right_y-sticker_size,     // bottom left
            sticker_top_right_x, sticker_top_right_y-sticker_size                   // bottom right
    };

    // texture coordinates of camera preview
    float textureVertices[] = {

            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    // texture coordinates of sticker
    float stickerTextureVertices[] = {

            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public DirectVideo(Context context)
    {
        mContext = context;

        loadImageTexture();

        // create square vertex buffer for camera preview
        ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareVertices);
        vertexBuffer.position(0);

        // create texture coordinates buffer for camera preview
        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        // create texture coordinates for sticker
        ByteBuffer bb3 = ByteBuffer.allocateDirect(stickerTextureVertices.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        imageTextureVerticesBuffer = bb3.asFloatBuffer();
        imageTextureVerticesBuffer.put(stickerTextureVertices);
        imageTextureVerticesBuffer.position(0);

        // create square vertex buffer for sticker
        ByteBuffer bb4 = ByteBuffer.allocateDirect(stickerSquareVertices.length * 4);
        bb4.order(ByteOrder.nativeOrder());
        stickerSquareVertexBuffer = bb4.asFloatBuffer();
        stickerSquareVertexBuffer.put(stickerSquareVertices);
        stickerSquareVertexBuffer.position(0);

        // Load vertex shader and fragment shader into program of camera preview
        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vertexShaderCode);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
        //-----------------------------------------------------------------------------------------------------

        // Load vertex shader and fragment shader into program of sticker
        int stickerVertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Sticker);
        int stickerFragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Sticker);

        sProgram = GLES20.glCreateProgram();                    // create empty OpenGL ES Program
        GLES20.glAttachShader(sProgram, stickerVertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(sProgram, stickerFragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(sProgram);
        //-----------------------------------------------------------------------------------------------------
    }

    private void loadImageTexture() {
        int[] textures = new int[1];

        GLES20.glGenTextures(1, textures, 0);

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mario);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        bmp.recycle();
    }

    public void draw(float[] mtrxProjectionAndView)
    {
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mtrxProjectionAndView, 0);

        int mCameraTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        GLES20.glUniform1i(mCameraTextureHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

        drawSticker(mtrxProjectionAndView);
    }

    private void drawSticker(float[] mtrxProjectionAndView) {
        GLES20.glUseProgram(sProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

        int mPositionHandle = GLES20.glGetAttribLocation(sProgram, "position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, stickerSquareVertexBuffer);

        int mImgTextureCoordHandle = GLES20.glGetAttribLocation(sProgram, "inputImgTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mImgTextureCoordHandle);
        GLES20.glVertexAttribPointer(mImgTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, imageTextureVerticesBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(sProgram, "uMVPMatrix");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mtrxProjectionAndView, 0);

        int mImageTextureHandle = GLES20.glGetUniformLocation(sProgram, "img_texture");
        GLES20.glUniform1i(mImageTextureHandle, 1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mImgTextureCoordHandle);
    }
}
