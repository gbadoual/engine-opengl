package com.android.opengl;

import com.android.opengl.view.WorldView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView fpsView;
	private WorldView worldView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fpsView = (TextView)findViewById(R.id.fps_view);
        worldView = (WorldView)findViewById(R.id.world_view);
        worldView.setFpsView(fpsView);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	worldView.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	worldView.onPause();
    }
    

}