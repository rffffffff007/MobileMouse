package com.mobilemouse;

import java.lang.reflect.Method;

import android.view.MotionEvent;

public class EventUtil {
	private static final Object[] EMPTY_PARAM = new Object[] {};

	public static float getPressure(MotionEvent e, int pointIndex) {
		return getMethodBySdk(e, "getPressure", e.getPressure(), 5, pointIndex);
	}

	public static float getHistoricalPressure(MotionEvent e, int pointIndex,
			int historyIndex) {
		return getMethodBySdk(e, "getHistoricalPressure",
				e.getHistoricalPressure(historyIndex), 5, historyIndex,
				pointIndex);
	}

	public static float getHistoricalX(MotionEvent e, int pointIndex,
			int historyIndex) {
		return getMethodBySdk(e, "getHistoricalX",
				e.getHistoricalX(historyIndex), 5, historyIndex, pointIndex);
	}

	public static float getHistoricalY(MotionEvent e, int pointIndex,
			int historyIndex) {
		return getMethodBySdk(e, "getHistoricalY",
				e.getHistoricalY(historyIndex), 5, historyIndex, pointIndex);
	}

	private static final float DEFAULT_TOUCH_MAJOR = 60;

	public static float getHistoricalTouchMajor(MotionEvent event,
			int pointIndex, int historyIndex) {
		return getMethodBySdk(event, "getHistoricalTouchMajor",
				DEFAULT_TOUCH_MAJOR, 9, historyIndex, pointIndex);
	}

	public static float getTouchMajor(MotionEvent event, int pointIndex) {
		return getMethodBySdk(event, "getTouchMajor", DEFAULT_TOUCH_MAJOR, 9,
				pointIndex);
	}

	public static int getPointerCount(MotionEvent e) {
		return getMethodBySdk(e, "getPointerCount", 1, 5);
	}

	public static float getX(MotionEvent e, int i) {
		return getMethodBySdk(e, "getX", e.getX(), 5, i);
	}

	public static float getY(MotionEvent e, int i) {
		return getMethodBySdk(e, "getY", e.getY(), 5, i);
	}

	private static final int DEFAULT_POINTER_ID = -1;

	public static int getPointerId(MotionEvent e, int i) {
		return getMethodBySdk(e, "getPointerId", DEFAULT_POINTER_ID, 5, i);
	}

	public static <T> T getMethodBySdk(Object obj, String method,
			T defaultValue, int startSdk) {
		return getMethodBySdk(obj, method, defaultValue, startSdk, EMPTY_PARAM);
	}

	public static <T> T getMethodBySdk(Object obj, String method,
			T defaultValue, int startSdk, Object... params) {
		T res = defaultValue;
		if (android.os.Build.VERSION.SDK_INT >= startSdk) {
			try {
				Class<?>[] cs = new Class<?>[params.length];
				for (int i = 0; i < params.length; i++) {
					cs[i] = params[i].getClass();
					cs[i] = covertClass(cs[i]);
				}

				Method m = obj.getClass().getMethod(method, cs);
				res = (T) m.invoke(obj, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	private static Class<?> covertClass(Class<?> c) {
		if (c == null)
			return null;
		if (c == Integer.class)
			return int.class;
		if (c == Float.class)
			return float.class;
		if (c == Double.class)
			return double.class;
		if (c == Long.class)
			return long.class;
		return c;
	}
}
