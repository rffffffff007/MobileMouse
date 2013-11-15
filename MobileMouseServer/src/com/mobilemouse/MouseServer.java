package com.mobilemouse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.mobilemouse.common.Constant;
import com.mobilemouse.common.MEvent;
import com.mobilemouse.common.Message;
import com.mobilemouse.common.Message.MsgType;
import com.mobilemouse.common.PingMsg;
import com.mobilemouse.util.GestureDetector;
import com.mobilemouse.util.GestureDetector.OnGestureListener;
import com.mobilemouse.util.MouseControler;

public class MouseServer implements Runnable {
	private GestureDetector mDetector;
	private MouseControler mControler;

	public MouseServer() {
		mDetector = new GestureDetector(mListener);
		mControler = new MouseControler();
	}

	private byte[] recvBuf = new byte[1024];
	private DatagramPacket recvPacket = new DatagramPacket(recvBuf,
			recvBuf.length);

	private InetAddress mClientAddress;

	@Override
	public void run() {
		try {
			MulticastSocket server = new MulticastSocket(Constant.DEFAULT_PORT);
			InetAddress broadcastGroup = InetAddress
					.getByName(Constant.MULTICAST_ADDRESS);
			server.joinGroup(broadcastGroup);
			System.out.println("Server Start ");
			while (true) {
				try {
					server.receive(recvPacket);
					Message message = Message.createFromBytes(recvPacket
							.getData());
					if (Constant.DEBUG)
						System.out.println("RECEIVE: " + message.toString());
					InetAddress clientAddress = recvPacket.getAddress();
					switch (message.getType()) {
					case MEVENT:
						if (clientAddress.equals(clientAddress)) {
							MEvent event = (MEvent) message.getData();
							mDetector.onTouchEvent(event);
						}
						break;
					case PINGMSG:
						PingMsg msg = (PingMsg) message.getData();
						int command = msg.getCommand();
						if (command == PingMsg.COMMAND_PING) {
							String deviceId = InetAddress.getLocalHost()
									.getHostName();
							PingMsg data = new PingMsg(deviceId,
									PingMsg.COMMAND_ACK);
							Message resMsg = new Message(MsgType.PINGMSG, data);
							if (Constant.DEBUG)
								System.out.println("RESPONSE: "
										+ resMsg.toString());
							sendUDPSocket(clientAddress.getHostAddress(),
									resMsg.writeToBytes());
						} else if (command == PingMsg.COMMAND_CONNECT) {
							System.out.println("Connected to "
									+ clientAddress.getHostAddress() + ", "
									+ msg.getDeviceId());
							if (mClientAddress != null)
								mClientAddress = clientAddress;
						} else if (command == PingMsg.COMMAND_RELEASE) {
							mClientAddress = null;
						}
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUDPSocket(String address, byte[] sendBuf) {
		try {
			DatagramSocket client = new DatagramSocket();
			InetAddress addr = InetAddress.getByName(address);
			DatagramPacket sendPacket = new DatagramPacket(sendBuf, 0,
					sendBuf.length, addr, Constant.DEFAULT_PORT);
			client.send(sendPacket);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private OnGestureListener mListener = new OnGestureListener() {
		@Override
		public boolean onSingleTap(MEvent e) {
			// System.out.println("onSingleTap");
			return true;
		}

		@Override
		public boolean onScroll(MEvent e1, MEvent e2, float distanceX,
				float distanceY) {
			// System.out.println("onScroll");
			mControler.move(-(int) distanceX, -(int) distanceY);
			return true;
		}

		@Override
		public boolean onSingleTapConfirm(MEvent e) {
			// System.out.println("onSingleTapConfirm");
			mControler.leftClick();
			return true;
		}

		@Override
		public boolean onDoubleTap(MEvent e) {
			// System.out.println("onDoubleTap");
			mControler.doubleClick();
			return true;
		}

		@Override
		public boolean onTwoPointerTap(MEvent e) {
			// System.out.println("onTwoPointerTap");
			mControler.rightClick();
			return true;
		}

		private static final float TWO_POINTER_SCROLL_SCALE = 0.1f;
		private static final int MAX_WHEEL_COUNT = 4;

		public boolean onTwoPointerScroll(MEvent e1, MEvent e2,
				float distanceX, float distanceY) {
			// System.out.println("onTwoPointerScroll");
			int wheel = (int) (distanceY * TWO_POINTER_SCROLL_SCALE);
			if (wheel == 0)
				return false;
			int flag = wheel / Math.abs(wheel);
			wheel = Math.abs(wheel);
			if (wheel == 0)
				wheel = 1;
			if (wheel > MAX_WHEEL_COUNT)
				wheel = MAX_WHEEL_COUNT;

			mControler.scroll(-flag * wheel);
			return true;
		};

	};

	public static void main(String[] args) {
		MouseServer server = new MouseServer();
		server.run();
	}
}
