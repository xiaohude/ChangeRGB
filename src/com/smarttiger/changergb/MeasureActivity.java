package com.smarttiger.changergb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

public class MeasureActivity extends Activity {
	
	private ImageView imageView;
	
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
		
		imageView = (ImageView) findViewById(R.id.image_view);
		Bitmap bitmap = BitmapFactory.decodeFile(picPath);
		imageView.setImageBitmap(bitmap);
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
}
