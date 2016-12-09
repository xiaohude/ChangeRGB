package com.smarttiger.changergb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.smarttiger.SweetAlert.widget.ShowDialogUtil;
import com.smarttiger.changergb.PathView.OnGetRGBlistener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private  String HEAD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/ChangeRGB/";
	
	private Context context;
	private EditText editOld;
	private EditText editNew;
	private Button button;
	private Button bwButton;
	private RelativeLayout imageLayout;
	private  int pathView_id = 0;
	private PathView pathView;
	private ImageView imageView;
	private TextView textView;
	
	private Bitmap bitmap;
	private Bitmap imageBitmap;//屏幕取色时用的
	
	private int bitmapWidth;
	private int bitmapHeight;
	private float imageviewWidth;
	private float imageviewHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		editOld = (EditText) findViewById(R.id.edit_old);
		editNew = (EditText) findViewById(R.id.edit_new);
		editNew.addTextChangedListener(new colorTextWatcher());
		textView = (TextView) findViewById(R.id.text_view);
		button = (Button) findViewById(R.id.test_button);
		bwButton = (Button) findViewById(R.id.bw_button);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String colorOld = editOld.getText().toString();
				String colorNew = editNew.getText().toString();
				if(!isColorString(colorNew) ){
					Toast toast = Toast.makeText(context, "格式错误！(f0f0f0)", 0);
					toast.setGravity(Gravity.CENTER, 0, -400);
					toast.show();
					return;
				}
				int rgbNew = HexStringToInt(colorNew);
				
				if(TextUtils.isEmpty(colorOld)) {
					bitmap = getAlphaBitmap(bitmap, rgbNew);
					imageView.setImageBitmap(bitmap);
					imageView.buildDrawingCache();
					imageBitmap = imageView.getDrawingCache();
					return;
				} else if(!isColorString(colorOld)){
					Toast toast = Toast.makeText(context, "格式错误！(f0f0f0)", 0);
					toast.setGravity(Gravity.CENTER, 0, -400);
					toast.show();
					return;
				}
				
				int rgbOld = HexStringToInt(colorOld);
				
				ShowDialogUtil.showRainbowProgress(context, "替换中");
				
				new ChangeBitmapRGB().execute(rgbOld, rgbNew);
			}
		});
		bwButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String colorOld = editOld.getText().toString();
				String colorNew = editNew.getText().toString();
				
				if(TextUtils.isEmpty(colorNew)) {
					colorNew = "000000";
				} else if(!isColorString(colorNew)){
					Toast toast = Toast.makeText(context, "格式错误！(f0f0f0)", 0);
					toast.setGravity(Gravity.CENTER, 0, -400);
					toast.show();
					return;
				}
				
				if(!TextUtils.isEmpty(colorOld))
					cs = Integer.valueOf(colorOld, 16);
				if(cs < 0 || cs > 255)
					cs = 0x06;
				int rgbNew = HexStringToInt(colorNew);
				
				ShowDialogUtil.showRainbowProgress(context, "处理中");
				new BlackWriteBitmap().execute(rgbNew);
			}
		});
		imageLayout = (RelativeLayout) findViewById(R.id.image_layout);
		imageView = (ImageView) findViewById(R.id.image_view);
		// 将长按图片保存功能转移到按钮上
//		imageView.setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				// TODO Auto-generated method stub
//				if(!isMoveTouch) {
//					Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//					saveBitmap(bitmap);
//				}
//				return false;
//			}
//		});
//		imageView.setOnTouchListener(new OnGetColorTouchListener());
		
		
//		String picPath = getRecentlyPhotoPath(this);
//		if(picPath != null)
//			bitmap = BitmapFactory.decodeFile(picPath);
//		else
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic);
		
//		bitmap = getAlphaBitmap(bitmap, 0xff65bbd2);//0xffd59591,b8a59f
		imageView.setImageBitmap(bitmap);
		
//		bitmapNew = changeBitmapRGB(bitmap, 0, 0xffff0000);
//		imageView.setImageBitmap(bitmapNew);
		
//		test("ffffff", 0xffffffff);
//		test("ff00ff", 0xffff00ff);
//		test("0000ff", 0xff0000ff);
//		test("ff0000", 0xffff0000);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		
		imageviewWidth = imageView.getWidth();
		imageviewHeight = imageView.getHeight();

		imageView.buildDrawingCache();
		imageBitmap = imageView.getDrawingCache();
		
		if(pathView_id == 0) {
			pathView = new PathView(MainActivity.this, imageBitmap);
			pathView.setOnGetRGBlistener(new OnGetRGBlistener());
			imageLayout.addView(pathView);
			pathView_id = imageLayout.getChildCount()-1;
		}
		
		bitmapWidth = bitmap.getWidth();
		bitmapHeight = bitmap.getHeight();
	}
	
//	void test (String s, int i){
//		int color = Integer.valueOf(s, 16);
//		System.out.println("-----------------------");
//		System.out.println("s=="+s);
//		System.out.println("i=="+i);
//		System.out.println("color=="+color);
//		System.out.println("color-0xffffff=="+(color-0x1000000));
//		System.out.println("color-i=="+(color-i));
//		System.out.println("0-color=="+(0-color));
//		System.out.println("~color=="+~color);
//		System.out.println("-----------------------");
//	}
	
	/** 将十六进制字符串RGB(xxxxxx)转换为int类型（0xffxxxxxx）*/
	private int HexStringToInt(String s){
		int color = Integer.valueOf(s, 16);
		color = color-0x1000000;
		return color;
	}
	//判断是否为ff00ff格式
	private boolean isColorString(String str) {
		if(str.length() == 0 || str.length() >6)
			return false;
		Pattern pattern = Pattern.compile("[0-9,a,b,c,d,e,f]{1,6}");
        Matcher isColor = pattern.matcher(str);
        if(isColor.matches())
              return true;
        return false;
	}
	
	/** 提取图像所有Alpha位图  并全替换为mColor颜色 */
    public static Bitmap getAlphaBitmap(Bitmap mBitmap,int mColor) {  
//      BitmapDrawable mBitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.enemy_infantry_ninja);  
//      Bitmap mBitmap = mBitmapDrawable.getBitmap();  

		System.out.println("mColor====="+mColor);
        //BitmapDrawable的getIntrinsicWidth（）方法，Bitmap的getWidth（）方法  
        //注意这两个方法的区别  
        //Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmapDrawable.getIntrinsicWidth(), mBitmapDrawable.getIntrinsicHeight(), Config.ARGB_8888);  
        Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_8888);  
          
        Canvas mCanvas = new Canvas(mAlphaBitmap);  
        Paint mPaint = new Paint();  
          
        mPaint.setColor(mColor);  
        //从原位图中提取只包含alpha的位图  
        Bitmap alphaBitmap = mBitmap.extractAlpha();  
        //在画布上（mAlphaBitmap）绘制alpha位图  
        mCanvas.drawBitmap(alphaBitmap, 0, 0, mPaint);  
          
        return mAlphaBitmap;  
    } 
    
    /** 将bitmap里颜色值为mColor的像素改为newColor */
    public Bitmap changeBitmapRGB(Bitmap mBitmap,int mColor, int newColor) {  

    	int width = mBitmap.getWidth();
    	int height = mBitmap.getHeight();
    	
    	cache = new int[width][height];
    	
//    	Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);  
    	Bitmap newBitmap = mBitmap.copy(Config.ARGB_8888, true);
    	
    	for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int color = mBitmap.getPixel(x, y);
				if(color == mColor)
					newBitmap.setPixel(x, y, newColor);
//				else
//					newBitmap.setPixel(x, y, color);
			}
		}
    	
    	
    	//初始测试黑白显边的调试代码
//    	for (int x = 1; x < width-1; x++) {
//    		for (int y = 1; y < height-1; y++) {
//    			cache[x][y] = isSimilarArea(mBitmap, x, y);
//        		if(cache[x][y])
//        			newColor = 0xffffffff;
//        		else
//        			newColor = 0xff000000;
//        		newBitmap.setPixel(x, y, newColor);
//    		}
//    	}
          
        return newBitmap;  
    } 
    
    class ChangeBitmapRGB extends AsyncTask<Integer, Integer, Bitmap>{

		@Override
		protected Bitmap doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			return changeBitmapRGB(bitmap, params[0], params[1]);
		}
    	
		protected void onPostExecute(Bitmap result) {
			
			imageView.setImageBitmap(result);
			
			bitmap = result;
			imageView.buildDrawingCache();
			imageBitmap = imageView.getDrawingCache();
			editOld.setText(editNew.getText());
			editOld.setBackgroundColor(HexStringToInt(editNew.getText().toString()));
			editOld.setTextColor(editNew.getTextColors());
			
			ShowDialogUtil.closeRainbowProgress();
		};
    }
    
    // 保存替换颜色后的图片
    public void onSavePic(View view) {
		Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
		saveBitmap(bitmap);
    }
    
    //记得加权限
    private void saveBitmap(Bitmap bm) {
		File headFile = new File(HEAD_DIR);
		if(!headFile.exists())
			headFile.mkdir();
		
		File f = new File(HEAD_DIR, "/RGB.png");
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			Toast.makeText(this, "新图片已保存到"+f.getPath(), 0).show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    //获取本地图片bitmap
	private Bitmap getFileBitmap(String filePath) {
		Bitmap bitmap = null;
		
		File file = new File(filePath);
		BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
		if(file != null){
			try {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, sBitmapOptions);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		bitmap = BitmapFactory.decodeFile(filePath); //还可以直接这么用

		return bitmap;
	}
    
	//使用自带图库选择照片
	private static final int RESULT_LOAD_IMAGE = 1001;
	private String picturePath;
	public void onGetPic(View view) {
		Intent i = new Intent(  
				Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  
		startActivityForResult(i, RESULT_LOAD_IMAGE);  
	}
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    super.onActivityResult(requestCode, resultCode, data);  
	  
	    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {  
	        Uri selectedImage = data.getData();  
	        String[] filePathColumn = { MediaStore.Images.Media.DATA };  
	  
	        Cursor cursor = getContentResolver().query(selectedImage,  
	                filePathColumn, null, null, null);  
	        cursor.moveToFirst();  
	        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
	        picturePath = cursor.getString(columnIndex);  
	        cursor.close();
	        
	        bitmap = getFileBitmap(picturePath);
			imageView.setImageBitmap(bitmap);
	    }
	}
	    
	
	private boolean isMoveTouch = false;
	private float downX = 0;
	private float downY = 0;
	private class OnGetColorTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// TODO Auto-generated method stub
			float x,y;
			x = event.getX();
			y = event.getY();

			int bx,by;
//			bx = (int) (bitmapWidth * x / imageviewWidth);
//			by = (int) (bitmapHeight * y /imageviewHeight);
			bx = (int) x;
			by = (int) y;
			if(bx < 0) bx = 0;
			if(by < 0) by = 0;
			
			int color = imageBitmap.getPixel(bx, by);
			String hexColor;
			if(color == 0) {
				hexColor = "ffffff";
				editOld.setText(""+hexColor);
				editOld.setBackgroundColor(color);
				editOld.setTextColor(0xff202020);
			}
			else {
				hexColor = Integer.toHexString(color + 0x01000000);
				editOld.setText(""+hexColor);
				editOld.setBackgroundColor(color);
				editOld.setTextColor(getTextColorForBackgroud(color));
			}
			 
            int action =event.getAction();
            if(action==MotionEvent.ACTION_DOWN){  
            	isMoveTouch = false;
                System.out.println("down");  
                downX = event.getX();
                downY = event.getY();
            }
            else if(action==MotionEvent.ACTION_MOVE){  
                System.out.println("move");  
                float moveX = event.getX();
                float moveY = event.getY();
                System.out.println("moveX=="+moveX+"--moveY=="+moveY);
                if(Math.abs(downX - moveX) >10 || Math.abs(downY - moveY) > 10)
                	isMoveTouch = true;
            }
            else if(action==MotionEvent.ACTION_UP){ 
                System.out.println("up");  
            }
            else if(action==MotionEvent.ACTION_CANCEL){  
                System.out.println("cancel");  
            }  
              
			return false;
		}
		
	}
	
	class OnGetRGBlistener implements com.smarttiger.changergb.PathView.OnGetRGBlistener {

		@Override
		public void getRGB(int color) {
			// TODO Auto-generated method stub
			String hexColor;
			if(color == 0) {
				hexColor = "ffffff";
				editOld.setText(""+hexColor);
				editOld.setBackgroundColor(color);
				editOld.setTextColor(0xff202020);
			}
			else {
				hexColor = Integer.toHexString(color + 0x01000000);
				editOld.setText(""+hexColor);
				editOld.setBackgroundColor(color);
				editOld.setTextColor(getTextColorForBackgroud(color));
			}
		}
		
	}
	
	/**
	 * 根据TextView的背景色，设置TextView的字体颜色，用来防止字体颜色和背景色相似。
	 */
	private int getTextColorForBackgroud(int bgColor) {
		int textColor = 0xff202020;
		int red = Color.red(bgColor);
		int green = Color.green(bgColor);
		int blue = Color.blue(bgColor);
		textView.setText(" red="+ red +"\n green="+green+"\n blue="+blue);
		if(red < 100 && green < 100)
			textColor = 0xfff0f0f0;
		return textColor;
	}
	
	/** 监听Edit，自动根据输入颜色改变背景色。 */
	class colorTextWatcher implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {}

		private Toast toast;//防止一直显示同样的Toast
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			int bgColor = 0xffffffff;
			if(isColorString(s.toString()))
				bgColor = HexStringToInt(s.toString());
			else {
				if(toast != null)
					toast.cancel();
				toast = Toast.makeText(context, "格式错误！(f0f0f0)", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, -400);
				toast.show();
			}
				
			editNew.setBackgroundColor(bgColor);
			editNew.setTextColor(getTextColorForBackgroud(bgColor));
		}
		
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	bitmap.recycle();
    	super.onDestroy();
    }
    

	public static final String PICTURE_PATH = "picture_path";
    //测量截图上控件尺寸
    public void onMeasure(View view) {
    	Intent intent = new Intent(this, MeasureActivity.class);
    	intent.putExtra(PICTURE_PATH, picturePath);
    	startActivity(intent);
    }
    
    /** 获取最新照片，截屏的路径 */
  	public static String getRecentlyPhotoPath(Context context) {
  		//这个地方利用like 和通配符 来寻找 系统相机存储照片的地方
  		//实际上还可以做的更夸张一点，寻找所有目录下的照片 并且可以限定格式  只要修改这个通配符语句即可
//  		String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + "/DCIM/Camera/" + "%' ";
  		String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + "/Screenshots/" + "%' ";
  		Uri uri = MediaStore.Files.getContentUri("external");
  		//这里做一个排序，因为我们实际上只需要最新拍得那张即可 你甚至可以取表里的 时间那个字段 然后判断一下 距离现在是否超过2分钟 超过2分钟就可以不显示缩略图的 
  		//微信就是2分钟之内刚拍的图片会显示，超过了就不显示，这里主要就是看对表结构的理解 
  		Cursor cursor = context.getContentResolver().query(
  		uri, new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE}, searchPath, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
  		String filePath = null;
  	    if (cursor != null && cursor.moveToFirst()) {
  	        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
  	    }
  	    if (!cursor.isClosed()) {
  	        cursor.close();
  	    }
  	    return filePath;
  	}
  	
  	
  	
  	//黑白显边线程
  	class BlackWriteBitmap extends AsyncTask<Integer, Integer, Bitmap>{

		@Override
		protected Bitmap doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			
			int width = bitmap.getWidth();
	    	int height = bitmap.getHeight();
	    	int newColor = params[0];
	    	int color = 0;
	    	
	    	cache = new int[width][height];
	    	
//	    	Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, true);
	    	Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
	    	
	    	for (int x = 1; x < width-1; x++) {
	    		for (int y = 1; y < height-1; y++) {
	    			cache[x][y] = isSimilarArea(bitmap, x, y);
//	        		if(cache[x][y] == 1) {
//	        			color = newColor;
//		        		newBitmap.setPixel(x, y, color);
//	        		}
////	        		else {
////	        			color = 0xffffffff;
////	        			newBitmap.setPixel(x, y, color);
////	        		}
	    		}
	    	}
	    	
	    	//清理边界
//	    	for (int x = 2; x < width-2; x++) {
//		    	cache[x][0] = 0;
//		    	cache[x][1] = 0;
//		    	cache[x][height-1] = 0;
//		    	cache[x][height-2] = 0;
//	    	}
//	    	for (int y = 2; y < height-2; y++) {
//	    		cache[0][y] = 0;
//	    		cache[1][y] = 0;
//	    		cache[width-1][y] = 0;
//	    		cache[width-2][y] = 0;
//	    	}
	    	
	    	//划分连通区
	    	for (int x = 2; x < width-2; x++) {
	    		for (int y = 2; y < height-2; y++) {
	    			if(cache[x][y] == 1){
	    				counter = 0;
	    				connectedId ++;
	    				cache[x][y] = connectedId;
	    				
	    				connectedArea = new ConnectedArea();
	    				connectedArea.connectedId = connectedId;
	    				connectedArea.xList = new ArrayList<Integer>();
	    				connectedArea.yList = new ArrayList<Integer>();
	    				
	    				dealBwlabe(x, y);
	    				
	    				connectedArea.length = counter;
	    				connectedAreaList.add(connectedArea);
	    			}
	    		}
	    	}
	    	
	    	//进行着色
	    	for (ConnectedArea area : connectedAreaList) {
	    		if(area.length > limit){
	    			for(int i = 0;i < area.length; i++) {
	    				int x = area.xList.get(i);
	    				int y = area.yList.get(i);
	    				
	    				switch (area.connectedId%7 + 2) {
							case 2:
				        		newBitmap.setPixel(x, y, 0xff00ff00);
								break;
							case 3:
			    				newBitmap.setPixel(x, y, 0xffffff00);
			    				break;
							case 4:
			    				newBitmap.setPixel(x, y, 0xff00ffff);
			    				break;
							case 5:
			    				newBitmap.setPixel(x, y, 0xffff0000);
			    				break;
							case 6:
			    				newBitmap.setPixel(x, y, 0xffff00ff);
			    				break;
							case 7:
			    				newBitmap.setPixel(x, y, 0xff0000ff);
			    				break;
							case 8:
			    				newBitmap.setPixel(x, y, 0xff000000);
			    				break;
						}
	    			}
	    		}
			}
	    	
	    	//进行着色
//	    	for (int x = 2; x < width-2; x++) {
//	    		for (int y = 2; y < height-2; y++) {
////	    			System.out.print(""+cache[x][y]);
//	    			if(cache[x][y] == 0 || cache[x][y] == 1)
//	    				continue;
//	    			switch (cache[x][y]%7+2) {
//						case 2:
//			        		newBitmap.setPixel(x, y, 0xff00ff00);
//							break;
//						case 3:
//		    				newBitmap.setPixel(x, y, 0xffffff00);
//		    				break;
//						case 4:
//		    				newBitmap.setPixel(x, y, 0xff00ffff);
//		    				break;
//						case 5:
//		    				newBitmap.setPixel(x, y, 0xffff0000);
//		    				break;
//						case 6:
//		    				newBitmap.setPixel(x, y, 0xffff00ff);
//		    				break;
//						case 7:
//		    				newBitmap.setPixel(x, y, 0xff0000ff);
//		    				break;
//						case 8:
//		    				newBitmap.setPixel(x, y, 0xff000000);
//		    				break;
//					}
//	    		}
////	    		System.out.println("");
//	    	}
	          
	        return newBitmap;  
		}
    	
		protected void onPostExecute(Bitmap result) {
			
			imageView.setImageBitmap(result);
			
			bitmap = result;
			imageView.buildDrawingCache();
			imageBitmap = imageView.getDrawingCache();
			editOld.setBackgroundColor(0xffffffff);
			editNew.setText(""+connectedId);
			
			ShowDialogUtil.closeRainbowProgress();
		};
    }
    
  	//用来缓存当前坐标是否在相似区   用来遍历和记录连通区
  	private int cache[][];
  	//用来标记当前是哪个连通区
  	private int connectedId = 1;
  	
  	//判断是否与周边颜色相似 九宫格比较 (八连通)
  	private int isSimilarArea(Bitmap mBitmap, int x, int y){

		int color1 = mBitmap.getPixel(x-1, y-1);
		int color2 = mBitmap.getPixel(x,   y-1);
		int color3 = mBitmap.getPixel(x+1, y-1);
		int color4 = mBitmap.getPixel(x-1, y);
		int color5 = mBitmap.getPixel(x,   y);
		int color6 = mBitmap.getPixel(x+1, y);
		int color7 = mBitmap.getPixel(x-1, y+1);
		int color8 = mBitmap.getPixel(x,   y+1);
		int color9 = mBitmap.getPixel(x+1, y+1);
//		cache[x][y] = isSameRGB(color0, color1);
		
		if( isSameRGB(color5, color1) && 
			isSameRGB(color5, color2) && 
			isSameRGB(color5, color3) && 
			isSameRGB(color5, color4) && 
			isSameRGB(color5, color6) && 
			isSameRGB(color5, color7) && 
			isSameRGB(color5, color8) && 
			isSameRGB(color5, color9) )
			return 0;
		else
			return 1;
  	}
  	
  	//颜色相似的阀值
  	private int cs = 0x10;
  	//判断两种颜色是否相同
  	private boolean isSameRGB (int color0, int color1) {
  		int red0 = (color0 & 0xff0000) >> 16;  
        int green0 = (color0 & 0x00ff00) >> 8;  
        int blue0 = (color0 & 0x0000ff);  
  		
        int red1 = (color1 & 0xff0000) >> 16;  
        int green1 = (color1 & 0x00ff00) >> 8;  
        int blue1 = (color1 & 0x0000ff);  
        
        if(Math.abs(red0-red1) < cs && Math.abs(green0-green1) < cs && Math.abs(blue0-blue1) < cs)
        	return true;
        else
        	return false;
  	}
  	
  	//得限制连通的点数大于一个值才能算是连通区。
  	//也就是累计连通点数大于这个值时，才开始着色，这样会隐藏掉小于这个值的连通点，
  	//但是也会导致大于这个值的连通区缺少前面一部分点。
  	private int limit = 1000;
  	private int counter = 0;
  	//递归处理黑白显边的数组，找出多个连通区。
	private void dealBwlabe(int i, int j) {
	    // TODO Auto-generated method stub

		//由于使用if语句会导致栈溢出的崩溃问题，所以将判断逻辑用数学算法写进赋值里。
//		if(counter++ > limit)
		
//			cache[i][j] = (counter++ - limit)%2 * connectedId;
		
		counter++;
		cache[i][j] = connectedId;
		connectedArea.xList.add(i);
		connectedArea.yList.add(j);
		
		//上
		if (cache[i-1][j] == 1) {
//			if(counter++ > limit)
//				cache[i-1][j] = connectedId;
		    dealBwlabe(i-1, j);
		}
		//左
		if (cache[i][j-1] == 1) {
//			if(counter++ > limit)
//				cache[i][j-1] = connectedId;
		    dealBwlabe(i, j-1);
		}
		//下
		if (cache[i+1][j] == 1) {
//			if(counter++ > limit)
//				cache[i+1][j] = connectedId;
		    dealBwlabe(i+1, j);
		}
		//右
		if (cache[i][j+1] == 1) {
//			if(counter++ > limit)
//				cache[i][j+1] = connectedId;
		    dealBwlabe(i, j+1);
		}

////八连通需要
//       //上左
//       if (cache[i-1][j-1] == 1) {
//           cache[i-1][j-1] = counter;
//           dealBwlabe(i-1, j-1);
//       }
//       //上右
//       if (cache[i-1][j+1] == 1) {
//           cache[i-1][j+1] = counter;
//           dealBwlabe(i-1, j+1);
//       }
//       //下左
//       if (cache[i+1][j-1] == 1) {
//           cache[i+1][j-1] = counter;
//           dealBwlabe(i+1, j-1);
//       }
//       //下右
//       if (cache[i+1][j+1] == 1) {
//           cache[i+1][j+1] = counter;
//           dealBwlabe(i+1, j+1);
//       }       

    }
	
	
	private ArrayList<ConnectedArea> connectedAreaList = new ArrayList<ConnectedArea>();
	private ConnectedArea connectedArea;
	//单个连通区类
	class ConnectedArea {
		int connectedId = 0;
		ArrayList<Integer> xList;
		ArrayList<Integer> yList;
		int length = 0;
	}
  	
}
