package com.example.dangkhoa.drawingtexture;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by dangkhoa on 14/07/2017.
 */

// http://androidblog.reindustries.com/a-real-opengl-es-2-0-2d-tutorial-part-4-transforming/

public class Sprite {
    float angle;
    float scale;
    private static RectF base;
    PointF translation;

    PointF rotationPoint;

    public static float top = 800;
    public static float bottom = 400;
    public static float left = 300;
    public static float right = 700;

    public Sprite()
    {
        // Initialise our intital size around the 0,0 point
        base = new RectF(left, top, right, bottom);

        // Initial translation
        translation = new PointF(0f, 0f);

        rotationPoint = new PointF((base.left+base.right)/2, (base.top+base.bottom)/2);

        // We start with our inital size
        scale = 1f;

        // We start in our inital angle
        angle = 0f;
    }


    public void translate(float deltax, float deltay)
    {
        // Update our location.
        translation.x = deltax;
        translation.y = deltay;
    }

    public void scale(float deltas)
    {
        scale = deltas;
    }

    public void rotate(float deltaa, PointF midpoint)
    {
        angle = deltaa;
        //rotationPoint = midpoint;
    }

    public float[] getTransformedVertices()
    {
        // Start with scaling
        float x1 = base.left * scale;
        float x2 = base.right * scale;
        float y1 = base.bottom * scale;
        float y2 = base.top * scale;

        // We now detach from our Rect because when rotating,
        // we need the seperate points, so we do so in opengl order
        PointF one = new PointF(x1, y2);            // top left
        PointF two = new PointF(x1, y1);            // bottom left
        PointF three = new PointF(x2, y1);          // bottom right
        PointF four = new PointF(x2, y2);           // top right

        // We create the sin and cos function once,
        // so we do not have calculate them each time.
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);

        // Then we rotate each point
        one.x = (x1) * c - (y2) * s;
        one.y = (x1) * s + (y2) * c;
        two.x = (x1) * c - (y1) * s;
        two.y = (x1) * s + (y1) * c;
        three.x = (x2) * c - (y1) * s;
        three.y = (x2) * s + (y1) * c;
        four.x = (x2) * c - (y2) * s;
        four.y = (x2) * s + (y2) * c;

        /*one.x = (x1-rotationPoint.x) * c - (y2-rotationPoint.y) * s + rotationPoint.x;
        one.y = (x1-rotationPoint.x) * s + (y2-rotationPoint.y) * c + rotationPoint.y;
        two.x = (x1-rotationPoint.x) * c - (y1-rotationPoint.y) * s + rotationPoint.x;
        two.y = (x1-rotationPoint.x) * s + (y1-rotationPoint.y) * c + rotationPoint.y;
        three.x = (x2-rotationPoint.x) * c - (y1-rotationPoint.y) * s + rotationPoint.x;
        three.y = (x2-rotationPoint.x) * s + (y1-rotationPoint.y) * c + rotationPoint.y;
        four.x = (x2-rotationPoint.x) * c - (y2-rotationPoint.y) * s + rotationPoint.x;
        four.y = (x2-rotationPoint.x) * s + (y2-rotationPoint.y) * c + rotationPoint.y;*/

        // Finally we translate the sprite to its correct position.
        one.x += translation.x;
        one.y += translation.y;
        two.x += translation.x;
        two.y += translation.y;
        three.x += translation.x;
        three.y += translation.y;
        four.x += translation.x;
        four.y += translation.y;

        setStickerCoordinates((int) one.x, (int) one.y, (int) three.x, (int) two.y);

        // We now return our float array of vertices.
        return new float[]
                {
                        one.x, one.y,       // top left
                        two.x, two.y,       // bottom left
                        three.x, three.y,   // bottom right
                        one.x, one.y,       // top left
                        three.x, three.y,   // bottom right
                        four.x, four.y,     // top right
                };
    }

    public static void setStickerCoordinates(float left, float top, float right, float bottom) {
        base.left = left;
        base.top = top;
        base.right = right;
        base.bottom = bottom;
    }

    public RectF getStickerCoordinates() {
        return base;
    }
}
