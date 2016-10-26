package com.smarttiger.changergb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
		
		
		String picPath = getRecentlyPhotoPath(this);
		if(picPath != null)
			bitmap = BitmapFactory.decodeFile(picPath);
		else
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
    public static Bitmap changeBitmapRGB(Bitmap mBitmap,int mColor, int newColor) {  

    	int width = mBitmap.getWidth();
    	int height = mBitmap.getHeight();
    	
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
    
}
