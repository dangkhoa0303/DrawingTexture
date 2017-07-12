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
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{"+
                    "gl_Position = position;"+
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;" +
                    "uniform samplerExternalOES s_texture;" +
                    "uniform sampler2D img_texture;" +
                    "void main() {" +
                    "  vec4 camera = texture2D(s_texture, textureCoordinate);" +
                    "  vec4 img = texture2D(img_texture, textureCoordinate);" +
                    //"  gl_FragColor = vec4(mix(camera.rgb, img.rgb, img.a*1.0), camera.a);" +
                    "  gl_FragColor = camera;" +
                    "}";

    private String vs_Image = "attribute vec2 a_texCoord;"
            + "varying vec2 v_texCoord;"
            + "attribute vec4 a_Position;"
            + "void main() {"
            + "  gl_Position = a_Position;"
            + "  v_texCoord = a_texCoord;"
            + "}";

    private String fs_Image = "varying vec2 v_texCoord;"
            + "uniform sampler2D s_texture;"
            + "void main() {"
            + "  gl_FragColor = texture2D(s_texture, v_texCoord);"
            + "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;

    // variables used for camera preview texture
    private final int mProgram;
    private int mPositionHandle;
    private int mCameraTextureHandle;
    private int mTextureCoordHandle;

    // variables used for image texture
    private final int mImageProgram;
    private int mImagePositionHandle;
    private int mImageTextureHandle;
    private int mImageTextureCoordHandle;

    private FloatBuffer imageVertexBuffer;


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    static float squareVertices[] = {
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, -1.0f,
    };

    static float imageSquareVertices[] = {
            0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,
            0.5f, -0.5f,
            -0.5f, 0.5f,
            -0.5f, -0.5f
    };

    // in counterclockwise order:
    float textureVertices[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private short drawOrder[] = {
            0, 1, 2, 0, 2, 3
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

        ByteBuffer bb1 = ByteBuffer.allocateDirect(imageSquareVertices.length * 4);
        bb1.order(ByteOrder.nativeOrder());
        imageVertexBuffer = bb1.asFloatBuffer();
        imageVertexBuffer.put(imageSquareVertices);
        imageVertexBuffer.position(0);

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

        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);

        int img_vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, vs_Image);
        int img_fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, fs_Image);

        mImageProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mImageProgram, img_vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mImageProgram, img_fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mImageProgram);
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
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        mCameraTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        GLES20.glUniform1i(mCameraTextureHandle, 0);

        //mImageTextureHandle = GLES20.glGetUniformLocation(mProgram, "img_texture");
        //GLES20.glUniform1i(mImageTextureHandle, 1);

        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

        //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void drawImage() {
        GLES20.glUseProgram(mImageProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

        mImagePositionHandle = GLES20.glGetAttribLocation(mImageProgram, "a_Position");
        GLES20.glEnableVertexAttribArray(mImagePositionHandle);
        GLES20.glVertexAttribPointer(mImagePositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, imageVertexBuffer);

        mImageTextureCoordHandle = GLES20.glGetAttribLocation(mImageProgram, "a_texCoord");
        GLES20.glEnableVertexAttribArray(mImageTextureCoordHandle);
        GLES20.glVertexAttribPointer(mImageTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        mImageTextureHandle = GLES20.glGetUniformLocation(mImageProgram, "s_texture");
        GLES20.glUniform1i(mImageTextureHandle, 1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }
}
