package com.messenger.cityoftwo;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import static com.messenger.cityoftwo.CityOfTwo.KEY_COMMON_LIKES;

public class ConversationActivity extends ConversationActivityBase {

    @Override
    protected void onUserOfflineReceived(Bundle data) {
        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                .remove(CityOfTwo.KEY_CHAT_PENDING)
                .remove(CityOfTwo.KEY_CHATROOM_ID)
                .remove(CityOfTwo.KEY_SESSION_ACTIVE)
                .apply();

        new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                .setTitle("Your last chat has ended")
                .setMessage("Start a new chat?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewChat();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitActivity(RESULT_CANCELED);

                    }
                }).show();
    }

    @Override
    protected void onChatBeginReceived(Bundle data) {
        new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                .setTitle("New Chat")
                .setMessage("You are now starting a new chat.")
                .setNeutralButton("Ok", null).show();

        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        String commonLikes = sp.getString(KEY_COMMON_LIKES, "");

        sp.edit().putBoolean(CityOfTwo.KEY_CHAT_PENDING, false).apply();

        if (mConversationAdapter.isWaiting())
            mConversationAdapter.hideWaitingDialog();

        setupNewChatWindow(commonLikes);

        mConversationAdapter.notifyDataSetChanged();
        mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());
    }

    @Override
    protected void onChatEndReceived(Bundle data) {
        new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                .setTitle("Stranger has left the chat")
                .setMessage("Start a new chat?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewChat();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitActivity(CityOfTwo.RESULT_EXIT_APP);
                    }
                }).show();
    }

    @Override
    protected void onNewMessageReceived(Bundle data) {
        String text = data.getString(CityOfTwo.KEY_TEXT);
        Integer flags = data.getInt(CityOfTwo.KEY_MESSAGE_FLAGS);
        long time = System.currentTimeMillis();

        Conversation c = new Conversation(text, flags, time);
        c.removeFlag(CityOfTwo.FLAG_SENT);
        c.addFlag(CityOfTwo.FLAG_RECEIVED);

        mConversationAdapter.insertItem(c);

        if (!mConversationAdapter.isLastVisible())
            mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemPosition(c));

    }

    @Override
    protected void onSendButtonClicked(View view) {
        final String bufferText = mInputText.getText().toString().replaceFirst("\\s+$", "");

        if (!bufferText.isEmpty()) {
            mInputText.setText("");
            Conversation conversation = new Conversation(
                    bufferText,
                    CityOfTwo.FLAG_SENT | CityOfTwo.FLAG_TEXT
            );
            sendMessage(conversation);
        }
    }



}
