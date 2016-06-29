package com.lab.tty.simplememo;

import android.provider.BaseColumns;

/**
 * Created by tty on 2016/05/07.
 */
public class MemoContract {

    public MemoContract() {}

    public static abstract class Memos implements BaseColumns {
        public static final String TABLE_NAME = "memos";
        public static final String COL_TITLE = "title";
        public static final String COL_BODY = "body";
    }
}
