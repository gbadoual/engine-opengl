package com.android.opengl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class MotionEventDispatcher {
	
	private static final String TAG = MotionEventDispatcher.class.getSimpleName();

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
		if(wrapperList.isEmpty()){
			Log.i(TAG, "no any Touchable registered. Use registerTouchable(Touchable) to deliever MotionEvent");
			return false;
		}
		
		boolean res = false;
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		for(TouchableWrapper touchableWrapper: wrapperList){
			touchableWrapper.syncState(event);
		}
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			PointerCoords pointerCoords = new PointerCoords();
			event.getPointerCoords(pointerIndex, pointerCoords);
			PointerData pointerData = new PointerData(pointerCoords, pointerId);

			for(TouchableWrapper wrapper: wrapperList){
				if(wrapper.touchable.getBoundariesRectInPixel().isWithinRect(pointerCoords.x, pointerCoords.y)){
					
					MotionEvent currentEvent;
					if(wrapper.motionEvent != null){
						wrapper.motionEvent.setAction(0);
						currentEvent = mergeEventWithCoordinates(wrapper.motionEvent, pointerData);
						wrapper.motionEvent.recycle();
					} else{
						currentEvent = MotionEventDispatcher.obtainMotionEvent(event, event.getEventTime(), pointerData);
					}
					wrapper.motionEvent = currentEvent;
					wrapper.syncAction(event);
					res = wrapper.deliverTouchEvent();
					break;
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			for(TouchableWrapper touchableWrapper: wrapperList){
				res = removePointer(touchableWrapper, pointerId);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			for(TouchableWrapper touchableWrapper: wrapperList){
				res = touchableWrapper.deliverTouchEvent();
				touchableWrapper.disposeMotionEvent();
			}
			break;
		default:
			for(TouchableWrapper wrapper: wrapperList){
				res |= wrapper.deliverTouchEvent();
			}
			break;
		}
		return res;
	}
	
	@SuppressWarnings("deprecation")
	public static MotionEvent obtainMotionEvent(MotionEvent event, long downTime, PointerData pointerData) {
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		for(int i = 0; i < pointerData.pointerIds.length; ++i){
			if(pointerData.pointerIds[i] == pointerId){
				pointerIndex = i;
				break;
			} 
		}

		int action = event.getAction();
		action = action & (0xFF | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
		MotionEvent mergedEvent = MotionEvent.obtain(downTime, 
				event.getEventTime(), 
				action, 
				pointerData.pointerIds.length, 
				pointerData.pointerIds, 
				pointerData.pointerCoords, 
				event.getMetaState(), 
				event.getXPrecision(), event.getYPrecision(), 
				event.getDeviceId(), 	
				event.getEdgeFlags(), 
				event.getSource(), 
				event.getFlags());
		
		return 	mergedEvent;
	}

	public boolean registerToucheble(Touchable touchable, int zOrder){
		boolean res = false;
		TouchableWrapper wrapper = new TouchableWrapper(touchable, null, zOrder);
		res = wrapperList.add(wrapper);
		if(res){
			Collections.sort(wrapperList, touchableWrapperComparator);
		}
		return res;
	}

	public boolean unregisterToucheble(Touchable touchable){
		return wrapperList.remove(new TouchableWrapper(touchable, null, 0));
	}
	
	public void addPointer(TouchableWrapper touchableWrapper, PointerCoords pointerCoords, int pointerId){
		PointerData pointerData = getEventPointerData(touchableWrapper.motionEvent);
		touchableWrapper.updatePointerCoordinates(mergePointerData(pointerData, new PointerData(pointerCoords, pointerId)));
	}

	public boolean removePointer(TouchableWrapper touchableWrapper, int pointerId){
		boolean res = false;
		int pointerIndexToDelete = touchableWrapper.findPointerIndex(pointerId);
		if(pointerIndexToDelete >= 0){
			res = touchableWrapper.deliverTouchEvent();
			int size = touchableWrapper.getPointerCount();
			if(size == 1){
				touchableWrapper.disposeMotionEvent();
				return true;
			}
			PointerData pointerData = new PointerData(new PointerCoords[size - 1], new int[size - 1]);
			int pointerCoordIndex = 0;
			for(int i = 0 ; i < size; ++i){
				if(i != pointerIndexToDelete){
					pointerData.pointerCoords[pointerCoordIndex] = new PointerCoords();
					touchableWrapper.getPointerCoords(i, pointerData.pointerCoords[pointerCoordIndex]);
					pointerData.pointerIds[pointerCoordIndex] = touchableWrapper.getPointerId(i);;
					pointerCoordIndex++;
				}
			}
			touchableWrapper.updatePointerCoordinates(pointerData);
			return res;
		}

		return false;
	}
	
	public MotionEvent mergeEventWithCoordinates(MotionEvent event, PointerData pointerDataToMerge){
		PointerData eventPointerData = getEventPointerData(event);
		return obtainMotionEvent(event, event.getDownTime(), mergePointerData(eventPointerData, pointerDataToMerge));
	}
	
	public PointerData getEventPointerData(MotionEvent event){
		int pointerCount = event.getPointerCount();
		PointerCoords[] pointerCoordsArray = new PointerCoords[pointerCount];
		int[] pointerIds = new int[pointerCount];
		for(int i = 0 ; i < pointerCoordsArray.length; ++i){
			pointerCoordsArray[i] = new PointerCoords();
			event.getPointerCoords(i, pointerCoordsArray[i]);
			pointerIds[i] = event.getPointerId(i);
		}
		return new PointerData(pointerCoordsArray, pointerIds);
	}
	
	public PointerData mergePointerData(PointerData left, PointerData right){
		int mergedSize = left.pointerCoords.length + right.pointerCoords.length;
		PointerData res = new PointerData(new PointerCoords[mergedSize], new int[mergedSize]);
		
		for(int i = 0; i < left.pointerCoords.length; ++i){
			res.pointerCoords[i] = left.pointerCoords[i];
			res.pointerIds[i] = left.pointerIds[i];
		}
		for(int i = 0; i < right.pointerCoords.length; ++i){
			res.pointerCoords[left.pointerCoords.length + i] = right.pointerCoords[i];
			res.pointerIds[left.pointerCoords.length + i] = right.pointerIds[i];
		}
		return res;
	}

	private static class TouchableWrapper{
		public Touchable touchable;
		private MotionEvent motionEvent;
		public int zOrder;
		
		
		public TouchableWrapper(Touchable touchable, MotionEvent motionEvent,
				int zOrder) {
			super();
			this.touchable = touchable;
			this.motionEvent = motionEvent;
			this.zOrder = zOrder;
		}
		
		public boolean syncAction(MotionEvent event) {
			if(motionEvent != null){
					MotionEventDispatcher.syncActionEvent(motionEvent, event);
					return true;
			}
			return false;
		}

		public void syncState(MotionEvent event) {
			if(motionEvent == null){
				return;
			}
			syncAction(event);
			int pointerCount = motionEvent.getPointerCount();
			PointerCoords[] pointerCoordsArray = new PointerCoords[pointerCount];
			for(int i = 0; i < pointerCount; ++i){
				pointerCoordsArray[i] = new PointerCoords();
				int gluePointerId = motionEvent.getPointerId(i);
				int eventPointerIndex = event.findPointerIndex(gluePointerId);
				if(eventPointerIndex >=0){
					event.getPointerCoords(eventPointerIndex, pointerCoordsArray[i]);
				} else{
					Log.w(TAG, "syncState(): no pointerId in child motionView. It is abnormal");
					motionEvent.getPointerCoords(i, pointerCoordsArray[i]);
				}
			}
			motionEvent.addBatch(event.getEventTime(), pointerCoordsArray, event.getMetaState());
		}

		public void disposeMotionEvent() {
			if(motionEvent != null){
				motionEvent.recycle();
				motionEvent = null;
			}
		}

		public int findPointerIndex(int pointerId) {
			if(motionEvent != null){
				return motionEvent.findPointerIndex(pointerId);
			}
			return -1;
		}

		public int getPointerId(int i) {
			if(motionEvent != null){
				return motionEvent.getPointerId(i);
			}
			return -1;
		}

		public void getPointerCoords(int i, PointerCoords pointerCoords) {
			if(motionEvent != null){
				motionEvent.getPointerCoords(i, pointerCoords);
			}
			
		}

		public int getPointerCount() {
			if(motionEvent != null){
				return motionEvent.getPointerCount();
			}
			return 0;
		}

		public void updatePointerCoordinates(PointerData pointerData){
			if(motionEvent != null){
				 MotionEvent tmpEvent= obtainMotionEvent(motionEvent, motionEvent.getDownTime(), pointerData);
				 motionEvent.recycle();
				 motionEvent = tmpEvent;
			}
		}
		
		public boolean deliverTouchEvent(MotionEvent event) {
			if(touchable != null ){
				Log.i(TAG, "delivering touchEvent: " + touchable.getClass().getSimpleName() + ", event = " + motionEvent);
				return touchable.onTouchEvent(event);
			}
			return false;
		}
		

		public boolean deliverTouchEvent(){
			if(touchable != null && motionEvent != null){
				return deliverTouchEvent(motionEvent);
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
	
	public static class PointerData{
		public PointerCoords[] pointerCoords;
		public int[] pointerIds;
		
		
		public PointerData() {
			
		}
		public PointerData(PointerCoords[] pointerCoords, int[] pointerIds) {
			this.pointerCoords = pointerCoords;
			this.pointerIds = pointerIds;
		}
		public PointerData(PointerCoords pointerCoords, int pointerId) {
			this.pointerCoords = new PointerCoords[]{pointerCoords};
			this.pointerIds = new int[]{pointerId};
		}
		
		
	}

	@SuppressWarnings("deprecation")
	public static int syncActionEvent(MotionEvent destEvent, MotionEvent srcEvent) {
		int action = MotionEvent.ACTION_MOVE;
		if(srcEvent.getActionMasked() == MotionEvent.ACTION_CANCEL){
			action = MotionEvent.ACTION_CANCEL;			
		}
		
		int pointerIdInEvent = srcEvent.getPointerId(srcEvent.getActionIndex());
		int pointerIndex = destEvent.findPointerIndex(pointerIdInEvent);
		// if pointerIndex < 0 then srcEvent action does not relate to this destEvent
		// no need to sync destEvent's action
		if(pointerIndex >= 0){
		
			action = srcEvent.getAction();
			// set appropriate pointerIndex for destEvent for the same pointerId in srcEvent 
			action = action & 0xFF | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);

			if(destEvent.getPointerCount() == 1){
				int actionMasked = srcEvent.getActionMasked();
				if(actionMasked == MotionEvent.ACTION_POINTER_1_DOWN ||
						   actionMasked == MotionEvent.ACTION_POINTER_2_DOWN ||
						   actionMasked == MotionEvent.ACTION_POINTER_3_DOWN ||
						   actionMasked == MotionEvent.ACTION_POINTER_DOWN){
					actionMasked = MotionEvent.ACTION_DOWN;														
				} else	if(actionMasked == MotionEvent.ACTION_POINTER_1_UP ||
						   actionMasked == MotionEvent.ACTION_POINTER_2_UP ||
						   actionMasked == MotionEvent.ACTION_POINTER_3_UP ||
						   actionMasked == MotionEvent.ACTION_POINTER_UP){
					actionMasked = MotionEvent.ACTION_UP;														
				}
				// set actionMasked for destEvent according to fingerPointCount()
				action = action & 0xFF00 | actionMasked;
			}
		}
		destEvent.setAction(action);
		return action;
	}

	public static MotionEvent obtainCancelEvent() {
		long curTime = System.currentTimeMillis();
		return MotionEvent.obtain(curTime, curTime, MotionEvent.ACTION_CANCEL, 0, 0, 0);
	}

}
