package nju.androidchat.client.hw1.mvp0;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import lombok.extern.java.Log;
import nju.androidchat.client.ClientMessage;
import nju.androidchat.client.R;
import nju.androidchat.client.Utils;
import nju.androidchat.client.component.ItemTextReceive;
import nju.androidchat.client.component.ItemTextSend;
import nju.androidchat.client.component.OnRecallMessageRequested;

@Log
public class HwTalkActivity extends AppCompatActivity implements HwContract.View, TextView.OnEditorActionListener, OnRecallMessageRequested {
    private HwContract.Presenter presenter;
    private HashMap<String, Bitmap> pictures = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HwTalkModel hwTalkModel = new HwTalkModel();

        // Create the presenter
        this.presenter = new HwTalkPresenter(hwTalkModel, this, new ArrayList<>());
        hwTalkModel.setIMvp0TalkPresenter(this.presenter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showMessageList(List<ClientMessage> messages) {
        runOnUiThread(() -> {
                    LinearLayout content = findViewById(R.id.chat_content);

                    // 删除所有已有的ItemText
                    content.removeAllViews();

                    // 增加ItemText
                    for (ClientMessage message : messages) {
                        String text = String.format("%s", message.getMessage());
                        // 如果是自己发的，增加ItemTextSend
                        if (message.getSenderUsername().equals(this.presenter.getUsername())) {
                            if(text.startsWith("![]{") && text.endsWith("}")){
                                String address = text.substring(4,text.length() - 1);

                                if(!pictures.containsKey(address)){
                                    System.out.println("if" );
                                    Thread thread = new Thread(){
                                        @Override
                                        public void run() {

                                            try {
                                                URL url = new URL(address);
                                                System.out.println("url is :" + url);
                                                Bitmap pic = BitmapFactory.decodeStream(url.openStream());
                                                pictures.put(address, pic);
                                                Thread.sleep(1000);
                                                showMessageList(messages);
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    };
                                    thread.start();

                                }
                                else{
                                    System.out.println("else");
                                    content.addView(new ItemTextSend(this, pictures.get(address), message.getMessageId(), this));
                                }

                            }else{
                                content.addView(new ItemTextSend(this, text, message.getMessageId(), this));
                            }

                        } else {
                            content.addView(new ItemTextReceive(this, text, message.getMessageId()));
                        }
                    }

                    Utils.scrollListToBottom(this);
                }
        );
    }

    @Override
    public void setPresenter(HwContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            return hideKeyboard();
        }
        return super.onTouchEvent(event);
    }

    private boolean hideKeyboard() {
        return Utils.hideKeyboard(this);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (Utils.send(actionId, event)) {
            hideKeyboard();
            // 异步地让Controller处理事件
            sendText();
        }
        return false;
    }

    private void sendText() {
        EditText text = findViewById(R.id.et_content);
        AsyncTask.execute(() -> {
            this.presenter.sendMessage(text.getText().toString());
        });
    }

    public void onBtnSendClicked(View v) {
        hideKeyboard();
        sendText();
    }

    // 当用户长按消息，并选择撤回消息时做什么，MVP-0不实现
    @Override
    public void onRecallMessageRequested(UUID messageId) {

    }
}
