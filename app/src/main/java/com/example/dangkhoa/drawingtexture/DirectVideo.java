package com.example.dangkhoa.drawingtexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


// http://www.felixjones.co.uk/neo%20website/Android_View/

/**
 * Created by chau on 04.03.15.
 */
public class DirectVideo {

    private Context mContext;

    private static ImageView stickerImageView;

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

    private RectF base;

    public DirectVideo(Context context, RectF base, ImageView imageView)
    {
        mContext = context;

        this.base = base;
        stickerImageView = imageView;

        /*stickerImageView = new ImageView(mContext);
        stickerImageView.setVisibility(View.INVISIBLE);
        stickerImageView.setImageResource(R.drawable.mario);*/

        sprite = new Sprite();
        sprite.setStickerCoordinates(base.left, base.top, base.right, base.bottom);

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

                /*0.0f, 0.0f,     // top left
                0.0f, 1.0f,     // bottom left
                1.0f, 1.0f,     // bottom right
                0.0f, 0.0f,     // top left
                1.0f, 1.0f,     // bottom right
                1.0f, 0.0f*/      // top right

                0.0f, 1.0f,             // bottom left
                1.0f, 1.0f,             // bottom right
                1.0f, 0.0f,             // top right
                0.0f, 1.0f,             // bottom left
                1.0f, 0.0f,             // top right
                0.0f, 0.0f              // top left
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

        //Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mario);
        Bitmap bmp = convertImageViewToBitmap(stickerImageView);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        //bmp.recycle();
    }

    private Bitmap convertImageViewToBitmap(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        return bitmap;
    }

    public static void showHideImageView() {
        stickerImageView.setVisibility(View.INVISIBLE);
        int v = stickerImageView.getVisibility();
        Log.d("CAMERA", "" + (v-View.VISIBLE));
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

    //------------------------------------------------------------------------------------------------------------------------------------
    /*
        HANDLE TOUCH EVENT
     */

    private static void UpdateSprite() {
        stickerSquareVertices = sprite.getTransformedVertices();

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(stickerSquareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        stickerSquareVertexBuffer = bb.asFloatBuffer();
        stickerSquareVertexBuffer.put(stickerSquareVertices);
        stickerSquareVertexBuffer.position(0);
    }

    // calculate the distance between 2 points
    private static float spacing(float x, float y, float x1, float y1) {
        float dx = x - x1;
        float dy = y - y1;
        return (float) Math.sqrt(dx*dx + dy*dy);
    }

    // calculate the middle point between 2 points
    private static PointF getMidpoint(float x, float y, float x1, float y1) {
        float midX = (x + x1)/2;
        float midY = (y + y1)/2;
        return new PointF(midX, midY);
    }

    // calculate the angle for rotation
    private static float getAngle(float x, float y, float x1, float y1) {
        float dx = x - x1;
        float dy = y - y1;
        float radians = (float) Math.atan2(dy, dx);
        return (float) Math.toRadians(radians);
        //return radians;
    }

    // keep track of the last touch position (used for moving)
    private static float mLastTouchX = 0, mLastTouchY = 0;

    // keep track of the previous distance between 2 pointers (used for scaling)
    private static float previousDistance = 0;

    // keep track of the previous angle
    private static float previousAngle = 0;

    // check whether the sticker can be moved or not
    private static boolean moveAble = false;
    // check whether the sticker can be scaled or not
    private  static boolean scalable = false;

    private static int DRAG = 1;
    private static int ZOOM = 2;
    private static int NONE = 0;
    private static int MODE;

    // using getY is different from getRawY
    // getDrawY returns the exact pixel of the screen resolution but cannot be used with pinter index
    // so switch to use getY, get Y return the pixels less than those of getRawY offsetY pixels
    private static float offsetY = 80;

    private static final int INVALID_POINTER_ID = -1;
    private static int mActivePointerId = INVALID_POINTER_ID;

    public static void processTouchEvent(MotionEvent event)
    {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch (action) {
            // first finger down
            case MotionEvent.ACTION_DOWN: {
                Log.d("CAMERA", "DOWN");
                float x = event.getX();
                float y = event.getY() + offsetY;

                // because the coordinates received from touch event are relative to the top left corner
                // while opengl view is drawn relatively to the bottom left corner
                // so, int order to pick up y coordinate correctly, we need to translate the motionevent y-axis into opengl y-axis
                float glY = screen_height - y;

                RectF sticker = sprite.getStickerCoordinates();

                // check if the touch coordinates locate inside the sticker
                if (x <= sticker.right && x >= sticker.left && glY <= sticker.top && glY >= sticker.bottom) {
                    // if true, the sticker can be moved
                    //moveAble = true;
                    //scalable = false;
                    MODE = DRAG;
                    mLastTouchX = x;
                    mLastTouchY = glY;

                    mActivePointerId = event.getPointerId(0);

                    Log.d("CAMERA DIRECT", "" + true + " " + stickerImageView.getX());
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                Log.d("CAMERA", "POINTER DOWN");
                // get the first pointer
                float x = event.getX(0);
                float y = event.getY(0);
                float glY = screen_height - y;

                // get the second pointer
                float x1 = event.getX(1);
                float y1 = event.getY(1);
                float glY1 = screen_height - y1;

                previousAngle = x1;

                // calculate the distance between 2 pointers
                previousDistance = spacing(x, glY, x1, glY1);

                // get the coordinates of the sticker
                RectF sticker = sprite.getStickerCoordinates();

                // check if the second pointer is in the range of zoom. and check if the distance between 2 pointers is large enough to scale
                if (x1 <= sticker.right && x1 >= sticker.left && glY1 <= sticker.top && glY1 >= sticker.bottom && previousDistance >= 100) {
                    scalable = true;
                    moveAble = false;
                    MODE = NONE;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                float x = event.getX(0);
                float y = event.getY(0) + offsetY;
                float glY = screen_height - y;

                //Log.d("CAMERA", "MOVE " + event.getY(1));


                if (MODE == DRAG) {
                    Log.d("CAMERA", "DRAG " + mLastTouchX + " " + mLastTouchY);
                    //Log.d("CAMERA", "MOVE " + scalable);
                    float dx = x - mLastTouchX;
                    float dy = glY - mLastTouchY;

                    /*Rect sticker = sprite.getStickerCoordinates();

                    if (sticker.left <= 1 && sticker.bottom > 1) {
                        if (dx < 0) {
                            sprite.translate(0, dy);
                        } else {
                            sprite.translate(dx, dy);
                        }
                    }
                    else if (sticker.bottom <= 1 && sticker.left > 1) {
                        if (dy < 0) {
                            sprite.translate(dx, 0);
                        } else {
                            sprite.translate(dx, dy);
                        }
                    }
                    else if (sticker.left <= 1 && sticker.bottom <= 1) {
                        if (dx < 0 && dy >=0) {
                            sprite.translate(0, dy);
                        }
                        else if (dy < 0 && dx >= 0) {
                            sprite.translate(dx, 0);
                        }
                        else if (dx < 0 && dy < 0) {
                            sprite.translate(0, 0);
                        } else {
                            sprite.translate(dx, dy);
                        }
                    }
                    else*/
                        sprite.translate(dx, dy);

                    UpdateSprite();
                    stickerImageView.setX(stickerImageView.getX() + dx);

                    stickerImageView.setY(stickerImageView.getY() - dy);

                    // update last position
                    mLastTouchX = x;
                    mLastTouchY = glY;
                }

                // check if scale mode is on
                // IMPORTANT NOTE: check the number of pointers >= 2.
                // If not, it crashes because it detects only one pointer for moving. So, calling getX(1) will cause pointer index out range pointer
                /*else if (MODE == ZOOM) {

                    // https://stackoverflow.com/questions/26452574/android-zooming-with-two-fingers-ontouch-and-setscalex-setscaley

                    Log.d("CAMERA", "SCALE " + scalable);

                    float x1 = event.getX(1);
                    float y1 = event.getY(1) + offsetY;
                    float glY1 = screen_height - y1;

                    Rect sticker = sprite.getStickerCoordinates();

                    // get the midpoint between 2 pointers
                    PointF midpoint = getMidpoint(x, glY, x1, glY1);

                    // get the midpoint of the sticker itself
                    PointF preMidpoint = getMidpoint(sticker.left, sticker.top, sticker.right, sticker.bottom);

                    // calculate delta between 2 midpoints
                    float deltaMidX = midpoint.x - preMidpoint.x;
                    float deltaMidY = midpoint.y - preMidpoint.y;

                    //Log.d("CAMERA", "SCALE " + x + " " + glY + " " + x1 + " " + glY1);

                    float newDistance = spacing(x, glY, x1, glY1);

                    if (newDistance >= 20) {
                        float ratio = newDistance/previousDistance;

                        // scale the sticker
                        sprite.scale(ratio);
                        // move the sticker to the midpoint between 2 pointers
                        sprite.translate(deltaMidX, deltaMidY);
                        // update data
                        UpdateSprite();

                        previousDistance = newDistance;
                    }
                }*/
                break;
            }
            case MotionEvent.ACTION_UP: {
                moveAble = false;
                scalable = false;
                MODE = NONE;
                mActivePointerId = INVALID_POINTER_ID;
                Log.d("CAMERA", "ACTION UP " + mLastTouchX + " " + mLastTouchY);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                moveAble = false;
                scalable = false;
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                scalable = false;
                moveAble = true;
                MODE = DRAG;
                Log.d("CAMERA", "POINTER UP " + event.getActionIndex());
                break;
            }
        }
    }
}
