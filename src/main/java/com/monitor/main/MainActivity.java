package com.monitor.main;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.connect.socket.SocketTransceiver;
import com.connect.socket.TcpClient;
import com.monitor.main.model.LampInfo;
import com.monitor.main.view.LampListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener,ActionBar.OnNavigationListener {

	private static final String IP = "10.10.10.1";
	private static final int PORT = 8080;

//	private static final String IP = "192.168.99.243";
//		private static final int PORT = 8000;
	//private Button bnConnect;
	//private TextView txReceive;
	//private EditText edIP, edPort, edData;
	private GridView gridView;
	private LampListAdapter mAdapter;
	private List<LampInfo> lampInfoList = new ArrayList<LampInfo>();
	private ProgressDialog progressDialog;
	static  MyLog log = new MyLog("MainActivity");
	private  Handler handler = new Handler(Looper.getMainLooper());

	public static SocketTransceiver socketTransceiver;
	private  TcpClient client = new TcpClient() {

		@Override
		public void onConnect(SocketTransceiver transceiver) {
			log.info("connect success");
			MainActivity.socketTransceiver = transceiver;


//			if (progressDialog.isShowing()) {
//				progressDialog.hide();
//			}
//			refreshUI(true);

			handler.post(new Runnable() {
				@Override
				public void run() {
					handler.removeCallbacks(overTimeConnect);
					if(progressDialog != null){
						progressDialog.hide();
						progressDialog = null;
					}
					SqliteHelper sqliteHelper = new SqliteHelper(getApplicationContext(),"lampData",1);
					SQLiteDatabase database = sqliteHelper.getWritableDatabase();
					Cursor cursor = database.rawQuery("select count(*) from LampInfo", null);

					int count = cursor.getCount();
					log.debug("Table LameInfo cont is " + count);
					researchDevice();
					if(count == 0){

					}else {

					}
				}
			});
		}

		@Override
		public void onDisconnect(SocketTransceiver transceiver) {
//			refreshUI(false);
		}

		@Override
		public void onConnectFailed() {
				handler.post(new Runnable() {
					@Override
					public void run() {
//						if (progressDialog.isShowing()) {
//							progressDialog.hide();
//						}
						Toast.makeText(MainActivity.this, "连接失败",
								Toast.LENGTH_SHORT).show();
					}
				});
		}

		@Override
		public void onReceive(final SocketTransceiver transceiver, final List<Integer> list) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					log.info("接收到的数据:" + list.size() + "    " + list);
					if (list.size() <= 6){
						return;
					}


					onResponse(list);
//					for (int i = 6;i < list.size() - 4; i++) {
//						 LampInfo lampInfo = new LampInfo();
//						 lampInfo.setLampCode(String.valueOf(list.get(i)));
//						 lampInfo.setLampName("bedroom" + list.get(i));
//						 lampInfoList.add(lampInfo);
//
//					}
//					mAdapter.notifyDataSetChanged();
//
//					Toast.makeText(MainActivity.this, "接收到数据" + list.get(6),
//							Toast.LENGTH_LONG).show();
//					list.clear();
				}
			});
		}
	};

	public static int[] CMD_SEARCH = {
			0x55, 0x55, 0x55, 0x01, 0x01, 0x00, 0x01, 0xaa, 0xaa, 0xaa
	};

	/**
	 * 打开或者关闭灯泡
	 * @param lampCode
	 * @param isOpen
	 * @return
	 */
	public static int[] getSwichCmd(int lampCode, boolean isOpen){

		int[] cmd = {
				0x55, 0x55, 0x55, 0x02, 0x02, 0x00, 0x03, 0xaa, 0xaa, 0xaa

		};

		cmd[3] = lampCode;
		cmd[4] = isOpen ? 0x02:0x03;

		return cmd;
	}


	public static int[] readLampState(int lampCode) {
		int[] cmd = {0x55 , 0x55,  0x55,  0x05,  0x04,  0x00,  0x08,  0xaa,  0xaa , 0xaa};
		cmd[3] = lampCode;
		return cmd;
	}

	public synchronized void onResponse(List<Integer> list){

		handler.removeCallbacks(overtimeCmd);
		log.info(list);

		if(list.get(4) == 0x04) {
			//返回灯的状态
			int lampCode = list.get(3);

			//读取灯的开光状态
			int state = list.get(6);

			for(int i = 0;i < lampInfoList.size();i++){
				if( lampInfoList.get(i).getLampCode() == lampCode){
					lampInfoList.get(i).setLampStatus(state);
				}
			}
			mAdapter.notifyDataSetChanged();
		}

		if(list.get(4) == 0x02 || list.get(4) == 0x03){
			//开关灯的返回状态
			if(list.get(6) == 0x01){
				//operator success

			}else {
				//operator fail
			}
		}

		if(list.get(4) == 0x01){
			//返回搜索的设备
			int index = 6;
			List<LampInfo> lamps = new ArrayList<>();
			for(int i = index;i < list.size() - 3  ; i++){
				LampInfo info = new LampInfo();
				info.setLampCode(list.get(i));
				lamps.add(info);
			}
			lampInfoList.clear();
			lampInfoList.addAll(lamps);
			mAdapter.notifyDataSetChanged();
			if(progressDialog != null){
				progressDialog.hide();
				progressDialog = null;
			}

		}


	}


	public void researchDevice(){
		if(progressDialog != null){
			progressDialog.hide();
			progressDialog = null;
		}
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在搜索设备，请稍后");
		progressDialog.setCanceledOnTouchOutside(false);

		//progressDialog.
		progressDialog.show();
		handler.postDelayed(overtimeCmd,20000);
		sendStr(CMD_SEARCH);
	}

	private Runnable overtimeCmd = new Runnable() {
		@Override
		public void run() {
			new AlertDialog.Builder(MainActivity.this)
					.setMessage("搜索超时，是否重试")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							researchDevice();
						}
					})
					.setNegativeButton("否", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							progressDialog.hide();
							MainActivity.this.finish();
						}
					})
					.setCancelable(false)
					.show();
		}
	};

	private Runnable overTimeConnect = new Runnable() {
		@Override
		public void run() {
			new AlertDialog.Builder(MainActivity.this)
					.setMessage("连接超时，是否重试")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							connect();
						}
					})
					.setNegativeButton("否", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							progressDialog.hide();
							MainActivity.this.finish();
						}
					})
					.setCancelable(false)
					.show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interest_recommend);

		gridView = (GridView) findViewById(R.id.gv_interest);
		connect();

        //链接后发送指令获取所有灯的信息



//		initData();
		mAdapter = new LampListAdapter(this,lampInfoList);
		gridView.setAdapter(mAdapter);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//		getActionBar().setCustomView(R.layout.actionbar_downloadlist_layout);


		Calendar calendar = Calendar.getInstance();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		client.disconnect();

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public void onStop() {
		client.disconnect();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bn_connect:
			connect();
			break;
		case R.id.bn_send:
			//sendStr();
			break;
		case R.id.tx_receive:
			clear();
			break;
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	/**
	 * 刷新界面显示
	 * 
	 * @param isConnected
	 */
	private void refreshUI(final boolean isConnected) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (isConnected) {
                    Log.v("gao","连接成功");
					Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_LONG).show();
					sendStr("0x55 0x55 0x55 0x01 0x01 0x00  0x01 0xaa 0xaa 0xaa");
					Log.v("gao","发送数据获取灯的列表");
				}
			}
		});
	}

	/**
	 * 设置IP和端口地址,连接或断开
	 */
	private void connect() {
		if(progressDialog != null){
			progressDialog.hide();
			progressDialog = null;
		}

		progressDialog = new ProgressDialog(MainActivity.this);
		progressDialog.setMessage("正在连接AP，请稍后");
		progressDialog.setCanceledOnTouchOutside(false);

		//progressDialog.
		progressDialog.show();

		handler.postDelayed(overTimeConnect,20000);

		if (client.isConnected()) {
			// 断开连接
			client.disconnect();
		} else {
			try {
//				String hostIP = edIP.getText().toString();
//				int port = Integer.parseInt(edPort.getText().toString());

//				client.connect("10.10.10.1", 8080);
				client.connect(IP, PORT);

				//搜索所有灯

				//client.getTransceiver().send("0x55 0x55 0x55 0x01 0x01 0x00  0x01 0xaa 0xaa 0xaa");

			} catch (NumberFormatException e) {
				Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发送数据
	 */
	private void sendStr(String data) {
		try {
			client.getTransceiver().send(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendStr(int[] data) {
		try {
			client.getTransceiver().send(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 清空接收框
	 */
	private void clear() {
		new AlertDialog.Builder(this).setTitle("确认清除?")
				.setNegativeButton("取消", null)
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//txReceive.setText("");
					}
				}).show();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return false;
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		log.info("onCreateOptionsMenu " + menu);
		getMenuInflater().inflate(R.menu.menu_main_activiey,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		log.info("onOptionsItemSelected " + item);
		if(item.getTitle().equals( "A")){
			PopupMenu popup = new PopupMenu(MainActivity.this,findViewById(R.id.menu_filter));
			log.info("popupMenu " + popup + findViewById(R.id.menu_filter));

//			popup.getMenuInflater().inflate(R.menu.menu_test_actionbar_filter_task, popup.getMenu());
//			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//				@Override
//				public boolean onMenuItemClick(MenuItem item) {
//					log.info("onMenuItemClick " + item);
//					return false;
//				}
//			});
//			popup.show();
		}

		switch (item.getItemId()){
			case R.id.menu_edit_name:

				if(item.getTitle().equals(getResources().getString(R.string.edit_lamp_name))){
					//begin edit
					item.setIcon(R.drawable.ic_done);
					item.setTitle(getString(R.string.edit_lamp_name_finish));
					for (LampInfo info: lampInfoList) {
						info.setEditLampName(true);
					}

				}else if(item.getTitle().equals(getResources().getString(R.string.edit_lamp_name_finish))){
					//edit finish
					item.setIcon(R.drawable.ic_edit);
					item.setTitle(getString(R.string.edit_lamp_name));
					for (LampInfo info: lampInfoList) {
						info.setEditLampName(false);
					}
				}


				mAdapter.notifyDataSetChanged();

				log.debug("change the lamb name");
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
