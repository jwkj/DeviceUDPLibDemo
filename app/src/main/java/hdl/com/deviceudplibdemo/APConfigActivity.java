package hdl.com.deviceudplibdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hdl.elog.ELog;
import com.jwkj.device.apmode.APManager;
import com.jwkj.device.apmode.ResultCallback;
import com.jwkj.device.entity.APDeviceConfig;
import com.jwkj.device.entity.SSIDType;
import com.jwkj.device.utils.WifiUtils;
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
        ELog.e("发送了");
//        String pwd = P2PHandler.getInstance().EntryPassword("123");
//        byte[] wsy12345678s = MyUtils.getApSendWifi("11922739", "WeiSanYun-YanFa", "wsy12345678", 2, "123");

//        ELog.e("pwd = "+ Arrays.toString(wsy12345678s));
        APManager.getInstance()
                .with(this)
                .setApDeviceConfig(new APDeviceConfig(apWifiSSID, "", devicePwd))
                .send(new ResultCallback() {
                    @Override
                    public void onStart() {
                        ELog.e("任务开始了");
                        tvLog.append("\n\n任务开始了");
                    }

                    @Override
                    public void onConfigPwdSuccess() {
                        ELog.e("配置wifi成功了");
                        tvLog.append("\n\n配置wifi成功了");
                        try {
                            WifiUtils.getInstance().with(APConfigActivity.this).connectWifi(wifiSSID, wifiPwd, wifiType);
                        } catch (Exception e) {
                            e.printStackTrace();
                            tvLog.append("\n\n连接之前的wifi出错了");
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        ELog.e("任务出错了" + throwable);
                        tvLog.append("\n\n任务出错了" + throwable);
                    }
                });
    }

    @OnClick(R.id.btn_apconfig_close)
    public void onStopTask() {
        APManager.getInstance().with(this).stopSend();
    }
}
