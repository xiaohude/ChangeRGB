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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measure_activity);
		
		Intent intent = getIntent();
		String picPath = intent.getExtras().getString(MainActivity.PICTURE_PATH);
		
		//如果路径为空，则默认使用最新截图
		if(picPath == null) {
			picPath = getRecentlyPhotoPath(this);
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
				relativeLayout.addView(pathView);
				
//				relativeLayout.destroyDrawingCache();
				
				return false;
			}
		});
		
	}
	
	
	//获取最新照片，截屏的路径
	public static String getRecentlyPhotoPath(Context context) {
		//这个地方利用like 和通配符 来寻找 系统相机存储照片的地方
		//实际上还可以做的更夸张一点，寻找所有目录下的照片 并且可以限定格式  只要修改这个通配符语句即可
//		String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + "/DCIM/Camera/" + "%' ";
		String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + "/Screenshots/" + "%' ";
		Uri uri = MediaStore.Files.getContentUri("external");
		//这里做一个排序，因为我们实际上只需要最新拍得那张即可 你甚至可以取表里的 时间那个字段 然后判断一下 距离现在是否超过2分钟 超过2分钟就可以不显示缩略图的 
		//微信就是2分钟之内刚拍的图片会显示，超过了就不显示，这里主要就是看对表结构的理解 
		Cursor cursor = context.getContentResolver().query(
		uri, new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE}, searchPath, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
		String filePath = "";
	    if (cursor != null && cursor.moveToFirst()) {
	        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
	    }
	    if (!cursor.isClosed()) {
	        cursor.close();
	    }
	    return filePath;
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
