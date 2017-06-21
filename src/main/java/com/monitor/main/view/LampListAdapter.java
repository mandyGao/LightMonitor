package com.monitor.main.view;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.monitor.main.MainActivity;
import com.monitor.main.MyLog;
import com.monitor.main.R;
import com.monitor.main.model.LampInfo;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by gaolixiao on 2017/4/22.
 */
public class LampListAdapter extends BaseAdapter {

    static MyLog log = new MyLog("LampListAdapter");
    private List<LampInfo> lampInfos;
    private MainActivity mContext;


    public LampListAdapter(MainActivity mContext, List<LampInfo> lampInfos) {
        this.lampInfos = lampInfos;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return lampInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return lampInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        log.debug("get item id "+ position);
        return position;
    }

    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                if(v instanceof EditText){
                    ((EditText) v).setSelection(((EditText) v).length());
                }
            }
        }
    };


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final LampInfo lampInfo = lampInfos.get(position);
        log.debug("getView execute " + position + "\t" + lampInfo);
        final ViewHolder holder;
        if (convertView == null) {
            convertView =  LayoutInflater.from(mContext).inflate(R.layout.layout_interest_recommend_item,null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.mEditText =  (EditText) convertView.findViewById(R.id.tv_interest_title);
            holder.mTextView =  (TextView) convertView.findViewById(R.id.tv_interest_title_code);
            holder.mEditText.setOnFocusChangeListener(mFocusChangeListener);
            holder.powerSwitch = (Button)convertView.findViewById(R.id.lamp_switch);
            holder.mImageview = (ImageView) convertView.findViewById(R.id.lamp_image);
            holder.delayButton = (Button) convertView.findViewById(R.id.lamp_switch_delay);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }



        switch (lampInfo.getLampStatus()) {

            case 0x02:
                holder.mImageview.setImageResource(R.drawable.device_status_poweroff);
                break;
            case 0x01:
                holder.mImageview.setImageResource(R.drawable.device_status_poweron);
                break;
            default:
                holder.mImageview.setImageResource(R.drawable.device_status_poweroff);
                break;
        }


        holder.powerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lampInfo.getLampStatus() == 0x01) {
                    setPowerOn(lampInfo,false);
                } else {
                    setPowerOn(lampInfo,true);
                }
            }
        });

        TextWatcher textWatcher = (TextWatcher) holder.mEditText.getTag();
        if(textWatcher != null){
            holder.mEditText.removeTextChangedListener(textWatcher);
        }

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                log.debug(s + "   " + position);
                lampInfo.setLampName(s+"");
            }
        };

        holder.mEditText.addTextChangedListener(textWatcher);
        holder.mEditText.setTag(textWatcher);

        holder.mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.debug(holder.mEditText.getTag());
            }
        });

        holder.delayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = LayoutInflater.from(mContext).inflate(R.layout.time_pick,null);
                new AlertDialog.Builder(mContext)
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TimePicker timer = (TimePicker) view.findViewById(R.id.time);
                                log.debug("hour " + timer.getHour());
                                log.debug("minute " + timer.getMinute());
                                log.debug(timer.getDrawingTime());


                                Intent intent = new Intent("com.monitor.main.TIME_TASK");
                                intent.putExtra("position",position);
                                intent.putExtra("code",lampInfo.getLampCode());
                                //定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
                                PendingIntent pi = PendingIntent.getBroadcast(mContext,0,intent,0);


                                //AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
                                AlarmManager am = (AlarmManager)mContext.getSystemService(ALARM_SERVICE);

                                am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),5*1000,pi);

                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.MINUTE,timer.getMinute());
                                calendar.set(Calendar.HOUR_OF_DAY,timer.getHour());

                                am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pi);
                            }
                        })
                        .setCancelable(false)
                        .show()
                        ;

            }
        });


        holder.mEditText.setText(lampInfo.getLampName());
        holder.mEditText.setEnabled(lampInfo.isEditLampName());
        holder.mTextView.setText("" + lampInfo.getLampCode());
        return convertView;
    }

    private void setPowerOn(LampInfo info,boolean isPowerOn){
        mContext.sendStr(MainActivity.getSwichCmd(info.getLampCode(),isPowerOn));
        mContext.sendStr(MainActivity.readLampState(info.getLampCode()));
    }


    public static class TimerTaskReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            log.info("onReceive " + intent);
            log.info("context " + context);

            if(MainActivity.socketTransceiver != null){
                int code = intent.getIntExtra("code",-1);
                MainActivity.socketTransceiver.send(MainActivity.getSwichCmd(code,false));
                MainActivity.socketTransceiver.send(MainActivity.readLampState(code));
            }

        }
    }

    static class ViewHolder {
        public EditText mEditText;
        public TextView mTextView;
        public Button powerSwitch;
        public ImageView mImageview;
        public Button delayButton;

    }
}
