package com.mobilemouse;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

import com.mobclick.android.MobclickAgent;
import com.mobilemouse.common.MEvent;
import com.mobilemouse.common.Message;
import com.mobilemouse.common.Message.MsgType;

public class MobileMouseActivity extends Activity {
	public static final String TAG = "MobileMouse";
	public static final String EXTAR_ADDRESS = "extra_address";
	private SocketControler mSocketControler;
	private FadingPainter mFadingPainter;
	private String mAddress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mAddress = getIntent().getStringExtra(EXTAR_ADDRESS);
		mFadingPainter = new FadingPainter(this);
		setContentView(mFadingPainter);
		mSocketControler = new SocketControler();
	}

	@Override
	protected void onResume() {
		mFadingPainter.startFading();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		mFadingPainter.stopFading();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		MEvent m = new MyEvent(ev);
		Log.d(TAG, m.toString());
		Message msg = new Message(MsgType.MEVENT, new MyEvent(ev));
		mSocketControler.sendUDPSocket(mAddress, msg.writeToBytes());
		return super.dispatchTouchEvent(ev);
	}

}