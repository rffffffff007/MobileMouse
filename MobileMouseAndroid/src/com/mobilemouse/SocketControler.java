package com.mobilemouse;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.mobilemouse.common.Constant;

import android.util.Log;

public class SocketControler {
	private static final String TAG = "SocketControler";

	public void sendUDPBroadcast(String str) {
		sendUDPBroadcast(str.getBytes());
	}

	public void sendUDPBroadcast(byte[] sendBuf) {
		sendUDPBroadcast(sendBuf, 0, sendBuf.length);
	}

	public void sendUDPBroadcast(byte[] sendBuf, int offset, int length) {
		try {
			DatagramSocket client = new DatagramSocket();
			InetAddress addr =
					InetAddress.getByName(Constant.MULTICAST_ADDRESS);
			DatagramPacket sendPacket =
					new DatagramPacket(sendBuf, offset, length, addr,
							Constant.DEFAULT_PORT);
			client.setBroadcast(true);
			client.send(sendPacket);
			client.close();
			Log.d(TAG, "Send length: " + length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendUDPSocket(String ipAddres, byte[] sendBuf) {
		try {
			DatagramSocket client = new DatagramSocket();
			InetAddress addr = InetAddress.getByName(ipAddres);
			DatagramPacket sendPacket =
					new DatagramPacket(sendBuf, 0, sendBuf.length, addr,
							Constant.DEFAULT_PORT);
			client.setBroadcast(true);
			client.send(sendPacket);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
