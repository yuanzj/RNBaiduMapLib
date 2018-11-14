package net.rokyinfo.rn.baidumap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private SDKReceiver mReceiver;

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "RNBaiduMapLib";
    }


    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = String.valueOf(intent.getAction());
            Log.d(TAG, "action: " + s);
            switch (s) {
                case SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR:
                    Log.d(TAG, "key 验证出错! 错误码 :" + intent.getIntExtra
                            (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                            + " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
                    break;
                case SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK:
                    Log.d(TAG, "key 验证成功! 功能可以正常使用");
                    break;
                case SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR:
                    Log.d(TAG, "网络出错");
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        //如果 API level 是大于等于 23(Android 6.0) 时
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                    showToast("自Android 6.0开始需要打开位置权限才可以搜索到Ble设备");
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消监听 SDK 广播
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                // 这里进行授权被允许的处理
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                // 这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
