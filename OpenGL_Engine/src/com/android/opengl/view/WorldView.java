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
	private static final float TRANSLATE_FACTOR = 10;
	
	private WorldRenderer worldRenderer;
	private TextView textView;
	private GestureDetector gestureDetector;
	
	private ProgressDialog progressDialog;
	
	private Handler handler = new Handler(){
		
		
		@Override
		public void dispatchMessage(Message msg) {
			Log.d("tag", "dispatchMessage");
			switch (msg.what) {
			case DIALOG_LOADING_SHOW:
				if(progressDialog == null){
					progressDialog = new ProgressDialog(getContext());
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
		gestureDetector = new GestureDetector(getContext(), gestureListener);
		worldRenderer = new WorldRenderer(this, handler);
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
		if(worldRenderer != null){
			worldRenderer.release();
		}
		handler.sendEmptyMessage(DIALOG_LOADING_DISMISS);
		progressDialog = null;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		super.onTouchEvent(event);
		return true;
	}
	
	private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			worldRenderer.translateScene(distanceX / TRANSLATE_FACTOR, distanceY / TRANSLATE_FACTOR);
			return true;
		}
	
		@Override
		public boolean onRotate(float centerX, float centerY, float angle) {
			worldRenderer.rotateScene(0, angle ,0);
			Log.i("tag", "onRotate: angle = " + angle);
			return true;
		}
		@Override
		public boolean onDoubleSlide(float distanceX, float distanceY) {
			worldRenderer.rotateScene(distanceY, 0 ,0);
			Log.i("tag", "onDoubleSlide: distanceY = " + distanceY);
			return true;
		}	
		@Override
		public boolean onPinch(float centerX, float centerY, float scaleFactor) {
			worldRenderer.scaleScene(scaleFactor);
			Log.i("tag", "onPinch: scaleFactor = " + scaleFactor);
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			worldRenderer.onSingleTap(e.getX(), e.getY());
			return true;
		}
	
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
	//		worldRenderer.translateScene((e2.getX() - e1.getX()) / TRANSLATE_FACTOR, 
	//				(e2.getY() - e1.getY()) / TRANSLATE_FACTOR);
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}
	
	};



}
