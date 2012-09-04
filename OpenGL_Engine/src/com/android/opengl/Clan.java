package com.android.opengl;

public enum Clan {
	BLUE,
	RED, 
	NEUTRAL;
	
	public float[] getColor(){
		switch (this) {
		case BLUE:
			return rgbaToNormolizedColor(-32, -32, 0, 0);
		case RED:
			return rgbaToNormolizedColor(32, 0, 0, 0);
		default:
			return rgbaToNormolizedColor(0, 0, -64, 0);
		}
	}
	
	private float[] rgbaToNormolizedColor(float r, float g, float b, float a){
		return new float[]{r / 255, g/255, b/255, a/255};
	}
	

}
