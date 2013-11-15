package com.mobilemouse;

import android.view.MotionEvent;
import com.mobilemouse.common.MEvent;

public class MyEvent extends MEvent {

	public MyEvent(MotionEvent m) {
		super();
		mAction = m.getAction();
		mTime = m.getEventTime();
		mNumPointers = EventUtil.getPointerCount(m);
		mX = new int[mNumPointers];
		mY = new int[mNumPointers];
		mPointerIdentifiers = new int[mNumPointers];
		for (int i = 0; i < mNumPointers; i++) {
			mX[i] = (int) EventUtil.getX(m, i);
			mY[i] = (int) EventUtil.getY(m, i);
			mPointerIdentifiers[i] = EventUtil.getPointerId(m, i);
			
		}
	}

}
