package com.android.opengl.view.state.startedgamestate;

import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.Clan;
import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.GLScene;
import com.android.opengl.gameobject.building.MainBase;
import com.android.opengl.gameobject.unit.vehicle.BMW;
import com.android.opengl.view.control.GLTextView;
import com.android.opengl.view.state.StartedGameState;


public class LoadingLevelState extends BaseStartedGameSubstate{

	private GLTextView glTextView;
	public LoadingLevelState(StartedGameState startedGameState) {
		super(startedGameState);
	}

	@Override
	public void onDrawFrame() {
		if(glTextView != null){
			glTextView.onDrawFrame();
		}
				
	}

	@Override
	public void loadLevel() {
		glTextView = new GLTextView(mStartedGameState.getCamera());
		glTextView.setText("Loading...");
		onDrawFrame();
		long time = System.currentTimeMillis();
		mStartedGameState.getCamera().initControls();

//		mStartedGameState.mEngineRenderer.getCallbackHandler().removeMessages(WorldView.DIALOG_LOADING_SHOW);
//		mEngineRenderer.getCallbackHandler().sendEmptyMessage(WorldView.DIALOG_LOADING_SHOW);
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
//		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		initGameObjects();
//		mEngineRenderer.getCallbackHandler().sendEmptyMessage(WorldView.DIALOG_LOADING_DISMISS);

		time = System.currentTimeMillis() - time;
		Log.i("tag", "world loaded for " + time / 1000.0f + " sec.");
		mStartedGameState.setCurrentState(mStartedGameState.getGameInProgressState());
		mStartedGameState.getCurrentState().beginGame();
		
	}
	public void initGameObjects() {
		CommonGameObject.facesCount = 0;
		
		GLScene scene = new GLScene(mStartedGameState.getCamera());
//		BMW bmw1 = new BMW(scene);
//		bmw1.setPosition(-8, -7);
//
//		bmw2 = new BMW(scene);
//		cube1 = new Cube(scene);
//		cube1.setPosition(0, -6);
//		cube1.moveTo(new Point3D(-5, 0, 0));
//
//		
//		cube2 = new Cube(scene);
//		cube2.setPosition(4, 4);
//
//		earth = new Earth(scene);
//		earth.setPosition(-6, 3);
//		MainBase mainBase = new MainBase(scene, Clan.BLUE);
//		mainBase.setPosition(-40, 40);
//
//		MainBase enemyMainBase = new MainBase(scene, Clan.RED);
//		enemyMainBase.setPosition(40, -40);
		mStartedGameState.setScene(scene);
//		for(int i = 0; i < 4; ++i){
//			gameObjectList.add(new BMW(scene));
//		}
	}

	@Override
	public void beginGame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

}
