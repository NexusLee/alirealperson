package com.paddington.cordova.realperson;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.security.cloud.CloudRealIdentityTrigger;
import com.alibaba.security.realidentity.RPConfig;
import com.alibaba.security.realidentity.RPEventListener;
import com.alibaba.security.realidentity.RPResult;
import com.alibaba.security.realidentity.RPVerify;
import com.alibaba.security.rp.RPSDK;

import android.content.Context;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class aliRealPerson extends CordovaPlugin {
    private final String TAG = "RealPerson";
    Context context = null;
    RPConfig config = null;

    @Override
    protected void pluginInitialize() {
        this.context = this.cordova.getActivity().getApplicationContext();
        RPConfig.Builder configBuilder = new RPConfig.Builder()
            .setSkinInAssets(true) // 是否是内置皮肤。
            .setSkinPath("skin"); // 设置皮肤路径。
        this.config = configBuilder.build();
        RPVerify.init(this.context);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if("startRealPerson".equals(action)) {
            String token = args.getString(0);
            this.startRealPerson(token, callbackContext);
            return true;
        }
        return false;
    }

    private void startRealPerson(String token, CallbackContext callbackContext) {
        if (token != null && token.length() > 0) {
            RPVerify.start(this.context, token, this.config, new RPEventListener() {
                @Override
                public void onFinish(RPResult auditResult, String code, String msg) {
                    if (auditResult == RPResult.AUDIT_PASS) {
                        // 认证通过。建议接入方调用实人认证服务端接口DescribeVerifyResult来获取最终的认证状态，并以此为准进行业务上的判断和处理。
                        // do something
                        callbackContext.success("SUCCESS");
                    } else if (auditResult == RPResult.AUDIT_FAIL) {
                        // 认证不通过。建议接入方调用实人认证服务端接口DescribeVerifyResult来获取最终的认证状态，并以此为准进行业务上的判断和处理。
                        // do something
                        callbackContext.error("认证失败");
                    } else if (auditResult == RPResult.AUDIT_NOT) {
                        // 未认证，具体原因可通过code来区分（code取值参见错误码说明），通常是用户主动退出或者姓名身份证号实名校验不匹配等原因，导致未完成认证流程。
                        // do something
                        Log.d(TAG, code + ": " + msg);
                        if("-1".equals(code)) {
                            callbackContext.error("用户退出认证");
                        }else if("-10".equals(code)) {
                            callbackContext.error("请检查摄像头及权限");
                        }else if("-30".equals(code)) {
                            callbackContext.error("网络异常，请检查网络");
                        }else if("-50".equals(code)) {
                            callbackContext.error("用户活体失败次数超过限制");
                        }else if("-60".equals(code)) {
                            callbackContext.error("手机的本地时间和网络时间不同步");
                        }else if("-10000".equals(code)) {
                            callbackContext.error("客户端发生未知错误");
                        }else if("3001".equals(code)) {
                            callbackContext.error("认证token无效或已过期");
                        }else if("3104".equals(code)) {
                            callbackContext.error("认证已通过，重复提交");
                        }else if("3203".equals(code)) {
                            callbackContext.error("设备不支持刷脸");
                        }else {
                            callbackContext.error(msg);
                        }
                    }
                }
            });
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
