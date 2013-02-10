package com.android.opengl.view.screen;

import java.util.Stack;


public class GLScreenStack extends Stack<GLScreen>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5906546821962383854L;


	@Override
	public synchronized boolean add(GLScreen screen) {
		if(!isEmpty()){
			lastElement().setVisible(false);
		}
		screen.onCreate();
		return super.add(screen);
	}
	
	@Override
	public synchronized GLScreen pop() {
		GLScreen poppedScreen = super.pop();
		poppedScreen.onDestroy();
		poppedScreen.release();
		if(!isEmpty()){
			lastElement().setVisible(true);
		}
		return poppedScreen;
	}
	
	public boolean onBackPressed(){
		if(!isEmpty()){
			lastElement().onBackPressed();
		}
		return !isEmpty();
	}

	public void onDrawFrame() {
		if(!isEmpty()){
			lastElement().onDrawFrame();
		}	
	}

}
