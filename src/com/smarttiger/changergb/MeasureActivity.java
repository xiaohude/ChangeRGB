package com.smarttiger.changergb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
			
		}
		
		imageView = (ImageView) findViewById(R.id.image_view);
		Bitmap bitmap = BitmapFactory.decodeFile(picPath);
		imageView.setImageBitmap(bitmap);
	}
}
