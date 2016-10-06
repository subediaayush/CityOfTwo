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
import java.util.Locale;

/**
 * Created by Aayush on 7/19/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "messenger.db";
    private static Patch PATCHES[] = new Patch[]{
            new Patch() {
                @Override
                public void apply(SQLiteDatabase db) {
                    String query = String.format(
                            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "%s INTEGER, %s VARCHAR, %s INTEGER, %s INTEGER);",
                            CityOfTwo.TABLE_MESSAGES,
                            CityOfTwo.COLUMN_ID,
                            CityOfTwo.COLUMN_CHATROOM_ID,
                            CityOfTwo.COLUMN_MESSAGE,
                            CityOfTwo.COLUMN_FLAGS,
                            CityOfTwo.COLUMN_TIME
                    );
                    db.execSQL(query);
                }

                @Override
                public void revert(SQLiteDatabase db) {
                    String query = String.format("DROP TABLE %s", CityOfTwo.TABLE_MESSAGES);
                    db.execSQL(query);
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
        String query = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s INTEGER, %s VARCHAR, %s INTEGER, %s INTEGER);",
                CityOfTwo.TABLE_MESSAGES,
                CityOfTwo.COLUMN_ID,
                CityOfTwo.COLUMN_CHATROOM_ID,
                CityOfTwo.COLUMN_MESSAGE,
                CityOfTwo.COLUMN_FLAGS,
                CityOfTwo.COLUMN_TIME
        );
        db.execSQL(query);
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
            cv.put(CityOfTwo.COLUMN_CHATROOM_ID, chatroomId);
            cv.put(CityOfTwo.COLUMN_MESSAGE, c.getText());
            cv.put(CityOfTwo.COLUMN_FLAGS, c.getFlags());
            cv.put(CityOfTwo.COLUMN_TIME, c.getTime());

            try {
                db.insertOrThrow(CityOfTwo.TABLE_MESSAGES, null, cv);
            } catch (SQLiteException e) {
                Log.e("Error on table " + CityOfTwo.TABLE_MESSAGES, e.toString());
            }
        }
        db.close();
    }

    public void insertMessage(Integer chatroomId, Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(CityOfTwo.COLUMN_CHATROOM_ID, chatroomId);
        cv.put(CityOfTwo.COLUMN_MESSAGE, conversation.getText());
        cv.put(CityOfTwo.COLUMN_FLAGS, conversation.getFlags());
        cv.put(CityOfTwo.COLUMN_TIME, conversation.getTime());

        try {
            db.insertOrThrow(CityOfTwo.TABLE_MESSAGES, null, cv);
        } catch (SQLiteException e) {
            Log.e("Error on table " + CityOfTwo.TABLE_MESSAGES, e.toString());
        }

        db.close();
    }

    public ArrayList<Conversation> retrieveMessages(Integer chatroomId) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<Conversation> conversations = new ArrayList<>();

        String query = String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = '%d' ORDER BY %s ASC;",
                CityOfTwo.TABLE_MESSAGES,
                CityOfTwo.COLUMN_CHATROOM_ID,
                chatroomId,
                CityOfTwo.COLUMN_TIME
        );

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while (!c.isAfterLast())
            try {
                int cId = c.getInt(c.getColumnIndex(CityOfTwo.COLUMN_CHATROOM_ID));
                String text = c.getString(c.getColumnIndex(CityOfTwo.COLUMN_MESSAGE));
                int flags = c.getInt(c.getColumnIndex(CityOfTwo.COLUMN_FLAGS));
                long time = c.getInt(c.getColumnIndex(CityOfTwo.COLUMN_TIME));

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

    public boolean clearTable() {
        SQLiteDatabase db = getWritableDatabase();

        int id = db.delete(
                CityOfTwo.TABLE_MESSAGES,
                null,
                null
        );

        db.close();

        return id > 0;
    }

    public boolean clearTable(Integer chatroomId) {
        SQLiteDatabase db = getWritableDatabase();

        int id = db.delete(
                CityOfTwo.TABLE_MESSAGES,
                CityOfTwo.COLUMN_CHATROOM_ID + "=?",
                new String[]{chatroomId + ""}
        );

        db.close();

        return id > 0;
    }

    private interface Patch {
        void apply(SQLiteDatabase db);

        void revert(SQLiteDatabase db);
    }
}
