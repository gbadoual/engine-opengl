package com.android.opengl.view.state.startedgamestate;

import com.android.opengl.view.state.BaseState;
import com.android.opengl.view.state.StartedGameState;

public abstract class BaseStartedGameSubstate implements BaseState{

	protected StartedGameState mStartedGameState;
	
	public BaseStartedGameSubstate(StartedGameState startedGameState) {
		mStartedGameState = startedGameState;
	}
	
	
	
	@Override
	public abstract void onDrawFrame();
	public void onWorldUpdate(){
	};
	public abstract void loadLevel();
	public abstract void beginGame();

}
