package edu.wm.cs.cs301.elise.amazebyelise.generation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Handles maze graphics.
 */
public class MazePanel extends View {
    // https://developer.android.com/training/custom-views/create-view
    // https://developer.android.com/training/custom-views/custom-drawing
    // on how to implement your own View class
    Bitmap bitMap;
    Canvas canvas;
    Paint paint;

    /**
     * Constructor with one context parameter.
     * @param context
     */
    public MazePanel(Context context) {
        // call super class constructor as necessary
        super(context);
        bitMap = Bitmap.createBitmap(Constants.VIEW_WIDTH, Constants.VIEW_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitMap);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }
    /**
     * Constructor with two parameters: context and attributes.
     * @param context
     * @param app
     */
    public MazePanel(Context context, AttributeSet app) {
        // call super class constructor as necessary
        super(context, app);
        bitMap = Bitmap.createBitmap(Constants.VIEW_WIDTH, Constants.VIEW_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitMap);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }
    /**
     * Draws given canvas.
     * @param c canvas
     */
    @Override
    public void onDraw(Canvas c) {
        c.drawBitmap(bitMap, 0, 0, paint);

    }

    /**
     * Measures the view and its content to determine the measured width and the measured height.
     * @param width
     * @param height
     */
    @Override
    public void onMeasure(int width, int height) {
        // as described for superclass method
        super.onMeasure(width, height);
        this.setMeasuredDimension(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT);
    }

    /**
     * Updates maze graphics.
     */
    public void update() {
        invalidate();
    }

    /**
     * Takes in color string, sets paint color to corresponding color.
     * @param c a string for color
     */
    public void setColor(String c) {
        switch (c) {
            case "Blue":
                paint.setColor(Color.BLUE);
                break;
            case "Red":
                paint.setColor(Color.RED);
                break;
            case "Yellow":
                paint.setColor(Color.YELLOW);
                break;
            case "Green":
                paint.setColor(Color.GREEN);
                break;
            case "Black":
                paint.setColor(Color.BLACK);
                break;
            case "Gray":
                paint.setColor(Color.GRAY);
                break;
            case "LightGray":
                paint.setColor(Color.LTGRAY);
                break;
            case "DarkGray":
                paint.setColor(Color.DKGRAY);
                break;
            case "White":
                paint.setColor(Color.WHITE);
                break;
        }
    }

    /**
     * Sets paint object color attribute to given color.
     * @param color
     */
    public void setColor(int color) {
        paint.setColor(color);
    }

    /**
     * Takes in color integer values [0-255], returns corresponding color-int value.
     * @param red
     * @param green
     * @param blue
     */
    public static int getColorEncoding(int red, int green, int blue) {
        return Color.rgb(red, green, blue);
    }

    /**
     * Returns the RGB value representing the current color.
     * @return integer RGB value
     */
    public int getColor() {
        return paint.getColor();
    }

    /**
     * Takes in rectangle params, fills rectangle in canvas based on these.
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void fillRect(int x, int y, int width, int height) {
        // draw a filled rectangle on the canvas, requires decision on its color
        Rect rectangle = new Rect(x, y, x+width,y+height);
        canvas.drawRect(rectangle, paint);
    }

    /**
     * Takes in polygon params, fills polygon in canvas based on these.
     * Paint is always that for corn.
     * @param xPoints
     * @param yPoints
     * @param nPoints
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints){
        // translate the points into a path
        // draw a path on the canvas
        Path path = new Path();
        path.reset();
        path.moveTo(xPoints[0],yPoints[0]);
        for(int i = 1; i < xPoints.length; i = i + 1) {
            path.lineTo(xPoints[i], yPoints[i]);
        }
        canvas.drawPath(path, paint);
    }

    /**
     * Takes in line params, draws line in canvas based on these.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    /**
     * Takes in oval params, fills oval in canvas based on these.
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void fillOval(int x, int y, int width, int height) {
        RectF oval = new RectF(x, y, x + width, y + height);
        canvas.drawOval(oval, paint);
    }

}