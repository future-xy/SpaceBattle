package com.fuyao.spacebattle;

import android.net.TrafficStats;
import android.os.Handler;


public class NetSpeed implements Runnable {
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    private int uid;
    private Handler handler;

    public NetSpeed(int uid, Handler handler) {
        this.uid = uid;
        this.handler = handler;
    }


    @Override
    public void run() {
        while (true) {
            long nowTotalRxBytes = getTotalRxBytes(uid);
            long nowTimeStamp = System.currentTimeMillis();
            long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));
            lastTimeStamp = nowTimeStamp;
            lastTotalRxBytes = nowTotalRxBytes;
            handler.sendEmptyMessage((int) speed);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
    }

}
