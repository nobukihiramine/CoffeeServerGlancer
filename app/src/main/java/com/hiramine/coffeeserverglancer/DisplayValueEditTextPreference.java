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

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayValueEditTextPreference extends EditTextPreference
{
	public DisplayValueEditTextPreference( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
	}

	public DisplayValueEditTextPreference( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
	}

	public DisplayValueEditTextPreference( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}

	public DisplayValueEditTextPreference( Context context )
	{
		super( context );
	}

	@Override
	protected View onCreateView( ViewGroup parent )
	{
		setWidgetLayoutResource( R.layout.widget_preferencevalue );
		return super.onCreateView( parent );
	}

	@Override
	protected void onBindView( View view )
	{
		super.onBindView( view );
		TextView textView = (TextView)view.findViewById( R.id.textview_preferencevalue );

		if( getTitle().toString().toLowerCase().contains( "password" ) )
		{
			textView.setText( "******" );
		}
		else
		{
			textView.setText( getText() );
		}
	}
}
