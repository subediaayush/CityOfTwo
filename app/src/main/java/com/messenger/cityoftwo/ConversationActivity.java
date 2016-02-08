package com.messenger.cityoftwo;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ConversationActivity extends AppCompatActivity {
    public static final String HOST = "http://192.168.100.1:5000";
    List<Conversation> mConversationList;
    ConversationAdapter mConversationAdapter;
    EditText mInputText;
    ImageButton mSendButton;
    RecyclerView mConversationListView;
    View mConnectionIndicatorView;
    WebSocketClient mWebSocketClient;
    Toolbar mToolbar;

    @Override
    protected void onStop() {
        super.onStop();
        mWebSocketClient.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mInputText = (EditText) findViewById(R.id.input_text);
        mSendButton = (ImageButton) findViewById(R.id.send_button);
        mConversationListView = (RecyclerView) findViewById(R.id.conversation_listview);
        mConnectionIndicatorView = findViewById(R.id.network_indicator);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Conversation");

        mConversationList = new ArrayList<>();
        mConversationAdapter = new ConversationAdapter(this, mConversationList);

        connectWebSocket();

        SlideInUpAnimator itemAnimator = new SlideInUpAnimator() {
            @Override
            protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
                super.preAnimateAddImpl(holder);
                int viewType = holder.getItemViewType();
                View view = holder.itemView;
                if (viewType == 1) {
                    ViewCompat.setRotation(view, -45);
                    ViewCompat.setPivotX(view, view.getWidth());
                } else if (viewType == 0) {
                    ViewCompat.setRotation(holder.itemView, 45);
                    ViewCompat.setPivotX(view, 0);
                }
            }

            @Override
            protected void animateAddImpl(RecyclerView.ViewHolder holder) {
                ViewCompat.animate(holder.itemView)
                        .translationY(0)
                        .alpha(1)
                        .rotation(0)
                        .setDuration(getAddDuration())
                        .setInterpolator(mInterpolator)
                        .setListener(new DefaultAddVpaListener(holder))
                        .start();
            }
        };
        itemAnimator.setAddDuration(200);
        itemAnimator.setInterpolator(new OvershootInterpolator(2.0f));
        mConversationListView.setItemAnimator(itemAnimator);

        mConversationListView.setAdapter(mConversationAdapter);
        mConversationListView.setLayoutManager(new LinearLayoutManager(this));

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bufferText = mInputText.getText().toString().replaceFirst("\\s+$", "");
                if (SendMessage(bufferText)) {
                    mInputText.setText("");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new Runnable() {
                                @Override
                                public void run() {
                                    mConversationAdapter.addItem(new Conversation(bufferText, Conversation.Type.RECEIVED));
                                    mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());
                                }
                            }.run();
                        }
                    }, 5500L);
                }
            }
        });
//        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_ACTION_SEND
//                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
//                    SendMessage();
//                }
//                return true;
//            }
//        });
    }

    private boolean SendMessage(String bufferText) {
        if (!bufferText.isEmpty()) {
            mConversationAdapter.addItem(new Conversation(bufferText, Conversation.Type.SENT));
            mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());

            mWebSocketClient.send(bufferText);

            return true;
        }
        return false;
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(HOST);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectionIndicatorView.setBackgroundColor(ContextCompat.getColor(ConversationActivity.this, R.color.LimeGreen));
                    }
                });
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConversationAdapter.addItem(new Conversation(message, Conversation.Type.RECEIVED));
                        mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectionIndicatorView.setBackgroundColor(ContextCompat.getColor(ConversationActivity.this, R.color.DarkRed));
                    }
                });
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }
}
