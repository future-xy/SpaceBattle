package com.fuyao.spacebattle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MyButton {

    Context context;
    final float RADIUS = 80;
    final int FONT_SIZE = 48;
    String text;
    float centerX;
    float centerY;
    Paint paint1;
    Paint paint2;
    final String TAG = "BTNN";


    public MyButton(Context context) {
        this.context = context;
        paint1 = new Paint();
        paint1.setColor(Color.argb(128, 100, 160, 100));
        paint2 = new Paint();
        paint2.setColor(Color.argb(128, 50, 50, 50));
        paint2.setTextAlign(Paint.Align.CENTER);
    }


    public void draw(Canvas canvas) {
        if (canvas == null) return;
        paint2.setTextSize(Global.v2Rx(FONT_SIZE));
        canvas.drawCircle(Global.v2Rx(centerX), Global.v2Ry(centerY), Global.v2Rx(RADIUS), paint1);
        canvas.drawText(text, Global.v2Rx(centerX), Global.v2Ry(centerY) + 18, paint2);
    }

    boolean getPressed(float x, float y) {
        return Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)) < RADIUS;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }
}
