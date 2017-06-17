package com.monitor.main;



import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.connect.socket.SocketTransceiver;
import com.connect.socket.TcpClient;
import com.monitor.main.model.LampInfo;
import com.monitor.main.view.LampListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener,ActionBar.OnNavigationListener {

	//private Button bnConnect;
	//private TextView txReceive;
	//private EditText edIP, edPort, edData;
	private GridView gridView;
	private LampListAdapter mAdapter;
	private List<LampInfo> lampInfoList = new ArrayList<LampInfo>();
	private ProgressDialog progressDialog;

	private Handler handler = new Handler(Looper.getMainLooper());

	private TcpClient client = new TcpClient() {

		@Override
		public void onConnect(SocketTransceiver transceiver) {

//			if (progressDialog.isShowing()) {
//				progressDialog.hide();
//			}
			refreshUI(true);
		}

		@Override
		public void onDisconnect(SocketTransceiver transceiver) {
			refreshUI(false);
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
		public void onReceive(final SocketTransceiver transceiver, final List list) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Log.v("gao","接收到的数据:" + list.size());
					if (list.size() <= 6)
						return;

					for (int i = 6;i < list.size() - 4; i++) {
						 LampInfo lampInfo = new LampInfo();
						 lampInfo.setLampCode(String.valueOf(list.get(i)));
						 lampInfo.setLampName("bedroom" + list.get(i));
						 lampInfoList.add(lampInfo);

					}
					mAdapter.notifyDataSetChanged();

					Toast.makeText(MainActivity.this, "接收到数据" + list.get(6),
							Toast.LENGTH_LONG).show();
					list.clear();
				}
			});
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interest_recommend);

		gridView = (GridView) findViewById(R.id.gv_interest);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在搜索，请稍后");
		progressDialog.setCanceledOnTouchOutside(false);
		//progressDialog.
		//progressDialog.show();
        //链接后发送指令获取所有灯的信息
		connect();
		initData();
		mAdapter = new LampListAdapter(this,lampInfoList);
		gridView.setAdapter(mAdapter);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.actionbar_downloadlist_layout);

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

	private	void initData() {
		for (int i = 0; i < 3;i++) {

			LampInfo lampInfo = new LampInfo();
			lampInfo.setLampCode("111" + i);
			lampInfo.setLampName("主卧-" + lampInfo.getLampCode());
			lampInfoList.add(lampInfo);
		}


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
				progressDialog.hide();
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
		if (client.isConnected()) {
			// 断开连接
			client.disconnect();
		} else {
			try {
//				String hostIP = edIP.getText().toString();
//				int port = Integer.parseInt(edPort.getText().toString());
				client.connect("10.10.10.1", 8080);
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
}
