package com.lab.tty.simplememo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.StringCharacterIterator;

/**
 * Created by tty on 2016/05/07.
 */
public class MemoOpenHelper extends SQLiteOpenHelper{

    public static String DB_NAME = "myapp.db";
    public static int DB_VERSION = 1;

    // テーブル作成
    public static final String CREATE_TABLE =
            "create table " + MemoContract.Memos.TABLE_NAME +
                    "(_id integer primary key autoincrement, " +
                    MemoContract.Memos.COL_TITLE + " text, " +
                    MemoContract.Memos.COL_BODY + " text, " +
                    "created datetime default current_timestamp, " +
                    "updated datetime default current_timestamp)";

    // データ更新
    public static final String INIT_TABLE =
            "insert into " + MemoContract.Memos.TABLE_NAME + " (" + MemoContract.Memos.COL_TITLE + ", "
                    + MemoContract.Memos.COL_BODY + ") values " +
                    "('title1', 'body1'), " +
                    "('title2', 'body2'), " +
                    "('title3', 'body3') ";

    // データ削除
    public static final String DROP_TABLE =
            "drop table if exists " + MemoContract.Memos.TABLE_NAME;

    public MemoOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        //db.execSQL(INIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
