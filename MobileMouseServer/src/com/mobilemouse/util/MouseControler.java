package com.mobilemouse.util;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class MouseControler {
	private Robot mRobot;

	public MouseControler() {
		try {
			mRobot = new Robot();
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}

	public void move(int dx, int dy) {
		PointerInfo a = MouseInfo.getPointerInfo();
		Point b = a.getLocation();
		int x = (int) b.getX();
		int y = (int) b.getY();
		x += dx;
		y += dy;
		mRobot.mouseMove(x, y);
	}

	public void down() {
		mRobot.mousePress(InputEvent.BUTTON1_MASK);
	}

	public void up() {
		mRobot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public void leftClick(){
		mRobot.mousePress(InputEvent.BUTTON1_MASK);
		mRobot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public void doubleClick(){
		leftClick();
		leftClick();
	}
	
	public void rightClick(){
		mRobot.mousePress(InputEvent.BUTTON3_MASK);
		mRobot.mouseRelease(InputEvent.BUTTON3_MASK);
	}
	
	public void scroll(int wheel){
		mRobot.mouseWheel(wheel);
	}
}
