package com.android.opengl.view;

import java.util.Arrays;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

public class GestureDetector{

	private enum GESTURE_STATE{
		ROTATE,
		SCALE,
		DOUBLE_SLIDE,
		NONE
	}

	private static final int INVALID_ID = -1;

	private static final float MIN_ANGLE_TO_BEGIN_ROTATION = (float) Math.toRadians(1.2);
	private static final float MIN_DISTANCE_TO_BEGIN_PINCH = 1.05f;
	private static final float MIN_DISTANCE_TO_BEGIN_DOUBLE_SLIDE = 3f;

	
	private static final double EPSILON = 0.000001f;

	private OnGestureListener gestureListener;
	private android.view.GestureDetector defaultGesturedetector;
	
	public boolean isMultitouchOccourred;

	private GESTURE_STATE currentState = GESTURE_STATE.NONE;

	private float prevX1, prevY1, prevX2, prevY2;
	private int[] pointerIndexes; 
	private float centerX, centerY;

	
	public GestureDetector(Context context, OnGestureListener listener) {
//		Log.i("tag", "thread = " + Thread.currentThread() + ", looper = " + Looper.myLooper());
//		StackTraceElement[] elements = Thread.getAllStackTraces().get(Thread.currentThread());
//		for(StackTraceElement element: elements){
//			Log.i("tag", "" + element);
//		}
//		Log.i("tag", "===========================================================");
		defaultGesturedetector = new android.view.GestureDetector(context, dispatcherGestureListener);
		gestureListener = listener;
		pointerIndexes = new int[2];
		Arrays.fill(pointerIndexes, INVALID_ID);
	}


	public boolean onTouchEvent(MotionEvent event) {
		if(event.getPointerCount() > 2){
			return false;
		}
		if(event.getPointerCount() == 1){
			prevX1 = event.getX();
			prevY1 = event.getY();				
		}
		Log.i("tag", "gesture detector event = " + event);
		
		Log.i("tag", "cur action = " + event.getAction());
		Log.i("tag", "cur pointerIndex = " + event.getActionIndex());
		Log.i("tag", "cur pointerId = " + event.getPointerId(event.getActionIndex()));
		Log.i("tag", "cur pointerIndex[0] = " + pointerIndexes[0]);
		Log.i("tag", "cur pointerIndex[1] = " + pointerIndexes[1]);

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			currentState = GESTURE_STATE.NONE;
			isMultitouchOccourred = false;
			pointerIndexes[0] = event.getActionIndex();
			prevX1 = event.getX(pointerIndexes[0]);
			prevY1 = event.getY(pointerIndexes[0]);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerIndexes[1] = event.getActionIndex();
			prevX2 = event.getX(pointerIndexes[1]);
			prevY2 = event.getY(pointerIndexes[1]);
			centerX = (prevX1 + prevX2) / 2;
			centerY = (prevY1 + prevY2) / 2;
			isMultitouchOccourred = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if(pointerIndexes[0] != INVALID_ID && pointerIndexes[1] != INVALID_ID && 
			 pointerIndexes[0] != pointerIndexes[1] && event.getPointerCount() == 2){
				float curX1 = event.getX(pointerIndexes[0]);
				float curY1 = event.getY(pointerIndexes[0]);
				float curX2 = event.getX(pointerIndexes[1]);
				float curY2 = event.getY(pointerIndexes[1]);
				float dAngle = angleBtwLines(prevX1, prevY1, prevX2, prevY2, curX1, curY1, curX2, curY2);
				float dScaleFactor = scaleFactor(prevX1, prevY1, prevX2, prevY2, curX1, curY1, curX2, curY2);
				float[] doubleSlideDistanceXY = new float[2];
				doubleSlideDistance(doubleSlideDistanceXY, prevX1, prevY1, prevX2, prevY2, curX1, curY1, curX2, curY2);

				if(Math.abs(doubleSlideDistanceXY[0]) > MIN_DISTANCE_TO_BEGIN_DOUBLE_SLIDE ||
					Math.abs(doubleSlideDistanceXY[1]) > MIN_DISTANCE_TO_BEGIN_DOUBLE_SLIDE){
					currentState = GESTURE_STATE.DOUBLE_SLIDE;
				}
				if(Math.abs(dAngle) > MIN_ANGLE_TO_BEGIN_ROTATION){
					currentState = GESTURE_STATE.ROTATE;
				}
				if(dScaleFactor > MIN_DISTANCE_TO_BEGIN_PINCH ||
						1/dScaleFactor > MIN_DISTANCE_TO_BEGIN_PINCH){
					currentState = GESTURE_STATE.SCALE;
				}
				
				prevX1 = curX1;
				prevY1 = curY1;
				prevX2 = curX2;
				prevY2 = curY2;
				
				switch (currentState) {
				case ROTATE:
					return gestureListener.onRotate(centerX, centerY, dAngle);
				case SCALE:
					return gestureListener.onPinch(centerX, centerY, dScaleFactor);
				case DOUBLE_SLIDE:
					return gestureListener.onDoubleSlide(doubleSlideDistanceXY[0], doubleSlideDistanceXY[1]);						
				default:
					break;
				}
			}
			
			break;

		case MotionEvent.ACTION_UP:
			pointerIndexes[0] = INVALID_ID;
			currentState = GESTURE_STATE.NONE;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if(event.getPointerCount() <= 2){
				if(event.getActionIndex() == pointerIndexes[0]){
					pointerIndexes[0] = pointerIndexes[1];
					prevX1 = prevX2;
					prevY1 = prevY2;
				}
				pointerIndexes[1] = INVALID_ID;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			Arrays.fill(pointerIndexes, INVALID_ID);
			currentState = GESTURE_STATE.NONE;
			isMultitouchOccourred = false;
			break;		

		default:
			break;
		}
		
		return defaultGesturedetector.onTouchEvent(event);

	}
	

	private static final double MAX_DOUBLE_SLIDE_DISTANCE_DIFFERENCE = 2;
	private static final double MAX_DOUBLE_SLIDE_ANGLE_DIFFERENCE = 0.3f;
	private void doubleSlideDistance(float[] doubleSlideDistanceXY, float fx1, float fy1, float fx2, float fy2, float sx1, float sy1, float sx2, float sy2) {

		boolean translateDistanceX = (fx1 - sx1 - fx2 + sx2) < MAX_DOUBLE_SLIDE_DISTANCE_DIFFERENCE;
		boolean translateDistanceY = (fy1 - sy1 - fy2 + sy2) < MAX_DOUBLE_SLIDE_DISTANCE_DIFFERENCE;
		if(currentState == GESTURE_STATE.DOUBLE_SLIDE){
	    	doubleSlideDistanceXY[0] = (float)(sx1 - fx1 + sx2 - fx2) / 2;
	    	doubleSlideDistanceXY[1] = (float)(sy1 - fy1 + sy2 - fy2) / 2;
		} else 	if(translateDistanceX && translateDistanceY){
		    double angle1 = Math.toDegrees(Math.atan2(fy1 - fy2, fx1 - fx2)) % 360;
		    double angle2 = Math.toDegrees(Math.atan2(sy1 - sy2, sx1 - sx2)) % 360;
//			Log.i("tag", "angle1 = " + angle1);
//			Log.i("tag", "angle2 = " + angle2);
		    if (Math.abs(angle1 - angle2) < MAX_DOUBLE_SLIDE_ANGLE_DIFFERENCE){
		    	doubleSlideDistanceXY[0] = (float)(sx1 - fx1 + sx2 - fx2) / 2;
		    	doubleSlideDistanceXY[1] = (float)(sy1 - fy1 + sy2 - fy2) / 2;
		    }
		}
	}

	private static final float MIN_ALLOWED_SCALEFACTOR = 0.5f;
	private static final float MAX_ALLOWED_SCALEFACTOR = 1/MIN_ALLOWED_SCALEFACTOR;

	private float scaleFactor(float prevX1, float prevY1,
			float prevX2, float prevY2, float curX1, float curY1,
			float curX2, float curY2) {
		double oldDistance = Math.sqrt((prevX1 - prevX2) * (prevX1 - prevX2) + (prevY1 - prevY2) * (prevY1 - prevY2));
		double newDistance = Math.sqrt((curX1 - curX2) * (curX1 - curX2) + (curY1 - curY2) * (curY1 - curY2));
		float res = 1;
		if(Math.abs(oldDistance) > EPSILON && Math.abs(newDistance) > EPSILON){
			res = (float)(newDistance/oldDistance);
		}
		if(res < MIN_ALLOWED_SCALEFACTOR || res > MAX_ALLOWED_SCALEFACTOR ){ res = 1; };
		return res;
	}


	private static final double MAX_ALLOWED_ANGLE = Math.toRadians(20);
	
	private float angleBtwLines (float fx1, float fy1, float fx2, float fy2, float sx1, float sy1, float sx2, float sy2){
		
	    double angle1 = Math.atan2(fy1 - fy2, fx1 - fx2);
	    double angle2 = Math.atan2(sy1 - sy2, sx1 - sx2);
	    double res = angle1 - angle2;
//	    if(res < -180) {res += 360;}
//	    if(res >  180) {res -= 360;}
	    if(res < -MAX_ALLOWED_ANGLE || res > MAX_ALLOWED_ANGLE) {res = 0;}

	    
	    return (float) res;
	}


	
	
	public static interface OnGestureListener extends android.view.GestureDetector.OnGestureListener{
		
		public boolean onPinch(float centerX, float centerY, float angle);
		public boolean onDoubleSlide(float distanceX,	float distanceY);
		public boolean onRotate(float centerX, float centerY, float angle);
	}
	
	
	// this is workaroud for android 2.3.3 firing osSingleTapUp if multitouch event occurred 
	private android.view.GestureDetector.OnGestureListener dispatcherGestureListener = new android.view.GestureDetector.OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			if(!isMultitouchOccourred){
				return gestureListener.onSingleTapUp(arg0);
			}
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent arg0) {
			gestureListener.onShowPress(arg0);
			
		}
		
		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			return gestureListener.onScroll(arg0, arg1, arg2, arg3);
		}
		
		@Override
		public void onLongPress(MotionEvent arg0) {
			gestureListener.onLongPress(arg0);				
		}
		
		@Override
		public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			return gestureListener.onFling(arg0, arg1, arg2, arg3);
		}
		
		@Override
		public boolean onDown(MotionEvent arg0) {
			return gestureListener.onDown(arg0);
		}
		
	};
	
}
