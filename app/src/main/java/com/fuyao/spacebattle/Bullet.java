package com.fuyao.spacebattle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import org.json.JSONObject;

public class Bullet {
    final float RADIUS = 5;
    static long seqnoX = 0;
    String spName;
    long seqno;
    float x;
    float y;
    float dir;
    float step = 10;
    boolean me = false;
    boolean active = true;
    Paint paint1;
    Context context;

    public Bullet(Context context) {
        this.context = context;
        paint1 = new Paint();
        paint1.setColor(Color.RED);
    }

    void init(String spName, float x, float y, float dir, float step, boolean me) {
        active = true;
        this.spName = spName;
        this.x = x;
        this.y = y;
        this.dir = dir;
        seqno = seqnoX++;
        this.step = step;
        this.me = me;
    }

    void draw(Canvas canvas, long loopTime) {
        if (canvas == null) return;
        pos(loopTime);
        canvas.drawCircle(x, y, Global.v2Rx(RADIUS), paint1);
    }

    boolean hitSprite(Sprite sprite) {
        Point leftTop = new Point((int) sprite.x, (int) sprite.y);
        Point rightTop = new Point((int) (sprite.x + sprite.WIDTH * Math.cos(Math.toRadians(sprite.dir))),
                (int) (sprite.y + sprite.WIDTH * Math.sin(Math.toRadians(sprite.dir))));
        Point leftBottom = new Point((int) (sprite.x - sprite.HEIGHT * Math.sin(Math.toRadians(sprite.dir))),
                (int) (sprite.y + sprite.HEIGHT * Math.cos(Math.toRadians(sprite.dir))));
        Point rightBottom = new Point((int) (sprite.x + sprite.WIDTH * Math.cos(Math.toRadians(sprite.dir)) - sprite.HEIGHT * Math.sin(Math.toRadians(sprite.dir))),
                (int) (sprite.y + sprite.WIDTH * Math.sin(Math.toRadians(sprite.dir)) + sprite.HEIGHT * Math.cos(Math.toRadians(sprite.dir))));
        return inRect(leftTop, rightTop, leftBottom, rightBottom);
    }

    private boolean inRect(Point p1, Point p4, Point p2, Point p3) {
        Point p = new Point((int) x, (int) y);
        return getCross(p1, p2, p) * getCross(p3, p4, p) >= 0 && getCross(p2, p3, p) * getCross(p4, p1, p) > 0;
    }

    private float getCross(Point p1, Point p2, Point p) {
        return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
    }

    private void pos(long loopTime) {
        float step1 = step * loopTime / Global.LOOP_TIME;
        x += step1 * (float) Math.cos(Math.toRadians(dir));
        y += step1 * (float) Math.sin(Math.toRadians(dir));
        if (x < 0 || x > Global.virtualW || y < 0 || y > Global.virtualH)
            active = false;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", "bullet");
            obj.put("name", spName);
            obj.put("x", x);
            obj.put("y", y);
            obj.put("dir", dir);
            obj.put("step", step);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj.toString();
    }
}
