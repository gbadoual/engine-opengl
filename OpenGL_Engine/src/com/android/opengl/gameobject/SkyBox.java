package com.android.opengl.gameobject;

import android.content.Context;
import android.content.res.Resources;

import com.android.opengl.shader.Shader;

public class SkyBox {
	
	private SkyBoxShader skyBoxShader = new SkyBoxShader();
	private Resources resources;
	
	
	public SkyBox(Context context) {
		this.resources = context.getResources();
		init();
	}
	public SkyBox(Resources resourcest) {
		this.resources = resourcest;
		init();
	}
	
	
	private void init() {
		
	}
	
	public void onDrawFrame(){
		
	}
	
	
	
	
	private static class SkyBoxShader extends Shader{

		@Override
		public String getVertexShaderSrc() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFragmentShaderSrc() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
