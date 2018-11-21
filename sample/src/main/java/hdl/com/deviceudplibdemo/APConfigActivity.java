package hdl.com.deviceudplibdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hdl.elog.ELog;
import com.jwkj.device.apmode.APManager;
import com.jwkj.device.apmode.ResultCallback;
import com.jwkj.device.entity.APDeviceConfig;
import com.jwkj.device.entity.SSIDType;
import com.jwkj.device.utils.WifiUtils;
import com.p2p.core.utils.MyUtils;
import com.p2p.core.utils.UDPApHander;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdl.com.deviceudplibdemo.utils.WifiTool;

/**
 * AP配网
 */
public class APConfigActivity extends AppCompatActivity {

    public final static int ON_START = 1;

    public final static int ON_STATE_CHANGE = 2;

    public final static int ON_ERROR = 3;

    public final static int ON_SUCCESS = 4;

    @BindView(R.id.tv_apconfig_wifiname)
    TextView tvWifiName;

    @BindView(R.id.tv_apconfig_log)
    TextView tvLog;

    @BindView(R.id.tv_apconfig_wifi_pwd)
    TextView tvWifiPwd;

    @BindView(R.id.tv_apconfig_device_pwd)
    TextView tvDevicePwd;

    private String wifiSSID;

    private String apWifiSSID;

    private String wifiPwd;

    private String devicePwd;
    /**
     * wifi加密类型
     */
    private SSIDType wifiType = SSIDType.NONE;
    private WifiTool tool;
    private List<String> scanSSIDsResult = new ArrayList<>();

    @BindView(R.id.rv_ap_list)
    RecyclerView rvAPWifiList;
    private CommonAdapter<String> apWifiListAdapter;

    APDeviceConfig deviceConfig;

    @BindView(R.id.select_wifi)
    TextView selectWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apconfig);
        ButterKnife.bind(this);
        tool = new WifiTool(getApplicationContext());
        //获取wifi列表
        getAPWifiList();
        wifiSSID = getIntent().getStringExtra("wifiSSID");
        tvWifiName.setText(wifiSSID);
        wifiPwd = getIntent().getStringExtra("pwd");
        tvWifiPwd.setText("wifi密码：" + wifiPwd);
        devicePwd = getIntent().getStringExtra("devicePwd");
        tvDevicePwd.setText("设备密码：" + devicePwd);
        wifiType = (SSIDType) getIntent().getSerializableExtra("type");
        ELog.e(wifiSSID);
        ELog.e(wifiPwd);
        ELog.e(wifiType.getValue());
        rvAPWifiList.setLayoutManager(new LinearLayoutManager(this));
        apWifiListAdapter = new CommonAdapter<String>(this, R.layout.item_ap_wifi, scanSSIDsResult) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                holder.setText(R.id.tv_wifi_name, s);
            }
        };
        rvAPWifiList.setAdapter(apWifiListAdapter);
        apWifiListAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                ELog.e(scanSSIDsResult.get(position));
                apWifiSSID = scanSSIDsResult.get(position);
                if (!TextUtils.isEmpty(apWifiSSID)) {
                    selectWifi.setVisibility(View.VISIBLE);
                    selectWifi.setText(apWifiSSID);
                }else{
                    selectWifi.setVisibility(View.GONE);
                }

//                tool.connectWifiTest(scanSSIDsResult.get(position),"");
                try {
                    WifiUtils.getInstance().with(APConfigActivity.this).connectWifi(scanSSIDsResult.get(position), "", SSIDType.PSK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
    }

    /**
     * 获取ap模式的wifi列表
     */
    private void getAPWifiList() {
        new Thread() {
            @Override
            public void run() {
                scanSSIDsResult.addAll(tool.accordSsid());
                ELog.e("result = " + scanSSIDsResult);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        apWifiListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }


    @OnClick(R.id.btn_apconfig_send)
    public void onSend() {
        if (!WifiUtils.getInstance().isConnectWifi(apWifiSSID)) {
            tvLog.append("\n\n需要先将WiFi连接到设备发出的热点网络");
            return;
        }
        ELog.e("发送了");
        if (TextUtils.isEmpty(apWifiSSID)) {
            return;
        }
        deviceConfig = new APDeviceConfig(wifiSSID, wifiPwd, apWifiSSID, devicePwd);
        String deviceId = apWifiSSID.substring(6);
        deviceConfig.setDeviceID(deviceId);
        APManager.getInstance()
                .with(this)
                .setApDeviceConfig(deviceConfig)
                .send(new ResultCallback() {
                    @Override
                    public void onStart() {
                        ELog.e("任务开始了");
                        Message msg = Message.obtain();
                        msg.what = ON_START;
                        Bundle data = new Bundle();
                        data.putString("result_data","\n\n任务开始了...");
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onStateChange(String deviceId, int stateCode) {
                        if (0 == stateCode) {
                            Message msg = Message.obtain();
                            msg.what = ON_STATE_CHANGE;
                            Bundle data = new Bundle();
                            data.putString("result_data","\n\n设备"+deviceId+"收到wifi名字和密码了");
                            msg.setData(data);
                            mHandler.sendMessage(msg);
                            Log.e("hdltag", "onNext(APManager.java:140):设备收到wifi名字和密码了");
                            Log.e("hdltag", "onNext(APManager.java:141):设备已经收到了，停止接收，然后再发送确认wifi");
                        }

                    }

                    @Override
                    public void onConfigPwdSuccess(String deviceId, int stateCode) {
                        ELog.e("配置wifi成功了");

                        Message msg = Message.obtain();
                        msg.what = ON_SUCCESS;
                        Bundle data = new Bundle();
                        data.putString("result_data","\n\n设备："+deviceId+"配置wifi成功了");
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        ELog.e("任务出错了" + throwable);
                        Message msg = Message.obtain();
                        msg.what = ON_ERROR;
                        Bundle data = new Bundle();
                        data.putString("result_data","\n任务出错了:\n"+throwable.getMessage());
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String msgData = msg.getData().getString("result_data");
            if (!TextUtils.isEmpty(msgData)) {
                tvLog.append(msgData);
            }
            switch (msg.what) {
                case ON_START:

                    break;
                case ON_STATE_CHANGE:

                    break;
                case ON_ERROR:
                    tvLog.append("\n\n任务出错了" + msgData);
                    break;
                case ON_SUCCESS:
                    try {
                        WifiUtils.getInstance().with(APConfigActivity.this).connectWifi(wifiSSID, wifiPwd, wifiType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @OnClick(R.id.btn_apconfig_close)
    public void onStopTask() {
        APManager.getInstance().with(this).stopSend();
    }

}
