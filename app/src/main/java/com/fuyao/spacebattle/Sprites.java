package com.fuyao.spacebattle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Sprites {
    Context context;
    ConcurrentHashMap<String, Sprite> hmSprites;
    String myName;

    Sprites(Context context, String myName) {
        this.context = context;
        hmSprites = new ConcurrentHashMap<>();
        this.myName = myName;
    }

    Sprite add(String spName, float x, float y, float dir, float step, boolean hit, boolean active) {
        Sprite sprite = null;
        if (hmSprites.containsKey(spName)) {
            sprite = hmSprites.get(spName);
        } else {
            for (Sprite sp : hmSprites.values()) {
                if (!sp.active) {
                    sprite = sp;
                }
            }
            if (sprite == null) {
                sprite = new Sprite(context);
                hmSprites.put(spName, sprite);
            }
            sprite.init(spName, x, y, dir, step, active, spName.equals(myName));
            if (spName.equals(myName))
                sprite.setImagePaint(new LightingColorFilter(0xFFFFFF, 0xA00000AA));
        }
        return sprite;
    }

    void draw(Canvas canvas, long loopTime) {
        for (String name : hmSprites.keySet()) {
            Sprite sprite = hmSprites.get(name);
            if (sprite != null) {
                if (sprite.active) {
                    if (sprite.ai) {
                        sprite.aiCalc(findOtherSprite(name), loopTime);
                    }
                    sprite.draw(canvas, loopTime);
                } else {
                    remove(name);
                }
            }
        }
    }

    void updateSprites(Bullets bullets, long loopTime) {
        for (String name : hmSprites.keySet()) {
            Sprite sprite = hmSprites.get(name);
            if (sprite != null) {
                sprite.deadCalc(loopTime);
                Bullet bullet = bullets.getHitBullet(sprite);
                if (bullet != null) {
                    sprite.hit = true;
                    bullet.active = false;
                    sprite.setImagePaint(new LightingColorFilter(0xFF0000, 0));
                }
            }
        }
    }

    private Sprite remove(String spName) {
        return hmSprites.remove(spName);
    }

    Sprite findOtherSprite(String spName) {
        for (String name : hmSprites.keySet()) {
            Sprite sprite = hmSprites.get(name);
            if (sprite != null && !name.equals(spName) && sprite.active) {
                return sprite;
            }
        }
        return null;
    }
}
