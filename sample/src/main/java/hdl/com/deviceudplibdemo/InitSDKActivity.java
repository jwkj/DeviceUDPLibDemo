package hdl.com.deviceudplibdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hdl.elog.ELog;
import com.jwkj.device.soundwave.SoundWaveManager;
import com.jwkj.device.utils.MUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdl.com.deviceudplibdemo.entity.PageType;

/**
 * 初始化sdk页面
 */
public class InitSDKActivity extends AppCompatActivity {
    @BindView(R.id.tv_init_sdk_wifiname)
    TextView tvWifiName;
    /**
     * wifi名字
     */
    private String wifiSSID;
    /**
     * 是否连接wifi
     */
    private boolean isConnWifi;
    /**
     * SDK是否注册成功
     */
    private boolean isSuccess;

    @BindView(R.id.et_wifi_pwd)
    EditText etPwd;

    @BindView(R.id.et_device_pwd)
    EditText etDevicePwd;

    private PageType pageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_sdk);
        ButterKnife.bind(this);
        pageType = (PageType) getIntent().getSerializableExtra("pageType");
        if (pageType == PageType.SOUND_WARE) {
            //初始化声波配置
            isSuccess = SoundWaveManager.init(this);
            ELog.e("声波注册成功了吗 isSuccess=" + isSuccess);
        }else {
            etDevicePwd.setVisibility(View.VISIBLE);
        }
        isConnWifi = getWifiName();
    }

    /**
     * 销毁的时候也要及时销毁
     */
    @Override
    protected void onDestroy() {
        if (pageType == PageType.SOUND_WARE) {
            SoundWaveManager.onDestroy(this);
        }
        super.onDestroy();
    }

    /**
     * 获取wifi名字
     *
     * @return
     */
    private boolean getWifiName() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ELog.e("SSID", wifiInfo.getSSID());
            tvWifiName.setText(wifiInfo.getSSID());
            wifiSSID = wifiInfo.getSSID();
            //去掉首尾"号
            if ("\"".equals(wifiSSID.substring(0, 1)) && "\"".equals(wifiSSID.substring(wifiSSID.length() - 1, wifiSSID.length()))) {
                wifiSSID = wifiSSID.substring(1, wifiSSID.length() - 1);
            }
            ELog.e("wifiSSID=" + wifiSSID);
            return true;
        } else {
            showMsgDialog("请将手机连接wifi");
            return false;
        }
    }

    /**
     * 显示连接wifi对话框
     */
    private void showMsgDialog(CharSequence msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @OnClick(R.id.btn_start)
    public void toStart() {
        if (!isConnWifi) {
            showMsgDialog("请将手机连接wifi");
            return;
        }
        if (pageType == PageType.SOUND_WARE) {//只有声波配网才会判断是否初始化声波配网库
            if (!isSuccess) {
                showMsgDialog("声波初始化失败，请重新进入本页面");
                return;
            }
        }else {
            String devicePwd = etDevicePwd.getText().toString().trim();
            if (TextUtils.isEmpty(devicePwd)) {
                showMsgDialog("设备密码不能为空");
                return;
            }
        }
        String pwd = etPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            showMsgDialog("请输入wifi密码");
            return;
        }
        Intent intent = new Intent();
        if (pageType == PageType.SOUND_WARE) {//去声波配网页面
            intent.setClass(this, SoundWareActivity.class);
        } else if (pageType == PageType.AP_MODE) {//去AP配网页面
            String devicePwd = etDevicePwd.getText().toString().trim();
            intent.setClass(this, APConfigActivity.class);
            intent.putExtra("devicePwd",devicePwd);
        }
        intent.putExtra("wifiSSID", wifiSSID);
        intent.putExtra("pwd", pwd);
        intent.putExtra("type", MUtils.getSSIDType(getApplicationContext()));
        startActivity(intent);
        finish();
    }
}
