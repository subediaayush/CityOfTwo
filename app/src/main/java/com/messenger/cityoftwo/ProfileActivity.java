package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Aayush on 1/5/2017.
 */
public class ProfileActivity extends AppCompatActivity {

	private static final String TAG = "ProfileActivity";
	View mSendButton;
	EditText mChatInput;

	NestedScrollView mScrollView;

	ConversationAdapter mAdapter;
	RecyclerView mConversationList;
	ChatLayoutManager mLayoutManager;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_profile);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mConversationList = (RecyclerView) findViewById(R.id.conversation_listview);
		mSendButton = findViewById(R.id.send_button);
		mChatInput = (EditText) findViewById(R.id.input_text);

		mLayoutManager = new ChatLayoutManager(this);
		mLayoutManager.setStackFromEnd(true);
		mAdapter = new ConversationAdapter(this, mLayoutManager);

		mConversationList.setLayoutManager(mLayoutManager);
		mConversationList.setAdapter(mAdapter);

		mAdapter.insertItem(new Conversation("start",
				CityOfTwo.FLAG_START
		));

		mAdapter.insertItem(new Conversation("end",
				CityOfTwo.FLAG_END
		));

		for (int i = 0; i < 25; i++) {
			mAdapter.insertItem(new Conversation("hi" + i,
					CityOfTwo.FLAG_TEXT | CityOfTwo.FLAG_RECEIVED
			));

			mAdapter.insertItem(new Conversation("hello" + i,
					CityOfTwo.FLAG_TEXT | CityOfTwo.FLAG_SENT
			));
		}

		mChatInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (v.hasFocus()) {
					mConversationList.smoothScrollToPosition(
							mAdapter.getItemCount() - 1
					);
				}
			}
		});

		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String input = mChatInput.getText().toString().trim();
				if (!input.isEmpty()) {
					Conversation c = new Conversation(input,
							CityOfTwo.FLAG_TEXT | CityOfTwo.FLAG_SENT
					);
					mChatInput.getText().clear();
					mAdapter.insertItem(c);

					mConversationList.smoothScrollToPosition(
							mAdapter.getItemCount() - 1
					);

				}
			}
		});
	}
}
