package com.smarttiger.changergb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


/**
 * 此方法是先截取要放大的区域，再进行放大，节约资源，不容易内存泄漏。
 * @author SmartTiger
 */
public class PathView extends View {  
    private final Path mPath = new Path();  
    private final Matrix matrix = new Matrix();  
    private final Bitmap bitmap;  
    // 放大镜的半径  
    private static final int RADIUS = 160;  
    // 放大倍数  
    private static final int FACTOR = 3;  
    // 小圆半径
    private static final int RADIUS_LITTLE = RADIUS / FACTOR;  
    // 大圆的圆心坐标
    private float mCenterX, mCenterY;  
    //大小圆中心坐标距离。
    private static final float DISTANCE = 2*(RADIUS + RADIUS_LITTLE);
    // 小圆的圆心坐标
    private float lCenterX, lCenterY;  
    // 手柄坐标
    private float startX, endX, startY, endY;
    
    
    private Paint paint;
    
	// 屏幕宽和高
	private int widths, heights;
	private boolean isConfineTop = false;// 是否接近上边界处，用来置反放大镜方向。
	private boolean isConfineLeft = false;// 是否接近左边界处，用来置反放大镜方向。

  
    public PathView(Context context, Bitmap bitmap) {  
        super(context); 
        mPath.addCircle(RADIUS, RADIUS, RADIUS, Direction.CW);  
        matrix.setScale(FACTOR, FACTOR);  
  
        this.bitmap = bitmap;
        
        paint = new Paint();
        paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);
		
		
		// 获取屏幕宽和高
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		widths = display.getWidth();
		heights = display.getHeight();
		
		//初始化放大镜在屏幕中间
		lCenterX = widths / 2;
		lCenterY = heights / 2;
        mCenterX = (float) (lCenterX - DISTANCE/1.414);  
        mCenterY = (float) (lCenterY - DISTANCE/1.414);
        startX = (float) (lCenterX - RADIUS_LITTLE/1.414);
        endX = (float) (mCenterX + RADIUS/1.414);
        startY = (float) (lCenterY - RADIUS_LITTLE/1.414);
        endY = (float) (mCenterY + RADIUS/1.414);
		
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) { 
    	lCenterX = event.getX();
    	lCenterY = event.getY();
        
    	//下面是两种置反放大镜逻辑，一种是左上优先，一种是碰边界再置反。
//        isConfineLeft = lCenterX < DISTANCE/1.414+RADIUS;
//        isConfineTop = lCenterY < DISTANCE/1.414+RADIUS;
        if(lCenterX < DISTANCE/1.414+RADIUS)
        	isConfineLeft = true;
        if(lCenterY < DISTANCE/1.414+RADIUS)
        	isConfineTop = true;
        if(lCenterX > widths - (DISTANCE/1.414+RADIUS))
        	isConfineLeft = false;
        if(lCenterY > heights - (DISTANCE/1.414+RADIUS))
        	isConfineTop = false;
        
        if(isConfineLeft) {
            mCenterX = (float) (lCenterX + DISTANCE/1.414);
            startX = (float) (lCenterX + RADIUS_LITTLE/1.414);
            endX = (float) (mCenterX - RADIUS/1.414);
        } else {
            mCenterX = (float) (lCenterX - DISTANCE/1.414);
            startX = (float) (lCenterX - RADIUS_LITTLE/1.414);
            endX = (float) (mCenterX + RADIUS/1.414);
        }
        if(isConfineTop) {
            mCenterY = (float) (lCenterY + DISTANCE/1.414);
            startY = (float) (lCenterY + RADIUS_LITTLE/1.414);
            endY = (float) (mCenterY - RADIUS/1.414);
        } else {
            mCenterY = (float) (lCenterY - DISTANCE/1.414);
            startY = (float) (lCenterY - RADIUS_LITTLE/1.414);
            endY = (float) (mCenterY + RADIUS/1.414);
        }
        
        invalidate();  
        return true;  
    }  
  
    //大小圆式放大镜
    @Override  
    public void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        // 底图  
        canvas.drawBitmap(bitmap, 0, 0, null);  
        
        //绘制放大镜圆形框
        canvas.drawCircle(mCenterX, mCenterY, RADIUS + 4, paint);
        //绘制放大镜手柄
        canvas.drawLine(startX, startY, endX, endY, paint);
        //绘制小圆
        canvas.drawCircle(lCenterX, lCenterY, RADIUS_LITTLE, paint);
        
        // 剪切  
        canvas.translate(mCenterX - RADIUS, mCenterY - RADIUS);  
        canvas.clipPath(mPath);  
        // 画放大后的图  
        canvas.translate((float)(RADIUS - lCenterX * FACTOR), 
        				 (float)(RADIUS - lCenterY * FACTOR));  
        canvas.drawBitmap(bitmap, matrix, null);  

    }  
    
    //手柄式放大镜
//    @Override  
//    public void onDraw(Canvas canvas) {  
//        super.onDraw(canvas);  
//        // 底图  
//        canvas.drawBitmap(bitmap, 0, 0, null);  
//    
//        //绘制放大镜圆形框
//        canvas.drawCircle(mCurrentX, mCurrentY, RADIUS + 4, paint);
//        //绘制瞄准镜
//        canvas.drawLine(mCurrentX, mCurrentY-RADIUS,
//						mCurrentX, mCurrentY-RADIUS-20, paint);
//        canvas.drawLine(mCurrentX, mCurrentY+RADIUS,
//						mCurrentX, mCurrentY+RADIUS+20, paint);
//        canvas.drawLine(mCurrentX-RADIUS, mCurrentY,
//						mCurrentX-RADIUS-20, mCurrentY, paint);
//        canvas.drawLine(mCurrentX+RADIUS, mCurrentY,
//						mCurrentX+RADIUS+20, mCurrentY, paint);
//        //绘制放大镜手柄
//        canvas.drawLine((float)(mCurrentX+RADIUS/1.414), (float)(mCurrentY+RADIUS/1.414), 
//        				(float)(mCurrentX+RADIUS*1.5), (float)(mCurrentY+RADIUS*1.5), paint);
////        //绘制小圆
////        canvas.drawCircle((float)(mCurrentX+RADIUS*1.5+RADIUS_LITTLE/1.414), 
////        				(float)(mCurrentY+RADIUS*1.5+RADIUS_LITTLE/1.414), RADIUS_LITTLE, paint);
//        
//        // 剪切  
//        canvas.translate(mCurrentX - RADIUS, mCurrentY - RADIUS);  
//        canvas.clipPath(mPath);  
//        // 画放大后的图  
//        canvas.translate((float)(RADIUS - mCurrentX * FACTOR), 
//        				 (float)(RADIUS - mCurrentY * FACTOR));  
////        canvas.translate((float)(RADIUS - (mCurrentX + RADIUS*1.5+RADIUS_LITTLE/1.414) * FACTOR), 
////        				 (float)(RADIUS - (mCurrentY + RADIUS*1.5+RADIUS_LITTLE/1.414) * FACTOR)); 
//        canvas.drawBitmap(bitmap, matrix, null);  
//
//    } 
}  
