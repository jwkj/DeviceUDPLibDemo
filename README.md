# UDP通讯模块

三大功能模块
- 局域网设备搜索（摇一摇功能）
- 声波配网
- AP配网

基础库(依赖库)： [UDPSender](https://github.com/huangdali/UDPSender)

### 导入gradle
app/build.gradle中加入

```java
dependencies {
       ...
       //UDP功能模块库
       compile 'com.jwkj:DeviceUDPLib:v1.0.9'
       //UDP基础库（不可少）
       compile 'com.jwkj:udpsender:v2.0.2'
   }
```

### 配置混淆文件

```java
-libraryjars libs/EMTMFSDK_0101_160914.jar
-dontwarn com.lsemtmf.**
-keep class com.lsemtmf.**{*; }
-dontwarn com.larksmart.**
-keep class com.larksmart.**{*; }
-dontwarn com.jwkj.device.**
-keep class com.jwkj.device.**{*; }
```

### 权限
```java
  <!-- 声波配网所需权限 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### 局域网搜索
>详细使用见demo

```java
 ShakeManager.getInstance()
                .shaking(new ShakeListener() {
                /**
                 *搜到设备
                 */
                    @Override
                    public void onNext(LocalDevice device) {
                    }

                    /**
                     * 搜索开始的时候回调
                     */
                    @Override
                    public void onStart() {
                    }

                    /**
                     * 搜索发生错误的时候开始回调
                     *
                     * @param throwable
                     */
                    @Override
                    public void onError(Throwable throwable) {
                    }

                    /**
                     * 搜索结束的时候回调
                     */
                    @Override
                    public void onCompleted() {
                    }
                });
```

### 声波配网

**注意事项：**
- 建议将手机音量调至最大
- 建议将手机靠近设备30cm以内
- 需要将手机连接到wifi
- 暂不支持5G的wifi


#### 初始化

 在发送广播之前需要先初始化，建议在发送广播的前2s之前初始化，所以建议在发送广播的上一个配置页就初始化（见demo）

 ```java
 SoundWaveManager.init(this);//初始化声波配置
 ```

在初始化页销毁的时候,需要将声波管理器销毁以节省系统资源

```java
   /**
     * 销毁的时候也要及时销毁
     */
    @Override
    protected void onDestroy() {
        SoundWaveManager.onDestroy(this);
    }
```
#### 发送声波

```java
  SoundWaveSender.getInstance()
                .with(this)//不要忘记写哦
                .setWifiSet(wifiSSID, wifiPwd)//wifi名字和wifi密码
                .send(new ResultCallback() {
                  /**
                    *拿到结果的时候会回调（温馨提示：由于设备的重发机制，可能会收到多条重复数据，需自己处理哦）
                    */
                    @Override
                    public void onNext(UDPResult udpResult) {
                      //get result
                    }
                   /**
                     * 声波发送失败的时候会回调
                     */
                    @Override
                    public void onError(Throwable throwable) {
                        //发生错误的时候需要处理一下，一般是先关闭声波发送，再重发
                    }

                    /**
                     * 当声波停止的时候
                     */
                    @Override
                    public void onStopSend() {
                       //当声波播放完成的时候会回调，此时如果还没拿到结果，那么建议在此处重新发送声波
                    }
                });
```

#### 关闭声波发送

```java
SoundWaveSender.getInstance().stopSend();
```


此外，为了避免非正常情况退出应用导致未能及时调用stopsend()停止任务，建议在activity/fragment的生命周期销毁的时候也关闭任务

```java
   /**
     * 页面停止的时候也要及时关闭
     */
     @Override
     protected void onStop() {
           SoundWaveSender.getInstance().with(this).stopSend();
           super.onStop();
     }
```


### AP配网

>AP配网是通过设备发出的AP热点来配置设备的网络。设备复位后，发出AP热点，手机连接上AP热点，然后通过UDP与设备进行两次握手，确保app发送的WiFi信息和所要设置的密码成功发送给设备。app与设备通信完成后，退出AP模式，连接app发过来的WiFi，并设置设备的初始密码

#### AP配网流程图：

![](http://onkrb3tob.bkt.clouddn.com/ap4.png)

```java
APManager.getInstance()
                .with(this)
                .setApDeviceConfig(new APDeviceConfig(apWifiSSID, "", devicePwd))
                .send(new ResultCallback() {
                    @Override
                    public void onStart() {
                        ELog.e("任务开始了");
                    }

                    @Override
                    public void onConfigPwdSuccess() {
                        ELog.e("配置wifi成功了");
                        try {
                            WifiUtils.getInstance().with(APConfigActivity.this).connectWifi(wifiSSID, wifiPwd, wifiType);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ELog.e("连接之前的wifi出错了");
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        ELog.e("任务出错了" + throwable);
                    }
                });
```

