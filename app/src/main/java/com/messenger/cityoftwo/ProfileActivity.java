package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import static com.messenger.cityoftwo.ChatAdapter.MODE_CHAT;
import static com.messenger.cityoftwo.ChatAdapter.MODE_PROFILE;

/**
 * Created by Aayush on 1/5/2017.
 */
public class ProfileActivity extends AppCompatActivity implements ChatAdapter.AdapterListener {

	private static final String TAG = "ProfileActivity";

	private final String ARG_CURRENT_GUEST = "current_guest";
	private final String ARG_PARAMS = "params";

	Contact mGuest;

	View mSendButton;
	EditText mChatInput;
	View mExtrasButton;
	View mInputContainer;
	View mOptionsContainer;
	View isTypingIndicator;

	ChatAdapter mAdapter;
	RecyclerView mConversationList;
	ChatLayoutManager mLayoutManager;

	Bundle params;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_profile);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mConversationList = (RecyclerView) findViewById(R.id.conversation_listview);
		mSendButton = findViewById(R.id.send_button);
		mChatInput = (EditText) findViewById(R.id.input_text);
		mExtrasButton = findViewById(R.id.chat_extras_button);
		mInputContainer = findViewById(R.id.chat_input_view);
		mOptionsContainer = findViewById(R.id.chat_options_container);
		isTypingIndicator = findViewById(R.id.chat_is_typing_indicator);

//		if (savedInstanceState == null) {
		params = getIntent().getExtras();
//		} else {
//			params = savedInstanceState.getBundle(ARG_PARAMS);
//		}

//		params.putParcelable(ARG_CURRENT_GUEST, new Contact(
//				"12",
//				"hello",
//				"michael",
//				false,
//				"ahoy",
//				"i want to break free",
//				new String[]{"this", "that"},
//				2,
//				true,
//				null));

		mGuest = params.getParcelable(ARG_CURRENT_GUEST);

		assert mGuest != null;

		mLayoutManager = new ChatLayoutManager(this);
		mLayoutManager.setStackFromEnd(true);

		mAdapter = new ChatAdapter(this, mGuest);
		mAdapter.setAdapterListener(this);

		mConversationList.setLayoutManager(mLayoutManager);
		mConversationList.setAdapter(mAdapter);

		switch (params.getInt(CityOfTwo.KEY_PROFILE_MODE, MODE_CHAT)) {
			case MODE_CHAT:
				mInputContainer.setVisibility(View.VISIBLE);
				mAdapter.setMode(MODE_CHAT);
				break;
			case MODE_PROFILE:
				mInputContainer.setVisibility(View.GONE);
				mAdapter.setMode(MODE_PROFILE);
				break;
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

		mExtrasButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOptionsContainer.getVisibility() == View.VISIBLE) {
					mOptionsContainer.setVisibility(View.GONE);
				} else {
					mOptionsContainer.setVisibility(View.VISIBLE);
					isTypingIndicator.setVisibility(View.INVISIBLE);
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
					mAdapter.insert(c);

					mConversationList.smoothScrollToPosition(
							mAdapter.getItemCount() - 1
					);
				}
			}
		});

		if (savedInstanceState != null) {
			Contact guest = savedInstanceState.getParcelable(ARG_CURRENT_GUEST);

			assert guest != null;

			if (guest.equals(mGuest)) {
				resumeConversation(savedInstanceState);
			} else {
				startNewConversation();
			}
		} else {
			startNewConversation();
		}

		ArrayList<Conversation> offlineConversation = params.getParcelableArrayList(
				CityOfTwo.KEY_BACKGROUND_MESSAGES
		);
		mAdapter.insertAll(offlineConversation);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ArrayList<Conversation> currentChat = mAdapter.getDataset();

		// guest
		// conversation
		// profile_mode

		outState.putParcelableArrayList(CityOfTwo.KEY_CURRENT_CHAT, currentChat);
		outState.putParcelable(ARG_CURRENT_GUEST, mGuest);

		super.onSaveInstanceState(outState);
	}

	private void startNewConversation() {
		Log.i(TAG, "Starting new conversation");
		mAdapter.insert(new Conversation("start",
				CityOfTwo.FLAG_START
		));
		mAdapter.insert(new Conversation("end",
				CityOfTwo.FLAG_END
		));
		ArrayList<Conversation> offlineMessages = params.getParcelableArrayList(
				CityOfTwo.KEY_CURRENT_CHAT
		);

		if (offlineMessages != null)
			mAdapter.insertAll(offlineMessages);
	}

	private void resumeConversation(Bundle savedInstanceState) {
		Log.i(TAG, "Loaded old conversation");
		ArrayList<Conversation> oldConversation = savedInstanceState.getParcelableArrayList(
				CityOfTwo.KEY_CURRENT_CHAT
		);

		mAdapter.insertAll(oldConversation);

	}
}
