package com.mobilemouse.common;

import com.mobilemouse.common.Message.Parser;

public class SimpleString implements ParseAble {
	private String mString;

	public SimpleString(String s) {
		mString = s;
	}

	@Override
	public void writeToParser(Parser parser) {
		parser.writeString(mString);
	}

	public static SimpleString createFromParser(Parser p) {
		return new SimpleString(p.readString());
	}
}
