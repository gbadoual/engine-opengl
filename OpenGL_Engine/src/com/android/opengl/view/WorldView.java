package com.android.opengl.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.TextView;

public class WorldView extends GLSurfaceView implements OnGestureListener{
	private WorldRenderer worldRenderer;
	private TextView textView;
	private GestureDetector gestureDetector;

	public WorldView(Context context) {
		super(context);
		init();
	}
	public WorldView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	private void init() {
		gestureDetector = new GestureDetector(this);
		worldRenderer = new WorldRenderer(this);
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
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		worldRenderer.rotateScene(-distanceY, -distanceX ,0);
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

}
