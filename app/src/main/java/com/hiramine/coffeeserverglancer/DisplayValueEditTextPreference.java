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
