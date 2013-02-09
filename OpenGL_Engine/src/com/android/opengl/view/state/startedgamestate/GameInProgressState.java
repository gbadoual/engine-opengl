package com.android.opengl.view.state.startedgamestate;

import com.android.opengl.view.state.BaseState;
import com.android.opengl.view.state.StartedGameState;

public class GameInProgressState extends BaseStartedGameSubstate{

	public GameInProgressState(StartedGameState startedGameState) {
		super(startedGameState);
	}

	@Override
	public void onDrawFrame() {
		mStartedGameState.getCamera().onDrawFrame();		
	}

	@Override
	public void loadLevel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beginGame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWorldUpdate() {
		mStartedGameState.getScene().onWorldUpdate();		
	}

}
