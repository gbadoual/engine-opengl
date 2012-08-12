package com.android.opengl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class MotionEventDispatcher {
	
	private List<TouchableWrapper> wrapperList = new ArrayList<TouchableWrapper>();
	
	public Comparator<TouchableWrapper> touchableWrapperComparator = new Comparator<MotionEventDispatcher.TouchableWrapper>() {
		
		@Override
		public int compare(TouchableWrapper lhs, TouchableWrapper rhs) {
			if(lhs != null && rhs != null){
				return lhs.zOrder - rhs.zOrder; 
			}
			return 0;
		}
	};
	
	public boolean dispatchTouchEvent(MotionEvent event){
		boolean res = false;
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			for(TouchableWrapper wrapper: wrapperList){
				int pointerId = Math.min(event.getPointerId(event.getActionIndex()), event.getPointerCount() - 1);
				
				float x = event.getX(pointerId);
				float y = event.getY(pointerId);
				MotionEvent currentEvent = MotionEvent.obtain(event.getDownTime(), 
															  event.getEventTime(), 
															  event.getAction(), 
															  x, y, 
															  event.getMetaState());
				if(wrapper.deliverTouchEvent(currentEvent)){
					// adding the new pointer to the motionEvent
					if (wrapper.motionEvent != null){
						PointerCoords newPointerCoords = new PointerCoords();
						event.getPointerCoords(pointerId, newPointerCoords);
						PointerCoords[] pointerCoordsArray = new PointerCoords[wrapper.motionEvent.getPointerCount() + 1];
						for(int i = 0 ; i < pointerCoordsArray.length - 1; ++i){
							wrapper.motionEvent.getPointerCoords(i, pointerCoordsArray[i]);
						}
						pointerCoordsArray[wrapper.motionEvent.getPointerCount()] = newPointerCoords;
						wrapper.motionEvent.addBatch(event.getEventTime(), pointerCoordsArray, event.getMetaState());
					} else {
						wrapper.motionEvent = currentEvent;
					}
				}
			}
//			if(worldRenderer.getScene().getGlView().onTouchEvent(event)){
//				currntlyTouched = worldRenderer.getScene().getGlView();
//			} else{
//				currntlyTouched = gestureDetector;
//				currntlyTouched.onTouchEvent(event);
//			}
			
			break;
		case MotionEvent.ACTION_UP:
//			currntlyTouched.onTouchEvent(event);
//			currntlyTouched = null;
			break;
		default:
			for(TouchableWrapper wrapper: wrapperList){
				res |= wrapper.deliverTouchEvent();
			}
			break;
		}
		return res;
	}
	
	public boolean registerToucheble(Touchable touchable, int zOrder){
		boolean res = false;
		TouchableWrapper wrapper = new TouchableWrapper(touchable, null, zOrder);
		if(!wrapperList.contains(wrapper)){
			res = wrapperList.add(wrapper);
			if(res){
				Collections.sort(wrapperList, touchableWrapperComparator);
			}
		}
		return res;
	}
	public boolean unregisterToucheble(Touchable touchable){
		return wrapperList.remove(new TouchableWrapper(touchable, null, 0));
	}
	
	
	
	private static class TouchableWrapper{
		public Touchable touchable;
		public MotionEvent motionEvent;
		public int zOrder;
		public TouchableWrapper(Touchable touchable, MotionEvent motionEvent,
				int zOrder) {
			super();
			this.touchable = touchable;
			this.motionEvent = motionEvent;
			this.zOrder = zOrder;
		}
		
		public boolean deliverTouchEvent(MotionEvent event) {
			if(touchable != null ){
				return touchable.onTouchEvent(event);
			}
			return false;
		}

		public boolean deliverTouchEvent(){
			if(touchable != null && motionEvent != null){
				return touchable.onTouchEvent(motionEvent);
			}
			return false;
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((motionEvent == null) ? 0 : motionEvent.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TouchableWrapper other = (TouchableWrapper) obj;
			if (motionEvent == null) {
				if (other.motionEvent != null)
					return false;
			} else if (!motionEvent.equals(other.motionEvent))
				return false;
			return true;
		}
	}

}
