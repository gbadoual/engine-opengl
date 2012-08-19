package com.android.opengl.view;

import com.android.opengl.util.geometry.Rect2D;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class WorldView extends GLSurfaceView implements Touchable{

	public static final int DIALOG_LOADING_SHOW = 0;
	public static final int DIALOG_LOADING_DISMISS = 1;
	
	private EngineRenderer worldRenderer;
	private TextView textView;
	
	private ProgressDialog progressDialog;
	
	private Handler handler = new Handler(){
		
		
		@Override
		public void dispatchMessage(Message msg) {
			Log.d("tag", "dispatchMessage");
			switch (msg.what) {
			case DIALOG_LOADING_SHOW:
				if(progressDialog == null){
					progressDialog = new ProgressDialog(getContext());
					progressDialog.setCancelable(false);
					progressDialog.setMessage("Loading world...");
					progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							Context context = getContext();
							if(context instanceof Activity){
								((Activity) context).finish();
							}
						}
					});
				}
				progressDialog.show();
				break;
			case DIALOG_LOADING_DISMISS:
				if(progressDialog!= null && progressDialog.isShowing()){
					progressDialog.dismiss();
				}
				progressDialog = null;
			default:
				break;
			}
			
			super.dispatchMessage(msg);
		}
	
	};


	public WorldView(Context context) {
		super(context);
		init();
	}
	
	public WorldView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		
		worldRenderer = new EngineRenderer(this, handler);
		setEGLContextClientVersion(2);
		setRenderer(worldRenderer);
	}
	
	public void updateFPS(final int fpsCount, final long facesCount){
		if (textView != null){
			textView.post(new Runnable() {
				
				@Override
				public void run() {
					textView.setText("FPS = "+fpsCount +" ("+facesCount+" faces)");					
				}
			});
		}
		
	}
	public void setFpsView(TextView textView) {
		this.textView = textView;
	}
	public TextView getFpsView() {
		return textView;
	}
	
	@Override
	public void onPause() {
		Log.d("tag", "onPause");
		super.onPause();
		handler.sendEmptyMessage(DIALOG_LOADING_DISMISS);
		if(worldRenderer != null){
			worldRenderer.release();
		}
//		progressDialog = null;
	}
	
	private boolean onTouchEventRes;
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		onTouchEventRes = true;

//		queueEvent(new Runnable() {
//			
//			@Override
//			public void run() {
				onTouchEventRes = worldRenderer.onTouchEvent(event);				
//			}
//		});
		return onTouchEventRes;
	}

	@Override
	public Rect2D getBoundariesRectInPixel() {
		// TODO Auto-generated method stub
		return null;
	}
	




}
