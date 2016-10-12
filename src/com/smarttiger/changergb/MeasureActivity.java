package com.smarttiger.changergb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MeasureActivity extends Activity {
	
	private RelativeLayout relativeLayout;
	private ImageView imageView;
	private ShaderView shaderView;
	private PathView pathView;
	private  int pathView_id = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measure_activity);
		
		Intent intent = getIntent();
		String picPath = intent.getExtras().getString(MainActivity.PICTURE_PATH);
		
		//如果路径为空，则默认使用最新截图
		if(picPath == null) {
			picPath = MainActivity.getRecentlyPhotoPath(this);
		}
		
		relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
		
		imageView = (ImageView) findViewById(R.id.image_view);
		Bitmap bitmap = BitmapFactory.decodeFile(picPath);
		imageView.setImageBitmap(bitmap);
		
//		shaderView = (ShaderView) findViewById(R.id.shaderView);
//		shaderView.setBitmap(getShot(this));
		

		imageView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				
				relativeLayout.buildDrawingCache();
				Bitmap bitmap = relativeLayout.getDrawingCache();
				
				
				
				pathView = new PathView(MeasureActivity.this, bitmap);
//				pathView.setMode(PathView.SCREEN_MODE);
				pathView.setMode(PathView.HANDLE_MODE);
				relativeLayout.addView(pathView);
				pathView_id = relativeLayout.getChildCount()-1;
				
				
				return false;
			}
		});
		
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(pathView != null) {
			relativeLayout.removeViewAt(pathView_id);
			relativeLayout.destroyDrawingCache();
			pathView = null;
		}
		else
			super.onBackPressed();
	}
	
	//获取当前屏幕截图
	public Bitmap getShot(Activity activity) {
		// 获取windows中最顶层的view
		View view = activity.getWindow().getDecorView();
		view.buildDrawingCache();

		// 获取状态栏高度
//		Rect rect = new Rect();
//		view.getWindowVisibleDisplayFrame(rect);
//		int statusBarHeights = rect.top;
//		Display display = activity.getWindowManager().getDefaultDisplay();
//
//		// 获取屏幕宽和高
//		int widths = display.getWidth();
//		int heights = display.getHeight();

		// 允许当前窗口保存缓存信息
		view.setDrawingCacheEnabled(true);

		// 去掉状态栏
//		Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
//				statusBarHeights, widths, heights - statusBarHeights);
		
        Bitmap bmp = view.getDrawingCache();

		// 销毁缓存信息
		view.destroyDrawingCache();

		return bmp;
	}
}
