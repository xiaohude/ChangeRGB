package com.smarttiger.changergb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.smarttiger.SweetAlert.widget.ShowDialogUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private  String HEAD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/ChangeRGB/";
	
	private Context context;
	private EditText editOld;
	private EditText editNew;
	private Button button;
	private ImageView imageView;
	private TextView textView;
	
	private Bitmap bitmap;
	private Bitmap bitmapNew;
	private Bitmap imageBitmap;//屏幕取色时用的
	
	private int bitmapWidth;
	private int bitmapHeight;
	private float imageviewWidth;
	private float imageviewHeight;
	
	private ProgressDialog pDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		editOld = (EditText) findViewById(R.id.edit_old);
		editNew = (EditText) findViewById(R.id.edit_new);
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
					bitmapNew = getAlphaBitmap(bitmap, rgbNew);
					imageView.setImageBitmap(bitmapNew);
					bitmap = bitmapNew;
					return;
				} else if(!isColorString(colorOld)){
					Toast toast = Toast.makeText(context, "格式错误！(f0f0f0)", 0);
					toast.setGravity(Gravity.CENTER, 0, -400);
					toast.show();
					return;
				}
				
				int rgbOld = HexStringToInt(colorOld);

//				bitmapNew = changeBitmapRGB(bitmap, rgbOld, rgbNew);
//				imageView.setImageBitmap(bitmapNew);
//				imageBitmap = imageView.getDrawingCache();
				
//				pDialog = showProgress(context, "替换中");

				ShowDialogUtil.showRainbowProgress(context, "替换中");
				
				new ChangeBitmapRGB().execute(rgbOld, rgbNew);
			}
		});
		imageView = (ImageView) findViewById(R.id.image_view);
		imageView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
				saveBitmap(bitmap);
				return false;
			}
		});
		imageView.setOnTouchListener(new OnGetColorTouchListener());
		
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic);
		bitmapNew = getAlphaBitmap(bitmap, 0xff65bbd2);//0xffd59591,b8a59f
		imageView.setImageBitmap(bitmapNew);
		bitmap = bitmapNew;
		
		
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
		if(str.length() != 6)
			return false;
		Pattern pattern = Pattern.compile("[0-9,a,b,c,d,e,f]{6}");
        Matcher isColor = pattern.matcher(str);
        if(isColor.matches())
              return true;
        return false;
	}
	
	//提取图像Alpha位图  并替换为mColor颜色
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
    
    //将bitmap里颜色值为mColor的像素改为newColor
    public static Bitmap changeBitmapRGB(Bitmap mBitmap,int mColor, int newColor) {  

    	int width = mBitmap.getWidth();
    	int height = mBitmap.getHeight();
    	
//    	Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);  
    	Bitmap newBitmap = mBitmap.copy(Config.ARGB_8888, true);
    	
    	for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int color = mBitmap.getPixel(x, y);
//				System.out.println("x-"+x+"-y-"+y+"-color="+color);
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
			imageBitmap = imageView.getDrawingCache();
//			pDialog.cancel();
			ShowDialogUtil.closeRainbowProgress();
		};
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
	        String picturePath = cursor.getString(columnIndex);  
	        cursor.close();
	        
	        bitmap = getFileBitmap(picturePath);
			imageView.setImageBitmap(bitmap);
	    }
	}
	    
	class OnGetColorTouchListener implements OnTouchListener {

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

			textView.setText("bitmapWidth="+ bitmapWidth +"bitmapHeight="+bitmapHeight+"\n");  
			textView.append("imageviewWidth="+ imageviewWidth +"imageviewHeight="+imageviewHeight+"\n"); 
			textView.append("x="+x+"y="+y+"\n");
			textView.append("bx="+bx+"by="+by+"\n");
			
			
			int color = imageBitmap.getPixel(bx, by);
			String hexColor = Integer.toHexString(color + 0x01000000);
			editOld.setText(""+hexColor);
			 
            int action =event.getAction();
            if(action==MotionEvent.ACTION_DOWN){  
                System.out.println("down");  
            }
            else if(action==MotionEvent.ACTION_MOVE){  
                System.out.println("move");  
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
	
	/**
	 * 显示蓝色圆形进度条，可以通过返回值调用pDialog.cancel();来结束进度条。
	 */
	private ProgressDialog showProgress(Context context, String title)
	{
		ProgressDialog pDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
		pDialog.setTitle(title);
		pDialog.setCanceledOnTouchOutside(false);
		pDialog.show();
		return pDialog;
	}
	
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	bitmap.recycle();
    	bitmapNew.recycle();
    	super.onDestroy();
    }
}
