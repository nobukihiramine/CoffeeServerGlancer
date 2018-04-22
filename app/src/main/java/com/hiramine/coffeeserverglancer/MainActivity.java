package com.hiramine.coffeeserverglancer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
	// 定数（Bluetooth LE Gatt UUID）
	// Private Service
	private static final UUID UUID_SERVICE_PRIVATE         = UUID.fromString( "022E9A96-348A-11E8-B467-0ED5F89F718B" );
	private static final UUID UUID_CHARACTERISTIC_PRIVATE1 = UUID.fromString( "022EA09A-348A-11E8-B467-0ED5F89F718B" );
	// for Notification
	private static final UUID UUID_NOTIFY                  = UUID.fromString( "00002902-0000-1000-8000-00805f9b34fb" );

	// 定数
	private static final int REQUEST_ENABLEBLUETOOTH     = 1; // Bluetooth機能の有効化要求時の識別コード
	private static final int REQUEST_CONNECTDEVICE       = 2; // デバイス接続要求時の識別コード
	private static final int REQUEST_MYPREFERENCEACTIVTY = 3; // 設定

	// メンバー変数
	private BluetoothAdapter mBluetoothAdapter;    // BluetoothAdapter : Bluetooth処理で必要
	private BluetoothGatt mBluetoothGatt = null;    // Gattサービスの検索、キャラスタリスティックの読み書き

	// GUIアイテム
	private TextView mTextView_DeviceName;    // デバイス名
	private TextView mTextView_DeviceAddress;    // デバイスアドレス
	private Button   mButton_Connect;    // 接続ボタン
	private Button   mButton_Disconnect;    // 切断ボタン
	private TextView mTextView_Value;    // センサ値
	private TextView mTextView_Remaining;    // コーヒーの残りの量

	// 設定値
	private int mFullSensorValue;
	private int mEmptySensorValue;
	private int mCoffeeServerSize;

	// BluetoothGattコールバックオブジェクト
	private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback()
	{
		// 接続状態変更（connectGatt()の結果として呼ばれる。）
		@Override
		public void onConnectionStateChange( BluetoothGatt gatt, int status, int newState )
		{
			if( BluetoothGatt.GATT_SUCCESS != status )
			{
				return;
			}

			if( BluetoothProfile.STATE_CONNECTED == newState )
			{    // 接続完了
				mBluetoothGatt.discoverServices();    // サービス検索
				runOnUiThread( new Runnable()
				{
					public void run()
					{
						// GUIアイテムの有効無効の設定
						// 切断ボタンを有効にする
						mButton_Disconnect.setEnabled( true );
					}
				} );
				return;
			}
			if( BluetoothProfile.STATE_DISCONNECTED == newState )
			{    // 切断完了（接続可能範囲から外れて切断された）
				// 接続可能範囲に入ったら自動接続するために、mBluetoothGatt.connect()を呼び出す。
				mBluetoothGatt.connect();
				runOnUiThread( new Runnable()
				{
					public void run()
					{
						// GUIアイテムの有効無効の設定
					}
				} );
				return;
			}
		}

		// サービス検索が完了したときの処理（mBluetoothGatt.discoverServices()の結果として呼ばれる。）
		@Override
		public void onServicesDiscovered( BluetoothGatt gatt, int status )
		{
			if( BluetoothGatt.GATT_SUCCESS != status )
			{
				return;
			}

			// 発見されたサービスのループ
			for( BluetoothGattService service : gatt.getServices() )
			{
				// サービスごとに個別の処理
				if( ( null == service ) || ( null == service.getUuid() ) )
				{
					continue;
				}
				if( UUID_SERVICE_PRIVATE.equals( service.getUuid() ) )
				{    // プライベートサービス
					runOnUiThread( new Runnable()
					{
						public void run()
						{
							// GUIアイテムの有効無効の設定
							// キャラクタリスティック１の変更通知は常にON
							setCharacteristicNotification( UUID_SERVICE_PRIVATE, UUID_CHARACTERISTIC_PRIVATE1, true );
						}
					} );
					continue;
				}
			}
		}

		// キャラクタリスティックが読み込まれたときの処理
		@Override
		public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status )
		{
			if( BluetoothGatt.GATT_SUCCESS != status )
			{
				return;
			}
			// キャラクタリスティックごとに個別の処理
		}

		// キャラクタリスティック変更が通知されたときの処理
		@Override
		public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic )
		{
			// キャラクタリスティックごとに個別の処理
			if( UUID_CHARACTERISTIC_PRIVATE1.equals( characteristic.getUuid() ) )
			{    // キャラクタリスティック１：データサイズは、2バイト（数値を想定。0～65,535）
				byte[] byteChara    = characteristic.getValue();
				String strChar_temp = "";
				if( 0 == byteChara.length )
				{
					strChar_temp = "";
				}
				else if( 1 == byteChara.length )
				{    // 00～FF : 0～255
					int iValue = (char)byteChara[0];
					strChar_temp = String.valueOf( iValue );
				}
				else if( 2 == byteChara.length )
				{    // 0000～FFFF : 0～65,535
					ByteBuffer bb     = ByteBuffer.wrap( byteChara );
					int        iValue = (char)bb.getShort();
					strChar_temp = String.valueOf( iValue );
				}
				else if( 4 == byteChara.length )
				{    // 00000000～FFFFFFFF : -2,147,483,648～2,147,483,647
					ByteBuffer bb     = ByteBuffer.wrap( byteChara );
					int        iValue = bb.getInt();
					strChar_temp = String.valueOf( iValue );
				}
				else
				{
					strChar_temp = "";
				}
				final String strChara = strChar_temp;
				runOnUiThread( new Runnable()
				{
					public void run()
					{
						// GUIアイテムへの反映
						mTextView_Value.setText( strChara );

						// センサ値から残量テキストの生成
						String strText = MakeRemainingText( Integer.parseInt( strChara ) );
						mTextView_Remaining.setText( strText );

						// 切断ボタンを押す
						mButton_Disconnect.callOnClick();
					}
				} );
				return;
			}
		}
	};

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// GUIアイテム
		mTextView_DeviceName = (TextView)findViewById( R.id.textview_devicename );
		mTextView_DeviceAddress = (TextView)findViewById( R.id.textview_deviceaddress );
		mButton_Connect = (Button)findViewById( R.id.button_connect );
		mButton_Connect.setOnClickListener( this );
		mButton_Disconnect = (Button)findViewById( R.id.button_disconnect );
		mButton_Disconnect.setOnClickListener( this );
		mTextView_Value = (TextView)findViewById( R.id.textview_value );
		mTextView_Remaining = (TextView)findViewById( R.id.textview_remaining );

		// 前回設定の読み込み
		updateSettings( false );

		// Android端末がBLEをサポートしてるかの確認
		if( !getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) )
		{
			Toast.makeText( this, R.string.ble_is_not_supported, Toast.LENGTH_SHORT ).show();
			finish();    // アプリ終了宣言
			return;
		}

		// Bluetoothアダプタの取得
		BluetoothManager bluetoothManager = (BluetoothManager)getSystemService( Context.BLUETOOTH_SERVICE );
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if( null == mBluetoothAdapter )
		{    // Android端末がBluetoothをサポートしていない
			Toast.makeText( this, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT ).show();
			finish();    // アプリ終了宣言
			return;
		}

		// 前回設定の読み込み
		updateSettings( false );
	}

	// 初回表示時、および、ポーズからの復帰時
	@Override
	protected void onResume()
	{
		super.onResume();

		// Android端末のBluetooth機能の有効化要求
		requestBluetoothFeature();

		// GUIアイテムの有効無効の設定
		mButton_Connect.setEnabled( false );
		mButton_Disconnect.setEnabled( false );

		// デバイスアドレスが空でなければ、接続ボタンを有効にする。
		if( !mTextView_DeviceAddress.getText().toString().equals( "" ) )
		{
			mButton_Connect.setEnabled( true );
		}

		// 接続ボタンを押す
		//mButton_Connect.callOnClick();
	}

	// 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
	@Override
	protected void onPause()
	{
		// 切断
		disconnect();

		// 設定値の保存
		updateSettings( true );

		super.onPause();
	}

	// アクティビティの終了直前
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if( null != mBluetoothGatt )
		{
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	// Android端末のBluetooth機能の有効化要求
	private void requestBluetoothFeature()
	{
		if( mBluetoothAdapter.isEnabled() )
		{
			return;
		}
		// デバイスのBluetooth機能が有効になっていないときは、有効化要求（ダイアログ表示）
		Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
		startActivityForResult( enableBtIntent, REQUEST_ENABLEBLUETOOTH );
	}

	// 機能の有効化ダイアログの操作結果
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		switch( requestCode )
		{
			case REQUEST_ENABLEBLUETOOTH: // Bluetooth有効化要求
				if( Activity.RESULT_CANCELED == resultCode )
				{    // 有効にされなかった
					Toast.makeText( this, R.string.bluetooth_is_not_working, Toast.LENGTH_SHORT ).show();
					finish();    // アプリ終了宣言
					return;
				}
				break;
			case REQUEST_CONNECTDEVICE: // デバイス接続要求
				String strDeviceName;
				if( Activity.RESULT_OK == resultCode )
				{
					// デバイスリストアクティビティからの情報の取得
					mTextView_DeviceName.setText( data.getStringExtra( DeviceListActivity.EXTRAS_DEVICE_NAME ) );
					mTextView_DeviceAddress.setText( data.getStringExtra( DeviceListActivity.EXTRAS_DEVICE_ADDRESS ) );
				}
				else
				{
					mTextView_DeviceName.setText( "" );
					mTextView_DeviceAddress.setText( "" );
				}
				mTextView_Value.setText( "" );
				break;
			case REQUEST_MYPREFERENCEACTIVTY: // Preferenceアクティビティ
				// 設定変更の反映
				updateSettings( false );
				break;

		}
		super.onActivityResult( requestCode, resultCode, data );
	}

	// オプションメニュー作成時の処理
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		getMenuInflater().inflate( R.menu.activity_main, menu );
		return true;
	}

	// オプションメニューのアイテム選択時の処理
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case R.id.menuitem_search:
				Intent devicelistactivityIntent = new Intent( this, DeviceListActivity.class );
				startActivityForResult( devicelistactivityIntent, REQUEST_CONNECTDEVICE );
				return true;
			case R.id.menuitem_settings:
				Intent intent = new Intent( this, MyPreferenceActivity.class );
				startActivityForResult( intent, REQUEST_MYPREFERENCEACTIVTY );
				return true;
		}
		return false;
	}

	@Override
	public void onClick( View v )
	{
		if( mButton_Connect.getId() == v.getId() )
		{
			mButton_Connect.setEnabled( false );    // 接続ボタンの無効化（連打対策）
			connect();            // 接続
			return;
		}
		if( mButton_Disconnect.getId() == v.getId() )
		{
			mButton_Disconnect.setEnabled( false );    // 切断ボタンの無効化（連打対策）
			disconnect();            // 切断
			return;
		}
	}

	// 接続
	private void connect()
	{
		if( mTextView_DeviceAddress.getText().toString().equals( "" ) )
		{    // DeviceAddressが空の場合は処理しない
			return;
		}

		if( null != mBluetoothGatt )
		{    // mBluetoothGattがnullでないなら接続済みか、接続中。
			return;
		}

		// 接続
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( mTextView_DeviceAddress.getText().toString() );
		mBluetoothGatt = device.connectGatt( this, false, mGattcallback );
	}

	// 切断
	private void disconnect()
	{
		if( null == mBluetoothGatt )
		{
			return;
		}

		// 切断
		//   mBluetoothGatt.disconnect()ではなく、mBluetoothGatt.close()しオブジェクトを解放する。
		//   理由：「ユーザーの意思による切断」と「接続範囲から外れた切断」を区別するため。
		//   ①「ユーザーの意思による切断」は、mBluetoothGattオブジェクトを解放する。再接続は、オブジェクト構築から。
		//   ②「接続可能範囲から外れた切断」は、内部処理でmBluetoothGatt.disconnect()処理が実施される。
		//     切断時のコールバックでmBluetoothGatt.connect()を呼んでおくと、接続可能範囲に入ったら自動接続する。
		mBluetoothGatt.close();
		mBluetoothGatt = null;
		// GUIアイテムの有効無効の設定
		// 接続ボタンのみ有効にする
		mButton_Connect.setEnabled( true );
		mButton_Disconnect.setEnabled( false );
	}

	// キャラクタリスティックの読み込み
	private void readCharacteristic( UUID uuid_service, UUID uuid_characteristic )
	{
		if( null == mBluetoothGatt )
		{
			return;
		}
		BluetoothGattCharacteristic blechar = mBluetoothGatt.getService( uuid_service ).getCharacteristic( uuid_characteristic );
		mBluetoothGatt.readCharacteristic( blechar );
	}

	// キャラクタリスティック通知の設定
	private void setCharacteristicNotification( UUID uuid_service, UUID uuid_characteristic, boolean enable )
	{
		if( null == mBluetoothGatt )
		{
			return;
		}
		BluetoothGattCharacteristic blechar = mBluetoothGatt.getService( uuid_service ).getCharacteristic( uuid_characteristic );
		mBluetoothGatt.setCharacteristicNotification( blechar, enable );
		BluetoothGattDescriptor descriptor = blechar.getDescriptor( UUID_NOTIFY );
		descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
		mBluetoothGatt.writeDescriptor( descriptor );
	}

	// 設定値の読み書き
	private void updateSettings( boolean bSave )
	{
		SharedPreferences        sharedpreferences = PreferenceManager.getDefaultSharedPreferences( this );
		SharedPreferences.Editor editor            = sharedpreferences.edit();

		if( bSave )
		{
			String str;
			str = mTextView_DeviceName.getText().toString();
			editor.putString( "DeviceName", str );
			str = mTextView_DeviceAddress.getText().toString();
			editor.putString( "DeviceAddress", str );
		}
		else
		{
			String str;
			str = sharedpreferences.getString( "DeviceName", "" );
			mTextView_DeviceName.setText( str );
			str = sharedpreferences.getString( "DeviceAddress", "" );
			mTextView_DeviceAddress.setText( str );
		}

		if( bSave )
		{
			// 設定の変更は、プリファレンスアクティビティで行う。
			;
		}
		else
		{
			// 未初期化パラメータに、初期値設定
			PreferenceManager.setDefaultValues( this, R.xml.preferences, false );

			String strValue;

			strValue = sharedpreferences.getString( "FullSensorValue", "" );
			mFullSensorValue = Integer.parseInt( strValue );

			strValue = sharedpreferences.getString( "EmptySensorValue", "" );
			mEmptySensorValue = Integer.parseInt( strValue );

			strValue = sharedpreferences.getString( "CoffeeServerSize", "" );
			mCoffeeServerSize = Integer.parseInt( strValue );
		}

		// 設定値の書き込みの実施
		if( bSave )
		{
			editor.apply();
		}
	}

	// センサ値から残量テキストの生成
	private String MakeRemainingText( int iSensorValue )
	{
		if( 0 >= mFullSensorValue )
		{    // 満杯の場合のセンサ値設定が正しくない
			return getString( R.string.invalid_fullsensorvalue_setting );
		}
		if( 0 >= mEmptySensorValue )
		{    // 空の場合のセンサ値設定が正しくない
			return getString( R.string.invalid_emptysensorvalue_setting );
		}
		if( 0 >= mCoffeeServerSize )
		{    // コーヒーサーバーサイズの設定値が正しくない
			return getString( R.string.invalid_coffeeserversize_setting );
		}
		if( mFullSensorValue <= mEmptySensorValue )
		{    // 設定値の関係が正しくない
			return getString( R.string.invalid_settings );
		}

		double dRemaining = (double)( iSensorValue - mEmptySensorValue ) * (double)mCoffeeServerSize / (double)( mFullSensorValue - mEmptySensorValue );
		if( -1.0 > dRemaining )
		{    // コーヒーサーバーが乗っていない
			return getString( R.string.no_coffeeserver );
		}
		else if( 0.2 > dRemaining )
		{    // コーヒーがほとんど残っていない（0.2杯未満）
			return getString( R.string.almost_none );
		}

		// まあまあ残っている
		return String.format( Locale.US, "%.1f %s", dRemaining, getString( R.string.unit_cups ) );
	}
}
