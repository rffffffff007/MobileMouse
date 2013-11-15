package com.mobilemouse.common;

import com.mobilemouse.common.Message.Parser;

public class MEvent implements ParseAble {
	public static final int ACTION_MASK = 0xff;
	public static final int ACTION_DOWN = 0;
	public static final int ACTION_UP = 1;
	public static final int ACTION_MOVE = 2;
	public static final int ACTION_CANCEL = 3;
	public static final int ACTION_OUTSIDE = 4;
	public static final int ACTION_POINTER_DOWN = 5;
	public static final int ACTION_POINTER_UP = 6;
	public static final int ACTION_POINTER_INDEX_MASK = 0xff00;
	public static final int ACTION_POINTER_INDEX_SHIFT = 8;

	public static final int INVALID_POINTER_ID = -1;
	protected int mAction;
	protected long mTime;
	protected int mNumPointers;
	protected int[] mPointerIdentifiers;
	protected int[] mX;
	protected int[] mY;

	public MEvent() {

	}

	public int getAction() {
		return mAction;
	}

	public long getEventTime() {
		return mTime;
	}

	public int getPointerId(int pointerIndex) {
		return mPointerIdentifiers[pointerIndex];
	}

	public int findPointerIndex(int pointerId) {
		int i = mNumPointers;
		while (i > 0) {
			i--;
			if (mPointerIdentifiers[i] == pointerId) {
				return i;
			}
		}
		return INVALID_POINTER_ID;
	}

	public final int getPointerCount() {
		return mNumPointers;
	}

	public int getX() {
		return mX[0];
	}

	public int getY() {
		return mY[0];
	}

	public int getX(int pointerIndex) {
		return mX[pointerIndex];
	}

	public int getY(int pointerIndex) {
		return mY[pointerIndex];
	}

	@Override
	public String toString() {
		Parser parser = Parser.obtain();
		writeToParser(parser);
		return parser.toString();
	}

	@Override
	public void writeToParser(Parser parser) {
		parser.writeInt(mAction);
		parser.writeLong(mTime);
		parser.writeInt(mNumPointers);
		for (int i = 0; i < mNumPointers; i++) {
			parser.writeInt(mPointerIdentifiers[i]);
			parser.writeInt(mX[i]);
			parser.writeInt(mY[i]);
		}
	}

	public static MEvent createFromParser(Parser parser) {
		try {
			MEvent event = new MEvent();
			event.mAction = parser.readInt();
			event.mTime = parser.readLong();
			event.mNumPointers = parser.readInt();
			event.mX = new int[event.mNumPointers];
			event.mY = new int[event.mNumPointers];
			event.mPointerIdentifiers = new int[event.mNumPointers];
			for (int i = 0; i < event.mNumPointers; i++) {
				event.mPointerIdentifiers[i] = parser.readInt();
				event.mX[i] = parser.readInt();
				event.mY[i] = parser.readInt();
			}
			return event;
		} catch (Exception e) {
			return null;
		}
	}

}
