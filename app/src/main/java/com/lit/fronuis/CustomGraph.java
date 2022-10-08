package com.lit.fronuis;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class CustomGraph extends View {

    ArrayList<Integer> pv, con = new ArrayList<>();
    int test = 10;
    Paint pvpaint, conpaint = new Paint();

    public CustomGraph(Context context) {
        super(context);
        construct();
    }

    public CustomGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public CustomGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construct();
    }


    public void construct()
    {
        conpaint = new Paint();
        pvpaint = new Paint();
        pvpaint.setStyle(Paint.Style.STROKE);
        conpaint.setStyle(Paint.Style.STROKE);
        pvpaint.setAntiAlias(true);
        conpaint.setAntiAlias(true);
        pvpaint.setStrokeWidth(8f);
        conpaint.setStrokeWidth(8f);
        conpaint.setColor(Color.RED);
        pvpaint.setColor(Color.YELLOW);
    }

    public void init(ArrayList<Integer> pvgraph, ArrayList<Integer> congraph) {
        pv = pvgraph;
        con = congraph;
        invalidate();
    }

    public void change(int kappa)
    {
        test = kappa;
        //invalidate();
    }

    public ArrayList<Point> intListToPoint(ArrayList<Integer> intList, int max)
    {
        int height = getHeight();
        int width = getWidth();
        ArrayList<Point> points = new ArrayList<>();

        for(int i = 0; i < intList.size(); i++)
            points.add(new Point((int) (width*1.0/(intList.size()-1)*i), (int) (height - (intList.get(i)/(max*1.0))*height)));

        return points;
    }

    public ArrayList<Point> connPoint(ArrayList<Point> points, int onetwo)
    {
        ArrayList<Point> conpoints = new ArrayList<>();
        for(int i = 1; i < points.size(); i++)
        {
            conpoints.add(new Point((points.get(i).x + points.get(i-1).x) / 2,points.get(i-onetwo).y));
        }
        return conpoints;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //wenn alle 30sec 1 neuer datenpunkt -> 2880 in 24h
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();

        int max = 0;
        for(Integer x: con)
            if(x > max)
                max = x;
        for(Integer x: pv)
            if(x > max)
                max = x;

        ArrayList<Point> conpoints = intListToPoint(con, max);
        ArrayList<Point> conconpoints2 = connPoint(conpoints, 0);
        ArrayList<Point> conconpoints1 = connPoint(conpoints, 1);

        ArrayList<Point> pvpoints = intListToPoint(pv, max);
        ArrayList<Point> pvconpoints2 = connPoint(pvpoints, 0);
        ArrayList<Point> pvconpoints1 = connPoint(pvpoints, 1);

        Path pvPath = new Path();
        Path conPath = new Path();

        pvPath.moveTo(pvpoints.get(0).x, pvpoints.get(0).y);
        conPath.moveTo(conpoints.get(0).x, conpoints.get(0).y);

        for(int i = 1; i < conpoints.size(); i++)
            conPath.cubicTo(
                    conconpoints1.get(i-1).x,
                    conconpoints1.get(i-1).y,
                    conconpoints2.get(i-1).x,
                    conconpoints2.get(i-1).y,
                    conpoints.get(i).x,
                    conpoints.get(i).y);

        for(int i = 1; i < pvpoints.size(); i++)
            pvPath.cubicTo(
                    pvconpoints1.get(i-1).x,
                    pvconpoints1.get(i-1).y,
                    pvconpoints2.get(i-1).x,
                    pvconpoints2.get(i-1).y,
                    pvpoints.get(i).x,
                    pvpoints.get(i).y);

        canvas.drawPath(pvPath, pvpaint);
        canvas.drawPath(conPath, conpaint);

    }
}