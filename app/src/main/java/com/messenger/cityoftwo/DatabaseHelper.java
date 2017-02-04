package com.messenger.cityoftwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Aayush on 7/19/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_NAME = "messenger.db";

	private static final String COLUMN_TIME = "time";
	private static final String COLUMN_FLAGS = "flags";
	private static final String COLUMN_MESSAGE = "name";
	private static final String COLUMN_CHATROOM_ID = "chatroom_id";
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_NICKNAME = "nickname";
	private static final String COLUMN_FID = "fid";
	private static final String COLUMN_HAS_REVEALED = "has_revealed";
	private static final String COLUMN_STATUS = "status";
	private static final String COLUMN_COMMON_LIKES = "common_likes";
	private static final String COLUMN_TOP_LIKES = "top_likes";
	private static final String COLUMN_IS_FRIEND = "is_friend";

	private static final String TABLE_GUESTS = "guests";
	private static final String TABLE_MESSAGES = "message";

	private static Patch PATCHES[] = new Patch[]{
			new Patch() {
				@Override
				public void apply(SQLiteDatabase db) {
					String query = String.format(
							"CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
									"%s INTEGER, %s VARCHAR, %s INTEGER, %s INTEGER);",
							TABLE_MESSAGES,
							COLUMN_ID,
							COLUMN_CHATROOM_ID,
							COLUMN_MESSAGE,
							COLUMN_FLAGS,
							COLUMN_TIME
					);
					db.execSQL(query);
				}

				@Override
				public void revert(SQLiteDatabase db) {
					String query = String.format("DROP TABLE %s", TABLE_MESSAGES);
					db.execSQL(query);
				}
			},
			new Patch() {
				@Override
				public void apply(SQLiteDatabase db) {
					String query = String.format(
							"CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
									"%s INTEGER, %s VARCHAR, %s INTEGER, %s INTEGER);",
							TABLE_GUESTS,
							COLUMN_ID,
							COLUMN_NAME,
							COLUMN_NICKNAME,
							COLUMN_FID,
							COLUMN_HAS_REVEALED,
							COLUMN_STATUS,
							COLUMN_COMMON_LIKES,
							COLUMN_TOP_LIKES,
							COLUMN_IS_FRIEND
					);

					db.execSQL(query);
				}

				@Override
				public void revert(SQLiteDatabase db) {

				}
			}
	};
	private Context context;

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
		this.context = context;
	}

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		List<String> queries = new ArrayList<>();

		queries.add(String.format(
				"CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s INTEGER, %s VARCHAR, %s INTEGER, %s INTEGER);",
				TABLE_MESSAGES,
				COLUMN_ID,
				COLUMN_CHATROOM_ID,
				COLUMN_MESSAGE,
				COLUMN_FLAGS,
				COLUMN_TIME
		));

		queries.add(String.format(
				"CREATE TABLE %s (%s INTEGER PRIMARY KEY, " +
						"%s VARCHAR, %s VARCHAR, %s VARCHAR, %s INTEGER, " +
						"%s VARCHAR, %s INTEGER, %s VARCHAR, %s INTEGER);",
				TABLE_GUESTS,
				COLUMN_ID,
				COLUMN_NAME,
				COLUMN_NICKNAME,
				COLUMN_FID,
				COLUMN_HAS_REVEALED,
				COLUMN_STATUS,
				COLUMN_COMMON_LIKES,
				COLUMN_TOP_LIKES,
				COLUMN_IS_FRIEND
		));

		for (String query : queries) {
			db.execSQL(query);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (int i = oldVersion; i < newVersion; i++)
			PATCHES[i].apply(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (int i = oldVersion; i < newVersion; i--)
			PATCHES[i].revert(db);
	}

	public void insertMessages(Integer chatroomId, ArrayList<Conversation> conversations) {
		SQLiteDatabase db = getWritableDatabase();

		for (Conversation c : conversations) {
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_CHATROOM_ID, chatroomId);
			cv.put(COLUMN_MESSAGE, c.getText());
			cv.put(COLUMN_FLAGS, c.getFlags());
			cv.put(COLUMN_TIME, c.getTime());

			try {
				db.insertOrThrow(TABLE_MESSAGES, null, cv);
			} catch (SQLiteException e) {
				Log.e("Error on table " + TABLE_MESSAGES, e.toString());
			}
		}
		db.close();
	}

	public void insertMessage(Integer chatroomId, Conversation conversation) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put(COLUMN_CHATROOM_ID, chatroomId);
		cv.put(COLUMN_MESSAGE, conversation.getText());
		cv.put(COLUMN_FLAGS, conversation.getFlags());
		cv.put(COLUMN_TIME, conversation.getTime());

		try {
			db.insertOrThrow(TABLE_MESSAGES, null, cv);
		} catch (SQLiteException e) {
			Log.e("Error on table " + TABLE_MESSAGES, e.toString());
		}

		db.close();
	}

	public ArrayList<Conversation> retrieveMessages(Integer chatroomId) {
		SQLiteDatabase db = getReadableDatabase();

		ArrayList<Conversation> conversations = new ArrayList<>();

		String query = String.format(Locale.getDefault(),
				"SELECT * FROM %s WHERE %s = '%d' ORDER BY %s ASC;",
				TABLE_MESSAGES,
				COLUMN_CHATROOM_ID,
				chatroomId,
				COLUMN_TIME
		);

		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();
		while (!c.isAfterLast())
			try {
				int cId = c.getInt(c.getColumnIndex(COLUMN_CHATROOM_ID));
				String text = c.getString(c.getColumnIndex(COLUMN_MESSAGE));
				int flags = c.getInt(c.getColumnIndex(COLUMN_FLAGS));
				long time = c.getInt(c.getColumnIndex(COLUMN_TIME));

				if (cId == chatroomId) conversations.add(new Conversation(text, flags, time));
				c.moveToNext();
			} catch (CursorIndexOutOfBoundsException e) {
				e.printStackTrace();
				break;
			}
		c.close();
		db.close();

		return conversations;
	}

	public boolean clearMessagesTable() {
		SQLiteDatabase db = getWritableDatabase();

		int id = db.delete(
				TABLE_MESSAGES,
				null,
				null
		);

		db.close();

		return id > 0;
	}

	public boolean clearMessagesTable(Integer chatroomId) {
		SQLiteDatabase db = getWritableDatabase();

		int id = db.delete(
				TABLE_MESSAGES,
				COLUMN_CHATROOM_ID + "=?",
				new String[]{chatroomId + ""}
		);

		db.close();

		return id > 0;
	}

	public void saveGuest(Contact guest) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put(COLUMN_ID, guest.id);
		cv.put(COLUMN_NAME, guest.name);
		cv.put(COLUMN_NICKNAME, guest.nickName);
		cv.put(COLUMN_FID, guest.fid);
		cv.put(COLUMN_HAS_REVEALED, guest.hasRevealed ? 1 : 0);
		cv.put(COLUMN_STATUS, guest.status);
		cv.put(COLUMN_COMMON_LIKES, guest.commonLikes);
		cv.put(COLUMN_TOP_LIKES, arrayToString(guest.topLikes));
		cv.put(COLUMN_IS_FRIEND, guest.isFriend ? 1 : 0);

		try {
			db.insertOrThrow(TABLE_GUESTS, null, cv);
		} catch (SQLiteException e) {
			Log.e("Error on table " + TABLE_GUESTS, e.toString());
		}

		db.close();
	}

	public Contact loadGuest(int guestId) {
		SQLiteDatabase db = getReadableDatabase();

		Contact guest = null;

		String query = String.format(Locale.getDefault(),
				"SELECT * FROM %s WHERE %s = '%d' LIMIT 1;",
				TABLE_GUESTS,
				COLUMN_ID,
				guestId
		);

		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();
		try {
			int id = c.getInt(c.getColumnIndex(COLUMN_ID));
			String name = c.getString(c.getColumnIndex(COLUMN_NAME));
			String nickname = c.getString(c.getColumnIndex(COLUMN_NICKNAME));
			String fid = c.getString(c.getColumnIndex(COLUMN_FID));
			boolean has_revealed = c.getInt(c.getColumnIndex(COLUMN_HAS_REVEALED)) == 1;
			String status = c.getString(c.getColumnIndex(COLUMN_STATUS));
			int common_likes = c.getInt(c.getColumnIndex(COLUMN_COMMON_LIKES));
			String[] top_likes = stringToArray(c.getString(c.getColumnIndex(COLUMN_TOP_LIKES)));
			boolean is_friend = c.getInt(c.getColumnIndex(COLUMN_IS_FRIEND)) == 1;
			if (guestId == id)
				guest = new Contact(id, fid, name, nickname, has_revealed, status, top_likes, common_likes, is_friend);
			c.moveToNext();
		} catch (CursorIndexOutOfBoundsException e) {
			e.printStackTrace();
		} finally {
			c.close();
			db.close();
		}

		return guest;
	}

	private String arrayToString(String[] array) {
		String output = "";
		for (String item : array)
			output += "," + item;

		return output.substring(1);
	}

	private String[] stringToArray(String array) {
		return array.split(",");
	}


	private interface Patch {
		void apply(SQLiteDatabase db);

		void revert(SQLiteDatabase db);
	}
}
