package com.mobilemouse;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobclick.android.MobclickAgent;
import com.mobilemouse.common.Constant;
import com.mobilemouse.common.Message;
import com.mobilemouse.common.Message.MsgType;
import com.mobilemouse.common.PingMsg;

public class ConnectorActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "Connector";

	public static final int WHAT_REFRESH_LIST = 1;
	public static final int WHAT_HIDE_PREOGRESS_BAR = 2;
	public static final int WHAT_SHOW_PREGRESS_BAR = 3;
	public static final int WHAT_REFRESH_TEXT_ERROR = 4;

	private List<ServerInfo> mServerInfos;
	private BaseAdapter mAdapter;
	private SocketServer mSocketServer;
	private PingTimer mPingTimer;

	private SocketControler mControler;

	private ListView mListView;
	private TextView tvError;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case WHAT_REFRESH_LIST:
				mAdapter.notifyDataSetChanged();
				break;
			case WHAT_HIDE_PREOGRESS_BAR:
				setProgressBarIndeterminateVisibility(false);
				break;
			case WHAT_SHOW_PREGRESS_BAR:
				setProgressBarIndeterminateVisibility(true);
				break;
			case WHAT_REFRESH_TEXT_ERROR:
				if (mServerInfos.size() == 0) {
					mListView.setVisibility(View.GONE);
					tvError.setVisibility(View.VISIBLE);
				} else {
					mListView.setVisibility(View.VISIBLE);
					tvError.setVisibility(View.GONE);
				}
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_connector);
		mServerInfos = new ArrayList<ServerInfo>();
		mControler = new SocketControler();
		initElements();
		initContent();
	}

	@Override
	protected void onResume() {
		mSocketServer = new SocketServer();
		mSocketServer.start();
		mPingTimer = new PingTimer();
		mPingTimer.start();
		mAdapter.notifyDataSetChanged();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		mSocketServer.finish();
		mPingTimer.finish();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ServerInfo mInfo = mServerInfos.get(position);
		Intent intent = new Intent(this, MobileMouseActivity.class);
		intent.putExtra(MobileMouseActivity.EXTAR_ADDRESS, mInfo.mIpAddress);
		Message msg = getPingMessage(PingMsg.COMMAND_CONNECT);
		mControler.sendUDPSocket(mInfo.mIpAddress, msg.writeToBytes());
		startActivity(intent);
	}

	private void initElements() {
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setOnItemClickListener(this);
		tvError = (TextView) findViewById(R.id.tv_error);
	}

	private void initContent() {
		mAdapter =
				new ArrayAdapter<ServerInfo>(this,
						android.R.layout.simple_list_item_1, mServerInfos);
		mListView.setAdapter(mAdapter);
	}

	private Message getPingMessage(int command) {
		PingMsg msg = getPingMsg(command);
		return new Message(MsgType.PINGMSG, msg);
	}

	private PingMsg getPingMsg(int command) {
		String deviceId = android.os.Build.MODEL;
		return new PingMsg(deviceId, command);
	}

	abstract class FinishThread extends Thread {
		protected boolean mShouldStop = false;

		public void finish() {
			mShouldStop = true;
		}

		public boolean isStoped() {
			return mShouldStop;
		}
	}

	class SocketServer extends FinishThread {
		private byte[] recvBuf = new byte[1024];
		private DatagramPacket recvPacket = new DatagramPacket(recvBuf,
				recvBuf.length);

		public SocketServer() {
		}

		@Override
		public void run() {
			Log.d(TAG, "SocketServer start");
			try {
				DatagramSocket server =
						new DatagramSocket(Constant.DEFAULT_PORT);
				server.setSoTimeout(10000);
				while (!isStoped()) {
					try {
						mHandler.sendEmptyMessage(WHAT_HIDE_PREOGRESS_BAR);
						server.receive(recvPacket);
						Message message =
								Message.createFromBytes(recvPacket.getData());
						System.out.println(message.toString());
						switch (message.getType()) {
						case PINGMSG:
							PingMsg msg = (PingMsg) message.getData();
							int command = msg.getCommand();
							if (command == PingMsg.COMMAND_ACK) {
								String ip =
										recvPacket.getAddress()
												.getHostAddress();
								int port = recvPacket.getPort();
								ServerInfo info =
										new ServerInfo(ip, port,
												msg.getDeviceId());
								if (!mServerInfos.contains(info))
									mServerInfos.add(info);
								mHandler.sendEmptyMessage(WHAT_REFRESH_LIST);
							}
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					mHandler.sendEmptyMessage(WHAT_REFRESH_TEXT_ERROR);
				}
				server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d(TAG, "SocketServer stop");
		}
	}

	class PingTimer extends FinishThread {
		public static final long SLEEP_TIME = 10 * 1000;

		@Override
		public void run() {
			Log.d(TAG, "PingTimer start");
			while (!isStoped()) {
				mServerInfos.clear();
				mHandler.sendEmptyMessage(WHAT_SHOW_PREGRESS_BAR);
				Message msg = getPingMessage(PingMsg.COMMAND_PING);
				mControler.sendUDPBroadcast(msg.writeToBytes());
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
			Log.d(TAG, "PingTimer stop");
		}
	}

	class ServerInfo {
		private String mIpAddress;
		private int mPort;
		private String mDeviceId;

		public ServerInfo(String ipAddress, int port, String deviceId) {
			if (ipAddress == null || ipAddress.length() == 0)
				throw new RuntimeException("ip address cannot be " + ipAddress);
			mIpAddress = ipAddress;
			mPort = port;
			mDeviceId = deviceId;
		}

		public String getIpAddress() {
			return mIpAddress;
		}

		public int getPort() {
			return mPort;
		}

		public String getDeviceId() {
			return mDeviceId;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ServerInfo) {
				ServerInfo s = ((ServerInfo) o);
				return s.getIpAddress().equals(getIpAddress());
			}
			return false;
		}

		@Override
		public String toString() {
			return mDeviceId;
		}
	}

}
