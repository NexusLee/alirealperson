/********* aliRealPerson.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <RPSDK/RPSDK.h>

@interface aliRealPerson : CDVPlugin {
  // Member variables go here.
}

- (void)startRealPerson:(CDVInvokedUrlCommand*)command;
@end

@implementation aliRealPerson

-(void) pluginInitialize{
    [RPSDK setup];
}

- (void)startRealPerson:(CDVInvokedUrlCommand*)command
{
    __block CDVPluginResult* pluginResult = nil;
    NSString* verifyToken = [command.arguments objectAtIndex:0];

    if (verifyToken != nil && [verifyToken length] > 0) {
        UIViewController * currentController = [self currentViewController];
        [RPSDK startByNativeWithVerifyToken: verifyToken
                        viewController: currentController
                            completion:^(RPResult * _Nonnull result) {
            // 建议接入方调用实人认证服务端接口DescribeVerifyResult，来获取最终的认证状态，并以此为准进行业务上的判断和处理。
            NSLog(@"实人认证结果：%@", result);
            switch (result.state) {
                case RPStatePass:
                    // 认证通过。
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                    break;
                case RPStateFail:
                    // 认证不通过。
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"认证失败"];
                    break;
                case RPStateNotVerify:
                    // 未认证。
                    // 通常是用户主动退出或者姓名身份证号实名校验不匹配等原因导致。
                    // 具体原因可通过result.errorCode来区分（详见文末错误码说明表格）。
                    if ([result.errorCode isEqual:@"-1"]){
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"用户退出认证"];
                    } else {
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:result.message];
                    }
                    break;
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Expected one non-empty string argument."];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }

}

//获取Window当前显示的ViewController
- (UIViewController*)currentViewController{
    //获得当前活动窗口的根视图
    UIViewController* vc = [UIApplication sharedApplication].keyWindow.rootViewController;
    while (1)
    {
        //根据不同的页面切换方式，逐步取得最上层的viewController
        if ([vc isKindOfClass:[UITabBarController class]]) {
            vc = ((UITabBarController*)vc).selectedViewController;
        }
        if ([vc isKindOfClass:[UINavigationController class]]) {
            vc = ((UINavigationController*)vc).visibleViewController;
        }
        if (vc.presentedViewController) {
            vc = vc.presentedViewController;
        }else{
            break;
        }
    }
    return vc;
}

@end
