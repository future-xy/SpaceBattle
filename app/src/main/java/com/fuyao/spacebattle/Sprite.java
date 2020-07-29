package com.fuyao.spacebattle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class Sprite {
    final String TAG = "mSP";
    final float HEIGHT = 120;
    final float WIDTH = 120;
    final int FONT_SIZE = 24;
    final long DEAD_TIME = 100 * Global.LOOP_TIME;
    final float FRAME_DURATION = 1 * Global.LOOP_TIME;
    final float AI_MOVE_GAP = 50 * Global.LOOP_TIME;
    final float AI_SHOT_GAP = 8 * Global.LOOP_TIME;
    float aiShotDelay;
    long deadTime;
    float aiMoveDelay = 0;
    float step = 10;
    String spName;
    float x;
    float y;
    float dir;
    boolean me = true;
    boolean ai = false;
    boolean active = true;
    boolean hit = false;
    int curFrameIndex;
    long frameDuration = 0;
    static MediaPlayer mp;

    int[] frames = {R.drawable.sprite1, R.drawable.sprite2, R.drawable.sprite3, R.drawable.sprite4,
            R.drawable.sprite5, R.drawable.sprite6, R.drawable.sprite7, R.drawable.sprite8};
    Paint paint1;
    Paint paint2;
    Context context;

    Sprite(Context context) {
        this.context = context;
        paint1 = new Paint();
        paint2 = new Paint();
        paint2.setColor(Color.argb(60, 50, 50, 50));
        paint2.setTextSize(Global.v2Rx(FONT_SIZE));
        curFrameIndex = 0;
        if (mp == null) {
            mp = new MediaPlayer();
            try {
                mp.setDataSource(context, Uri.parse("android.resource://com.fuyao.spacebattle/" + R.raw.bullet));
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void init(String spName, float x, float y, float dir, float step, boolean active, boolean me) {
        deadTime = 0;
        this.spName = spName;
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.step = step;
        this.active = active;
        this.me = me;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
    }

    void draw(Canvas canvas, long loopTime) {
        if (canvas == null) return;
        nextFrame(loopTime);
        if (hit)
            drop(loopTime);
        else if (me || ai)
            pos(loopTime);
        Bitmap frame = BitmapFactory.decodeResource(context.getResources(), frames[curFrameIndex]);
        frame = Bitmap.createScaledBitmap(frame, (int) Global.v2Rx(WIDTH), (int) Global.v2Ry(HEIGHT), true);
        frame = rotateSprite(frame, dir);
        canvas.drawBitmap(frame, x, y, paint1);
        canvas.drawText(spName, x, y, paint2);
    }

    void setImagePaint(LightingColorFilter lightingColorFilter) {
        paint1.setColorFilter(lightingColorFilter);
    }

    void setDirection(float newX, float newY) {
        float dx = newX - x;
        float dy = newY - y;
        //更简单的形式
        dir = (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    Bullet shot(Bullets bullets) {
        mp.seekTo(0);
        Point point = getShotStPos();
        Bullet bullet = bullets.add(spName, point.x, point.y, dir, step * 3);
        mp.start();
        return bullet;
    }

    Point getShotStPos() {
        return new Point((int) (x + WIDTH / 2 + WIDTH / 2 * Math.cos(Math.toRadians(dir))),
                (int) (y + HEIGHT / 2 + WIDTH / 2 * Math.sin(Math.toRadians(dir))));
    }

    private Bitmap rotateSprite(Bitmap bitmap, float degree) {
        Log.d(TAG, degree + "");
        Matrix matrix = new Matrix();
        if (degree <= -90 || degree >= 90) {
            matrix.postTranslate(-bitmap.getWidth(), 0);
            matrix.postScale(-1, 1);
            degree += 180;
        }

        Bitmap tempBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBmp);
        matrix.postRotate(degree, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        canvas.drawBitmap(bitmap, matrix, null);
        return tempBmp;
    }

    private void nextFrame(long loopTime) {
        frameDuration += loopTime;
        if (frameDuration > FRAME_DURATION) {
            frameDuration = 0;
            curFrameIndex = (curFrameIndex + 1) % frames.length;
        }
    }

    void deadCalc(long loopTime) {
        if (me || ai) return;
        deadTime += loopTime;
        if (deadTime > DEAD_TIME) {
            active = false;
            deadTime = 0;
        }
    }

    private void pos(long loopTime) {
        float step1 = step * loopTime / Global.LOOP_TIME;
        x += step1 * (float) Math.cos(Math.toRadians(dir));
        y += step1 * (float) Math.sin(Math.toRadians(dir));
        x = Math.max(0, Math.min(x, Global.virtualW - WIDTH));
        y = Math.max(0, Math.min(y, Global.virtualH - HEIGHT));
    }

    private void drop(long loopTime) {
        y += 4 * step * loopTime / Global.LOOP_TIME;
        if (y > Global.virtualH)
            active = false;
    }

    void aiCalc(Sprite objSprite, long loopTime) {
        if (objSprite == null) return;
        aiMoveDelay += loopTime;
        if (aiMoveDelay > AI_MOVE_GAP) {
            aiMoveDelay = 0;
            setAiDirect(objSprite.x, objSprite.y, objSprite.dir, objSprite.step);
        }
    }

    void aiShot(Bullets bullets, long loopTime) {
        if (hit || !active) return;
        aiShotDelay += loopTime;
        if (aiShotDelay > AI_SHOT_GAP) {
            aiShotDelay = 0;
            shot(bullets);
        }
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", "sprite");
            obj.put("name", spName);
            obj.put("x", x);
            obj.put("y", y);
            obj.put("dir", dir);
            obj.put("hit", hit);
            obj.put("active", active);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj.toString();
    }

    //方向:不断修正能够追上对方
    private void setAiDirect(float objX, float objY, float objDir, float objSpeed) {
        float newX = (float) (objX + objSpeed * AI_MOVE_GAP / Global.LOOP_TIME * Math.cos(Math.toRadians(objDir)));
        float newY = (float) (objY + objSpeed * AI_MOVE_GAP / Global.LOOP_TIME * Math.sin(Math.toRadians(objDir)));
        setDirection(newX, newY);
    }

}
