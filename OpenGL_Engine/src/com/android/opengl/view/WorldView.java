package com.android.opengl.view;

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

public class WorldView extends GLSurfaceView{

	public static final int DIALOG_LOADING_SHOW = 0;
	public static final int DIALOG_LOADING_DISMISS = 1;
	
	private EngineRenderer engineRenderer;
	private TextView textView;
	private MotionEventDispatcher motionEventDispatcher;
	
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
		
		engineRenderer = new EngineRenderer(this, handler);
		motionEventDispatcher = new MotionEventDispatcher();
		setEGLContextClientVersion(2);
		setRenderer(engineRenderer);
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
		if(engineRenderer != null){
			engineRenderer.release();
		}
//		progressDialog = null;
	}
	
	
	private boolean mOnTouchEventRes;
	private Object syncObj = new Object();
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		mOnTouchEventRes = true;
		queueEvent(new Runnable() {
			
			@Override
			public void run() {
				synchronized (syncObj) {
					mOnTouchEventRes = motionEventDispatcher.dispatchTouchEvent(event);
					syncObj.notifyAll();
				}
			}
		});
		synchronized (syncObj) {
			try {
				syncObj.wait(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return mOnTouchEventRes;
	}


	public MotionEventDispatcher getMotionEventDispatcher() {
		return motionEventDispatcher;
	}

	public boolean registerToucheble(Touchable touchable, int zOrder) {
		return motionEventDispatcher.registerToucheble(touchable, zOrder);
	}

	public boolean unregisterToucheble(Touchable touchable) {
		return motionEventDispatcher.unregisterToucheble(touchable);
	}


}
