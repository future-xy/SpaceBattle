package com.fuyao.spacebattle;

import android.content.Context;
import android.graphics.Canvas;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Bullets {
    Context context;
    ConcurrentLinkedQueue<Bullet> lqBullets;
    String myName;

    public Bullets(Context context, String myName) {
        this.context = context;
        this.myName = myName;
        lqBullets = new ConcurrentLinkedQueue<>();
    }

    public Bullet add(String spName, float x, float y, float dir, float step) {
        Bullet bullet = null;
        for (Bullet bul : lqBullets) {
            if (!bul.active)
                bullet = bul;
        }
        if (bullet == null) {
            bullet = new Bullet(context);
            lqBullets.add(bullet);
        }
        bullet.init(spName, x, y, dir, step, spName.equals(myName));
        return bullet;
    }

    void draw(Canvas canvas, long loopTime) {
        for (Bullet bullet : lqBullets) {
            if (bullet != null && bullet.active)
                bullet.draw(canvas, loopTime);
        }
    }

    Bullet getHitBullet(Sprite sprite) {
        if (sprite.hit) return null;
        for (Bullet bullet : lqBullets) {
            if (!bullet.spName.equals(sprite.spName) && bullet.active && bullet.hitSprite(sprite)) {
                return bullet;
            }
        }
        return null;
    }
}
