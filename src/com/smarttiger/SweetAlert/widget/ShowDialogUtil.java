package com.smarttiger.SweetAlert.widget;


import java.util.Timer;
import java.util.TimerTask;



import com.smarttiger.changergb.R;
import com.smarttiger.changergb.R.color;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * 用于提示消息的功能类
 * @author xiaohu
 */
public class ShowDialogUtil {
	
	/**
	 * 通过Toast显示消息
	 * @param context
	 * @param message
	 */
	public static void showToast(Context context, String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		
		//第一个参数：设置toast在屏幕中显示的位置。我现在的设置是居中靠顶 
		//第二个参数：相对于第一个参数设置toast位置的横向X轴的偏移量，正数向右偏移，负数向左偏移 
		//第三个参数：同的第二个参数道理一样 
		//如果你设置的偏移量超过了屏幕的范围，toast将在屏幕内靠近超出的那个边界显示 
		//toast.setGravity(Gravity.TOP|Gravity.CENTER, -50, 100); 
	}
	
	
	/**
	 * 通过类似登陆失败提示类型
	 * @param context Activity
	 * @param title 标题
	 * @param message 显示信息
	 */
	public static void showDialog(Context context, String title, String message)
	{
		showDialog(context, 0, title, message);
	}
	/**
	 * 通过类似登陆失败提示类型
	 * @param context Activity
	 * @param icon 对话框标题前的图标 0-4表示SweetDialog默认的动态图标，其他则使用图片资源。
	 * @param title 标题
	 * @param message 显示信息
	 */
	public static void showDialog(Context context, int icon, String title, String message)
	{
		SweetAlertDialog sweetAlertDialog;
		if(icon < 4)
			sweetAlertDialog = new SweetAlertDialog(context, icon);
		else
			sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE).setCustomImage(icon);
		sweetAlertDialog.setTitleText(title);
	    sweetAlertDialog.setContentText(message);
	    sweetAlertDialog.show();
	}
	
	/**
	 * 默认添加上确认，取消的对话框，并有确认按钮的触发事件。
	 * @param context
	 * @param title
	 * @param message
	 * @param onClickListener1 点击确认按钮的onclick  类型：DialogInterface.OnClickListener
	 */
	public static void showDialog(Context context, String title, String message, DialogInterface.OnClickListener onClickListener1)
	{
		showDialog(context, SweetAlertDialog.WARNING_TYPE, title, message, null, null, onClickListener1, null);
	}
	
	/**
	 * @param context
	 * @param icon 对话框标题前的图标 0-4表示SweetDialog默认的动态图标，其他则使用图片资源。
	 * @param title 对话框的标题
	 * @param message 要显示的信息
	 * @param bt1 确认按钮的名字
	 * @param bt2 取消按钮的名字
	 * @param onClickListener1 确认按钮的Onclick  类型：DialogInterface.OnClickListener
	 * @param onClickListener2 取消按钮的Onclick  类型：DialogInterface.OnClickListener
	 */
	public static void showDialog(Context context,int icon,String title,String message,
			String bt1, 
			String bt2,
			final DialogInterface.OnClickListener onClickListener1,
			final DialogInterface.OnClickListener onClickListener2) 
	{
		final SweetAlertDialog sweetAlertDialog;
		if(icon < 4)
			sweetAlertDialog = new SweetAlertDialog(context, icon);
		else
			sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE).setCustomImage(icon);
		sweetAlertDialog.setTitleText(title).setContentText(message)
        .setConfirmText(bt1)
        .setCancelText(bt2)
        .showCancelButton(true)
        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            public void onClick(SweetAlertDialog sDialog) {
            	if(onClickListener1 != null)
            		onClickListener1.onClick(sDialog, 0);
        		sweetAlertDialog.cancel();
            }
        })
        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            public void onClick(SweetAlertDialog sDialog) {
            	if(onClickListener2 != null)
            		onClickListener2.onClick(sDialog, 0);
        		sweetAlertDialog.cancel();
            }
        });
        sweetAlertDialog
        .show();
	}
	
	/**
	 * 显示蓝色圆形进度条，可以通过返回值调用pDialog.cancel();来结束进度条。
	 */
	public static SweetAlertDialog showProgress(Context context, String title)
	{
		SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
		pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
		pDialog.setTitleText(title);
		pDialog.setCanceledOnTouchOutside(false);
		pDialog.show();
		return pDialog;
	}
	/**
	 * 显示彩虹进度圈,关闭需调用closeRainbowProgress()方法
	 */
	private static int i = -1;
	private static SweetAlertDialog rainbowProgress;
	private static Timer rainbowTimer;
	public static void showRainbowProgress(final Context context, String title)
	{
		rainbowProgress = showProgress(context, title);
		rainbowTimer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				i++;
				i = i%7;
                switch (i){
                    case 0:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_red));
                        break;
                    case 1:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_orange));
                        break;
                    case 2:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_yellow));
                        break;
                    case 3:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_green));
                        break;
                    case 4:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_cyan));
                        break;
                    case 5:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_blue));
                        break;
                    case 6:
                    	rainbowProgress.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_purple));
                        break;
                }
			}
		};
		rainbowTimer.schedule(timerTask, 0, 1000);	
	}
	/**
	 * 关闭彩虹进度圈,并弹出成功对话框并自动消失
	 */
	public static void closeRainbowProgress()
	{
		i = -1;
		rainbowProgress.setTitleText("成功!")
        	.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
		
		Thread thread = new Thread(){
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Message msgMessage=new Message();  
	            msgMessage.arg1=1;  
	            handler.sendMessage(msgMessage);
			}
		};
		thread.start();
		
	}
	
	private static Handler handler=new Handler(){  
        public void handleMessage(Message msg){  
            switch (msg.arg1) {  
            case 1: 
            	if(rainbowProgress!=null) {
        			rainbowProgress.cancel();
        		}
        		if(rainbowTimer!=null)
        			rainbowTimer.cancel();
                break;  
            default:  
                break;  
            }  
        }  
    }; 
	
	/**
	 * 显示固定时间进度条。
	 * @param context
	 * @param title 提示信息
	 * @param time 持续秒数（800ms）
	 */
	public static void showTimeProgress(final Context context, String title, int time)
	{
		final SweetAlertDialog pDialog = ShowDialogUtil.showProgress(context, title);
		new CountDownTimer(800 * (time+1), 800) {
	        public void onTick(long millisUntilFinished) {
	            // you can change the progress bar color by ProgressHelper every 800 millis
	            i++;
				i = i%7;
	            switch (i){
	                case 0:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_red));
	                    break;
	                case 1:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_orange));
	                    break;
	                case 2:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_yellow));
	                    break;
	                case 3:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_green));
	                    break;
	                case 4:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_cyan));
	                    break;
	                case 5:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_blue));
	                    break;
	                case 6:
	                    pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.rainbow_purple));
	                    break;
	            }
	        }
	        public void onFinish() {
	            i = -1;
	            pDialog.setTitleText("成功!")
	                    .setConfirmText("OK")
	                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
	        }
	    }.start();
	}
	
	
	
	
	
	//直接用系统Dialog的方法
//	public static void showDialog(Context context, String title, String message)
//	{
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setTitle(title);
//		builder.setIcon(android.R.drawable.ic_dialog_info);
//		builder.setMessage(message);
//		builder.setPositiveButton(R.string.confirm, null);
//		builder.create().show();
//	}
//	/**
//	 * @param context
//	 * @param icon 对话框标题前的图标 0-4表示SweetDialog默认的动态图标，其他则使用图片资源。
//	 * @param title 对话框的标题
//	 * @param message 要显示的信息
//	 * @param bt1 确认按钮的名字
//	 * @param bt2 取消按钮的名字
//	 * @param onClickListener1 确认按钮的Onclick  类型：DialogInterface.OnClickListener
//	 * @param onClickListener2 取消按钮的Onclick  类型：DialogInterface.OnClickListener
//	 */
//	public static void showDialog(Context context,int icon,String title,String message,
//			String bt1, 
//			String bt2,
//			final DialogInterface.OnClickListener onClickListener1,
//			final DialogInterface.OnClickListener onClickListener2) 
//	{
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setTitle(title);
//		builder.setMessage(message);
//		//设置标题图标
//		if(icon == 0)
//			builder.setIcon(android.R.drawable.ic_dialog_info);
//		else
//			builder.setIcon(icon);
//		//设置确认按钮
//		if(bt1 == null)
//			builder.setPositiveButton(R.string.confirm, onClickListener1);
//		else
//			builder.setPositiveButton(bt1, onClickListener1);
//		//设置取消按钮
//		if(bt2 == null)
//			builder.setNegativeButton(R.string.cancel, onClickListener2);
//		else
//			builder.setNegativeButton(bt2, onClickListener2);
//		builder.create().show();
//	}


}
