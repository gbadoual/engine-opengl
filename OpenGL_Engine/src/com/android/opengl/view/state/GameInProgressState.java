package com.android.opengl.view.state;

import android.view.MotionEvent;

import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.GestureDetector;

public class GameInProgressState extends EngineState{
	
	private GestureDetector gestureDetector;
	
	public GameInProgressState(EngineRenderer engineRenderer) {
		super(engineRenderer);
		gestureDetector = new GestureDetector(engineRenderer.getContext(), gestureListener);
	}
	

	@Override
	public void onWorldUpdate() {
		mEngineRenderer.getScene().onWorldUpdate();		
	}		

	@Override
	public void onDrawFrame() {
		mEngineRenderer.getCamera().onDrawFrame();
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}
	
	
	
	private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {

		private static final float SLIDE_FACTOR = (float) (Math.PI / 180);
		private static final float TRANSLATE_FACTOR = 10;

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			mEngineRenderer.translateScene(distanceX / TRANSLATE_FACTOR, distanceY / TRANSLATE_FACTOR);
			return true;
		}
	
		@Override
		public boolean onRotate(float centerX, float centerY, float angle) {
			mEngineRenderer.rotateScene(0, angle ,0);
//			Log.i("tag", "onRotate: angle = " + angle);
			return true;
		}
		@Override
		public boolean onDoubleSlide(float distanceX, float distanceY) {
			mEngineRenderer.rotateScene(distanceY * SLIDE_FACTOR, 0 ,0);
//			Log.i("tag", "onDoubleSlide: distanceY = " + distanceY);
			return true;
		}	
		@Override
		public boolean onPinch(float centerX, float centerY, float scaleFactor) {
			mEngineRenderer.scaleScene(scaleFactor);
//			Log.i("tag", "onPinch: scaleFactor = " + scaleFactor);
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			mEngineRenderer.onSingleTap(e.getX(), e.getY());
			return true;
		}
	
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
	
	};



}
