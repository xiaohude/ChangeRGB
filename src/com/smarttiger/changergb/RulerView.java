package com.smarttiger.changergb;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class RulerView extends View {

	private boolean isCalibration = false;
	private float calibration = 1;//用来校准尺子的基数
	private float calibration_saved;
	private float xmm_saved;
	private Rect rectBtn;
	

	private float xdp = 3;//dp单位
	
	private float xmm;//毫米单位
	private float ruler_length;
	private float ruler_width;
	private PointF mid_point = new PointF(0, 0);//尺子刻度中间点的坐标，已此坐标为参考点方便绘画移动等。
	private Rect rect = new Rect();
	private float angle_rotate = 0;
	private float angle_initial;
	private float angle_saved;
	private float distance_initial;
	private float distance_saved;
	private Boolean MultiToSingle = false;
	private Boolean SingleToMulti = true;
	private PointF mid_point_between_fingers = new PointF();
	private PointF mid_point_between_fingers_down = new PointF();
	private PointF finger_first_down = new PointF();
	private PointF finger_second_down = new PointF();
	private PointF mid_point_saved = new PointF();
	private String MODE = "NONE";
	private Paint paint;
	private Paint paintTxt;
	private Paint paintBtn;
	

	private SharedPreferences preferences;
	
	public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		init(context, dm, 6);
	}
	public RulerView(Context context, DisplayMetrics dm,int length) {
		super(context);
		init(context, dm, length);
	}
	public RulerView(Context context, DisplayMetrics dm,PointF point) {
		super(context);
		init(context, dm, 6);
		mid_point.set(point);
	}
	public void init(Context context, DisplayMetrics dm,int length) {
		find_pixal(dm);
		ruler_length = length * xmm * 10; // 设置一开始为length厘米的尺子
		ruler_width = 200;
		mid_point.set((float) (ruler_length * 0.5), 0);
		rect = new Rect((int)(mid_point.x-ruler_length/2), 
						(int) mid_point.y, 
						(int)(mid_point.x+ruler_length/2), 
						(int)(mid_point.y+ruler_width));
		rectBtn = new Rect(rect.right, rect.top, (int)(rect.right+ruler_width), rect.bottom);
		
		paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		
		paintTxt = new Paint();
		paintTxt.setColor(Color.GREEN);
		paintTxt.setStyle(Style.FILL);
		paintTxt.setTextSize(30);
		
		paintBtn = new Paint();
		paintBtn.setColor(Color.GREEN);
		paintBtn.setStyle(Style.FILL_AND_STROKE);
		paintBtn.setStrokeWidth(2);
		paintBtn.setTextSize(50);
		paintBtn.setTextAlign(Paint.Align.CENTER);//文本居中
		
	}
	
	//用于方便校准尺子，将尺子竖右边，并设置8个长度。
	private void HorizontalRuler() {
		ruler_length = 8 * xmm * 10;
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		mid_point.set((float) (width-80), ruler_length/2+4);
		angle_rotate = 90;
		rect = new Rect((int)(mid_point.x-ruler_length/2), 
						(int) mid_point.y, 
						(int)(mid_point.x+ruler_length/2), 
						(int)(mid_point.y+ruler_width));
		rectBtn = new Rect(rect.right, rect.top, (int)(rect.right+ruler_width), rect.bottom);
	}
	
	private static final String SAVE_CALIBRATION = "save_calibration";
	private void saveCalibration() {
		preferences.edit().putFloat(SAVE_CALIBRATION, calibration).commit();
	}
	private float getCalibration() {
		return preferences.getFloat(SAVE_CALIBRATION, 1);
	}
	
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.save();
		canvas.rotate(angle_rotate, mid_point.x, mid_point.y);
		canvas.drawRect(rect, paint);//画尺子边界
		
		if(!isCalibration) {
			for (int i = 0; i < ruler_length / xmm; i++) {
				float Left = mid_point.x - ruler_length / 2;
				if (i == 0) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 60, paint);
					canvas.drawText("0cm",
									Left + i * xmm, mid_point.y + 70, paintTxt);
				} else if (i % 10 == 0) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 60, paint);
					canvas.drawText(Integer.toString(i / 10),
									Left + i * xmm, mid_point.y + 70, paintTxt);
				} else if ((i+5) % 10 == 0) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 40, paint);
				} else {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 25, paint);
				}
			}
			canvas.drawRect(rectBtn, paint);
			canvas.drawText("校准", mid_point.x+ruler_length/2+ruler_width/2, mid_point.y+ruler_width/2+15,  paintBtn);
		}
		else {
			for (int i = 0; i < ruler_length / xmm; i++) {
				float Left = mid_point.x - ruler_length / 2;
				if (i == 0) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 35, paint);
					canvas.drawText("0cm",
									Left + i * xmm, mid_point.y + 70, paintTxt);
				} else if (i == 63) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 60, paint);
					canvas.drawText("1,5元",
									Left + i * xmm -30, mid_point.y + 90, paintTxt);
					canvas.drawText("高度",
									Left + i * xmm -30, mid_point.y + 125, paintTxt);
				} else if (i == 70) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 60, paint);
					canvas.drawText("10元高度" ,
									Left + i * xmm -30, mid_point.y + 90, paintTxt);
					canvas.drawText("20元高度",
									Left + i * xmm -30, mid_point.y + 125, paintTxt);
				} else if (i % 10 == 0) {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 35, paint);
				} else {
					canvas.drawLine(Left + i * xmm, mid_point.y,
									Left + i * xmm, mid_point.y + 15, paint);
				}
			}
			canvas.drawRect(rectBtn, paint);
			canvas.drawText("完成", mid_point.x+ruler_length/2+ruler_width/2, mid_point.y+ruler_width/2+15,  paintBtn);
		}
		
		for (int i = 0; i < ruler_length / xdp; i++) {
			float Left = mid_point.x - ruler_length / 2;
			if (i == 0) {
				canvas.drawLine(Left + i*xdp, mid_point.y+ruler_width,
								Left + i*xdp, mid_point.y+ruler_width - 60, paint);
				canvas.drawText("0dp",
								Left + i*xdp, mid_point.y+ruler_width - 70, paintTxt);
			}
			else if (i % 10 == 0) {
				canvas.drawLine(Left + i*xdp, mid_point.y+ruler_width, 
								Left + i*xdp, mid_point.y+ruler_width - 40, paint);
			if(i % 50 == 0) {
				canvas.drawLine(Left + i*xdp, mid_point.y+ruler_width, 
								Left + i*xdp, mid_point.y+ruler_width - 60, paint);
				canvas.drawText(Integer.toString(i),
								Left + i*xdp, mid_point.y+ruler_width - 70, paintTxt);
			}
			} else if (i % 5 == 0)  {
				canvas.drawLine(Left + i*xdp, mid_point.y+ruler_width,
								Left + i*xdp, mid_point.y+ruler_width - 25, paint);
			}
		}
		
		canvas.restore();
	}

	
	public boolean onTouchEvent(MotionEvent event) {
		PointF touchPoint1;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 主点按下
		case MotionEvent.ACTION_DOWN:
			touchPoint1 = supposedPoint(new PointF(event.getX(), event.getY()));
			if (rect.contains((int) touchPoint1.x, (int) touchPoint1.y)) {
				MODE = "DRAG";
				finger_first_down.set(event.getX(), event.getY());
				mid_point_saved.set(mid_point);
			}
			else if(rectBtn.contains((int) touchPoint1.x, (int) touchPoint1.y)) {
				if(isCalibration)
					saveCalibration();
				else
					HorizontalRuler();
				isCalibration = !isCalibration;
				invalidate();
			}
			else {
				return false;
			}
			break;
		case MotionEvent.ACTION_POINTER_1_DOWN:
			PointF touchPoint2 = supposedPoint(new PointF(event.getX(1),
					event.getY(1)));
			if (rect.contains((int) touchPoint2.x, (int) touchPoint2.y)) {
				MODE = "ZOOM";
				finger_first_down.set(event.getX(0), event.getY(0));
				finger_second_down.set(event.getX(1), event.getY(1));
				angle_initial = rotation(event);
				angle_saved = angle_rotate;
				SingleToMulti = true;
				distance_initial = distance(event);
				distance_saved = ruler_length;
				calibration_saved = calibration;
				mid_point_between_fingers_down.set(
						(event.getX(0) + event.getX(1)) / 2,
						(event.getY(0) + event.getY(1)) / 2);
			}
			else {
				return false;
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (MODE == "DRAG") {
				if (MultiToSingle) {
					finger_first_down.set(event.getX(), event.getY());
					mid_point_saved.set(mid_point);
					MultiToSingle = false;
				}
				mid_point.set(mid_point_saved.x + event.getX()
						- finger_first_down.x, mid_point_saved.y + event.getY()
						- finger_first_down.y);
				renewRect();
			} else if (MODE == "ZOOM") {
				if (SingleToMulti) {
					mid_point_saved.set(mid_point);
					SingleToMulti = false;
				}
				mid_point_between_fingers.set(
						(event.getX(0) + event.getX(1)) / 2,
						(event.getY(0) + event.getY(1)) / 2);
				mid_point.set(mid_point_saved.x + mid_point_between_fingers.x
						- mid_point_between_fingers_down.x, mid_point_saved.y
						+ mid_point_between_fingers.y
						- mid_point_between_fingers_down.y);
				if(!isCalibration)
					angle_rotate = angle_saved + rotation(event) - angle_initial;
				ruler_length = distance_saved * distance(event) / distance_initial;
				if(isCalibration) {
					calibration = calibration_saved * distance(event) / distance_initial;
					xmm = xmm_saved *  calibration;
				}
				renewRect();
			}
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
				MODE = "NONE";
			break;
		case MotionEvent.ACTION_POINTER_1_UP:
			MultiToSingle = true;
//			MODE = "DRAG";
//			System.out.println(MODE);
//			break;
			MODE = "NONE";
			break;
		}
		return true;
	}

	/** 初始化单位，校准尺子 */
	protected void find_pixal(DisplayMetrics dm) {
		calibration = getCalibration();
		xmm_saved = (float) (dm.xdpi / 25.4);
		xmm = xmm_saved * calibration; // 单位都是pixal
		
		xdp = dm.density;
	}

	/** 获取两指成线的水平角度 */
	private float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians);
	}

	/** 获取两个手指触点之间的距离 */
	private float distance(MotionEvent event) {
		double x = (event.getX(0) - event.getX(1));
		double y = (event.getY(0) - event.getY(1));
		return (float) Math.sqrt(x * x + y * y);
	}

	protected float in360(float angel) {
		if (angel >= 360) {
			do {
				angel -= 360;
			} while (angel < 360);
		} else if (angel < 0) {
			do {
				angel += 360;
			} while (angel > 0);
		}
		return angel;
	}

	protected float distanceToMidPoint(PointF touchPoint) {
		double x = (touchPoint.x - mid_point.x);
		double y = (touchPoint.y - mid_point.y);
		return (float) Math.sqrt(x * x + y * y);
	}

	protected PointF supposedPoint(PointF touchPoint) {
		Float k = new Float(Math.toRadians(angle_rotate));
		PointF point_map = new PointF();
		point_map.x = new Float((touchPoint.x - mid_point.x) * Math.cos(k)
				+ (touchPoint.y - mid_point.y) * Math.sin(k) + mid_point.x);
		point_map.y = new Float(-(touchPoint.x - mid_point.x) * Math.sin(k)
				+ (touchPoint.y - mid_point.y) * Math.cos(k) + mid_point.y);
		return point_map;
	}
	
	/** 更新矩形 */
	protected void renewRect() {
		rect.left = (int) (mid_point.x - ruler_length * 0.5 );
		rect.right = (int) (mid_point.x + ruler_length * 0.5 );
		rect.top = (int) mid_point.y;
		rect.bottom = (int) (mid_point.y + ruler_width);
		
		rectBtn.left = (int) (mid_point.x + ruler_length/2);
		rectBtn.right = (int) (mid_point.x + ruler_length/2 + ruler_width);
		rectBtn.top = (int) mid_point.y;
		rectBtn.bottom = (int) (mid_point.y + ruler_width);
	}
}
