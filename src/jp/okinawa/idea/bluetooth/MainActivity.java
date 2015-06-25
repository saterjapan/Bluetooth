package jp.okinawa.idea.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity
{

	private final int REQUEST_ENABLE_BT = 999;

	private BluetoothAdapter btAdapter;

	private Button           btnBtCheck;
	private Button           btnBtEnabled;
	private Button           btnBtPairedevices;
	private Button           btnBtSearch;
	private Button           btnBtDiscovery;

	private ListView             listDevice;
	private ArrayList<String>    data;
	private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // ListView
        listDevice = (ListView)findViewById(R.id.list_device);

        // レシーバーを登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);						// デバイス検出時
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);		// デバイス検出終了時
        registerReceiver(receiver, filter);

        // [ブルートゥース]確認ボタン
        btnBtCheck = (Button)findViewById(R.id.btn_bt_check);
        btnBtCheck.setOnClickListener
        (
        	// 匿名クラス
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
			        if(btAdapter != null)
			        {
			        	Toast.makeText(MainActivity.this, "Bluetooth 機能があります。", Toast.LENGTH_LONG).show();
			        }
			        else
			        {
			        	Toast.makeText(MainActivity.this, "Bluetooth 機能がありません。", Toast.LENGTH_LONG).show();
			        }
				}
        	}
        );

        // [ブルートゥース]有効化ボタン
        btnBtEnabled = (Button)findViewById(R.id.btn_bt_enabled);
        btnBtEnabled.setOnClickListener
        (
        	// 匿名クラス
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
			        if(!btAdapter.isEnabled())
			        {
			        	Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			        	startActivityForResult(i, REQUEST_ENABLE_BT);
			        }
			        else
			        {
			        	Toast.makeText(MainActivity.this, "Bluetooth は既に有効です。", Toast.LENGTH_LONG).show();
			        }
				}
        	}
        );

        // [ペアデバイス]表示ボタン
        btnBtPairedevices = (Button)findViewById(R.id.btn_bt_pairedevices);
        btnBtPairedevices.setOnClickListener
        (
        	// 匿名クラス
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
			        data = setPaireDevices();
			        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data);
			        listDevice.setAdapter(adapter);
				}
        	}
        );

        // [デバイス]検索ボタン
        btnBtSearch = (Button)findViewById(R.id.btn_bt_search);
        btnBtSearch.setOnClickListener
        (
        	// 匿名クラス
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
					data = new ArrayList<String>();

					searchDevice();
				}
        	}
        );

        // 発見の有効化ボタン
        btnBtDiscovery = (Button)findViewById(R.id.btn_bt_discovery);
        btnBtDiscovery.setOnClickListener
        (
        	// 匿名クラス
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
					// 発見不可状態の時は発見可能にする
					if(btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
					{
						Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
						startActivity(i);
					}
				}
        	}
        );

    }

    // 有効化の戻り処理
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(requestCode == REQUEST_ENABLE_BT)
    	{
    		if(resultCode == Activity.RESULT_OK)
    		{
    			Toast.makeText(MainActivity.this, "Bluetooth の有効化に成功しました。", Toast.LENGTH_LONG).show();
    		}
    		else
    		{
    			Toast.makeText(MainActivity.this, "Bluetooth の有効化に失敗しました。", Toast.LENGTH_LONG).show();
    		}
    	}
    }

    // ペアデバイスをリストに追加
    public ArrayList<String> setPaireDevices()
    {
    	ArrayList<String> data = new ArrayList<String>();

    	if(btAdapter != null)
    	{
    		Set<BluetoothDevice> devices = btAdapter.getBondedDevices();

    		if(devices.size() > 0)
    		{
    			for(BluetoothDevice device : devices)
    			{
    				data.add(device.getName() + "\n" + device.getAddress());
    			}
    		}
    		else
    		{
    			data.add("なし");
    		}
    	}

    	return data;
    }

    // デバイスの検索
    public void searchDevice()
    {
    	if(btAdapter.isDiscovering())
    	{
    		btAdapter.cancelDiscovery();
    	}

    	btAdapter.startDiscovery();		// 開始
    }

    // ブロードキャストレシーバー
    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {

    	@Override
		public void onReceive(Context context, Intent intent)
		{
    		String action = intent.getAction();

    		if(action.equals(BluetoothDevice.ACTION_FOUND))							// デバイス検出時
    		{
    			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

    			// 接続履歴がないデバイスの場合はリストに追加
    			if (device.getBondState() != BluetoothDevice.BOND_BONDED)
    			{
    				data.add(device.getName() + "\n" + device.getAddress());
    			}
    		}
    		else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))		// デバイス検出終了時
    		{
    			if (data.size() == 0)
    			{
    				data.add("なし");
    			}
    		}

	        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data);
    		listDevice.setAdapter(adapter);
		}
    	
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
