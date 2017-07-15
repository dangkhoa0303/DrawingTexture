package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.MotionEvent;

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
    private static FloatBuffer stickerSquareVertexBuffer, imageTextureVerticesBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    private static float screen_width = 1080;
    private static float screen_height = 1920;

    private static Sprite sprite;

    static float squareVertices[];
    static float stickerSquareVertices[];

    // texture coordinates of camera preview
    float textureVertices[];
    // texture coordinates of sticker
    float stickerTextureVertices[];

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public DirectVideo(Context context)
    {
        mContext = context;

        sprite = new Sprite();

        SetupSticker();
        SetupCamera();

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

    private void SetupCamera() {

        squareVertices = new float[] {

                0f, screen_height,
                0f, 0f,
                screen_width, 0f,
                0f, screen_height,
                screen_width, 0f,
                screen_width, screen_height
        };

        textureVertices = new float[] {

                0.0f, 0.0f,     // top left
                0.0f, 1.0f,     // bottom left
                1.0f, 1.0f,     // bottom right
                0.0f, 0.0f,     // top left
                1.0f, 1.0f,     // bottom right
                1.0f, 0.0f      // top right
        };

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
    }

    private void SetupSticker() {
        loadImageTexture();

        //sprite.setSize(bitmapWidth/2, bitmapHeight/2);

        stickerSquareVertices = sprite.getTransformedVertices();

        // texture coordinates of sticker
        stickerTextureVertices = new float[] {

                0.0f, 0.0f,     // top left
                0.0f, 1.0f,     // bottom left
                1.0f, 1.0f,     // bottom right
                0.0f, 0.0f,     // top left
                1.0f, 1.0f,     // bottom right
                1.0f, 0.0f      // top right
        };

        // create texture coordinates for sticker
        ByteBuffer bb1 = ByteBuffer.allocateDirect(stickerTextureVertices.length * 4);
        bb1.order(ByteOrder.nativeOrder());
        imageTextureVerticesBuffer = bb1.asFloatBuffer();
        imageTextureVerticesBuffer.put(stickerTextureVertices);
        imageTextureVerticesBuffer.position(0);

        // create square vertex buffer for sticker
        ByteBuffer bb2 = ByteBuffer.allocateDirect(stickerSquareVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        stickerSquareVertexBuffer = bb2.asFloatBuffer();
        stickerSquareVertexBuffer.put(stickerSquareVertices);
        stickerSquareVertexBuffer.position(0);
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

    private static void UpdateSprite() {
        stickerSquareVertices = sprite.getTransformedVertices();

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(stickerSquareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        stickerSquareVertexBuffer = bb.asFloatBuffer();
        stickerSquareVertexBuffer.put(stickerSquareVertices);
        stickerSquareVertexBuffer.position(0);
    }

    // keep track of the last touch position
    static float mLastTouchX = 0, mLastTouchY = 0;
    // check whether the sticker can be moved or not
    static boolean moveAble = false;

    public static void processTouchEvent(MotionEvent event)
    {
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                float x = event.getRawX();
                float y = event.getRawY();

                // because the coordinates received from touch event are relative to the top left corner
                // while opengl view is drawn relatively to the bottom left corner
                // so, int order to pick up y coordinate correctly, we need to translate the motionevent y-axis into opengl y-axis
                float glY = screen_height - y;

                Rect sticker = sprite.getStickerCoordinates();

                // check if the touch coordinates locate inside the sticker
                if (x <= sticker.right && x >= sticker.left && glY <= sticker.top && glY >= sticker.bottom) {
                    // if true, the sticker can be moved
                    moveAble = true;
                    mLastTouchX = x;
                    mLastTouchY = glY;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (moveAble) {
                    float x = event.getRawX();
                    float y = event.getRawY();

                    float glY = screen_height - y;

                    float dx = x - mLastTouchX;
                    float dy = glY - mLastTouchY;

                    sprite.translate(dx, dy);
                    // update new coordinates
                    UpdateSprite();

                    mLastTouchX = x;
                    mLastTouchY = glY;
                } else {
                    Log.d("CAMERA", "false");
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                moveAble = false;
            }
        }
    }
}
