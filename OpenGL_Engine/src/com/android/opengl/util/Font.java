package com.android.opengl.util;

import java.io.FileOutputStream;

import com.android.opengl.Camera;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.util.FloatMath;

public class Font {
	public static final int DEFAULT_FONT_SIZE = 16;

	private final static byte CHAR_START = 32;           // First Character (ASCII Code)
	private final static byte CHAR_END = 126;            // Last Character (ASCII Code)
	private final static byte CHAR_COUNT = CHAR_END - CHAR_START + 1;
	private final static byte CHAR_UNKNOWN = CHAR_END;

	private static  float PADDING_X;
	private static  float PADDING_Y = 2;

	public static String ASSET_FONT_FOLDER_PATH = "fonts/";
   
	private Resources mResources;
	private String mFontName;
	private FontMetrics mFontMetrics;
	
	private CharRegion[] charRegions = new CharRegion[CHAR_COUNT];

	private float mFontHeight;

	private float[] mCharWidths;

	private int mTextureHandle;

	private static float EXTRA_PADDING;



	public Font(Resources resources, String fontName) {
		mResources = resources;
		loadFont(fontName, DEFAULT_FONT_SIZE);
	}
	
	public Font(Resources resources, String ttfFontFileName, int fontSize) {
		mResources = resources;
		loadFont(ttfFontFileName, fontSize);
	}
	
	
	public void setSize(int fontSize){
		loadFont(mFontName, fontSize);
	}
	
	public void loadFont(String fontFileName, int fontSize){
		if(!fontFileName.endsWith(".ttf")){
			fontFileName += ".ttf"; 
		}
		mFontName = fontFileName;
		if(mTextureHandle != 0){
			release();
		}
		Paint paint = getConfiguredPaint(fontFileName, fontSize);
		Bitmap bitmap = renderFontToBitmap(paint);
		mTextureHandle = LoaderManager.getInstance(mResources).loadTexture(bitmap);

		try {
		       FileOutputStream out = new FileOutputStream("mnt/sdcard/font1.jpg");
		       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		       out.close();
		       bitmap.recycle();
		} catch (Exception e) {
			Log.e("tag", e.toString());
		}

	}
	
	

	private Bitmap renderFontToBitmap(Paint paint) {


		char[] allChars = new char[CHAR_COUNT];
		mFontHeight = mFontMetrics.bottom - mFontMetrics.top;
		mCharWidths = new float[CHAR_COUNT + 1];
	    for(char c = CHAR_START; c <= CHAR_END; ++c){
	    	allChars[c - CHAR_START] = c;
	    }
    	paint.getTextWidths(allChars, 0, CHAR_COUNT, mCharWidths);
    	float tmpTextureWidth = 0;
    	int widthPow2 = 1;
    	int heightPow2 = 1;
    	float doubleHeight = (PADDING_Y + mFontHeight) * 8;
    	while(doubleHeight > heightPow2){heightPow2 <<=1;}
    	for(int i = 0; i < mCharWidths.length; ++i){
    		tmpTextureWidth += mCharWidths[i] + PADDING_X;
    		while(tmpTextureWidth > widthPow2){widthPow2 <<= 1;}
    	}
    	widthPow2 >>= 3;
		
		int textureWidth = widthPow2;
		int textureHeight = heightPow2;
	    Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight , Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    bitmap.eraseColor(0x00000000);
//	    bitmap.eraseColor(0xBBBBBBFF);
	    float x = 0;//PADDING_X;
	    float y = mFontHeight - mFontMetrics.bottom + PADDING_Y;
	    char[] inChar = new char[1];
//	    paint.setTextAlign(Paint.Align.RIGHT);
	    for(int i = 0; i< CHAR_COUNT; ++i){
	    	inChar[0] = allChars[i];
	    	canvas.drawText(inChar, 0, 1, x, y, paint);
//	    	canvas.drawCircle(x, y, 2, paint);
//	    	canvas.drawRect(x, y -mCharHeight - PADDING_Y, x + mCharWidths[i],  y, paint);
	    	charRegions[i] = new CharRegion(inChar[0], textureWidth, textureHeight, x, y + mFontMetrics.top, mCharWidths[i], mFontHeight); 
	    	x += mCharWidths[i] + PADDING_X;
	    	if(x + mCharWidths[i + 1] + PADDING_X>= textureWidth){
	    		x = PADDING_X;
	    		y += mFontHeight + PADDING_Y;
	    	}
	    }
		return bitmap;
	}

	private Paint getConfiguredPaint(String fontFileName, int fontSize) {
		Typeface typeface = Typeface.createFromAsset(mResources.getAssets(), ASSET_FONT_FOLDER_PATH + fontFileName);
		Paint paint = new Paint();
//		paint.setDither(true);
//		paint.setFilterBitmap(true);
		paint.setAntiAlias(true);
		float resIndepFontSize = FloatMath.ceil(fontSize * Camera.percentToScreenRatio / 4);
		PADDING_X = resIndepFontSize * 0.15f;
		EXTRA_PADDING = resIndepFontSize * 0.05f;
		paint.setTextSize(resIndepFontSize);
		paint.setColor(0xffffffff);
		paint.setTypeface(typeface);
		mFontMetrics = paint.getFontMetrics();
		return paint;
	}
	
	public void release(){
		int[] textureId = new int[]{mTextureHandle};
		GLES20.glDeleteTextures(1, textureId, 0);
	}
	
	public float getFontHeight(){
		return mFontHeight;
	}



	public CharRegion getCharRegion(char c) {
		if(c < CHAR_START || c > CHAR_END){
			c = CHAR_UNKNOWN;
		}
		return charRegions[c - CHAR_START];
	}
	





	public int getTextureHandle() {
		return mTextureHandle;
	}



	public static class FontVboDataHandle{
		public int textureHandle;
		public int textureCoordHandle;
	}


	public static class CharRegion{
		public char owner;
		public float u1;
		public float v1;
		public float u2;
		public float v2;
		
		public float width;
		public float height;
		public float widthInPercents;
		public float heightInPercents;
		
		public CharRegion(char owner, float textureWidth, float textureHeight, float x, float y, float charWidth, float charHeight) {
			this.owner = owner;
//			float extraSpacing = 2;
			x-= EXTRA_PADDING;
			charWidth += EXTRA_PADDING * 2;
			u1 = x / textureWidth;
			v1 = y / textureHeight;
			u2 = (x + charWidth) / textureWidth;
			v2 = (y + charHeight) / textureHeight;
			width = charWidth;
			height = charHeight;
			widthInPercents = charWidth * Camera.screenToPercentRatio;
			heightInPercents = charHeight * Camera.screenToPercentRatio;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			return builder.append("{ u1 = ").append(u1).append(", v1 = ").append(v1).append(", u2 = ").
					append(u2).append(", v2 = ").append(v2).append("}").toString();
		}
	
	}
	
}
