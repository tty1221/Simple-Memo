
package com.lab.tty.simplememo;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.android.listview.EnhancedListView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_MYID = "com.lab.tty.simplememo.MYID";
    public static final String ACTIONBAR_TITLE = "List of Memo";

    // リストとアダプター
    private SimpleCursorAdapter mAdapter;
    private EnhancedListView mListView;
    private List<Map<String, String>> mItemList;
    private ArrayList tempList;

    // 取得するDBのカラム名
    private static final String[] from = {
            MemoContract.Memos.COL_TITLE,
            MemoContract.Memos.COL_BODY
    };
    // データをセットするテキスト
    private static final int[] to = {
            R.id.text1,
            R.id.text2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // レイアウトの決定
        setContentView(R.layout.activity_main);
        // タイトル表示の設定
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        }
        // ボタン表示の設定
        Button button = (Button) findViewById(R.id.btnNew);
        button.setAllCaps(false);
        // / 広告の設定
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // 内部リストにデータをセット
        mItemList = setItemList();

        mAdapter = new SimpleCursorAdapter(
                MainActivity.this,
                R.layout.list_item,
                null,
                from,
                to,
                0
        );
        // 出力先のListViewを取得してデータセット
        mListView = (EnhancedListView) findViewById(R.id.list);
        mListView.setAdapter(mAdapter);

        // リストのメモをクリックした場合
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                Intent intent = new Intent(MainActivity.this, FormActivity.class);
                intent.putExtra(EXTRA_MYID, id);
                startActivity(intent);
            }
        });

        // スワイプで消す設定
        mListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                // 削除するデータのID（DB）の取得
                final long id = listView.getItemIdAtPosition(position);

                // 戻す処理のためにデータを一時保存
                tempList = new ArrayList<Map>();
                tempList.add(mItemList.get(position));

                // リストから削除
                mItemList.remove(position);

                // データベースから削除
                Uri uri = ContentUris.withAppendedId(
                        MemoContentProvider.CONTENT_URI,
                        id
                );
                getContentResolver().delete(
                        uri,
                        MemoContract.Memos._ID + " =?",
                        new String[]{Long.toString(id)}
                );

                // データベースからデータを再取得してリストにセット
                mAdapter = new SimpleCursorAdapter(
                        MainActivity.this,
                        R.layout.list_item,
                        null,
                        from,
                        to,
                        0
                );
                mListView.setAdapter(mAdapter);

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        // 元に戻す処理
                        Map tempMap = (Map) tempList.get(0);
                        mItemList.add(position, tempMap);
                        ContentValues values = new ContentValues();
                        values.put(MemoContract.Memos.COL_TITLE, (String) tempMap.get("title"));
                        values.put(MemoContract.Memos.COL_BODY, (String) tempMap.get("body"));

                        // 削除したデータをデータベースに再追加
                        getContentResolver().insert(
                                MemoContentProvider.CONTENT_URI,
                                values
                        );

                        // データベースからデータを再取得してリストにセット
                        mAdapter = new SimpleCursorAdapter(
                                MainActivity.this,
                                R.layout.list_item,
                                null,
                                from,
                                to,
                                0
                        );
                        mListView.setAdapter(mAdapter);
                    }
                };
            }
        });
        mListView.enableSwipeToDismiss();

        // 新規作成ボタンを押した場合（新規作成画面に遷移する）
        findViewById(R.id.btnNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FormActivity.class);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(0, null, MainActivity.this);
    }

    /*
     内部リストへのデータセット
     */
    public List<Map<String, String>> setItemList () {
        MemoOpenHelper mDbHelper = new MemoOpenHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final Cursor cursor = db.rawQuery("select * from memos order by " + MemoContract.Memos.COL_TITLE + " DESC", null);
        mItemList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            Map<String, String> map = new HashMap<>();
            map.put("title", cursor.getString(cursor.getColumnIndex(MemoContract.Memos.COL_TITLE)));
            map.put("body", cursor.getString(cursor.getColumnIndex(MemoContract.Memos.COL_BODY)));
            mItemList.add(map);
        }
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put("title", cursor.getString(cursor.getColumnIndex(MemoContract.Memos.COL_TITLE)));
            map.put("body", cursor.getString(cursor.getColumnIndex(MemoContract.Memos.COL_BODY)));
            mItemList.add(map);
        }
        return mItemList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MemoContract.Memos._ID,
                MemoContract.Memos.COL_TITLE,
                MemoContract.Memos.COL_BODY
        };
        return new CursorLoader(
                MainActivity.this,
                MemoContentProvider.CONTENT_URI,
                projection,
                null,
                null,
                MemoContract.Memos.COL_TITLE + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
       mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}