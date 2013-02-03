package com.android.opengl.view.control;

import android.opengl.GLES20;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.Font;
import com.android.opengl.util.Font.CharRegion;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.ShaderManager;

public class GLTextView extends GLView{

//	private static final String DEFAULT_FONT_NAME = "Airmole_Antique.ttf";
	private static final String DEFAULT_FONT_NAME = "Roboto-Regular.ttf";
	private static final int DEFAULT_FONT_SIZE = 10;
	private Font mFont;
	private String mText;
	
	private float mTextLeftCoord;
	private float mTextTopCoord;
	
	private float[] mTextPositionOffset;
	private GLTextViewShader mTextShader = ShaderManager.getInstance().getShader(GLTextViewShader.class);
	
//	VboDataHandler vboDataHandler = new VboDataHandler();
	private int mVboIndexDataHendler;
	private int mVboVertexDataHandler;
	private int indexDataLength;
	private int mVboTextureCoordHandle;
	private boolean mIsShownBackground;

	public GLTextView(Camera camera, String fontFileName, int fontSize) {
		super(camera);
		init();
		loadFont(fontFileName, fontSize);
//		onLayout(20, 5);
		
	}
	
	public GLTextView(Camera camera) {
		this(camera, DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE);
	}
	public GLTextView(Camera camera, String fontFileName) {
		this(camera, fontFileName, DEFAULT_FONT_SIZE);
	}

	private void init(){
		int[] vboBufs = new int[3];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);
		mVboIndexDataHendler = vboBufs[0];
		mVboVertexDataHandler = vboBufs[1];
		mVboTextureCoordHandle = vboBufs[2];

	}
	
	
	public void loadFont(String fontFileName, int fontSize){
		if(mFont == null){
			mFont = new Font(mCamera.getContext().getResources(), fontFileName, fontSize);
		} else {
			mFont.loadFont(fontFileName, fontSize);
		}
	}
	
	@Override
	public void onDrawFrame() {
		if(mIsVisible){
			if(mIsShownBackground){
				super.onDrawFrame();
			}
			onDrawText();
		}
	}
	
	private void onDrawText(){
	
//		if(isVisible){

			GLUtil.setGLState(GLES20.GL_DEPTH_TEST, false);
			GLUtil.setGLState(GLES20.GL_CULL_FACE, false);

			GLUtil.setGLState(GLES20.GL_BLEND, true);
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			
			GLUtil.glUseProgram(mTextShader.programHandle);
			
			GLES20.glUniform2fv(mTextShader.positionOffsetHandle, 1, mTextPositionOffset , 0);

		    GLUtil.passTextureToShader(mFont.getTextureHandle(), mTextShader.textureHandle);
		    GLUtil.passBufferToShader(mVboTextureCoordHandle, mTextShader.textureCoordHandle, GLUtil.TEXTURE_SIZE);
			
			GLUtil.passBufferToShader(mVboVertexDataHandler, mTextShader.positionHandle, GLUtil.VERTEX_SIZE_2D);
			
			GLUtil.drawElements(mVboIndexDataHendler, indexDataLength);
	        
	        GLUtil.glUseProgram(0);	

	        GLUtil.restorePrevGLState(GLES20.GL_DEPTH_TEST);
			GLUtil.restorePrevGLState(GLES20.GL_CULL_FACE);
			GLUtil.restorePrevGLState(GLES20.GL_BLEND);
//		}				
		
	}
	
	@Override
	protected void onLayout(float leftCoord, float topCoord) {
		super.onLayout(leftCoord, topCoord);
		onTextLayout(0, 0);
	}
	
	protected void onTextLayout(float leftTextCoord, float topTextCoord) {
		mTextLeftCoord = leftTextCoord;
		mTextTopCoord = topTextCoord;
		if(mTextPositionOffset == null){
			mTextPositionOffset = new float[2];
		}
		mTextPositionOffset[0] = (mLeftCoord + mTextLeftCoord) * Camera.percentToWorldRatioX - 1;
		mTextPositionOffset[1] = -(mTopCoord + mTextTopCoord) * Camera.percentToWorldRatioY + 1;
	}

	public String getText() {
		return mText;
	}

	public  void setText(String text) {
		mText = text;
		notifyTextChanged();
	}
	
	private void notifyTextChanged() {
		float textLengthInPixel = 0;
		int textLen = mText.length();
		for(int i = 0; i < textLen; ++i){
			textLengthInPixel += mFont.getCharRegion(mText.charAt(i)).width;
		}
		
		onMeasure(textLengthInPixel * Camera.screenToPercentRatio, mFont.getFontHeight() * Camera.screenToPercentRatio);
		initIndexData();
	}

	private void initIndexData() {
		char[] charArray = mText.toCharArray();
		
		int[] indexData = new int[charArray.length * 3 * 2]; // every char is represented by square wich in turn is represented by 2 triangles
		indexDataLength = indexData.length;
		float[] vertexData = new float[charArray.length * 4 * 2];
		float[] textureCoordData = new float[charArray.length * 4 * 2];
		float curCharXOffset = 0;
//		float widthInPercents
		for(int i = 0; i < charArray.length; ++i){
			CharRegion charRegion = mFont.getCharRegion(charArray[i]);
			
			int id = i * 8; 
//			vertexData[id + 0] = (0 + curCharXOffset) * Camera.percentToWorldRatioX; 
//			vertexData[id + 1] = (-charRegion.height) * Camera.percentToWorldRatioY;
//			vertexData[id + 2] = (0 + curCharXOffset) * Camera.percentToWorldRatioX; 
//			vertexData[id + 3] = 0; 
//			vertexData[id + 4] = (charRegion.width + curCharXOffset) * Camera.percentToWorldRatioX; 
//			vertexData[id + 5] = 0;
//			vertexData[id + 6] = (charRegion.width + curCharXOffset) * Camera.percentToWorldRatioX; 
//			vertexData[id + 7] = (-charRegion.height)  * Camera.percentToWorldRatioY; 
//			curCharXOffset += charRegion.width + 1;
			float m = 1;
			vertexData[id + 0] = (0 + curCharXOffset) * Camera.screenToWorldRatioX * m; 
			vertexData[id + 1] = (-charRegion.height) * Camera.screenToWorldRatioY  * m;
			vertexData[id + 2] = (0 + curCharXOffset) * Camera.screenToWorldRatioX  * m; 
			vertexData[id + 3] = 0; 
			vertexData[id + 4] = (charRegion.width + curCharXOffset) * Camera.screenToWorldRatioX  * m; 
			vertexData[id + 5] = 0;
			vertexData[id + 6] = (charRegion.width + curCharXOffset) * Camera.screenToWorldRatioX  * m; 
			vertexData[id + 7] = (-charRegion.height)  * Camera.screenToWorldRatioY  * m; 
			curCharXOffset += charRegion.width;

			
			id = i * 8;
			textureCoordData[id + 0] = charRegion.u1;
			textureCoordData[id + 1] = charRegion.v2;
			textureCoordData[id + 2] = charRegion.u1;
			textureCoordData[id + 3] = charRegion.v1;
			textureCoordData[id + 4] = charRegion.u2;
			textureCoordData[id + 5] = charRegion.v1;
			textureCoordData[id + 6] = charRegion.u2;
			textureCoordData[id + 7] = charRegion.v2;

			id = i * 6;
			int indexOffset = i * 4;
//			int[] charIndexData = mFont.getCharIndexData(charArray[i]);
			indexData[id + 0] = 0 + indexOffset;
			indexData[id + 1] = 2 + indexOffset;
			indexData[id + 2] = 1 + indexOffset;
			indexData[id + 3] = 0 + indexOffset;
			indexData[id + 4] = 3 + indexOffset;
			indexData[id + 5] = 2 + indexOffset;

		}
			
		GLUtil.attachArrayToHandler(textureCoordData, mVboTextureCoordHandle);
		GLUtil.attachArrayToHandler(vertexData, mVboVertexDataHandler);
		GLUtil.attachIndexesToHandler(indexData, mVboIndexDataHendler);
	}
	
	
	@Override
	public void release() {
		mFont.release();
		super.release();
	}
	
	

	public static class GLTextViewShader extends GLViewShader{

		@Override
		public String getVertexShaderSrc() {
			return 
			"uniform 	vec2 " + UNIFORM_POSITION_OFFSET + ";											" +

			"attribute 	vec2 " + ATTRIBUTE_POSITION + ";												" +
			"attribute 	vec2 " + ATTRIBUTE_TEXTURE_COORD + ";											" +
			"attribute 	float " + UNIFORM_INSTANCE_ID + ";											" +
			
			"																							" +
			"varying vec2 v_TexCoord; 																" +
			"																							" +
			"void main(){																				" +
			"	v_TexCoord = " + ATTRIBUTE_TEXTURE_COORD + ";												" +
			"	gl_Position = vec4(" + ATTRIBUTE_POSITION + " + " + UNIFORM_POSITION_OFFSET + ", 0.0, 1.0);										" +
			"}																							";
		}


		@Override
		public String getFragmentShaderSrc() {
			return 			
				"precision mediump float;																" +
				"uniform	sampler2D "+UNIFORM_TEXTURE+ ";												" +

				"varying vec2 v_TexCoord; 															" +

				"void main(){																			" +
				"		vec4 resColor = texture2D("+UNIFORM_TEXTURE+", v_TexCoord);						" +
				"		gl_FragColor = resColor;	" +
				"}																						";	
			}
	}



	public void showBackground(boolean isShownBackground) {
		mIsShownBackground = isShownBackground;
		
	}


}
