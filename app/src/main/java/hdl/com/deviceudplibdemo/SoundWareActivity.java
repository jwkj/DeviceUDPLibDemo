package hdl.com.deviceudplibdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.hdl.elog.ELog;
import com.jwkj.device.entity.LocalDevice;
import com.jwkj.device.soundwave.ResultCallback;
import com.jwkj.device.soundwave.SoundWaveSender;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 声波配网页面.
 *
 * @author HDL
 */
public class SoundWareActivity extends AppCompatActivity {

    private String wifiSSID;
    private String pwd;

    @BindView(R.id.tv_sound_ware_wifiname)
    TextView tvWifiName;

    @BindView(R.id.tv_sound_ware_pwd)
    TextView tvPwd;

    @BindView(R.id.tv_sound_ware_log)
    TextView tvLog;

    @BindView(R.id.tv_sound_ware_tip)
    TextView tvTip;

    /**
     * 是否配网成功
     */
    private boolean isSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_ware);
        ButterKnife.bind(this);
        tvTip.setSelected(true);
        wifiSSID = getIntent().getStringExtra("wifiSSID");
        tvWifiName.setText(wifiSSID);
        pwd = getIntent().getStringExtra("pwd");
        tvPwd.setText("wifi密码：" + pwd);
    }

    /**
     * 发送声波
     */
    @OnClick(R.id.btn_sound_ware_send)
    public void sendSoundWare() {
        isSuccess = false;
        tvLog.append("\n声波发送中....");
        sendSoundWave();
    }

    /**
     * 发送声波
     */
    private void sendSoundWave() {
        SoundWaveSender.getInstance()
                .with(this)
                .setWifiSet(wifiSSID, pwd)
                .send(new ResultCallback() {

                    @Override
                    public void onNext(LocalDevice localDevice) {
                        ELog.e("配网成功了 localDevice=" + localDevice);
                        tvLog.append("\n设备联网成功：（设备信息）" + localDevice.toString());
                        isSuccess = true;//标记成功了
                        SoundWaveSender.getInstance().stopSend();//配网成功之后尽快停止任务
                        //进入设置密码页面
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        ELog.e("" + throwable);
                        tvLog.append("\n出错：" + throwable.toString());
                        SoundWaveSender.getInstance().stopSend();//出错了就要停止任务，然后重新发送
                        sendSoundWave();
                    }

                    /**
                     * 当声波停止的时候（每次声波发送都是有时长的，播放结束就会停止）
                     */
                    @Override
                    public void onStopSend() {
                        if (!isSuccess) {//如果没有成功，则继续发送声波
                            ELog.e("继续发送声波");
                            tvLog.append("\n继续发送声波...");
                            sendSoundWave();
                        } else {//结束了就需要将发送器关闭
                            SoundWaveSender.getInstance().stopSend();
                            ELog.e("关闭声波");
                            tvLog.append("\n停止发送声波");
                        }
                    }
                });
    }

    /**
     * 停止声波发送
     */
    @OnClick(R.id.btn_sound_ware_close)
    public void closeSoundWare() {
        SoundWaveSender.getInstance().with(this).stopSend();
        tvLog.append("\n停止发送声波");
    }

    /**
     * 绑定生命周期
     */
    @Override
    protected void onStop() {
        SoundWaveSender.getInstance().with(this).stopSend();
        finish();
        super.onStop();
    }
}
