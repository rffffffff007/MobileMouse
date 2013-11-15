package com.mobilemouse;

import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class FadingPainter extends View {
	/** How often to fade the contents of the window (in ms). */
	private static final int FADE_DELAY = 100;
	private static final int FADE_ALPHA = 0x06;
	private static final int MAX_FADE_STEPS = 256 / FADE_ALPHA + 4;
	private static final float TOUCH_WIDTH_RADIO = 0.6f;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private final Rect mRect = new Rect();
	private final Paint mPaint;
	private final Paint mFadePaint;
	private float mCurX;
	private float mCurY;
	private int mFadeSteps = MAX_FADE_STEPS;

	private String mInitText = "Click & Move Here";

	public FadingPainter(Context c) {
		super(c);
		mInitText = c.getString(R.string.fadding_init_text);
		setFocusable(true);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setARGB(255, 255, 255, 255);
		mFadePaint = new Paint();
		mFadePaint.setDither(true);
		mFadePaint.setARGB(FADE_ALPHA, 0, 0, 0);
		startFading();
	}

	private static final int WAIT_TIME = 3 * 1000;
	private static final int WAIT_STEP = 100;

	/**
	 * Start up the pulse to fade the screen, clearing any existing pulse to
	 * ensure that we don't have multiple pulses running at a time.
	 */
	public void startFading() {
		new Thread() {
			public void run() {
				int sleepTime = 0;
				while (sleepTime < WAIT_TIME) {
					Handler h = getHandler();
					if (h != null) {
						h.removeCallbacks(FaddingRunnbale);
						h.post(FaddingRunnbale);
						break;
					}
					try {
						Thread.sleep(WAIT_STEP);
						sleepTime += WAIT_STEP;
					} catch (Exception e) {
					}
				}
			};
		}.start();
	}

	/**
	 * Stop the pulse to fade the screen.
	 */
	public void stopFading() {
		Handler h = getHandler();
		if (h == null)
			return;
		h.removeCallbacks(FaddingRunnbale);
	}

	public void clear() {
		if (mCanvas != null) {
			mPaint.setARGB(0xff, 0, 0, 0);
			mCanvas.drawPaint(mPaint);
			invalidate();
			mFadeSteps = MAX_FADE_STEPS;
		}
	}

	private void fade() {
		if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
			mCanvas.drawPaint(mFadePaint);
			invalidate();
			mFadeSteps++;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int curW = mBitmap != null ? mBitmap.getWidth() : 0;
		int curH = mBitmap != null ? mBitmap.getHeight() : 0;
		if (curW >= w && curH >= h) {
			return;
		}

		if (curW < w)
			curW = w;
		if (curH < h)
			curH = h;

		Bitmap newBitmap =
				Bitmap.createBitmap(curW, curH, Bitmap.Config.RGB_565);
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		if (mBitmap != null) {
			newCanvas.drawBitmap(mBitmap, 0, 0, null);
		}
		mBitmap = newBitmap;
		mCanvas = newCanvas;

		drawInitText(curW, curH);
		mFadeSteps = MAX_FADE_STEPS;
	}

	private static final int INIT_TEXT_SIZE = 30;
	private static final int INIT_TEXT_SPACING = 40;

	private void drawInitText(int curW, int curH) {
		mPaint.setTextSize(INIT_TEXT_SIZE);
		mPaint.getTextBounds(mInitText, 0, mInitText.length(), mRect);
		String[] items = mInitText.split("\n");
		int totalH =
				mRect.bottom - mRect.top + items.length * INIT_TEXT_SPACING;
		int sumH = 0;
		for (int i = 0; i < items.length; i++) {
			float textWidth = mPaint.measureText(items[i]);
			mPaint.getTextBounds(items[i], 0, items[i].length(), mRect);
			float textX = (curW - textWidth) / 2;
			float textY = (curH - totalH + sumH) / 2;
			sumH += mRect.bottom - mRect.top + INIT_TEXT_SPACING;
			mCanvas.drawText(items[i], textX, textY, mPaint);
		}

		// mRect.set(0, 0, curW, curH);
		// invalidate(mRect);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// XXX Whether to implement track ball event.
		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (action != MotionEvent.ACTION_UP
				&& action != MotionEvent.ACTION_CANCEL) {
			int N = event.getHistorySize();
			int P = EventUtil.getPointerCount(event);
			float width;
			float pressure;
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < P; j++) {
					mCurX = EventUtil.getHistoricalX(event, j, i);
					mCurY = EventUtil.getHistoricalY(event, j, i);
					width =
							EventUtil.getHistoricalTouchMajor(event, j, i)
									* TOUCH_WIDTH_RADIO;
					pressure = EventUtil.getHistoricalPressure(event, j, i);
					drawPoint(mCurX, mCurY, pressure, width);
				}
			}
			for (int j = 0; j < P; j++) {
				mCurX = EventUtil.getX(event, j);
				mCurY = EventUtil.getY(event, j);
				width = EventUtil.getTouchMajor(event, j) * TOUCH_WIDTH_RADIO;
				pressure = EventUtil.getPressure(event, j);
				drawPoint(mCurX, mCurY, pressure, width);
			}
		}
		return true;
	}

	private void drawPoint(float x, float y, float pressure, float width) {
		if (width < 1)
			width = 1;
		if (pressure > 1)
			pressure = 1;
		if (mBitmap != null) {
			float radius = width / 2;
			int pressureLevel = (int) (pressure * 255);
			mPaint.setARGB(pressureLevel, 255, 255, 255);
			mCanvas.drawCircle(x, y, radius, mPaint);
			mRect.set((int) (x - radius - 2), (int) (y - radius - 2), (int) (x
					+ radius + 2), (int) (y + radius + 2));
			invalidate(mRect);
		}
		mFadeSteps = 0;
	}

	Runnable FaddingRunnbale = new Runnable() {
		@Override
		public void run() {
			FadingPainter mView = FadingPainter.this;
			mView.fade();
			mView.getHandler().postDelayed(this, FADE_DELAY);
		}
	};
}