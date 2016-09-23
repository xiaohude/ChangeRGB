package com.smarttiger.changergb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.view.MotionEvent;
import android.view.View;


/**
 * 此方法是先截取要放大的区域，再进行放大，节约资源，不容易内存泄漏。
 * @author zhuxh2
 */
public class PathView extends View {  
    private final Path mPath = new Path();  
    private final Matrix matrix = new Matrix();  
    private final Bitmap bitmap;  
    // 放大镜的半径  
    private static final int RADIUS = 200;  
    // 放大倍数  
    private static final int FACTOR = 3;  
    private int mCurrentX, mCurrentY;  
  
    public PathView(Context context, Bitmap bitmap) {  
        super(context);  
        mPath.addCircle(RADIUS, RADIUS, RADIUS, Direction.CW);  
        matrix.setScale(FACTOR, FACTOR);  
  
//        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo);  
        this.bitmap = bitmap;
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        mCurrentX = (int) event.getX();  
        mCurrentY = (int) event.getY();  
  
        invalidate();  
        return true;  
    }  
  
    @Override  
    public void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        // 底图  
        canvas.drawBitmap(bitmap, 0, 0, null);  
        // 剪切  
        canvas.translate(mCurrentX - RADIUS, mCurrentY - RADIUS);  
        canvas.clipPath(mPath);  
        // 画放大后的图  
        canvas.translate(RADIUS - mCurrentX * FACTOR, RADIUS - mCurrentY * FACTOR);  
        canvas.drawBitmap(bitmap, matrix, null);  
    }  
}  
