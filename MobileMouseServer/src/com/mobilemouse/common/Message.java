package com.mobilemouse.common;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.CharBuffer;

public class Message implements ParseAble {
	private MsgType mType;
	private ParseAble mData;

	private static final String CHARSET = "US-ASCII";
	public static final char BREAKER = ',';

	public Message(MsgType type, ParseAble data) {
		mType = type;
		mData = data;
		if (type == null)
			throw new RuntimeException("MsgType cannot be null");
	}

	public MsgType getType() {
		return mType;
	}

	public ParseAble getData() {
		return mData;
	}

	@Override
	public void writeToParser(Parser parser) {
		parser.writeString(mType.name());
		if (parser != null)
			mData.writeToParser(parser);
	}

	public byte[] writeToBytes() {
		Parser parser = new Parser();
		writeToParser(parser);
		try {
			return parser.toString().getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Message createFromParser(Parser parser) {
		String type = parser.readString();
		MsgType msgType = MsgType.valueOf(type);
		ParseAble data = null;
		if (msgType != null) {
			Method m;
			try {
				m = msgType.getDataClass().getMethod("createFromParser",
						Parser.class);
				data = (ParseAble) m.invoke(null, parser);
				return new Message(msgType, data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Message createFromBytes(byte[] bytes) {
		String str;
		try {
			str = new String(bytes, CHARSET);
			Parser parser = Parser.obtain();
			parser.setData(str);
			return createFromParser(parser);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		Parser p = Parser.obtain();
		writeToParser(p);
		return p.toString();
	}

	public enum MsgType {
		MEVENT(MEvent.class), PINGMSG(PingMsg.class);

		private Class<? extends ParseAble> mClass;

		MsgType(Class<? extends ParseAble> c) {
			mClass = c;
		}

		public Class<? extends ParseAble> getDataClass() {
			return mClass;
		}

	}

	public static class Parser {
		private static final int POOL_SIZE = 6;
		private static final Parser[] mPools = new Parser[POOL_SIZE];
		private java.io.StringReader mReader;
		private java.io.StringWriter mWriter;

		private Parser() {
		}

		public void setData(String s) {
			mReader = new StringReader(s);
		}

		public int readInt() {
			char c;
			int res = 0;
			try {
				while ((c = (char) mReader.read()) != BREAKER && c >= 0) {
					res *= 10;
					res += c - '0';
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		}

		public long readLong() {
			char c;
			long res = 0;
			try {
				while ((c = (char) mReader.read()) != BREAKER && c >= 0) {
					res *= 10;
					res += c - '0';
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		}

		public String readString() {
			CharArrayWriter writer = new CharArrayWriter();
			char c;
			try {
				while ((c = (char) mReader.read()) != BREAKER && c >= 0) {
					writer.write(c);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return writer.toString();
		}

		public void writeInt(int i) {
			if (mWriter == null)
				mWriter = new StringWriter();
			mWriter.write("" + i + BREAKER);
		}

		public void writeLong(long i) {
			if (mWriter == null)
				mWriter = new StringWriter();
			mWriter.write("" + i + BREAKER);
		}

		public void writeString(String s) {
			if (mWriter == null)
				mWriter = new StringWriter();
			mWriter.write(s + BREAKER);
		}

		public String toString() {
			if (mWriter == null)
				return null;
			return mWriter.toString();
		}

		public static Parser obtain() {
			synchronized (mPools) {
				Parser p;
				for (int i = 0; i < POOL_SIZE; i++) {
					p = mPools[i];
					if (p != null) {
						mPools[i] = null;
						return p;
					}
				}
			}
			return new Parser();
		}

		public void recycle() {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (Exception e) {
				}
				mReader = null;
			}
			if (mWriter != null) {
				try {
					mWriter.close();
				} catch (Exception e) {
				}
				mWriter = null;
			}
			synchronized (mPools) {
				for (int i = 0; i < POOL_SIZE; i++) {
					if (mPools[i] == null) {
						mPools[i] = this;
						return;
					}
				}
			}
		}
	}
}
