package com.android.opengl.gameobject.util;

public class ParseNumberUtil {
	
	public static float parseFloat(char[] f , int beg, int len) {
//		final int len   = f.length;
		float     ret   = 0f;         // return value
		int       pos   = beg;          // read pointer position
		len += pos;
		int       part  = 0;          // the current part (int, float and sci parts of the number)
		byte   neg   = 1;      // true if part is a negative number
	 
		// find start
		while (pos < len && (f[pos] < '0' || f[pos] > '9') && f[pos] != '-' && f[pos] != '.')
			pos++;
	 
		// sign
		if (f[pos] == '-') { 
			neg = -1; 
			pos++; 
		}
	 
		// integer part
		while (pos < len && !(f[pos] > '9' || f[pos] < '0'))
			part = part*10 + (f[pos++] - '0');
		ret = neg * part;
	 
		// float part
		if (pos < len && f[pos] == '.') {
			pos++;
			int mul = 1;
			part = 0;
			while (pos < len && !(f[pos] > '9' || f[pos] < '0')) {
				part = part*10 + (f[pos] - '0'); 
				mul*=10; pos++;
			}
			ret = ret + neg * (float)part / (float)mul;
		}
	 
		// scientific part
		if (pos < len && (f[pos] == 'e' || f[pos] == 'E')) {
			pos++;
			neg = (f[pos] == '-')?(byte)-1:1; pos++;
			part = 0;
			while (pos < len && !(f[pos] > '9' || f[pos] < '0')) {
				part = part*10 + (f[pos++] - '0'); 
			}
			if (neg < 0)
				ret = ret / (float)Math.pow(10, part);
			else
				ret = ret * (float)Math.pow(10, part);
		}	
		return ret;
	}

	public static int parseInt(char[] f , int beg, int len) {
//		final int len   = f.length;
		int     ret   = 0;         // return value
		int       pos   = beg;          // read pointer position
		len += pos;
		int       part  = 0;          // the current part (int, float and sci parts of the number)
		byte   neg   = 1;      // true if part is a negative number
	 
		// find start
		while (pos < len && (f[pos] < '0' || f[pos] > '9') && f[pos] != '-' && f[pos] != '.')
			pos++;
	 
		// sign
		if (f[pos] == '-') { 
			neg = -1; 
			pos++; 
		}
	 
		// integer part
		while (pos < len && !(f[pos] > '9' || f[pos] < '0'))
			part = part*10 + (f[pos++] - '0');
		ret = neg * part;
	 
		// float part
//		if (pos < len && f[pos] == '.') {
//			pos++;
//			int mul = 1;
//			part = 0;
//			while (pos < len && !(f[pos] > '9' || f[pos] < '0')) {
//				part = part*10 + (f[pos] - '0'); 
//				mul*=10; pos++;
//			}
//			ret = neg ? ret - (float)part / (float)mul : ret + (float)part / (float)mul;
//		}
//	 
//		// scientific part
//		if (pos < len && (f[pos] == 'e' || f[pos] == 'E')) {
//			pos++;
//			neg = (f[pos] == '-'); pos++;
//			part = 0;
//			while (pos < len && !(f[pos] > '9' || f[pos] < '0')) {
//				part = part*10 + (f[pos++] - '0'); 
//			}
//			if (neg)
//				ret = ret / (float)Math.pow(10, part);
//			else
//				ret = ret * (float)Math.pow(10, part);
//		}	
		return ret;
	}
}
