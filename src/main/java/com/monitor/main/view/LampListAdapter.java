package com.monitor.main.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.monitor.main.R;
import com.monitor.main.model.LampInfo;

import java.util.List;

/**
 * Created by gaolixiao on 2017/4/22.
 */
public class LampListAdapter extends BaseAdapter {

    private List<LampInfo> lampInfos;
    private Context mContext;
    private View rootView;

    public LampListAdapter(Context mContext,List<LampInfo> lampInfos) {
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LampInfo lampInfo = lampInfos.get(position);

        if (convertView == null) {
           convertView =  LayoutInflater.from(mContext).inflate(R.layout.layout_interest_recommend_item,null);
        }
        rootView = convertView.findViewById(R.id.layout_interest_recommend_item);
        TextView lampNameText =  (TextView) convertView.findViewById(R.id.tv_interest_title);
        lampNameText.setText(lampInfo.getLampName());

        final ImageView lampImage = (ImageView) convertView.findViewById(R.id.lamp_image);
        Button switchButton = (Button)convertView.findViewById(R.id.lamp_switch);
        Button switchDelayButton = (Button) convertView.findViewById(R.id.lamp_switch_delay);

        switch (lampInfo.getLampStatus()) {

            case 0:
                lampImage.setImageResource(R.drawable.device_status_poweroff);
                break;
            case 1:
                lampImage.setImageResource(R.drawable.device_status_poweron);
                break;
            default:
                break;
        }

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lampInfo.getLampStatus() == 0) {
                    lampInfo.setLampStatus(1);
                    lampImage.setImageResource(R.drawable.device_status_poweron);
                } else {
                    lampInfo.setLampStatus(0);
                    lampImage.setImageResource(R.drawable.device_status_poweroff);
                }

            }
        });


       // TextView lampCodeText = (TextView) convertView.findViewById(R.id.tv_interest_tip);
       // lampCodeText.setText(lampInfo.getLampCode());
//        int which = position % 6;
//        switch (which) {
//            case 0:
//                rootView.setBackgroundResource(R.drawable.bg_interest_item1);
//                break;
//            case 1:
//                rootView.setBackgroundResource(R.drawable.bg_interest_item2);
//                break;
//            case 2:
//                rootView.setBackgroundResource(R.drawable.bg_interest_item3);
//                break;
//            case 3:
//                rootView.setBackgroundResource(R.drawable.bg_interest_item4);
//                break;
//            case 4:
//                rootView.setBackgroundResource(R.drawable.bg_interest_item5);
//                break;
//            case 5:
//                rootView.setBackgroundResource(R.drawable.bg_interest_item6);
//                break;
//        }



        return convertView;
    }
}
