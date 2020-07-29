package com.fuyao.spacebattle;

import android.content.Context;
import android.graphics.Canvas;

public class Buttons {
    MyButton[] arrButtons;
    String[] texts = {"开始", "开火", "自动", "关闭"};
    final String TAG = "BTNS";

    Buttons(Context context) {
        arrButtons = new MyButton[4];
        for (int i = 0; i < arrButtons.length; i++) {
            arrButtons[i] = new MyButton(context);
            arrButtons[i].setText(texts[i]);
        }
    }

    void pos() {
        float r = 1;
        for (MyButton button : arrButtons) {
            button.setCenterX(0.2f * r * Global.virtualW);
            button.setCenterY(0.95f * Global.virtualH);
            r++;
        }
    }

    void draw(Canvas canvas) {
        for (MyButton button : arrButtons) {
            button.draw(canvas);
        }
    }

    String getPressedButton(float x, float y) {
        for (int i = 0; i < arrButtons.length; i++) {
            if (arrButtons[i].getPressed(x, y))
                return texts[i];
        }
        return "";
    }
}
