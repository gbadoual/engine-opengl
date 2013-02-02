package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.Scene;

public class GLGridLayout extends GLLayout{
	
	private static final int DEFAULT_ROW_COUNT = 3;
	private static final int DEFAULT_COLUMN_COUNT = 3;
	
	public enum Orientation{
		VERTICAL,
		HORIZONTAL
	}
	

	private GLGridInfo mGridInfo;
	

	


	private float nextX;
	private float nextY;
	private float prevRowHeight;
	
 	public GLGridLayout(Scene scene, float left, float top, float width, float height) {
		super(scene, left, top, width, height);
		init();
	}


	public GLGridLayout(Scene scene) {
		super(scene);
		init();
	}


	public GLGridLayout(Camera camera) {
		super(camera);
		init();
	}
	
	
	public GLGridLayout(Camera camera, float left, float top, float width, float height) {
		super(camera, left, top, width, height);
		init();
	}

	private void init(){
		mGridInfo = getGridInfo();
		resetLayoutParams();
	}

	private void resetLayoutParams(){
		nextX = 0;
		nextY = getGridInfo().verticalSpacing;
		prevRowHeight = 0;
		
	}

	private GLGridInfo getGridInfo() {
		if(mGridInfo == null){
			mGridInfo = new GLGridInfo(this);
		}
		return mGridInfo;
	}


	@Override
	public void addChild(GLView child) {
		super.addChild(child);
		layoutNextChild(child);
	}
	
	private void layoutNextChild(GLView child) {
		nextX += getHorizantalSpacing();
		float newLayoutX;
		float newLayoutY;
		if (nextX + child.mWidth >= mWidth){
			nextX = getHorizantalSpacing();

			if(mChildren.size() > 1){
				nextY += prevRowHeight + getVerticalSpacing();
			}
			newLayoutX = nextX;
			newLayoutY = nextY;
			prevRowHeight = child.mHeight;
		} else{
			newLayoutX = nextX;
			newLayoutY = nextY;
			
			prevRowHeight = Math.max(prevRowHeight, child.mHeight);
		}
		if(nextY + child.mHeight > mHeight){
			onMeasure(Math.max(mWidth, 2 * getHorizantalSpacing() + child.mWidth), nextY + getVerticalSpacing() + child.mHeight);
		}
		Log.i("tag", "layout left/bottom = " + newLayoutX + "/" + newLayoutY);
		Log.i("tag", "layout child.width = " + child.mWidth);
		
		nextX += child.mWidth;
		child.onLayout(newLayoutX, newLayoutY);
	}
	
	private void layoutNextChild2(GLView glView){
		if(mGridInfo.getGridWidth() == 0){
			mGridInfo.addColumn(glView.mWidth);
		}
		if(mGridInfo.getGridHeight() == 0){
			mGridInfo.addRow(glView.mHeight);
		}
//		if(mGridInfo.getColumnWidth(colunmNumber));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		resetLayoutParams();
		for(GLView child: mChildren){
			layoutNextChild(child);
		}
	}

	@Override
	public void removeChildren() {
		super.removeChildren();
		onMeasure(mWidth, 1);
		resetLayoutParams();
	}



	public float getVerticalSpacing() {
		return mGridInfo.verticalSpacing;
	}

	public float getHorizantalSpacing() {
		return mGridInfo.horizantalSpacing;
	}

	public void setSpacing(float horizontalSpacing, float verticalSpacing){
		mGridInfo.horizantalSpacing = horizontalSpacing;
		mGridInfo.verticalSpacing = verticalSpacing;
	}

	
	
	
	
	
	public static class GLGridInfo{
		private static final int INAVLID_RAW_COLUM = -1;
		private int mMaxRowCount = DEFAULT_ROW_COUNT;
		private int mMaxColumnCount = DEFAULT_COLUMN_COUNT;
		
		// dimensions are represented in percents from the largest side of the screen
		public float horizantalSpacing = 2;
		public float verticalSpacing = 2;
		
		private int mCurColumn;
		private int mCurRow;
		
		private Orientation mOrientation = Orientation.VERTICAL;
		
		private List<Float> mRowsHeightList = new ArrayList<Float>();
		private List<Float> mColumnsWidthList = new ArrayList<Float>();
		private GLGridLayout mParentGLGridLayout;
		
		public GLGridInfo(GLGridLayout parentGLGridLayout) {
			mParentGLGridLayout = parentGLGridLayout;
		}
		
		public void resetGridInfo(){
			mRowsHeightList.clear();
			mColumnsWidthList.clear();
			mParentGLGridLayout.onMeasure(2 * horizantalSpacing, 2 * verticalSpacing);
		}
		
		public boolean addColumn(float width){
			width += 2 * horizantalSpacing;
			if(width <= 0){
				throw new IllegalArgumentException("width should be > 0");
			}
			boolean res = mColumnsWidthList.size() < getMaxColumnCount() - 1 && mColumnsWidthList.add(width);
			if(res){
				mParentGLGridLayout.onMeasure(getGridWidth(), getGridHeight());
			}
			return res;
		}
		
		public float getColumnWidth(int colunmNumber){
			return mColumnsWidthList.get(colunmNumber);
		}
		
		public float getCurColumnWidth(){
			return mColumnsWidthList.get(mCurColumn);
		}
		
		public float setColumnWidth(int colunmNumber, float columnWidth){
			return mColumnsWidthList.set(colunmNumber, columnWidth);
		}
		
		public float getRowHeight(int rowNumber){
			return mRowsHeightList.get(rowNumber);
		}

		public float getCurRowHeight(){
			return mRowsHeightList.get(mCurRow);
		}
		
		public float setRowHeight(int rowNumber, float rowHeight){
			return mRowsHeightList.set(rowNumber, rowHeight);
		}

		public boolean addRow(float height){
			height += 2 * verticalSpacing;
			if(height <= 0){
				throw new IllegalArgumentException("height should be > 0");
			}
			boolean res = mRowsHeightList.size() < getMaxRowCount() - 1 && mRowsHeightList.add(height); 
			if(res){
				mParentGLGridLayout.onMeasure(getGridWidth(), getGridHeight());
			}
			return res;
		}

		public boolean removeColumns(){
			boolean res = !mColumnsWidthList.isEmpty() && mColumnsWidthList.remove(mColumnsWidthList.size() - 1) > 0;
			if(res){
				decCurColumn();
				mParentGLGridLayout.onMeasure(getGridWidth(), getGridHeight());
			}
			return res;
		}



		public boolean removeRow(){
			boolean res = !mRowsHeightList.isEmpty() && mRowsHeightList.remove(mRowsHeightList.size() - 1) > 0;
			if(res){
				decCurRow();
				mParentGLGridLayout.onMeasure(getGridWidth(), getGridHeight());
			}
			return res;
		}
		

		public float getGridWidth(){
			float gridWidth = 0;
			for(float columnWidth: mColumnsWidthList){
				gridWidth += columnWidth;
			}
			return gridWidth;
		}
		
		public float getGridHeight(){
			float gridHeight = 0;
			for(float rowHeight: mRowsHeightList){
				gridHeight += rowHeight;
			}
			return gridHeight;
		}

		public int getMaxRowCount() {
			return mMaxRowCount;
		}

		public void setMaxRowCount(int mMaxRowCount) {
			this.mMaxRowCount = mMaxRowCount;
		}

		public int getMaxColumnCount() {
			return mMaxColumnCount;
		}

		public void setMaxColumnCount(int mMaxColumnCount) {
			this.mMaxColumnCount = mMaxColumnCount;
		}

		public float getHorizantalSpacing() {
			return horizantalSpacing;
		}

		public void setHorizantalSpacing(float horizantalSpacing) {
			this.horizantalSpacing = horizantalSpacing;
		}

		public float getVerticalSpacing() {
			return verticalSpacing;
		}

		public void setVerticalSpacing(float verticalSpacing) {
			this.verticalSpacing = verticalSpacing;
		}

		public int getCurColumn() {
			return mCurColumn;
		}

		public void incHorizontally(GLView glView) {
			if(mCurColumn < mMaxColumnCount - 1){
				mCurColumn++;
				if(mCurColumn >= mColumnsWidthList.size()){
					addColumn(glView.mWidth);
				};
			} else {
				if(mCurRow < mMaxRowCount - 1){
					mCurColumn = 0;
					incVertycally(glView);
				}
			}
		}

		public int getCurRow() {
			return mCurRow;
		}

		public void incVertycally(GLView glView) {
			if(mCurRow < mMaxRowCount - 1){
				mCurRow ++;
				if(mCurRow >= mRowsHeightList.size()){
					addRow(glView.mHeight);
				}
			} else {
				if(mCurColumn < mMaxColumnCount - 1){
					mCurRow = 0;
					incHorizontally(glView);
				}
			}
		}
		
		private void decCurColumn() {
			// TODO Auto-generated method stub
		}
		
		private void decCurRow() {
			// TODO Auto-generated method stub
		}
		
	}


}
