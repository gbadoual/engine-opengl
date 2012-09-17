package com.android.opengl.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.android.opengl.shader.Shader;

public class ShaderManager {
	private static final String TAG = ShaderManager.class.getSimpleName();

	private static ShaderManager mInstance;
	
	private HashMap<Class<? extends Shader>, Shader> mShaderMap = new HashMap<Class<? extends Shader>, Shader>();
	
	private ShaderManager(){
	}
	
	public static synchronized ShaderManager getInstance(){
		if(mInstance == null){
			mInstance = new ShaderManager();
		}
		return mInstance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Shader> T getShader(Class<T> shaderClass){
		T shader = (T)mShaderMap.get(shaderClass);
		if(shader == null){
			shader = createShaderByType(shaderClass);
			if(shader != null){
				mShaderMap.put(shaderClass, shader);
			}
		}
		return shader;
	}
	//(facepalm) Should I be punished for this?
	private <T extends Shader>  T createShaderByType(Class<T> clazz) {
		T shaderInstance = null;
		try {
			shaderInstance = (T)clazz.newInstance();
		} catch (InstantiationException e) {
			Log.e(TAG, e.toString());
		} catch (IllegalAccessException e) {
			Log.e(TAG, e.toString());
		}
		return shaderInstance;
	}

	public void release(){
		Collection<Shader> shaderCollection = mShaderMap.values();
		for(Shader shader: shaderCollection){
			shader.release();
		}
		mShaderMap.clear();
		mInstance = null;
	}

}
