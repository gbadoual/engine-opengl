package com.android.opengl.view.screen;

import com.android.opengl.Camera;
import com.android.opengl.util.Log;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.control.GLView;
import com.android.opengl.view.state.EngineState;

public abstract class GLScreen extends GLView{

	private EngineState mGLScreenContext;
	
	public GLScreen(Camera camera) {
		super(camera);
		mGLScreenContext = mCamera.getEngineRenderer().getCurrentEngineState();
		onLayout(0, 0);
		onMeasure(camera.getViewportWidth() * Camera.screenToPercentRatio, 
				camera.getViewportHeight() * Camera.screenToPercentRatio);
	}

	@Override
	public void onViewportChanged(Rect2D newViewportRect) {
		onMeasure(newViewportRect.mWidth * Camera.screenToPercentRatio, 
				newViewportRect.mHeight * Camera.screenToPercentRatio);
	}
	
	@Override
	public void onDrawFrame() {
		super.onDrawFrame();
	}
	
	protected abstract void onCreate();
//	public abstract void onStart();
//	public abstract void onStop();
	protected void onDestroy(){
		
	};
	
	
	protected void onBackPressed(){
		close();
	} 
	
	protected void close(){
		mGLScreenContext.close(this);
	}
	
	protected EngineState getGlScreenContext(){
		return mGLScreenContext;
	};

}
