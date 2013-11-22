package com.dongluh.ddz.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public abstract class BaseView extends SurfaceView implements Callback, Runnable {

	private SurfaceHolder holder; // ����SufaceView
	private Thread pThread; // ��ͼ�߳�
	protected Paint paint; // Ĭ�ϻ���
	private boolean isRunning; // �Ƿ��ڻ�ͼ
	private static final int SPAN = 100;
	
	public BaseView(Context context, AttributeSet attrs) {
		super(context, attrs);

		holder = getHolder();
		holder.addCallback(this);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);     // ���ÿ����
		paint.setTextSize(25);
		paint.setColor(Color.WHITE);
	}

	@Override
	public void run() {
		while (isRunning) {
			Canvas canvas = null;
			try {
				// ��������
				synchronized (holder) {
					canvas = holder.lockCanvas();
				}
				// ��ͼ�߼�
				render(canvas);
			} finally {
				// �����������ص����̣߳���Ⱦ����Ļ��
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
			
			try {
				Thread.sleep(SPAN);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// ������ͼ�߳�
		pThread = new Thread(this);
		pThread.start();
		isRunning = true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// ���ٻ�ͼ�߳�
		isRunning = false;
		if (pThread != null && pThread.isAlive()) {
			try {
				pThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract void render(Canvas canvas);
}
