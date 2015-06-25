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

        // ���V�[�o�[��o�^
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);						// �f�o�C�X���o��
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);		// �f�o�C�X���o�I����
        registerReceiver(receiver, filter);

        // [�u���[�g�D�[�X]�m�F�{�^��
        btnBtCheck = (Button)findViewById(R.id.btn_bt_check);
        btnBtCheck.setOnClickListener
        (
        	// �����N���X
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
			        if(btAdapter != null)
			        {
			        	Toast.makeText(MainActivity.this, "Bluetooth �@�\������܂��B", Toast.LENGTH_LONG).show();
			        }
			        else
			        {
			        	Toast.makeText(MainActivity.this, "Bluetooth �@�\������܂���B", Toast.LENGTH_LONG).show();
			        }
				}
        	}
        );

        // [�u���[�g�D�[�X]�L�����{�^��
        btnBtEnabled = (Button)findViewById(R.id.btn_bt_enabled);
        btnBtEnabled.setOnClickListener
        (
        	// �����N���X
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
			        	Toast.makeText(MainActivity.this, "Bluetooth �͊��ɗL���ł��B", Toast.LENGTH_LONG).show();
			        }
				}
        	}
        );

        // [�y�A�f�o�C�X]�\���{�^��
        btnBtPairedevices = (Button)findViewById(R.id.btn_bt_pairedevices);
        btnBtPairedevices.setOnClickListener
        (
        	// �����N���X
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

        // [�f�o�C�X]�����{�^��
        btnBtSearch = (Button)findViewById(R.id.btn_bt_search);
        btnBtSearch.setOnClickListener
        (
        	// �����N���X
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

        // �����̗L�����{�^��
        btnBtDiscovery = (Button)findViewById(R.id.btn_bt_discovery);
        btnBtDiscovery.setOnClickListener
        (
        	// �����N���X
        	new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
					// �����s��Ԃ̎��͔����\�ɂ���
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

    // �L�����̖߂菈��
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(requestCode == REQUEST_ENABLE_BT)
    	{
    		if(resultCode == Activity.RESULT_OK)
    		{
    			Toast.makeText(MainActivity.this, "Bluetooth �̗L�����ɐ������܂����B", Toast.LENGTH_LONG).show();
    		}
    		else
    		{
    			Toast.makeText(MainActivity.this, "Bluetooth �̗L�����Ɏ��s���܂����B", Toast.LENGTH_LONG).show();
    		}
    	}
    }

    // �y�A�f�o�C�X�����X�g�ɒǉ�
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
    			data.add("�Ȃ�");
    		}
    	}

    	return data;
    }

    // �f�o�C�X�̌���
    public void searchDevice()
    {
    	if(btAdapter.isDiscovering())
    	{
    		btAdapter.cancelDiscovery();
    	}

    	btAdapter.startDiscovery();		// �J�n
    }

    // �u���[�h�L���X�g���V�[�o�[
    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {

    	@Override
		public void onReceive(Context context, Intent intent)
		{
    		String action = intent.getAction();

    		if(action.equals(BluetoothDevice.ACTION_FOUND))							// �f�o�C�X���o��
    		{
    			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

    			// �ڑ��������Ȃ��f�o�C�X�̏ꍇ�̓��X�g�ɒǉ�
    			if (device.getBondState() != BluetoothDevice.BOND_BONDED)
    			{
    				data.add(device.getName() + "\n" + device.getAddress());
    			}
    		}
    		else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))		// �f�o�C�X���o�I����
    		{
    			if (data.size() == 0)
    			{
    				data.add("�Ȃ�");
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
