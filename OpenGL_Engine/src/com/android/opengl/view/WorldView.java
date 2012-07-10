package com.android.opengl.view;

import com.android.opengl.view.GestureDetector.OnGestureListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

public class WorldView extends GLSurfaceView implements OnGestureListener{

	public static final int DIALOG_LOADING_SHOW = 0;
	public static final int DIALOG_LOADING_DISMISS = 1;
	
	private WorldRenderer worldRenderer;
	private TextView textView;
	private GestureDetector gestureDetector;
	
	private ProgressDialog progressDialog;
	
	private Handler handler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case DIALOG_LOADING_SHOW:
				if(progressDialog == null){
					progressDialog = new ProgressDialog(getContext());
					progressDialog.setMessage("Loading world...");
				}
				progressDialog.show();
				break;
			case DIALOG_LOADING_DISMISS:
				if(progressDialog!= null && progressDialog.isShowing()){
					progressDialog.dismiss();
				}

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
		gestureDetector = new GestureDetector(getContext(), this);
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
		super.onPause();
		if(worldRenderer != null){
			worldRenderer.deinit();
		}
		handler.sendEmptyMessage(DIALOG_LOADING_DISMISS);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		worldRenderer.translateScene(distanceX/10, 0, distanceY/10);//worldRenderer.rotateScene(-distanceY, -distanceX ,0);
		return true;
	}
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		worldRenderer.onSingleTap(e, getWidth(), getHeight());
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
		// TODO Auto-generated method stub
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
	@Override
	public boolean onPinch(ScaleGestureDetector detector) {
		worldRenderer.scaleScene(detector.getScaleFactor());
		return true;
	}

}
