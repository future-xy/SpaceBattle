package com.fuyao.spacebattle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    final String TAG = "sv";
    GameObjects objects;
    Context context;
    boolean isRun;


    public GameSurfaceView(final Context context) {
        super(context);
        this.context = context;
        SurfaceHolder holder = this.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        objects = new GameObjects(context, holder);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
        Intent intent = new Intent();
        intent.putExtra("Id", 2);
        intent.setAction("com.fuyao.musicplayer.MusicService");
        context.sendBroadcast(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = Global.r2Vx(event.getX());
        float y = Global.r2Vy(event.getY());
//        Log.d(TAG, x + " " + y);
        String touchBtn = objects.getPressedButton(x, y);
        if (!touchBtn.equals("")) {
            String logs = "按了[" + touchBtn + "]按钮";
            Log.d(TAG, logs);
//            Toast.makeText(context, logs, Toast.LENGTH_SHORT).show();
            switch (touchBtn) {
                case "关闭":
                    ((MainActivity) context).finish();
                    break;
                case "开始":
                    objects.begin();
                    break;
                case "自动":
                    objects.setAI();
                    break;
                case "开火":
                    objects.fire();
                    break;
            }
        } else {
            if (objects.mySprite != null)
                objects.mySprite.setDirection(x, y);
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        Global.realW = getWidth();
        Global.realH = getHeight();
        Log.d(TAG, Global.realH + " " + Global.realW);
        Thread myThread = new Thread(this);
        myThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun = false;
    }

    @Override
    public void run() {
        long loopTime = 0;
        long start = 0;
        while (isRun) {
            start = System.currentTimeMillis();
            if (objects != null)
                objects.draw(loopTime);
            loopTime = System.currentTimeMillis() - start;
        }
    }


}

