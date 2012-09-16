package com.android.opengl.util;

public class Log {
	public static boolean isLogginEnabled = true;
	
	public static void i(String tag, String message){
		if(isLogginEnabled){
			android.util.Log.i(tag, message);
		}
	}
	public static void d(String tag, String message){
		if(isLogginEnabled){
			android.util.Log.d(tag, message);
		}
	}
	public static void e(String tag, String message){
		if(isLogginEnabled){
			android.util.Log.e(tag, message);
		}
	}
	public static void v(String tag, String message){
		if(isLogginEnabled){
			android.util.Log.v(tag, message);
		}
	}
	public static void w(String tag, String message){
		if(isLogginEnabled){
			android.util.Log.w(tag, message);
		}
	}
	
	public static void printStackTrace(String tag){
		if(!isLogginEnabled){
			return;
		}
		StackTraceElement[] elements = Thread.getAllStackTraces().get(Thread.currentThread());
		for(StackTraceElement element: elements){
			Log.i(tag, element.toString());
		}
	}

}
