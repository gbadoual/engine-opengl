package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import com.android.opengl.Camera;
import com.android.opengl.gameobject.GameObject;
import com.android.opengl.util.Log;

public class GLIconGridLayout extends GLGridLayout{
	
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, List<GameObject>> iconIdToUnitListMap = new HashMap<Integer, List<GameObject>>();
	private boolean isExpanded;
	private int maxIconCount;

	public GLIconGridLayout(Camera camera) {
		super(camera);
		init();
	}
	

	public GLIconGridLayout(Camera camera, int left, int top, int width, int height) {
		super(camera, left, top, width, height);
		init();
	}
	
	private void init(){
		setMaxIconCount(8);
		expand();
	}


	public void addUnitListToGrid(List<GameObject> gameObjectList) {
		if(gameObjectList != null){
			for(GameObject gameObject: gameObjectList){
				addUnitToGrid(gameObject);
			}
		}
	}
	
	public void addUnitToGrid(GameObject gameObject) {
		Integer key = gameObject.getUnitIconResId();
		List<GameObject> gameObjectList = iconIdToUnitListMap.get(key);
		if(gameObjectList == null){
			gameObjectList = new ArrayList<GameObject>();
			iconIdToUnitListMap.put(key, gameObjectList);
		}
		gameObjectList.add(gameObject);
		if(getUnitsCount() <= maxIconCount){
			expand();
		} else {
			collapse();
		}
	}
	
	private boolean addGLIconAsChild(GameObject gameObject) {
		if(mChildren.size() < maxIconCount){
			GLUnitIcon glUnitIcon = gameObject.getUnitIconView();
			glUnitIcon.enableLongTap(true);
			glUnitIcon.setOnTapListener(onIconTapListener);
			super.addChild(glUnitIcon);
			return true;
		}
		return false;
	}


	@Override
	public void removeChildren() {
//		Collection<GameObject> gameObjectList = hashMap.values();
//		for(GameObject gameObject: gameObjectList){
//			
//		}
		iconIdToUnitListMap.clear();
		super.removeChildren();
		expand();
	}


	public boolean isExpanded() {
		return isExpanded;
	}


	public void expand(){
		isExpanded = true;
		super.removeChildren();
		
		Set<Entry<Integer, List<GameObject>>> entrySet = iconIdToUnitListMap.entrySet();
		for(Entry<Integer, List<GameObject>> entry: entrySet){
			for(GameObject gameObject: entry.getValue()){
				boolean addingRes = addGLIconAsChild(gameObject);
				if(!addingRes){
					return;
				};
			} 
		}
	}
	
	public void collapse(){
		isExpanded = false;
		super.removeChildren();

		Set<Entry<Integer, List<GameObject>>> entrySet = iconIdToUnitListMap.entrySet();
		for(Entry<Integer, List<GameObject>> entry: entrySet){
			if(!entry.getValue().isEmpty()){
				boolean addingRes = addGLIconAsChild(entry.getValue().get(0));
				if(!addingRes){
					return;
				};
			}
		}
		
	}
	
	public int getUnitsCount(){
		int unitsCount = 0;
		Set<Entry<Integer, List<GameObject>>> entrySet = iconIdToUnitListMap.entrySet();
		for(Entry<Integer, List<GameObject>> entry: entrySet){
			unitsCount += entry.getValue().size();
		}
		return unitsCount;
	}
	
	public int getMaxIconCount() {
		return maxIconCount;
	}


	public void setMaxIconCount(int maxIconCount) {
		this.maxIconCount = maxIconCount;
	}
	
	private OnTapListener onIconTapListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			GLUnitIcon glUnitIcon = (GLUnitIcon)glView;
			
			Set<Entry<Integer, List<GameObject>>> entrySet = iconIdToUnitListMap.entrySet();
			Entry<Integer, List<GameObject>> selectedEntry = null;
			for(Entry<Integer, List<GameObject>> entry: entrySet){
				if(entry.getKey() != glUnitIcon.getBackgroundResId()){
					for(GameObject gameObject: entry.getValue()){
						gameObject.setSelected(false);
					} 
				} else {
					selectedEntry = entry;
				}
			}
			if(selectedEntry == null){
				Log.w(TAG, "List in hasMap with key " + glUnitIcon.getBackgroundResId() + " is not presented. Aborting");
				return;
			}
			if(isExpanded){
				for(GameObject gameObject: selectedEntry.getValue()){
					if(gameObject != glUnitIcon.getGameObject()){
						gameObject.setSelected(false);
					}
				}
			}
			glUnitIcon.getGameObject().getParentScene().getCamera().notifySelectedObjectsChanged();
		}

		@Override
		public void onLongTap(GLView glView) {
			GLUnitIcon glUnitIcon = (GLUnitIcon)glView;
			
			Set<Entry<Integer, List<GameObject>>> entrySet = iconIdToUnitListMap.entrySet();
			Entry<Integer, List<GameObject>> selectedEntry = null;
			for(Entry<Integer, List<GameObject>> entry: entrySet){
				if(entry.getKey() == glUnitIcon.getBackgroundResId()){
					if(!isExpanded){
						for(GameObject gameObject: entry.getValue()){
							gameObject.setSelected(false);
						} 
					} else {
						int id = entry.getValue().indexOf(glUnitIcon.getGameObject());
						if(id < 0){
							Log.w(TAG, glUnitIcon.getGameObject() + "not found. Aborting");
							return;
						}
						entry.getValue().get(id).setSelected(false);
					}
					
				}
			}
			glUnitIcon.getGameObject().getParentScene().getCamera().notifySelectedObjectsChanged();
		}
	};

}
