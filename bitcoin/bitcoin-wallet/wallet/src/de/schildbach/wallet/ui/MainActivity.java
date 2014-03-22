package de.schildbach.wallet.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.schildbach.wallet_test.R;

public class MainActivity extends FragmentActivity {
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private static final String TAG = "MainActivity";
	
	private ListView mFriend;

    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Select 
		setContentView(R.layout.activity_main);

		mFriend = (ListView) findViewById(R.id.friend);
        findViewById(R.id.go_to_wallet).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WalletActivity.class);
                startActivity(intent);
            }
        });
        setPerson();
	}

	private TextView mName;
	private ImageView mPic;
	private TextView mText;

	private SimpleCursorAdapter mCursorAdapter;

	private ArrayList<Integer> mProfilePics;

	private static final String[] PROJECTION =
        {
			ContactsContract.CommonDataKinds.Phone._ID,
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.NUMBER,
        };
	
	private static final String[] FROM_COLUMNS = {
		ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
		ContactsContract.CommonDataKinds.Phone.NUMBER,
	};

	private static final int[] TO_IDS = {
		R.id.name,
		R.id.text
	};
	
	private void setPerson() {
		mFriend = (ListView) findViewById(R.id.friend);
		
		mCursorAdapter = new FriendAdapter(
                this,
                R.layout.friend_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
		mFriend.setAdapter(mCursorAdapter);
		

        // Initializes the loader
	    getSupportLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
				return new CursorLoader(
		                MainActivity.this,
		                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
		                PROJECTION,
		                null,
		                null,
		                null
		        );
			}

			@Override
			public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
				int count = cursor.getCount();
			    mProfilePics = new ArrayList<Integer>();
			   for (int i = 0; i < count; ++i) {
					// mProfilePics.add(resId);
				}
				mCursorAdapter.swapCursor(cursor);
			}

			@Override
			public void onLoaderReset(Loader<Cursor> arg0) {
		        // Delete the reference to the existing Cursor
		        mCursorAdapter.swapCursor(null);
			}
		});
		
	    
		mFriend.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				Intent intent = new Intent(MainActivity.this, SendCoinsActivity.class);
				Cursor cursor = mCursorAdapter.getCursor();
				if (cursor.moveToPosition(position)) {
					String name = cursor.getString(cursor.getColumnIndex(
							ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					String phone = cursor.getString(cursor.getColumnIndex(
							ContactsContract.CommonDataKinds.Phone.NUMBER));
					intent.putExtra("name", name);
					intent.putExtra("room_id", position + 1);
					intent.putExtra("phone", phone);
				}
				startActivity(intent);
			}
		});
	}
	
	private class FriendAdapter extends SimpleCursorAdapter {

		public FriendAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}
		
		@Override
		public void bindView(View view, Context arg1, Cursor cursor) {
			super.bindView(view, arg1, cursor);
            /*
			ImageView image = (ImageView) view.findViewById(R.id.pic);
			image.setImageResource(mProfilePics.get(cursor.getPosition()));*/
		}
	}
}
