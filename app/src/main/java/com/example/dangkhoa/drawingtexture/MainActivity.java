package com.example.dangkhoa.drawingtexture;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, View.OnTouchListener {

    private OpenGLSurface openGLSurface;
    private Camera mCamera;
    private SurfaceTexture surfaceTexture;
    private GLRenderer renderer;

    private float[] mOrientationM = new float[16];
    private float[] mRatio = new float[2];

    private RelativeLayout root;
    private RelativeLayout openglLayout;
    private Button showHideButton;
    private ImageView sticker;

    private RectF stickerCoords;

    private Point size;

    private int screen_width, screen_height;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        Log.d("MAIN", "SIZE " + screen_width + " " + screen_height);

        openGLSurface = new OpenGLSurface(this);
        renderer = openGLSurface.getRenderer();

        openglLayout = (RelativeLayout) findViewById(R.id.opengl_layout);
        openglLayout.addView(openGLSurface, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        sticker = (ImageView) findViewById(R.id.sticker);
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.mario);
        sticker.setImageBitmap(bmp);
        // use this to obtain coordinates of the sticker imageview
        sticker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float left = sticker.getX();
                float right = left + sticker.getWidth();

                float top = sticker.getY() + 168;
                float topGL = screen_height - top;

                float bottomGL = topGL - sticker.getHeight();

                stickerCoords = new RectF(left, topGL, right, bottomGL);

                renderer.setStickerCoordinates(stickerCoords);

                renderer.setStickerImageView(sticker);

                Log.d("MAIN", "" + left + " " + top + " " + right + " " + bottomGL);
                sticker.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        root = (RelativeLayout) findViewById(R.id.relative_layout);
        root.setOnTouchListener(this);

        //Log.d("BASE", "" + stickerCoords.left);

        showHideButton = (Button) findViewById(R.id.show_hide_button);
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sticker.setVisibility(View.INVISIBLE);
                //DirectVideo.showHideImageView();
            }
        });
    }


    public void startCamera(int texture, int width, int height) {
        surfaceTexture = new SurfaceTexture(texture);
        surfaceTexture.setOnFrameAvailableListener(this);
        renderer.setSurface(surfaceTexture);

        int camera_width =0;
        int camera_height =0;

        mCamera = Camera.open(0);

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Parameters param = mCamera.getParameters();
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        if(psize.size() > 0 ){
            int i;
            for (i = 0; i < psize.size(); i++){
                if(psize.get(i).width < width || psize.get(i).height < height)
                    break;
            }
            if(i>0)
                i--;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);

            camera_width = psize.get(i).width;
            camera_height= psize.get(i).height;
        }

        //get the camera orientation and display dimension------------
        if(this.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT){
            Matrix.setRotateM(mOrientationM, 0, 90.0f, 0f, 0f, 1f);
            mRatio[1] = camera_width*1.0f/height;
            mRatio[0] = camera_height*1.0f/width;
        }
        else{
            Matrix.setRotateM(mOrientationM, 0, 0.0f, 0f, 0f, 1f);
            mRatio[1] = camera_height*1.0f/height;
            mRatio[0] = camera_width*1.0f/width;
        }

        mCamera.setParameters(param);
        mCamera.startPreview();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        openGLSurface.requestRender();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            System.exit(0);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        DirectVideo.processTouchEvent(motionEvent);
        return true;
    }
}
