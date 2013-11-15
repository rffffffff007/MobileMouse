package com.mobilemouse.util;

public class TimeTaskDispatcher extends Thread {
	public long mSleepInterval;
	protected TimeTaskQueue mQueue;

	public TimeTaskDispatcher() {
		this(50);
	}

	public TimeTaskDispatcher(long sleepInterval) {
		mSleepInterval = sleepInterval;
		mQueue = new TimeTaskQueue();
	}

	public TimeTaskQueue getTaskQueue() {
		return mQueue;
	}

	public void push(TimeTask t) {
		mQueue.push(t);
		synchronized (this) {
			notify();
		}
	}

	public void remove(TimeTask t) {
		mQueue.remove(t);
	}

	public void removeById(int id) {
		mQueue.removeById(id);
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			TimeTask task = (TimeTask) mQueue.top();
			if (task != null && task.getTime() < System.currentTimeMillis()) {
				mQueue.pop();
				task.run();
			}
			synchronized (this) {
				try {
					this.wait(mSleepInterval);
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
				}
			}
		}
	}

	public void finish() {
		interrupt();
	}

}
