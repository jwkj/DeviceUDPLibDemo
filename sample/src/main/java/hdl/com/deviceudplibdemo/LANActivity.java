package hdl.com.deviceudplibdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hdl.elog.ELog;
import com.jwkj.device.entity.LocalDevice;
import com.jwkj.device.shake.ShakeListener;
import com.jwkj.device.shake.ShakeManager;

import java.util.Set;
import java.util.TreeSet;

/**
 * 局域网搜索页面
 */
public class LANActivity extends AppCompatActivity {

    private TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan);
        tvLog = (TextView) findViewById(R.id.tv_log);
    }

    private Set<LocalDevice> devices = new TreeSet<>();

    public void onScan(View view) {
        ShakeManager.getInstance()
                .shaking(new ShakeListener() {
                    @Override
                    public void onNext(LocalDevice device) {
                        devices.add(device);
                        tvLog.setText("拿到结果了" + device + "\n\n" + tvLog.getText().toString());
//                tvLog.append("拿到结果了"+device+"\n\n");
                        ELog.e("hdltag", device);
//                ELog.e("hdltag", Arrays.toString(device.getResultData()));
                    }

                    /**
                     * 搜索开始的时候回调
                     */
                    @Override
                    public void onStart() {
                        super.onStart();
//                tvLog.append("开始搜索了\n\n");
                        tvLog.setText("开始搜索了\n\n" + tvLog.getText().toString());
                    }

                    /**
                     * 搜索发生错误的时候开始回调
                     *
                     * @param throwable
                     */
                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
//                tvLog.append("失败了" + throwable + "\n\n");
                        tvLog.setText("失败了" + throwable + "\n\n" + tvLog.getText().toString());
                    }

                    /**
                     * 搜索结束的时候回调
                     */
                    @Override
                    public void onCompleted() {
                        tvLog.setText("扫描完成了\n\n" + tvLog.getText().toString());
//                tvLog.append("扫描完成了\n\n");
                        tvLog.setText("接收到的个数为：" + devices.size() + "\n\n" + tvLog.getText().toString());
//                tvLog.append("接收到的个数为："+devices.size()+"\n\n");
//                tvLog.append("内容：\n\n");
                        tvLog.setText("内容：\n\n" + tvLog.getText().toString());
                        for (LocalDevice device : devices) {
//                    tvLog.append("id = "+device.getId()+"\n\n");
                            tvLog.setText("id = " + device.getId() + "\n\n" + tvLog.getText().toString());
                        }
                        super.onCompleted();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ShakeManager.getInstance().isShaking()) {
            ShakeManager.getInstance().closeShake();
        }
    }
}
