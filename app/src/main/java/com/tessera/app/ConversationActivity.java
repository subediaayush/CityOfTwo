package com.tessera.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.tessera.app.Conversation.Type;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {
    List<Conversation> conversationList;
    ConversationAdapter conversationAdaper;
    EditText InputText;
    ImageButton SendButton;
    RecyclerView ConversationListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        InputText = (EditText) findViewById(R.id.input_text);
        SendButton = (ImageButton) findViewById(R.id.send_button);
        ConversationListView = (RecyclerView) findViewById(R.id.conversation_listview);

        conversationList = new ArrayList<>();
        conversationAdaper = new ConversationAdapter(this, conversationList);

        ConversationListView.setAdapter(conversationAdaper);
        ConversationListView.setLayoutManager(new LinearLayoutManager(this));

        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
//        InputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

    private void SendMessage() {
        String bufferText = InputText.getText().toString();
        bufferText = bufferText.replaceFirst("\\s+$", "");
        if (!bufferText.isEmpty()) {
            conversationList.add(new Conversation(bufferText, Type.SENT));
            conversationAdaper.notifyDataSetChanged();
            InputText.setText("");
        }
    }
}
