package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.messenger.cityoftwo.CityOfTwo.FLAG_PROFILE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_IS_TYPING;
import static com.messenger.cityoftwo.CityOfTwo.KEY_LAST_SEEN;
import static com.messenger.cityoftwo.FacebookHelper.getFacebookPageURI;

/**
 * Created by Aayush on 1/5/2017.
 */
public class ProfileActivity extends ChatListenerPumpedActivity implements ChatAdapter.ChatEventListener {

	public static final String ARG_CURRENT_GUEST = "current_guest";
	public static final String ARG_CURRENT_CHAT = "current_chat";
	public static final String ARG_CHATROOM_ID = "chatroom_id";
	private static final String TAG = "ProfileActivity";

	private final String ARG_PARAMS = "params";

	Contact mGuest;

	View mSendButton;
	EditText mChatInput;
	View mExtrasButton;
	View mInputContainer;
	View mOptionsContainer;
	View isTypingIndicator;

	View mBackgroundContainer;

	ChatAdapter mAdapter;
	RecyclerView mConversationList;
	ChatLayoutManager mLayoutManager;

	OptionButtonHolder mNewChat;
	OptionButtonHolder mSave;
	OptionButtonHolder mReveal;
	OptionButtonHolder mFacebook;
	OptionButtonHolder mFilters;
	OptionButtonHolder mRefer;

	View.OnClickListener saveContactClickListener;
	View.OnClickListener revealProfileClickListener;
	View.OnClickListener viewFacebookProfileClickListener;
	View.OnClickListener newChatClickListener;
	View.OnClickListener changeFiltersClickListener;
	View.OnClickListener referClickListener;

	BroadcastReceiver mChatBroadcastReceiver;

	Bundle params;
	boolean isShowingTyping = false;
	private Runnable seenSignalRunnable;
	private Handler seenSignalHandler;
	private Handler typingSignalHandler;
	private Runnable typingSignalRunnable;
	private String mInputBuffer = "";
	private boolean isTypingSignalPending;

	private ContactsFragment mNewChatFragment;
	private int mChatroomId;


	@Override
	protected int getContentLayout() {
		return R.layout.activity_profile;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mConversationList = (RecyclerView) findViewById(R.id.conversation_listview);
		mSendButton = findViewById(R.id.send_button);
		mChatInput = (EditText) findViewById(R.id.input_text);
		mExtrasButton = findViewById(R.id.chat_extras_button);
		mInputContainer = findViewById(R.id.chat_input_view);
		mOptionsContainer = findViewById(R.id.chat_options_container);
		isTypingIndicator = findViewById(R.id.chat_is_typing_indicator);

		mBackgroundContainer = findViewById(R.id.background_container);
		mBackgroundContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOptionsContainer.getVisibility() == View.VISIBLE) hideOptionsContainer();
			}
		});

		hideOptionsContainer();

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

		mExtrasButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOptionsContainer.getVisibility() == View.VISIBLE) {
					hideOptionsContainer();
				} else {
					showOptionsContainer();
				}
			}
		});

		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String input = mChatInput.getText().toString().trim();
				if (!input.isEmpty()) {
					final Conversation c = new Conversation(input,
							CityOfTwo.FLAG_TEXT | CityOfTwo.FLAG_SENT
					);
					mChatInput.getText().clear();

					sendMessage(c);
				}
			}
		});

		mChatInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!isTypingSignalPending) {
					mInputBuffer = s.toString();
					isTypingSignalPending = true;

					sendTypingSignal();
				}
			}
		});

		boolean resume = false;
		if (savedInstanceState != null) {
			mGuest = savedInstanceState.getParcelable(ARG_CURRENT_GUEST);
			mChatroomId = savedInstanceState.getInt(ARG_CHATROOM_ID);
		} else {
			mGuest = params.getParcelable(ARG_CURRENT_GUEST);
			mChatroomId = params.getInt(ARG_CHATROOM_ID);
		}

		mLayoutManager = new ChatLayoutManager(this);
		mLayoutManager.setStackFromEnd(true);

		mAdapter = new ChatAdapter(this, mGuest, mChatroomId);
		mAdapter.setChatEventListener(this);

		mConversationList.setLayoutManager(mLayoutManager);

		OverScrollDecoratorHelper.setUpOverScroll(mConversationList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

		mConversationList.setAdapter(mAdapter);

		int lastChatRoomId = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).getInt(CityOfTwo.KEY_LAST_CHATROOM, -1);
		if (savedInstanceState == null) {
			startNewConversation();
		} else {
			if (lastChatRoomId == -1 || lastChatRoomId != mChatroomId) startNewConversation();
			else resumeConversation(savedInstanceState);
		}

		isTypingIndicator.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				isTypingIndicator.getViewTreeObserver().removeOnPreDrawListener(this);

				hideTypingIndicator(false);

				return true;
			}
		});

		ArrayList<Conversation> offlineConversation = params.getParcelableArrayList(
				CityOfTwo.KEY_BACKGROUND_MESSAGES
		);

		mAdapter.insertAll(offlineConversation);

		mChatBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				switch (action) {
					case CityOfTwo.ACTION_IS_TYPING: {
						// handle is_typing
						SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
						Boolean isTyping = sp.getBoolean(KEY_IS_TYPING, false);

						sp.edit().remove(KEY_IS_TYPING).apply();

						if (isTyping) {
							showTypingIndicator(true);
							isShowingTyping = true;
						} else {
							isShowingTyping = false;
						}
					}
					break;
					case CityOfTwo.ACTION_LAST_SEEN:
						// handle last_seen
						SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
						Long lastSeen = sp.getLong(KEY_LAST_SEEN, -1);
						sp.edit().remove(KEY_LAST_SEEN).apply();

						mAdapter.setLastSeen(lastSeen);
						break;
					case CityOfTwo.ACTION_END_CHAT:
						// handle end_chat
						new AlertDialog.Builder(ProfileActivity.this, R.style.AppTheme_Dialog)
								.setMessage("Stranger has left the chat.")
								.setPositiveButton("New Chat", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										onNewChat();
									}
								})
								.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										finish();
									}
								}).show();
						break;
					case CityOfTwo.ACTION_NEW_MESSAGE:
						// handle new_message
						Bundle data = intent.getExtras();
						onNewMessageReceived(data);
						break;
				}
			}
		};

		setupOptionButtons();
	}

	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mChatBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		int chatRoomId = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getInt(CityOfTwo.KEY_LAST_CHATROOM, -1);

		if (chatRoomId == -1) {
			finish();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(CityOfTwo.ACTION_IS_TYPING);
		filter.addAction(CityOfTwo.ACTION_LAST_SEEN);
		filter.addAction(CityOfTwo.ACTION_END_CHAT);
		filter.addAction(CityOfTwo.ACTION_NEW_MESSAGE);

		LocalBroadcastManager.getInstance(this).registerReceiver(mChatBroadcastReceiver, filter);

		sendSeenSignal();

		if (mChatroomId != -1) loadBackgroundMessages();
	}

	@Override
	int getActivityCode() {
		return CityOfTwo.ACTIVITY_PROFILE;
	}

	@Override
	protected void onStart() {
		super.onStart();

		setupHandlers();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ArrayList<Conversation> currentChat = mAdapter.getDataset();

		// guest
		// conversation
		// profile_mode

		outState.putParcelableArrayList(ARG_CURRENT_CHAT, currentChat);
		outState.putParcelable(ARG_CURRENT_GUEST, mGuest);
		outState.putInt(ARG_CHATROOM_ID, mChatroomId);

		super.onSaveInstanceState(outState);
	}

	public void hideLater(int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				hideOptionsContainer();
			}
		}, delay);
	}

	private void hideOptionsContainer() {
		mOptionsContainer.setVisibility(View.GONE);
		mBackgroundContainer.setVisibility(View.GONE);
	}

	private void showOptionsContainer() {
		mOptionsContainer.setVisibility(View.VISIBLE);
		mBackgroundContainer.setVisibility(View.VISIBLE);
	}

	private void sendTypingSignal() {
		sendTypingRequest(true);
		typingSignalHandler.postDelayed(typingSignalRunnable, 2000);
	}

	private void setupHandlers() {
		seenSignalHandler = new Handler();
		seenSignalRunnable = new Runnable() {
			@Override
			public void run() {
				sendSeenSignal();
			}
		};

		typingSignalHandler = new Handler();
		typingSignalRunnable = new Runnable() {
			@Override
			public void run() {
				String newBuffer = mChatInput.getText().toString();
				if (mChatInput.hasFocus() && (!mInputBuffer.equals(newBuffer))) {
					mInputBuffer = newBuffer;
					typingSignalHandler.postDelayed(this, 2000);
				} else sendTypingRequest(false);
			}
		};
	}

	private void sendTypingRequest(final boolean isTyping) {
		final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String typing_request = getString(R.string.url_is_typing);

		JSONObject j = new JSONObject();
		try {
			j.put(CityOfTwo.HEADER_IS_TYPING, isTyping);
			j.put(CityOfTwo.HEADER_CHATROOM_ID, mChatroomId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String[] Path = {CityOfTwo.API, typing_request};

		HttpHandler typingHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
			@Override
			protected void onPostExecute() {
				if (!isTyping) isTypingSignalPending = false;
			}
		};

		String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		typingHttpHandler.addHeader("Authorization", token);

		typingHttpHandler.execute();
	}

	private void endCurrentChat() {
		final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String dump_chat = getString(R.string.url_chat_end);

		JSONObject j = new JSONObject();
		try {
			j.put(CityOfTwo.HEADER_CHATROOM_ID, mChatroomId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String[] Path = {CityOfTwo.API, dump_chat};

		HttpHandler dumpHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
			@Override
			protected void onPostExecute() {
				sp.edit().remove(CityOfTwo.KEY_LAST_CHATROOM).apply();
			}
		};

		String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		dumpHttpHandler.addHeader("Authorization", token);

		dumpHttpHandler.execute();
	}

	private void onNewMessageReceived(Bundle data) {
		String text = data.getString(CityOfTwo.KEY_MESSAGE_DATA);
		Integer flags = data.getInt(CityOfTwo.KEY_MESSAGE_FLAGS);

		Long time = data.getLong(CityOfTwo.KEY_MESSAGE_TIME);

		Log.i(TAG, "Message received: " + text);

		final Conversation c = new Conversation(text, flags, time);

		if (c.containsFlag(FLAG_PROFILE)) {
			new FacebookHelper(this, text, mChatroomId, "name") {
				private void setGuest(String name) {
					mAdapter.setGuest(mGuest);

					int position = mAdapter.insert(c);
					mConversationList.smoothScrollToPosition(position);
				}

				@Override
				public void onResponse(String response) {
					setGuest(response);
				}


				@Override
				public void onError() {
					setGuest("Facebook User");
				}
			}.execute();
		} else {
			int position = mAdapter.insert(c);
			mConversationList.smoothScrollToPosition(position);
		}

		seenSignalHandler.removeCallbacksAndMessages(null);
		seenSignalHandler.postDelayed(seenSignalRunnable, 2000);
	}

	private void sendSeenSignal() {
		final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String seen = getString(R.string.url_last_seen);

		JSONObject j = new JSONObject();
		try {
			j.put(CityOfTwo.HEADER_LAST_SEEN, System.currentTimeMillis());
			j.put(CityOfTwo.HEADER_CHATROOM_ID, mChatroomId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String[] Path = {CityOfTwo.API, seen};

		HttpHandler seenHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j);

		String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		seenHttpHandler.addHeader("Authorization", token);

		seenHttpHandler.execute();
	}

	private void hideTypingIndicator(boolean animate) {

		int translationY = isTypingIndicator.getHeight() + (int) CityOfTwo.dpToPixel(this, 10);

		if (animate) translateView(isTypingIndicator, translationY, null, new Runnable() {
			@Override
			public void run() {
				isTypingIndicator.setAlpha(1);
			}
		});
		else {
			isTypingIndicator.setTranslationY(translationY);
			isTypingIndicator.setAlpha(1);
		}
	}

	private void showTypingIndicator(boolean animate) {
		if (animate) translateView(isTypingIndicator, 0, null, new Runnable() {
			@Override
			public void run() {
				blinkTypingIndicator();
			}
		});
		else {
			isTypingIndicator.setTranslationX(0);
			blinkTypingIndicator();
		}
	}
	
	private void blinkTypingIndicator() {
		isTypingIndicator.animate()
				.alpha(0)
				.withEndAction(new Runnable() {
					@Override
					public void run() {
						if (isShowingTyping) {
							isTypingIndicator.animate()
									.alpha(1)
									.withEndAction(new Runnable() {
										@Override
										public void run() {
											blinkTypingIndicator();
										}
									});
						} else {
							hideTypingIndicator(false);
						}
					}
				});
	}
	
	private void translateView(View view, int translation, Runnable startAction, Runnable endAction) {
		view.animate().translationY(translation).withStartAction(startAction).withEndAction(endAction);
	}

	private void setupOptionButtons() {
		mNewChat = new OptionButtonHolder(findViewById(R.id.message));
		mSave = new OptionButtonHolder(findViewById(R.id.save));
		mReveal = new OptionButtonHolder(findViewById(R.id.reveal));
		mFacebook = new OptionButtonHolder(findViewById(R.id.profile));
		mFilters = new OptionButtonHolder(findViewById(R.id.filters));
		mRefer = new OptionButtonHolder(findViewById(R.id.refer));

		if (mGuest.isFriend) {
			mSave.setText("Remove from contacts");
			mSave.setIcon(R.drawable.ic_unsave_light);
		} else {
			mSave.setText("Add as contact");
			mSave.setIcon(R.drawable.ic_send_request_light);
		}

		mNewChat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLater(100);
				onNewChat();
			}
		});
		mSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLater(100);

				if (mGuest.isFriend) onRemoveContact();
				else onSaveContact();
			}
		});
		mReveal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLater(100);
				onRevealContact();
			}
		});
		mFacebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLater(100);
				onViewProfile(mGuest.fid);
			}
		});
		mFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLater(100);
				onChangeFilters();
			}
		});
		mRefer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideLater(100);
				onReferCoyRudy();
			}
		});
	}

	private void showMessage(String errorMessage) {
		final Toast popup = new Toast(this);

		View popupView = LayoutInflater.from(this).inflate(
				R.layout.popup,
				null,
				false
		);

		TextView message = (TextView) popupView.findViewById(R.id.text);
		message.setText(errorMessage);

		popup.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, (int) CityOfTwo.dpToPixel(this, 56 + 5));

		popup.setDuration(Toast.LENGTH_LONG);
		popup.setView(popupView);

		popup.show();
	}

	private int sendMessage(Conversation c) {
		final String value = c.getText().replaceFirst("\\s+$", "");

		final int position = mAdapter.insert(c);

		mConversationList.smoothScrollToPosition(
				position
		);

		JSONObject params = new JSONObject();

		String send_message = "";
		try {
			send_message = getString(R.string.url_send_message);
			params.put(CityOfTwo.HEADER_CHATROOM_ID, mChatroomId);

			params.put(CityOfTwo.HEADER_DATA, value);
			params.put(CityOfTwo.HEADER_FLAGS, c.getFlags());
			params.put(CityOfTwo.HEADER_TIME, c.getTime());
			params.put(CityOfTwo.HEADER_TO, mGuest.id);
		} catch (JSONException e) {
			e.printStackTrace();
		}


		String[] Path = {CityOfTwo.API, send_message};

		HttpHandler chatHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, params) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject Response = new JSONObject(response);

					Boolean status = Response.getBoolean("parsadi");

					if (!status)
						onFailure(getResponseStatus());
					else {
						mAdapter.setStatus(position, ChatAdapter.STATUS_SENT);
					}


				} catch (Exception e) {
					onFailure(getResponseStatus());
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Integer status) {
				mAdapter.setStatus(position, ChatAdapter.STATUS_ERROR);
			}
		};

		String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		chatHttpHandler.addHeader("Authorization", token);
		chatHttpHandler.execute();

		return position;
	}

	private void removeFriend() {

	}

	private void addFriend() {

	}

	private void startNewConversation() {
		Log.i(TAG, "Starting new conversation");

//		if (mGuest == null) {
//			DatabaseHelper db = new DatabaseHelper(this);
//			mGuest = db.loadGuest(mChatroomId);
//		}

		String guest = mGuest.toString();

		ArrayList<Conversation> offlineMessages = mGuest.lastMessages;
		if (offlineMessages == null) offlineMessages = new ArrayList<>();

		offlineMessages.add(new Conversation(guest,
				CityOfTwo.FLAG_START
		));
		offlineMessages.add(new Conversation("end",
				CityOfTwo.FLAG_END
		));

		DatabaseHelper db = new DatabaseHelper(this);

		mAdapter.insertAll(offlineMessages);
		mGuest.lastMessages.clear();

		if (mChatroomId != -1) mAdapter.insertAll(db.retrieveMessages(mChatroomId));

//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				mConversationList.smoothScrollToPosition(0);
//			}
//		}, 500);

	}

	@Override
	public void onBackPressed() {
		if (mOptionsContainer.getVisibility() == View.VISIBLE) {
			hideOptionsContainer();
			return;
		}

		dumpGuest();

		setResult(RESULT_OK);
		finish();
	}

	private void dumpGuest() {
		final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String token = sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		sp.edit().remove(CityOfTwo.KEY_LAST_CHATROOM).apply();

		new DumpHttpHandler(this, token, mChatroomId) {
			@Override
			public void onPostExecute() {

			}
		}.execute();
	}

	private void resumeConversation(Bundle savedInstanceState) {
		Log.i(TAG, "Loaded old conversation");
		ArrayList<Conversation> oldConversation = savedInstanceState.getParcelableArrayList(
				ARG_CURRENT_CHAT
		);

		if (mChatroomId != -1) loadBackgroundMessages();

		mAdapter.insertAll(oldConversation);

	}

	private void loadBackgroundMessages() {
		DatabaseHelper db = new DatabaseHelper(this);
		mAdapter.insertAll(db.retrieveMessages(mChatroomId));
		db.clearMessagesTable(mChatroomId);

	}

	private void onReferCoyRudy() {
		Utils.CoyRudy.referCoyRudy(ProfileActivity.this);
	}

	private void onChangeFilters() {
		startActivity(new Intent(ProfileActivity.this, FilterActivity.class));
	}

	private void onNewChat() {
		setupNewChatDialog();
	}

	private void setupNewChatDialog() {
		final String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		Bundle contactsArgs = new Bundle();
		contactsArgs.putString(ContactsFragment.ARG_TOKEN, token);
		int searchMode = ContactsFragment.SEARCH_MODE_CONTACTS | ContactsFragment.SEARCH_MODE_MATCHES;
		contactsArgs.putInt(ContactsFragment.ARG_SEARCH_MODE, searchMode);

		FragmentManager fm = getSupportFragmentManager();

		mNewChatFragment = ContactsFragment.newInstance(contactsArgs);

		final DialogFragmentWrapper wrapper = DialogFragmentWrapper.newInstance("New Chat");
		wrapper.setFragment(mNewChatFragment);

		mNewChatFragment.setListener(new ContactsFragment.ContactsFragmentListener() {
			@Override
			public void onContactSelected(Contact contact) {
				wrapper.dismiss();
				if (contact.equals(mGuest)) return;

				showProfile(contact);
			}

			@Override
			public void onContactsLoaded(int number) {
				wrapper.onItemsPrepared();
				if (number == 0) {
					wrapper.dismiss();
					new AlertDialog.Builder(ProfileActivity.this, R.style.AppTheme_Dialog)
							.setMessage("No one available for chat.")
							.setPositiveButton("Ok", null)
							.show();
				}
			}

			@Override
			public void onContactLoadError() {
				wrapper.dismiss();

				new AlertDialog.Builder(ProfileActivity.this, R.style.AppTheme_Dialog)
						.setMessage("Error while searching for available people")
						.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								setupNewChatDialog();
							}
						})
						.show();
			}

		});
		wrapper.show(fm);
	}

	private void showProfile(final Contact contact) {
		Bundle params = new Bundle();
		params.putParcelable(ARG_CURRENT_GUEST, contact);

		final ProfileFragment profileFragment = ProfileFragment.newInstance(params);
		profileFragment.setEventListener(new ProfileFragment.ProfileFragmentListener() {
			@Override
			public void onEditProfile() {

			}

			@Override
			public void onChatRequestSent(int requestId) {
				dumpGuest();
				showSentRequest(contact, requestId);
				profileFragment.dismiss();
			}

			@Override
			public void onOfflineMessageSent() {
				profileFragment.dismiss();
				showMessage("Message sent to " + contact.nickName);
			}

			@Override
			public void onViewProfile(String fid) {
				Uri uri = FacebookHelper.getFacebookPageURI(ProfileActivity.this, fid);

				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		FragmentManager fm = getSupportFragmentManager();
		profileFragment.show(fm, "CONTACT");
	}

	@Override
	public void onConversationClicked(int postion, int status) {

	}

	@Override
	public void onEditNickname() {

	}

	@Override
	public void onSendMessage() {
		mInputContainer.setVisibility(View.VISIBLE);
		mChatInput.requestFocus();

		mConversationList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
	}

	@Override
	public void onSaveContact() {
		Conversation c = new Conversation(
				"",
				CityOfTwo.FLAG_REQUEST | CityOfTwo.FLAG_SENT
		);

		sendMessage(c);
	}

	@Override
	public void onRevealContact() {

		Conversation c = new Conversation(
				"",
				CityOfTwo.FLAG_PROFILE | CityOfTwo.FLAG_SENT
		);

		sendMessage(c);
	}

	@Override
	public void onViewProfile(String fbid) {
		if (!mGuest.fid.equals(fbid) || mGuest.hasRevealed)
			showFacebookProfile(fbid);
		else
			showMessage("Cannot view profile of users until they reveal themselves to you.");
	}

	@Override
	public void onViewCommonLikes() {
		String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		Bundle args = new Bundle();
		args.putString(CommonLikesFragmentDialog.ARG_TOKEN, token);
		args.putParcelable(CommonLikesFragmentDialog.ARG_CURRENT_GUEST, mGuest);

		FragmentManager fm = getSupportFragmentManager();

		DialogFragmentWrapper wrapper = DialogFragmentWrapper.newInstance("Common Likes");

		CommonLikesFragmentDialog commonLikesFragment = CommonLikesFragmentDialog.newInstance();
		commonLikesFragment.setArguments(args);

		wrapper.setFragment(commonLikesFragment);
		wrapper.show(fm);
	}

	@Override
	public void onBlockProfile() {

	}

	@Override
	public void onAcceptRequest(final boolean accept) {
		String host = CityOfTwo.HOST;

		String[] path = new String[]{
				CityOfTwo.API,
				getString(R.string.url_accept_request)
		};

		JSONObject j = new JSONObject();

		try {
			j.put(CityOfTwo.KEY_FRIEND_ID, mGuest.id);
			j.put(CityOfTwo.KEY_RESPONSE, accept);
		} catch (JSONException e) {
			e.printStackTrace();
		}


		HttpHandler handler = new HttpHandler(host, path, HttpHandler.POST, j) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);
					boolean success = j.getBoolean("parsadi");

					if (success) {
						mAdapter.setRequestResponse(accept);
					}
				} catch (JSONException e) {
					mAdapter.setRequestResponse(null);
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Integer status) {
				mAdapter.setRequestResponse(null);
			}
		};
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		handler.addHeader("Authorization", token);

	}

	@Override
	public void onRemoveContact() {
		String host = CityOfTwo.HOST;

		String[] path = new String[]{
				CityOfTwo.API,
				getString(R.string.url_remove_from_contact)
		};

		JSONObject j = new JSONObject();

		try {
			j.put(CityOfTwo.KEY_FRIEND_ID, mGuest.id);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HttpHandler handler = new HttpHandler(host, path, HttpHandler.POST, j) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);
					boolean success = j.getBoolean("parsadi");

					if (success) {
						mAdapter.notifyItemChanged(0);
						setupOptionButtons();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		handler.addHeader("Authorization", token);
	}

	private void showFacebookProfile(String fbid) {
		Uri uri = getFacebookPageURI(this, fbid);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		this.startActivity(intent);
	}
}
