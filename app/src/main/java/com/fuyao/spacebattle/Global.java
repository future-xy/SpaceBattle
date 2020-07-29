package com.fuyao.spacebattle;

public class Global {
    static int realW;
    static int realH;
    static int virtualW = 1080;
    static int virtualH = 1920;
    static long LOOP_TIME = 50;

    static float v2Rx(float virtualSize) {
        return virtualSize * realW / virtualW;
    }

    static float v2Ry(float virtualSize) {
        return virtualSize * realH / virtualH;
    }

    static float r2Vx(float realSize) {
        return realSize * virtualW / realW;
    }

    static float r2Vy(float realSize) {
        return realSize * virtualH / realH;
    }
}
