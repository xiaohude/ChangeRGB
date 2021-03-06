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
	
	/**手柄式放大镜*/
	public static final int HANDLE_MODE = 101;
	/**大小圆式放大镜*/
	public static final int CIRCLE_MODE = 102;
	/**全屏式放大镜*/
	public static final int SCREEN_MODE = 103;
	private int mode = CIRCLE_MODE;
	
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

  
	private OnGetRGBlistener onGetRGBlistener;
	
    public PathView(Context context, Bitmap bitmap) {  
        super(context); 
        mPath.addCircle(RADIUS, RADIUS, RADIUS, Direction.CW);  
        matrix.setScale(FACTOR, FACTOR);  
  
        this.bitmap = bitmap;
        
        paint = new Paint();
        paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);
		
		
		// 获取图片宽和高
		widths = bitmap.getWidth();
		heights = bitmap.getHeight();
		
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
    	
    	lCenterX = lCenterX < 0 ? 0 : lCenterX;
    	lCenterX = lCenterX > widths ? widths : lCenterX;
    	lCenterY = lCenterY < 0 ? 0 : lCenterY;
    	lCenterY = lCenterY > heights ? heights : lCenterY;
    	
    	if(onGetRGBlistener != null) {
	    	int color = bitmap.getPixel((int)lCenterX, (int)lCenterY);
	    	onGetRGBlistener.getRGB(color);
    	}
        
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
  
    
    @Override  
    public void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        // 底图  
        canvas.drawBitmap(bitmap, 0, 0, null);  
        
        if(mode == CIRCLE_MODE)
        	onCircleDraw(canvas);
        else if(mode == SCREEN_MODE)
        	onScreenDraw(canvas);
        else if(mode == HANDLE_MODE)
        	onHandleDraw(canvas);
        
    }  
    
	//大小圆式放大镜
    public void onCircleDraw(Canvas canvas) {  
    	
    	//绘制放大镜圆形框
    	canvas.drawCircle(mCenterX, mCenterY, RADIUS + 4, paint);
    	//绘制放大镜手柄
    	canvas.drawLine(startX, startY, endX, endY, paint);
    	//绘制小圆
    	canvas.drawCircle(lCenterX, lCenterY, RADIUS_LITTLE, paint);
    	//画小圆准星
    	if(onGetRGBlistener != null) {
	        canvas.drawLine(lCenterX , lCenterY + 2,
	        				lCenterX , lCenterY + 8, paint);
	        canvas.drawLine(lCenterX + 2, lCenterY ,
	        				lCenterX + 8, lCenterY, paint);
	        canvas.drawLine(lCenterX , lCenterY - 2,
	        				lCenterX , lCenterY - 8, paint);
	        canvas.drawLine(lCenterX - 2, lCenterY,
	        				lCenterX - 8, lCenterY, paint);
    	}
    	
    	
    	// 剪切  
    	// 先平移画布到大圆的切方形左上角
    	canvas.translate(mCenterX - RADIUS, mCenterY - RADIUS);  
    	// 再剪切一个大圆形画布窟窿
    	canvas.clipPath(mPath);  
    	// 画放大后的图  
    	// 再反向平移到合适的位置
    	canvas.translate((float)(RADIUS - lCenterX * FACTOR), 
    			(float)(RADIUS - lCenterY * FACTOR));  
    	// 最后画放大后的bitmap
    	canvas.drawBitmap(bitmap, matrix, null);  
    	//画大圆准星
    	if(onGetRGBlistener != null) {
	        canvas.drawLine(lCenterX * FACTOR , lCenterY * FACTOR + 3,
	        				lCenterX * FACTOR , lCenterY * FACTOR + 20, paint);
	        canvas.drawLine(lCenterX * FACTOR + 3, lCenterY * FACTOR ,
	        				lCenterX * FACTOR + 20, lCenterY * FACTOR, paint);
	        canvas.drawLine(lCenterX * FACTOR , lCenterY * FACTOR - 3,
	        				lCenterX * FACTOR , lCenterY * FACTOR - 20, paint);
	        canvas.drawLine(lCenterX * FACTOR - 3, lCenterY * FACTOR ,
	        				lCenterX * FACTOR - 20, lCenterY * FACTOR, paint);
    	}
    	
    }  
    
    //全屏式放大镜
    public void onScreenDraw(Canvas canvas) {  
    	
    	// 画放大后的图  
    	canvas.translate((float)(widths/2 - (lCenterX)* FACTOR ), 
    			(float)(heights/2 - (lCenterY)* FACTOR ));  
    	canvas.drawBitmap(bitmap, matrix, null); 
    	// 画个外边框
    	canvas.drawRect(0, 0, widths * FACTOR, heights * FACTOR, paint);
    	
    }  
    
    //手柄式放大镜
    public void onHandleDraw(Canvas canvas) {  
    
        //绘制放大镜圆形框
        canvas.drawCircle(mCenterX, mCenterY, RADIUS + 4, paint);
        //绘制瞄准镜
        canvas.drawLine(mCenterX, mCenterY-RADIUS,
						mCenterX, mCenterY-RADIUS-20, paint);
        canvas.drawLine(mCenterX, mCenterY+RADIUS,
						mCenterX, mCenterY+RADIUS+20, paint);
        canvas.drawLine(mCenterX-RADIUS, mCenterY,
						mCenterX-RADIUS-20, mCenterY, paint);
        canvas.drawLine(mCenterX+RADIUS, mCenterY,
						mCenterX+RADIUS+20, mCenterY, paint);
        //绘制放大镜手柄
        paint.setStrokeWidth(10);
    	canvas.drawLine(startX, startY, endX, endY, paint);
        paint.setStrokeWidth(4);
        canvas.drawRect(startX, startY, endX, endY, paint);
        
        // 剪切  
        canvas.translate(mCenterX - RADIUS, mCenterY - RADIUS);  
        canvas.clipPath(mPath);  
        // 画放大后的图  
        canvas.translate((float)(RADIUS - mCenterX * FACTOR), 
        				 (float)(RADIUS - mCenterY * FACTOR));  
        canvas.drawBitmap(bitmap, matrix, null);  

    } 
    
    

    public interface OnGetRGBlistener {
    	void getRGB (int color);
    }
    
    public void setOnGetRGBlistener (OnGetRGBlistener onGetRGBlistener) {
    	this.onGetRGBlistener = onGetRGBlistener;
    }
    
    /** 设置放大镜模式:
     * @param CIRCLE_MODE 大小圆式放大镜,
     * @param SCREEN_MODE 全屏式放大镜 ,
     * @param HANDLE_MODE 手柄式放大镜
     */
    public void setMode(int mode) {
    	this.mode = mode;
    }
}  
