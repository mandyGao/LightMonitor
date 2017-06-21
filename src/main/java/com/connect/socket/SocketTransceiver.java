package com.connect.socket;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 */
public abstract class SocketTransceiver implements Runnable {

	protected Socket socket;
	protected InetAddress addr;
	protected DataInputStream in;
	protected DataOutputStream out;
	private boolean runFlag;
	private static final int BUFFER_SIZE=12;

	/**
	 * 实例化
	 * 
	 * @param socket
	 *            已经建立连接的socket
	 */
	public SocketTransceiver(Socket socket) {
		this.socket = socket;
		this.addr = socket.getInetAddress();
	}

	/**
	 * 获取连接到的Socket地址
	 * 
	 * @return InetAddress对象
	 */
	public InetAddress getInetAddress() {
		return addr;
	}

	/**
	 * 开启Socket收发
	 * <p>
	 * 如果开启失败，会断开连接并回调{@code onDisconnect()}
	 */
	public void start() {
		runFlag = true;
		new Thread(this).start();
	}

	/**
	 * 断开连接(主动)
	 * <p>
	 * 连接断开后，会回调{@code onDisconnect()}
	 */
	public void stop() {
		runFlag = false;
		try {
			//socket.shutdownInput();
			socket.close();
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 发送字符串
	 *
	 * @param cmd
	 *            字符串
	 * @return 发送成功返回true
	 */
	public boolean send(int[] cmd) {
		if (out != null) {
			try {

				for (int value:cmd
					 ) {
					int byteCmd = value & 0xff;
					out.write(byteCmd);
					Log.v("gao"," send bytes:" + Integer.toHexString(byteCmd) );
				}
				out.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 发送字符串
	 * 
	 * @param s
	 *            字符串
	 * @return 发送成功返回true
	 */
	public boolean send(String s) {
		if (out != null) {
			try {

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);
				dos.writeInt(0x55);
				dos.writeInt(0x55);
				dos.writeInt(0x55);

				dos.writeInt(0x01);
				dos.writeInt(0x01);
				dos.writeInt(0x00);
				dos.writeInt(0x01);

				dos.writeInt(0xaa);
				dos.writeInt(0xaa);
				dos.writeInt(0xaa);

				byte[] bytes = byteArrayOutputStream.toByteArray();
				dos.close();
				byteArrayOutputStream.close();
				out.write(bytes);
				Log.v("gao"," send bytes:" + bytes.toString());
				out.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}






	public static int byte2int(byte[] res) {
// 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000

		int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
				| ((res[2] << 24) >>> 8) | (res[3] << 24);
		return targets;
	}
	/**
	 * 监听Socket接收的数据(新线程中运行)
	 */
	@Override
	public void run() {

//		Log.v("gao","run....");

		List dataByteList = new ArrayList();
		try {
			in = new DataInputStream(this.socket.getInputStream());
			out = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			Log.getStackTraceString(e);
			runFlag = false;
		}
		while (runFlag) {

//			Log.v("gao","runFlag s....");
			try {

				//byte[] data = new byte[BUFFER_SIZE];
				//BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

				//String one = dataInputStream.readUTF();
				//int two = dataInputStream.readInt();
				//int three = dataInputStream.readInt();
                int available =  dataInputStream.available();

				//if (available != 0) {
					byte data = dataInputStream.readByte();
				    if (data != 0) {
						dataByteList.add(Integer.toHexString(data));
					}
				//}

				//Log.v("gao"," available:" + available);


				//String rexml = String.valueOf(data, 0, len);
				//final String s = in.readUTF();
				//Log.v("gao","receive s...." + Integer.toHexString(one) + " two: " + Integer.toHexString(two) + " three: " + Integer.toHexString(three));

			    //int content = byte2int(data);
				Log.v("gao"," recevice : " + dataByteList.size());

				for (int i = 0; i < dataByteList.size(); i++) {

					Log.v("gao","receive byte:" + (dataByteList.get(i)));
					//data[i]

				}

				if (dataByteList.size() > 0 && dataInputStream.available() == 0) {
					this.onReceive(addr, dataByteList);
				}
				//runFlag = false;
			} catch (IOException e) {
				// 连接被断开(被动)
				Log.getStackTraceString(e);
				Log.v("gao","io exception..." + e.getStackTrace());
				runFlag = false;
			}
		}
		// 断开连接
//		try {
//			in.close();
//			out.close();
//			socket.close();
//			in = null;
//			out = null;
//			socket = null;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		//this.onDisconnect(addr);  j,jl
	}

	/**
	 * 接收到数据
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param addr
	 *            连接到的Socket地址
	 * @param
	 *
	 */
	public abstract void onReceive(InetAddress addr, List list);

	/**
	 * 连接断开
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param addr
	 *            连接到的Socket地址
	 */
	public abstract void onDisconnect(InetAddress addr);
}
