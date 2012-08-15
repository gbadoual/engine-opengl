package com.android.opengl.view.state;

import android.view.MotionEvent;

import com.android.opengl.view.GestureDetector;
import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.MotionEventDispatcher;
import com.android.opengl.view.Touchable;

public class GameInProgressState extends EngineState{
	
	private GestureDetector gestureDetector;
	private MotionEventDispatcher motionEventDispatcher;
	
	public GameInProgressState(EngineRenderer worldRenderer) {
		super(worldRenderer);
		gestureDetector = new GestureDetector(worldRenderer.getContext(), gestureListener);
	}
	
	@Override
	public void loadLevel() {
		motionEventDispatcher = new MotionEventDispatcher();
		motionEventDispatcher.registerToucheble(worldRenderer.getScene().getGlView(), 0);
		motionEventDispatcher.registerToucheble(gestureDetector, 1);
	}

	@Override
	public void onDrawFrame() {
		worldRenderer.getScene().drawFrame();
	}
	
	
	
	private Touchable currntlyTouched;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		motionEventDispatcher.dispatchTouchEvent(event);
//		switch (event.getActionMasked()) {
//		case MotionEvent.ACTION_DOWN:
//			if(worldRenderer.getScene().getGlView().onTouchEvent(event)){
//				currntlyTouched = worldRenderer.getScene().getGlView();
//			} else{
//				currntlyTouched = gestureDetector;
//				currntlyTouched.onTouchEvent(event);
//			}
//			
//			break;
//		case MotionEvent.ACTION_UP:
//			currntlyTouched.onTouchEvent(event);
//			currntlyTouched = null;
//			break;
//		default:
//			if(currntlyTouched != null){
//				currntlyTouched.onTouchEvent(event);
//			}
//			break;
//		}
		return true;
	}
	
	
	
	private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {

		private static final float SLIDE_FACTOR = (float) (Math.PI / 180);
		private static final float TRANSLATE_FACTOR = 10;

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			worldRenderer.translateScene(distanceX / TRANSLATE_FACTOR, distanceY / TRANSLATE_FACTOR);
			return true;
		}
	
		@Override
		public boolean onRotate(float centerX, float centerY, float angle) {
			worldRenderer.rotateScene(0, angle ,0);
//			Log.i("tag", "onRotate: angle = " + angle);
			return true;
		}
		@Override
		public boolean onDoubleSlide(float distanceX, float distanceY) {
			worldRenderer.rotateScene(distanceY * SLIDE_FACTOR, 0 ,0);
//			Log.i("tag", "onDoubleSlide: distanceY = " + distanceY);
			return true;
		}	
		@Override
		public boolean onPinch(float centerX, float centerY, float scaleFactor) {
			worldRenderer.scaleScene(scaleFactor);
//			Log.i("tag", "onPinch: scaleFactor = " + scaleFactor);
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
