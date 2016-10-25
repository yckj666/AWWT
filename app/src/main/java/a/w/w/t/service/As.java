package a.w.w.t.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

import a.w.w.t.util.SP;

/**
 * Created by duhuanhuan on 2016/9/27.
 */


public class As extends AccessibilityService {


    private boolean enableKeyguard = true;//默认有屏幕锁
    private SP sp;

    private boolean isBack=false;//判断是否是手动进入红包详情
    private boolean isHB=false;//是否是本程序点击的
    //锁屏、解锁相关
    private KeyguardManager km;
    private DecimalFormat df = new DecimalFormat("######0.00");
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private String msg ="领取红包";
    private int X=0;
    private AccessibilityEvent event;



    //接收事件,如触发了通知栏变化、界面变化等
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        this.event = event;

       // findFocus()
        //第一种是通过节点View的Text内容来查找
        //findAccessibilityNodeInfosByText("查找内容")
        switch (event.getEventType()){
            //第一步：监听通知栏消息
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:

                isBack = false;

                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.i("AAAA", "text:"+content);
                        if (content.contains("[微信红包]")) {
                            //wakeAndUnlock(true);
                            //模拟打开通知栏消息
                            if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                break;

            //第二步：监听是否进入微信红包消息界面
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                X++;
                Log.i("领取红包后点击-----》", "X---"+X);
                String className = event.getClassName().toString();
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    //开始抢红包
                    if (X<4){
                        getPacket();
                    }else {
                        X=0;
                    }

                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    //开始打开红包


                        openPacket();

                }else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                     //返回
                    Log.i("demo", "返回");

                    if (isHB){
                        selectHB_JL();
                        isHB=false;
                    }

                    if (!isBack){

                        backP();
                    }

                }
                Log.i("领取红包后点击-----》", "打开拆红包界面12");

                break;

            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                String nn=event.getClassName().toString();

                    Log.i("领取红包后点击-----》", "触摸");
                   // isBack =true;

                break;
        }




    }
    @SuppressLint("NewApi")
    private void selectHB_JL() {
        //查询抢到的金额 累加 存储以供查询
        List<AccessibilityNodeInfo> accessibilityNodeInfosByViewId = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bb0");
        if (accessibilityNodeInfosByViewId.size()>0){
            String JL=accessibilityNodeInfosByViewId.get(0).getText().toString();
            Double aDouble=Double.parseDouble(sp.getHBTotle_JL())+Double.parseDouble(JL);
            String format = df.format(aDouble);
            sp.saveHB_JL(format);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public void backP(){
    this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
}

    //唤醒屏幕和解锁
    private void wakeAndUnlock(boolean unLock)
    {
        if(unLock)
        {
            //若为黑屏状态则唤醒屏幕
            if(!pm.isScreenOn()) {
                //获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
                wl = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON| PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
                //点亮屏幕
                wl.acquire();
                Log.i("demo", "亮屏");
            }
            //若在锁屏界面则解锁直接跳过锁屏
            if(km.inKeyguardRestrictedInputMode()) {
                //设置解锁标志，以判断抢完红包能否锁屏
                enableKeyguard = false;
                //解锁
                kl.disableKeyguard();
                Log.i("demo", "解锁");
            }
        }
        else
        {
            //如果之前解过锁则加锁以恢复原样
            if(!enableKeyguard) {
                //锁屏
                kl.reenableKeyguard();
                Log.i("demo", "加锁");
            }
            //若之前唤醒过屏幕则释放之使屏幕不保持常亮
            if(wl != null) {
                wl.release();
                wl = null;
                Log.i("demo", "关灯");
            }
        }
    }





    @SuppressLint("NewApi")
    private void getPacket() {
        //查询红包节点id
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

         recycle(rootNode);
    }

    /**
     * 查找到拆红包按钮id
     */
    @SuppressLint("NewApi")
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bdg");
            for (AccessibilityNodeInfo n : list) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                isHB=true;
                /**抢到的总红包**/
                sp.saveHB(sp.getHBTotle()+1);

            }
        }

    }
    //接收按键事件
    @Override
    protected boolean onKeyEvent(KeyEvent event) {

//        switch (event.getAction()){
//            case KeyEvent.KEYCODE_BACK:
//                isBack = false;
//                break;
//            case KeyEvent.KEYCODE_HOME:
//                isBack = false;
//                break;
//        }



        return super.onKeyEvent(event);

    }


    //服务中断，如授权关闭或者将服务杀死
    @Override
    public void onInterrupt() {

        Toast.makeText(this,"抢红包服务已被关闭，请重启开启服务",Toast.LENGTH_SHORT).show();

    }

    /**
     * 打印一个节点的结构
     * @param info
     */
    @SuppressLint("NewApi")
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if(info.getText() != null){
                if(msg.equals(info.getText().toString())){
                    //这里有一个问题需要注意，就是需要找到一个可以点击的View
                    Log.i("demo", "Click"+",isClick:"+info.isClickable());
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    AccessibilityNodeInfo parent = info.getParent();
                    if (parent!=null){
                        Log.i("领取红包后点击-----》", "-----:"+parent.isClickable());
                        if(parent.isClickable()){
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                        }
                    }

                }
            }

        } else {

            int x=info.getChildCount();
            for (int i = 0; x>i ;x-- ) {
                if(info.getChild(x-1)!=null){
                    recycle(info.getChild(x-1));
                }
            }
        }
    }
    //连接服务后,一般是在授权成功后会接收到
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

//       //获取电源管理器对象
//        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
//        //得到键盘锁管理器对象
//        km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
//        //初始化一个键盘锁管理器对象
//        kl = km.newKeyguardLock("unLock");
        sp = new SP(this);
        Toast.makeText(this,"服务已开启，开心抢红包吧！",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       // wakeAndUnlock(false);
    }
}
