package hdl.com.deviceudplibdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hdl.elog.ELog;

import butterknife.ButterKnife;
import butterknife.OnClick;
import hdl.com.deviceudplibdemo.entity.PageType;
import hdl.com.lib.runtimepermissions.HPermissions;
import hdl.com.lib.runtimepermissions.PermissionsResultAction;

/**
 * 功能引导页面
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        /**
         * 请求所有清单文件中的权限
         */
        HPermissions.getInstance()
                .requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        ELog.e("权限已被授予");
                    }

                    @Override
                    public void onDenied(String s) {
                        ELog.e("权限被拒绝了" + s);
                    }
                });
    }

    @OnClick(R.id.tv_home_lan_search)
    void toLanSearch() {
        ELog.e("进入局域网搜索页面");
        startActivity(new Intent(this, LANActivity.class));
    }

    @OnClick(R.id.tv_home_soundware)
    void toSoundWare() {
        ELog.e("进入声波配网页面");
        startActivity(new Intent(this, InitSDKActivity.class).putExtra("pageType", PageType.SOUND_WARE));
    }

    @OnClick(R.id.tv_home_ap)
    void toAP() {
        ELog.e("进入AP配网页面");
        startActivity(new Intent(this, InitSDKActivity.class).putExtra("pageType", PageType.AP_MODE));
    }
}
