package com.fuyao.spacebattle;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

public class GameObjects implements Handler.Callback {
    final String TAG = "GAO";
    Context context;
    private SurfaceHolder holder;
    private long speed;
    final private Paint defaultPaint;
    Buttons buttons;
    Bitmap background;
    Sprites sprites;
    Bullets bullets;
    Sprite mySprite;
    String myName;
    TcpSocket tcpSocket;
    boolean networkMode = false;
    boolean isBegin = false;
    private Handler handler = new Handler(this);

    public GameObjects(Context context, SurfaceHolder holder) {
        this.context = context;
        this.holder = holder;
        NetSpeed netSpeed = new NetSpeed(context.getApplicationInfo().uid, handler);
        Thread thread = new Thread(netSpeed);
        thread.start();
        mySprite = null;
        background = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud);
        buttons = new Buttons(context);
        buttons.pos();
        String name = "N" + (int) (Math.random() * (10000 - 10 + 1));
        myName = name;
        defaultPaint = new Paint();
        defaultPaint.setColor(Color.BLACK);
        defaultPaint.setTextSize(24);
        bullets = new Bullets(context, name);
        sprites = new Sprites(context, name);
    }

    void connect(String host, int port) {
        if (networkMode) {
            tcpSocket = new TcpSocket();
            if (tcpSocket.testConnection(host, port))
                Log.d(TAG, "TCP OK!");
            else
                Log.d(TAG, host + port);
            tcpSocket.connect(host, port);
            RecvData recvData = new RecvData(tcpSocket.socket, this);
            Thread thread = new Thread(recvData);
            thread.start();
        }
    }

    void handleRecvData(String data) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (obj == null)
            return;
        if (obj.optString("type").equals("sprite")) {
            String name = obj.optString("name");
            if (name.equals(myName))
                return;
            float x, y, dir;
            boolean hit, active;
            x = (float) obj.optDouble("x");
            y = (float) obj.optDouble("y");
            dir = (float) obj.optDouble("dir");
            hit = obj.optBoolean("hit");
            active = obj.optBoolean("active");
            if (!sprites.hmSprites.containsKey(name)) {
                Sprite sprite = sprites.add(name, x, y, dir, 10, hit, active);
                sprite.deadTime = 0;
            } else {
                Sprite sprite = sprites.hmSprites.get(name);
                sprite.init(name, x, y, dir, 10, active, false);
                sprite.hit = hit;
                sprite.deadTime = 0;
            }
        } else {
            String name = obj.optString("name");
            if (name.equals(myName))
                return;
            float x, y, dir, step;
            x = (float) obj.optDouble("x");
            y = (float) obj.optDouble("y");
            dir = (float) obj.optDouble("dir");
            step = (float) obj.optDouble("step");
            bullets.add(name, x, y, dir, step);
        }

    }

    private void handleAI(long loopTime) {
        if (sprites == null || bullets == null) return;
        for (String name : sprites.hmSprites.keySet()) {
            Sprite sprite = sprites.hmSprites.get(name);
            if (sprite != null && sprite.ai) {
                sprite.aiShot(bullets, loopTime);
            }
        }
    }

    void draw(long loopTime) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            drawBackground(canvas);
            canvas.drawText(speed + "kb/s", 10, 30, defaultPaint);
            buttons.draw(canvas);
            if (networkMode && mySprite != null)
                tcpSocket.sendString(mySprite.toString());
            handleAI(loopTime);
            if (sprites != null) {
                sprites.updateSprites(bullets, loopTime);
                sprites.draw(canvas, loopTime);
            }
            if (bullets != null)
                bullets.draw(canvas, loopTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    void drawBackground(Canvas canvas) {
        if (canvas == null) return;
        background = Bitmap.createScaledBitmap(background, Global.realW, Global.realH, true);
        canvas.drawBitmap(background, 0, 0, new Paint());
    }

    void begin() {
        if (!isBegin) {
            final RelativeLayout inputUserView = (RelativeLayout) ((MainActivity) context).getLayoutInflater().inflate(R.layout.input_user, null);
            final EditText inputName = inputUserView.findViewById(R.id.editTextNumber);
            inputName.setText(myName);
            final EditText inputServer = inputUserView.findViewById(R.id.editTextTextPersonName);
            inputServer.setText("192.168.1.104");
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            AlertDialog dialog = dialogBuilder
                    .setIcon(R.mipmap.sysu)
                    .setTitle("输入用户")
                    .setView(inputUserView)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNeutralButton("单机版", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isBegin = true;
                                    networkMode = false;
                                    myName = inputName.getText().toString();
                                    begin();
                                }
                            }
                    )
                    .setPositiveButton("联网", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isBegin = true;
                            networkMode = true;
                            myName = inputName.getText().toString();
                            String server = inputServer.getText().toString();
                            connect(server, 50000);
                            begin();
                        }
                    })
                    .create();
            dialog.show();
            return;
        }
        float x, y, dir;
        x = (float) (Math.random() * Global.virtualW);
        y = (float) (Math.random() * Global.virtualH);
        dir = (float) (Math.random() - 0.5) * 180;
        Log.d(TAG, x + " " + y + " " + dir);
        if (mySprite == null || !mySprite.active) {
            Log.d(TAG, "BEGIN");
            mySprite = sprites.add(myName, x, y, dir, 10, false, true);
        } else if (!networkMode) {
            Sprite other = sprites.add("other", x, y, dir, 10, false, true);
            other.setAi(true);
        }
    }

    void fire() {
        if (mySprite == null) return;
        Bullet bullet = mySprite.shot(bullets);
        if (networkMode)
            tcpSocket.sendString(bullet.toString());
    }

    void setAI() {
        if (mySprite == null) return;
        mySprite.ai = !mySprite.ai;
    }

    String getPressedButton(float x, float y) {
        return buttons.getPressedButton(x, y);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        speed = msg.what;
        Log.d(TAG, speed + "");
        return true;
    }
}
