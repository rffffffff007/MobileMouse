package com.mobilemouse.util;

import com.mobilemouse.common.MEvent;

public class GestureDetector {
	public interface OnGestureListener {
		boolean onSingleTapConfirm(MEvent e);

		boolean onSingleTap(MEvent e);

		boolean onDoubleTap(MEvent e);

		boolean onTwoPointerTap(MEvent e);

		boolean onScroll(MEvent e1, MEvent e2, float distanceX, float distanceY);

		boolean onTwoPointerScroll(MEvent e1, MEvent e2, float distanceX,
				float distanceY);

	}

	private static final int DOUBLE_TAP_TIMEOUT = 200;

	private final OnGestureListener mListener;
	private boolean mStillDown;

	private boolean mAlwaysInTwoTapRegion;
	private boolean mAlwaysInTapRegion;
	private boolean mAlwaysInBiggerTapRegion;

	private MEvent mCurrentDownEvent;
	private MEvent mPreviousUpEvent;

	/**
	 * True when the user is still touching for the second tap (down, move, and
	 * up events). Can only be true if there is a double tap listener attached.
	 */
	private boolean mIsDoubleTapping;

	private float mLastMotionY;
	private float mLastMotionX;

	private TimeTaskDispatcher mDispatcher;

	private static final int TASK_TAP_ID = 3;
	private TimeTask mTapTask = new TimeTask(TASK_TAP_ID) {
		public void run() {
			if (mListener != null && !mStillDown) {
				mListener.onSingleTapConfirm(mCurrentDownEvent);
			}
		};
	};

	public GestureDetector(OnGestureListener listener) {
		mListener = listener;
		init();
	}

	private static final int TOUCH_SLOP = 16;
	private static final int DOUBLE_TAP_SLOP = 80;
	private static final int TWO_TAP_SLOP = 50;

	private int mBiggerTouchSlopSquare = 20 * 20;

	private int TOUCH_SLOP_SQUARE = TOUCH_SLOP * TOUCH_SLOP;
	private int TOUCH_TWO_SLOP_SQUARE = TWO_TAP_SLOP * TWO_TAP_SLOP;
	private int DOUBLE_TAP_SLOP_SQUARE = DOUBLE_TAP_SLOP * DOUBLE_TAP_SLOP;

	private void init() {
		if (mListener == null) {
			throw new NullPointerException("OnGestureListener must not be null");
		}

		mDispatcher = new TimeTaskDispatcher();
		mDispatcher.start();
	}

	private static final int INVALID_POINTER_ID = MEvent.INVALID_POINTER_ID;
	private int mLastPointerId = INVALID_POINTER_ID;

	/**
	 * Analyzes the given motion event and if applicable triggers the
	 * appropriate callbacks on the {@link OnGestureListener} supplied.
	 * 
	 * @param ev
	 *            The current motion event.
	 * @return true if the {@link OnGestureListener} consumed the event, else
	 *         false.
	 */
	public boolean onTouchEvent(MEvent ev) {
		final int action = ev.getAction();
		int pointerIndex = ev.findPointerIndex(mLastPointerId);
		if(pointerIndex == INVALID_POINTER_ID){
			mLastPointerId = ev.getPointerId(0);
			pointerIndex = 0;
		}
		final float y = ev.getY(pointerIndex);
		final float x = ev.getX(pointerIndex);

		boolean handled = false;

		switch (action & MEvent.ACTION_MASK) {
		case MEvent.ACTION_POINTER_DOWN:
			// Multitouch event - abort.
			// cancel();
			mAlwaysInTwoTapRegion = true;
			break;

		case MEvent.ACTION_POINTER_UP:
			if (ev.getPointerCount() == 2) {
				int index = (((action & MEvent.ACTION_POINTER_INDEX_MASK) >> MEvent.ACTION_POINTER_INDEX_SHIFT) == 0) ? 1
						: 0;
				mLastMotionX = ev.getX(index);
				mLastMotionY = ev.getY(index);
				if (mAlwaysInTwoTapRegion) {
					if (mListener != null)
						mListener.onTwoPointerTap(ev);
				}
			}
			break;
		case MEvent.ACTION_DOWN:
			mDispatcher.remove(mTapTask);
			if (mListener != null) {
				if ((mCurrentDownEvent != null)
						&& (mPreviousUpEvent != null)
						&& isConsideredDoubleTap(mCurrentDownEvent,
								mPreviousUpEvent, ev)) {
					// This is a second tap
					mIsDoubleTapping = true;
					// Give a callback with the first tap of the double-tap
					handled |= mListener.onDoubleTap(mCurrentDownEvent);
				} else {
					// This is a first tap
					mTapTask.setDelay(DOUBLE_TAP_TIMEOUT);
					mDispatcher.push(mTapTask);
				}
			}

			mLastMotionX = x;
			mLastMotionY = y;
			mCurrentDownEvent = ev;
			mAlwaysInTapRegion = true;
			mAlwaysInTwoTapRegion = true;
			mAlwaysInBiggerTapRegion = true;
			mStillDown = true;
			break;

		case MEvent.ACTION_MOVE:
			if (ev.getPointerCount() > 1) {
				final float scrollX = mLastMotionX - x;
				final float scrollY = mLastMotionY - y;
				if (mAlwaysInTwoTapRegion) {
					final int deltaX = (int) (x - mCurrentDownEvent.getX());
					final int deltaY = (int) (y - mCurrentDownEvent.getY());
					int distance = (deltaX * deltaX) + (deltaY * deltaY);
					if (distance > TOUCH_TWO_SLOP_SQUARE) {
						handled = mListener.onTwoPointerScroll(
								mCurrentDownEvent, ev, scrollX, scrollY);
						mLastMotionX = x;
						mLastMotionY = y;
						mAlwaysInTwoTapRegion = false;
					}
				} else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
					handled = mListener.onTwoPointerScroll(mCurrentDownEvent,
							ev, scrollX, scrollY);
					mLastMotionX = x;
					mLastMotionY = y;
				}
				break;
			} else {
				final float scrollX = mLastMotionX - x;
				final float scrollY = mLastMotionY - y;
				if (mAlwaysInTapRegion) {
					final int deltaX = (int) (x - mCurrentDownEvent.getX());
					final int deltaY = (int) (y - mCurrentDownEvent.getY());
					int distance = (deltaX * deltaX) + (deltaY * deltaY);
					if (distance > TOUCH_SLOP_SQUARE) {
						handled = mListener.onScroll(mCurrentDownEvent, ev,
								scrollX, scrollY);
						mLastMotionX = x;
						mLastMotionY = y;
						mAlwaysInTapRegion = false;
						mDispatcher.remove(mTapTask);
					}
					if (distance > mBiggerTouchSlopSquare) {
						mAlwaysInBiggerTapRegion = false;
					}
				} else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
					handled = mListener.onScroll(mCurrentDownEvent, ev,
							scrollX, scrollY);
					mLastMotionX = x;
					mLastMotionY = y;
				}
				break;
			}
		case MEvent.ACTION_UP:
			mStillDown = false;
			MEvent currentUpEvent = ev;
			if (mIsDoubleTapping) {
			} else if (mAlwaysInTapRegion) {
				if (mListener != null)
					mListener.onSingleTap(ev);
			}
			// Hold the event we obtained above - listeners may have changed the
			// original.
			mPreviousUpEvent = currentUpEvent;
			mIsDoubleTapping = false;
			break;
		case MEvent.ACTION_CANCEL:
			cancel();
		}
		return handled;
	}

	private void cancel() {
		mIsDoubleTapping = false;
		mStillDown = false;
		mDispatcher.remove(mTapTask);
	}

	private boolean isConsideredDoubleTap(MEvent firstDown, MEvent firstUp,
			MEvent secondDown) {
		if (!mAlwaysInBiggerTapRegion) {
			return false;
		}

		if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
			return false;
		}

		int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
		int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
		return (deltaX * deltaX + deltaY * deltaY < DOUBLE_TAP_SLOP_SQUARE);
	}
}