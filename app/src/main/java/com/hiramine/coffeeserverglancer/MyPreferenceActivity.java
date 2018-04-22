package com.hiramine.coffeeserverglancer;

import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MyPreferenceActivity extends AppCompatActivity
{
	public static class MyPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate( final Bundle savedInstanceState )
		{
			super.onCreate( savedInstanceState );

			// 設定画面を定義したXMLを読み込む
			addPreferencesFromResource( R.xml.preferences );
		}
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getFragmentManager().beginTransaction().replace( android.R.id.content, new MyPreferenceFragment() ).commit();

		// アクションバーに前画面に戻る機能をつける
		ActionBar actionBar = getSupportActionBar();
		if( null != actionBar )
		{
			actionBar.setDisplayHomeAsUpEnabled( true );
		}
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected( item );
	}
}
