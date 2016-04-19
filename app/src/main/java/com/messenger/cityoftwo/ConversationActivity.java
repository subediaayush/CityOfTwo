package com.messenger.cityoftwo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONObject;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import static com.messenger.cityoftwo.CityOfTwo.BackgroundConversation;

public class ConversationActivity extends AppCompatActivity {
    public static final String HOST = "http://192.168.100.1:5000";
    List<Conversation> mConversationList;
    ConversationAdapter mConversationAdapter;
    EditText mInputText;
    ImageButton mSendButton;
    RecyclerView mConversationListView;
    View mConnectionIndicatorView;
    Toolbar mToolbar;
    HttpHandler mHttpHandler;

    BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mInputText = (EditText) findViewById(R.id.input_text);
        mSendButton = (ImageButton) findViewById(R.id.send_button);
        mConversationListView = (RecyclerView) findViewById(R.id.conversation_listview);
//        mConnectionIndicatorView = findViewById(R.id.network_indicator);

//        setSupportActionBar(mToolbar);

//        getSupportActionBar().setTitle("Conversation");

        mConversationList = new ArrayList<>();
        mConversationAdapter = new ConversationAdapter(this, mConversationList);

        ChatItemAnimator itemAnimator = new ChatItemAnimator(0.1f, 200);

        mConversationListView.setItemAnimator(itemAnimator);

        mConversationList.add(new Conversation("", CityOfTwo.START));
        mConversationList.add(new Conversation("", CityOfTwo.END));
        mConversationListView.setAdapter(mConversationAdapter);

        LinearLayoutManager l = new LinearLayoutManager(this);
        l.setStackFromEnd(true);
        mConversationListView.setLayoutManager(l);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bufferText = mInputText.getText().toString().replaceFirst("\\s+$", "");

                if (!bufferText.isEmpty()) {
                    mInputText.setText("");
                    sendMessage(new Conversation(bufferText, CityOfTwo.SENT));
                }
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getExtras();

                String message = intent.getStringExtra(CityOfTwo.KEY_TYPE);

                Log.i("ConversationReceiver", "Signal Received: " + message);

                switch (message) {
                    case "MESSAGE":
                        String text = intent.getStringExtra(CityOfTwo.KEY_TEXT);

                        Conversation c = new Conversation(text, CityOfTwo.RECEIVED);
                        mConversationList.add(mConversationList.size() - 1, c);

                        mConversationAdapter.notifyItemInserted(mConversationList.indexOf(c));
                        mConversationListView.smoothScrollToPosition(mConversationList.size());
                        break;
                    case "END_CHAT":
                        exitActivity(RESULT_OK, new Intent());
                }
            }
        };
    }

    private void exitActivity(int resultCode, Intent intent) {
        setResult(resultCode, intent);
        finish();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            ArrayList<String> textList = savedInstanceState.getStringArrayList(CityOfTwo.KEY_CURRENT_CHAT);
            mConversationList.clear();
            for (String text : textList)
                mConversationList.add(new Conversation(text));
        }

        mConversationAdapter.notifyDataSetChanged();
        mConversationListView.scrollToPosition(
                mConversationList.size()
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_BACKGROUND;

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.cancel("CHAT_NOTIFICATION", 0);

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_FOREGROUND;
        if (BackgroundConversation == null)
            BackgroundConversation = new ArrayList<>();

        mConversationList.addAll(mConversationList.size() - 1, BackgroundConversation);
        mConversationAdapter.notifyDataSetChanged();
        BackgroundConversation.clear();

        IntentFilter filter = new IntentFilter();

        filter.addAction(CityOfTwo.PACKAGE_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!mConversationList.isEmpty()) {
            ArrayList<String> textList = new ArrayList<>();
            for (Conversation c : mConversationList) {
                textList.add(c.toString());
            }
            outState.putStringArrayList(CityOfTwo.KEY_CURRENT_CHAT, textList);
        }


    }

    private void sendMessage(final Conversation bufferConv) {
        mConversationList.add(mConversationList.size() - 1, bufferConv);

        mConversationAdapter.notifyItemInserted(mConversationList.indexOf(bufferConv));
        mConversationListView.smoothScrollToPosition(mConversationList.size());

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

                        if (!status) onFailure(getResponseStatus());

                    } catch (Exception e) {
                        onFailure(getResponseStatus());
                        e.printStackTrace();
                    }
                }

                @Override
                protected void onFailure(Integer status) {
//                    Toast.makeText(ConversationActivity.this, "Message couldnot be sent." +
//                            " Please try again later", Toast.LENGTH_SHORT).show();

                    Snackbar s = Snackbar.make(
                            mConversationListView,
                            "Your message was not sent!",
                            Snackbar.LENGTH_SHORT
                    );

                    s.setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMessage(new Conversation(
                                    bufferConv.getText(),
                                    bufferConv.getType()
                            ));
                        }
                    });

                    s.show();

                    if (!BuildConfig.DEBUG) {
                        mConversationList.remove(bufferConv);
                        mConversationAdapter.notifyDataSetChanged();
                    }
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
        exitActivity(RESULT_CANCELED, new Intent());
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
