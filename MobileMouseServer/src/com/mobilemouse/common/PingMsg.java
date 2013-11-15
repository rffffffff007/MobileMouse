package com.mobilemouse.common;

import com.mobilemouse.common.Message.Parser;

public class PingMsg implements ParseAble {
	public static int COMMAND_PING = 1;
	public static int COMMAND_ACK = 2;
	public static int COMMAND_CONNECT = 3;
	public static int COMMAND_RELEASE = 4;

	private String mDeviceId;
	private int mCommand;

	public PingMsg(String deviceId, int command) {
		mDeviceId = deviceId;
		mCommand = command;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public int getCommand() {
		return mCommand;
	}

	@Override
	public void writeToParser(Parser parser) {
		parser.writeString(mDeviceId);
		parser.writeInt(mCommand);
	}

	public static PingMsg createFromParser(Parser parser) {
		String d = parser.readString();
		int c = parser.readInt();
		PingMsg msg = new PingMsg(d, c);
		return msg;
	}

}
