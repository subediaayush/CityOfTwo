package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONObject;
import org.solovyev.android.views.llm.LinearLayoutManager;

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
    HttpHandler mHttpHandler;

    BroadcastReceiver mBroadcastReceiver;


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

                SendMessage(new Conversation(bufferText, Conversation.SENT));
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getExtras();

                String message = intent.getStringExtra(CityOfTwo.KEY_TYPE);

                Log.i("Receiver", "Signal Received: " + message);

                switch (message) {
                    case "MESSAGE":
                        String text = intent.getStringExtra(CityOfTwo.KEY_TEXT);
//                        Log.i("Chat", text);
                        mConversationList.add(new Conversation(text, Conversation.RECEIVED));
                        mConversationAdapter.notifyDataSetChanged();
                        mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());
                        break;
                    case "END_CHAT":
                        ExitActivity(RESULT_OK, new Intent());
                }
            }
        };
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

    private void ExitActivity(int resultCode, Intent intent) {
        setResult(resultCode, intent);
        finish();
    }

    private void SendMessage(final Conversation bufferConv) {
        mConversationList.add(bufferConv);
        mConversationAdapter.notifyDataSetChanged();
        mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());

        final String value = bufferConv.getText().replaceFirst("\\s+$", "");

        String send_message = getString(R.string.url_send_message);

        if (!value.isEmpty()) {
            String[] Path = {CityOfTwo.API, send_message};

            String header = CityOfTwo.HEADER_SEND_MESSAGE;

            mHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, header, value) {
                @Override
                protected void onPreRun() {
                }

                @Override
                protected void onSuccess(String response) {
                    try {
                        JSONObject Response = new JSONObject(response);

                        Boolean status = Response.getBoolean("parsadi");

                        if (!status) {
                            onFailure(getResponseStatus());
                        } else{
                            mInputText.setText("");
                        }

                    } catch (Exception e ) {
                        onFailure(getResponseStatus());
                        e.printStackTrace();
                    }
                }

                @Override
                protected void onFailure(Integer status) {
                    Toast.makeText(ConversationActivity.this, "Message couldnot be sent." +
                            " Please try again later", Toast.LENGTH_SHORT).show();
                    mConversationList.remove(bufferConv);
                    mConversationAdapter.notifyDataSetChanged();
                }
            };

            String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                    .getString(CityOfTwo.KEY_SESSION_TOKEN, "");

            mHttpHandler.addHeader("Authorization", token);

            mHttpHandler.execute();
        }
    }

    @Override
    public void onBackPressed() {
        ExitActivity(RESULT_CANCELED, new Intent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();

        filter.addAction(CityOfTwo.PACKAGE_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }
}
