/*
 * Copyright 2018 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
