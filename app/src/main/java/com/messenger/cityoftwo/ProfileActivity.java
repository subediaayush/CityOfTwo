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
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.messenger.cityoftwo.CityOfTwo.FLAG_SENT;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CHATROOM_ID;
import static com.messenger.cityoftwo.CityOfTwo.KEY_IS_TYPING;
import static com.messenger.cityoftwo.CityOfTwo.KEY_LAST_SEEN;
import static com.messenger.cityoftwo.CityOfTwo.getFacebookPageURI;
import static com.messenger.cityoftwo.ContactsFragment.MODE_OFFLINE;
import static com.messenger.cityoftwo.ContactsFragment.MODE_ONLINE;

/**
 * Created by Aayush on 1/5/2017.
 */
public class ProfileActivity extends AppCompatActivity implements ChatAdapter.ChatEventListener {

	public static final String ARG_PROFILE_MODE = "profile_mode";
	public static final String ARG_CURRENT_GUEST = "current_guest";
	public static final String ARG_OFFLINE_MESSAGES = "offline_messsages";
	public static final String ARG_CURRENT_CHAT = "current_chat";
	public static final String ARG_CURRENT_GUEST_POSITION = "current_guest_position";
	private static final String TAG = "ProfileActivity";
	private static final String ARG_OFFLINE_MESSAGES_SENT = "offline_messages_sent";
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
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;
	private int mPosition;
	private int mChatMode;

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

		mGuest = params.getParcelable(ARG_CURRENT_GUEST);

		assert mGuest != null;

		mLayoutManager = new ChatLayoutManager(this);
		mLayoutManager.setStackFromEnd(true);

		mAdapter = new ChatAdapter(this, mGuest);
		mAdapter.setChatEventListener(this);

		mConversationList.setLayoutManager(mLayoutManager);

		OverScrollDecoratorHelper.setUpOverScroll(mConversationList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

		mConversationList.setAdapter(mAdapter);

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

					boolean offlineMessageSent = false;
					for (int i = 0; i < mAdapter.getItemCount(); i++) {
						if (mAdapter.get(i).containsFlag(FLAG_SENT)) {
							offlineMessageSent = true;
							break;
						}
					}

					if (mChatMode == MODE_OFFLINE && offlineMessageSent) {
						new AlertDialog.Builder(ProfileActivity.this, R.style.AppTheme_Dialog)
								.setTitle("Send message to " + mGuest.nickName)
								.setMessage("Sending offline message will delete your older message. Are you sure you want to do this?")
								.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										sendMessage(c);
									}
								})
								.setNegativeButton("No", null)
								.show();
					} else {
						sendMessage(c);
					}
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
				if (!isTypingSignalPending && mChatMode == MODE_ONLINE) {
					mInputBuffer = s.toString();
					isTypingSignalPending = true;

					sendTypingSignal();
				}
			}
		});

		if (savedInstanceState != null) {
			Contact guest = savedInstanceState.getParcelable(ARG_CURRENT_GUEST);
			mPosition = savedInstanceState.getInt(ARG_CURRENT_GUEST_POSITION);
			mChatMode = savedInstanceState.getInt(ARG_PROFILE_MODE);

			assert guest != null;

			if (guest.equals(mGuest)) {
				resumeConversation(savedInstanceState);
			} else {
				startNewConversation();
			}
		} else {
			mChatMode = params.getInt(ARG_PROFILE_MODE);
			startNewConversation();
		}


		ArrayList<Conversation> offlineConversation = params.getParcelableArrayList(
				CityOfTwo.KEY_BACKGROUND_MESSAGES
		);

		if (mChatMode == MODE_OFFLINE) {
			showMessage(mGuest.nickName + " is not available right now." +
					" However you can leave them an offline message.");
		}

		isTypingIndicator.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				isTypingIndicator.getViewTreeObserver().removeOnPreDrawListener(this);

				hideTypingIndicator(false);

				return true;
			}
		});

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
								.setTitle("Stranger has left the chat")
								.setMessage("Start a new chat?")
								.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										onNewChat();
									}
								})
								.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
	protected void onStart() {
		super.onStart();

		IntentFilter filter = new IntentFilter();
		filter.addAction(CityOfTwo.ACTION_IS_TYPING);
		filter.addAction(CityOfTwo.ACTION_LAST_SEEN);
		filter.addAction(CityOfTwo.ACTION_END_CHAT);
		filter.addAction(CityOfTwo.ACTION_NEW_MESSAGE);

		LocalBroadcastManager.getInstance(this).registerReceiver(mChatBroadcastReceiver, filter);

		setupHandlers();
	}

	@Override
	protected void onStop() {
		super.onStop();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mChatBroadcastReceiver);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ArrayList<Conversation> currentChat = mAdapter.getDataset();

		// guest
		// conversation
		// profile_mode

		outState.putParcelableArrayList(ARG_CURRENT_CHAT, currentChat);
		outState.putParcelable(ARG_CURRENT_GUEST, mGuest);
		outState.putInt(ARG_CURRENT_GUEST_POSITION, mPosition);

		super.onSaveInstanceState(outState);
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
			j.put(CityOfTwo.HEADER_CHATROOM_ID, sp.getInt(KEY_CHATROOM_ID, -1));
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
			j.put(CityOfTwo.HEADER_CHATROOM_ID, sp.getInt(KEY_CHATROOM_ID, -1));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String[] Path = {CityOfTwo.API, dump_chat};

		HttpHandler dumpHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
			@Override
			protected void onPostExecute() {
				sp.edit().remove(CityOfTwo.KEY_CHATROOM_ID).apply();
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

		Conversation c = new Conversation(text, flags, time);
		c.removeFlag(CityOfTwo.FLAG_SENT);
		c.addFlag(CityOfTwo.FLAG_RECEIVED);

		int position = mAdapter.insert(c);

		mConversationList.smoothScrollToPosition(position);

		seenSignalHandler.removeCallbacksAndMessages(null);
		seenSignalHandler.postDelayed(seenSignalRunnable, 2000);
	}

	private void sendSeenSignal() {
		final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		String seen = getString(R.string.url_last_seen);

		JSONObject j = new JSONObject();
		try {
			j.put(CityOfTwo.HEADER_LAST_SEEN, System.currentTimeMillis());
			j.put(CityOfTwo.HEADER_CHATROOM_ID, sp.getInt(KEY_CHATROOM_ID, -1));
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
				Utils.hideLater(mOptionsContainer, 100);
				onNewChat();
			}
		});
		mSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.hideLater(mOptionsContainer, 100);

				if (mGuest.isFriend) onRemoveContact();
				else onSaveContact();
			}
		});
		mReveal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.hideLater(mOptionsContainer, 100);
				onRevealContact();
			}
		});
		mFacebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.hideLater(mOptionsContainer, 100);
				onViewProfile(mGuest.fid);
			}
		});
		mFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.hideLater(mOptionsContainer, 100);
				onChangeFilters();
			}
		});
		mRefer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.hideLater(mOptionsContainer, 100);
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
	
	private void sendMessage(Conversation c) {
		final String value = c.getText().replaceFirst("\\s+$", "");

		final int position = mAdapter.insert(c);

		mConversationList.smoothScrollToPosition(
				position
		);

		JSONObject params = new JSONObject();

		try {
			params.put(CityOfTwo.HEADER_DATA, value);
			params.put(CityOfTwo.HEADER_FLAGS, c.getFlags());
			params.put(CityOfTwo.HEADER_TIME, c.getTime());
			params.put(CityOfTwo.HEADER_TO, mGuest.id);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String send_message;
		if (mChatMode == MODE_ONLINE) send_message = getString(R.string.url_send_message);
		else send_message = getString(R.string.url_send_offline_message);


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
						if (mChatMode == MODE_OFFLINE) {
							for (int i = 0; i < position; i++) {
								if (mAdapter.get(i).containsFlag(CityOfTwo.FLAG_SENT)) {
									mAdapter.removeAt(i);
									break;
								}
							}
						}
					}


				} catch (Exception e) {
					onFailure(getResponseStatus());
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Integer status) {
				if (mChatMode == MODE_OFFLINE) {
					mAdapter.removeAt(position);
					showMessage("Message sending failed!");
				} else {
					mAdapter.setStatus(position, ChatAdapter.STATUS_ERROR);
				}
			}
		};

		String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		chatHttpHandler.addHeader("Authorization", token);
		chatHttpHandler.execute();
	}
	
	private void removeFriend() {

	}

	private void addFriend() {

	}

	private void startNewConversation() {
		Log.i(TAG, "Starting new conversation");
		String guest = mGuest.toString();

		ArrayList<Conversation> offlineMessages = mGuest.lastMessages;
		if (offlineMessages == null) offlineMessages = new ArrayList<>();

		offlineMessages.add(new Conversation(guest,
				CityOfTwo.FLAG_START
		));
		offlineMessages.add(new Conversation("end",
				CityOfTwo.FLAG_END
		));

		mAdapter.insertAll(offlineMessages);
		mGuest.lastMessages.clear();

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

		setResultAsGuest();
		finish();
	}

	private void setResultAsGuest() {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(ARG_CURRENT_GUEST, mGuest);
		resultIntent.putExtra(ARG_CURRENT_GUEST_POSITION,
				getIntent().getIntExtra(ARG_CURRENT_GUEST_POSITION, -1));

		setResult(RESULT_OK, resultIntent);

	}

	private void resumeConversation(Bundle savedInstanceState) {
		Log.i(TAG, "Loaded old conversation");
		ArrayList<Conversation> oldConversation = savedInstanceState.getParcelableArrayList(
				ARG_CURRENT_CHAT
		);

		mAdapter.insertAll(oldConversation);

	}

	private void onReferCoyRudy() {
		hideOptionsContainer();
		Utils.CoyRudy.referCoyRudy(ProfileActivity.this);
	}

	private void onChangeFilters() {
		hideOptionsContainer();
		startActivity(new Intent(ProfileActivity.this, FilterActivity.class));
	}

	private void onNewChat() {
		hideOptionsContainer();
		setupNewChatDialog();
	}

	private void setupNewChatDialog() {
		String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		Bundle contactsArgs = new Bundle();
		contactsArgs.putString(ContactsFragment.ARG_TOKEN, token);
		int searchMode = ContactsFragment.SEARCH_MODE_CONTACTS | ContactsFragment.SEARCH_MODE_MATCHES;
		contactsArgs.putInt(ContactsFragment.ARG_SEARCH_MODE, searchMode);

		FragmentManager fm = getSupportFragmentManager();

		mNewChatFragment = ContactsFragment.newInstance(contactsArgs);

		final DialogWrapper wrapper = DialogWrapper.newInstance("New Chat");
		wrapper.setFragment(mNewChatFragment);

		mNewChatFragment.setListener(new ContactsFragment.ContactsFragmentListener() {
			@Override
			public void onContactSelected(Contact contact, int position) {
				wrapper.dismiss();

				if (contact.equals(mGuest)) return;

				Intent contactIntent = new Intent(ProfileActivity.this, ProfileActivity.class);

				contactIntent.putExtra(
						ProfileActivity.ARG_PROFILE_MODE,
						ContactsFragment.MODE_ONLINE
				);

				contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST, contact);
				contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST_POSITION, position);

				startActivityForResult(contactIntent, CityOfTwo.ACTIVITY_PROFILE);

				setResultAsGuest();
				finish();
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

	@Override
	public void onConversationClicked(int postion, int status) {

	}

	@Override
	public void onEditNickname() {

	}

	@Override
	public void onSendMessage() {
		params.putInt(ARG_PROFILE_MODE, MODE_ONLINE);
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
		args.putString(CommonLikesFragment.ARG_TOKEN, token);
		args.putParcelable(CommonLikesFragment.ARG_CURRENT_GUEST, mGuest);

		FragmentManager fm = getSupportFragmentManager();

		DialogWrapper wrapper = DialogWrapper.newInstance("Common Likes");

		CommonLikesFragment commonLikesFragment = CommonLikesFragment.newInstance();
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
