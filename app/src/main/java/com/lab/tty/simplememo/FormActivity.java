package com.lab.tty.simplememo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormActivity extends AppCompatActivity {

    private final String CREATE_UPDATE = "Create/Update : ";
    private final String ACTIONBAR_NEW = "New Memo";
    private final String ACTIONBAR_EDIT = "Edit Memo";
    private final String ENTER_MESSAGE = "Please enter Memo";
    private final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";
    private final String DELETE_MASSAGE = "Delete Memo ?";
    private final String OK_MASSAGE = "OK";
    private final String CANCEL_MASSAGE = "CANCEL";

    private long memoId;
    private TextView titleText;
    private EditText bodyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        titleText = (TextView) findViewById(R.id.titleText);
        bodyText = (EditText) findViewById(R.id.bodyText);
        String create_update_date = new SimpleDateFormat(DATE_FORMAT, Locale.US)
                .format(new Date());

        Intent intent = getIntent();
        memoId = intent.getLongExtra(MainActivity.EXTRA_MYID, 0L);

        if (memoId == 0) {
            // new memo
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(ACTIONBAR_NEW);
            }
            titleText.setText(CREATE_UPDATE + create_update_date);

        } else {
            // show memo
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(ACTIONBAR_EDIT);
            }
            Uri uri = ContentUris.withAppendedId(
                    MemoContentProvider.CONTENT_URI,
                    memoId
            );
            String[] projection = {
                    MemoContract.Memos.COL_TITLE,
                    MemoContract.Memos.COL_BODY
            };
            Cursor cursor = getContentResolver().query(
                    uri,
                    projection,
                    MemoContract.Memos._ID + " = ?",
                    new String[] { Long.toString(memoId) },
                    null
            );
            cursor.moveToFirst();
            titleText.setText
                    (cursor.getString(cursor.getColumnIndex(MemoContract.Memos.COL_TITLE)));
            bodyText.setText(
                    cursor.getString(cursor.getColumnIndex(MemoContract.Memos.COL_BODY)));
            cursor.close();
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    // メニューの保存ボタンを押した場合
    public void saveMemo() {
        String title = titleText.getText().toString().trim();
        String body = bodyText.getText().toString().trim();
        if (body.isEmpty()) {
            Toast.makeText(
                    FormActivity.this,
                    ENTER_MESSAGE,
                    Toast.LENGTH_LONG
            ).show();
        } else {
            ContentValues values = new ContentValues();
            values.put(MemoContract.Memos.COL_BODY, body);
            if (memoId == 0L) {
                // new memo
                values.put(MemoContract.Memos.COL_TITLE, title);
                getContentResolver().insert(
                        MemoContentProvider.CONTENT_URI,
                        values
                );
            } else {
                // updated memo
                values.put(MemoContract.Memos.COL_TITLE, CREATE_UPDATE
                        + new SimpleDateFormat(DATE_FORMAT, Locale.US)
                        .format(new Date()));

                Uri uri = ContentUris.withAppendedId(
                        MemoContentProvider.CONTENT_URI,
                        memoId
                );
                getContentResolver().update(
                        uri,
                        values,
                        MemoContract.Memos._ID + " = ?",
                        new String[]{ Long.toString(memoId)}
                );
            }
            Intent intent = new Intent(FormActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    // メニューの削除ボタンを押した場合
    public void deleteMemo() {
        new AlertDialog.Builder(this)
                .setTitle(DELETE_MASSAGE)
                .setPositiveButton(OK_MASSAGE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = ContentUris.withAppendedId(
                                MemoContentProvider.CONTENT_URI,
                                memoId
                        );
                        getContentResolver().delete(
                                uri,
                                MemoContract.Memos._ID + " =?",
                                new String[]{Long.toString(memoId)}
                        );
                        Intent intent = new Intent(FormActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(CANCEL_MASSAGE, null)
                .show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (memoId == 0L) {
            deleteItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveMemo();
                break;
            case R.id.action_delete:
                deleteMemo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}