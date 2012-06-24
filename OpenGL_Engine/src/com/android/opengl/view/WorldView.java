package com.android.opengl.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class WorldView extends GLSurfaceView{
	private WorldRenderer worldRenderer;
	private TextView textView;

	public WorldView(Context context) {
		super(context);
		init();
	}
	public WorldView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	private void init() {
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
	
	private float lastX;
	private float lastY;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float dx = lastX - event.getX();			
			float dy = lastY - event.getY();
//			worldRenderer.rotateScene(-dy, 0 ,0);
			worldRenderer.rotateScene(-dy, -dx ,0);
			lastX = event.getX();
			lastY = event.getY();
			
			break;
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			worldRenderer.onSingleTap(event, getWidth(), getHeight());
			break;

		default:
			break;
		}
		return true;
	}

}
