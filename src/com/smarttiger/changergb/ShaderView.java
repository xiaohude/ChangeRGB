package com.smarttiger.changergb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/** 这个方法是先把原图放大到设置倍数，再截取出那块圆形，很浪费资源容易内存泄漏崩溃 */
public class ShaderView extends View {  
    private Bitmap bitmap;  
    private ShapeDrawable drawable;  
    // 放大镜的半径  
    private static final int RADIUS = 120;  
    // 放大倍数  
    private static final int FACTOR = 3;  
    private final Matrix matrix = new Matrix();  

	public ShaderView(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context, attrs);
//        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.pic);  
//        bitmap = bmp;  
//        BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(bmp,  
//                bmp.getWidth() * FACTOR, bmp.getHeight() * FACTOR, true),  
//                TileMode.CLAMP, TileMode.CLAMP);  
//        // 圆形的drawable  
//        drawable = new ShapeDrawable(new OvalShape());  
//        drawable.getPaint().setShader(shader);  
//        drawable.setBounds(0, 0, RADIUS * 2, RADIUS * 2);  
	}
    public ShaderView(Context context, Bitmap bmp) {  
        super(context);  
//        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.pic);  
        bitmap = bmp;  
        BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(bmp,  
                bmp.getWidth() * FACTOR, bmp.getHeight() * FACTOR, true),  
                TileMode.CLAMP, TileMode.CLAMP);  
        // 圆形的drawable  
        drawable = new ShapeDrawable(new OvalShape());  
        drawable.getPaint().setShader(shader);  
        drawable.setBounds(0, 0, RADIUS * 2, RADIUS * 2);  
    }  
    
//    public void setBitmap(Bitmap bitmap) {
//    	this.bitmap = bitmap;
//        BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(bitmap,  
//        		bitmap.getWidth() * FACTOR, bitmap.getHeight() * FACTOR, true),  
//                TileMode.CLAMP, TileMode.CLAMP);  
//        // 圆形的drawable  
//        drawable = new ShapeDrawable(new OvalShape());  
//        drawable.getPaint().setShader(shader);  
//        drawable.setBounds(0, 0, RADIUS * 2, RADIUS * 2);  
//    }
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        final int x = (int) event.getX();  
        final int y = (int) event.getY();  
        // 这个位置表示的是，画shader的起始位置  
  
        matrix.setTranslate(RADIUS - x * FACTOR, RADIUS - y * FACTOR);  
        drawable.getPaint().getShader().setLocalMatrix(matrix);  
        // bounds，就是那个圆的外切矩形  
  
        drawable.setBounds(x - RADIUS, y - RADIUS, x + RADIUS, y + RADIUS);  
        invalidate();  
        return true;  
    }  
  
    @Override  
    public void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        canvas.drawBitmap(bitmap, 0, 0, null);  
        drawable.draw(canvas);  
    }  
}  