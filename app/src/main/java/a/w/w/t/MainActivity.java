package a.w.w.t;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import a.w.w.t.service.As;
import a.w.w.t.util.SP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG="Main";
    private TextView totle_hb,totle_jl;
    SP sp;
    private Button open;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = new SP(MainActivity.this);
        totle_hb= (TextView) findViewById(R.id.totle_hb);
        totle_jl= (TextView) findViewById(R.id.totle_jl);

        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this,"57ee1e07e0f55a24d3000406","01",MobclickAgent.EScenarioType.E_UM_NORMAL));

        //sp.cleraHB();
        totle_hb.setText("共抢到："+sp.getHBTotle()+" 红包");
        totle_jl.setText("累计金额："+ sp.getHBTotle_JL()+"元");



        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,HelpActivity.class);
                ActivityOptionsCompat ss = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, v, "ss");
                ActivityCompat.startActivity(MainActivity.this,intent,ss.toBundle());

            }
        });

        open= (Button) findViewById(R.id.open);

       open.setOnClickListener(this);


        //检测服务是否在运行
        if(isAccessibilitySettingsOn()){
            open.setText(R.string.runing);
        }else {
            open.setText(R.string.open);
        }
    }

    private boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + As.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                   getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        totle_hb.setText("共抢到："+sp.getHBTotle()+" 红包");
        totle_jl.setText("累计金额："+ sp.getHBTotle_JL()+"元");
        if(isAccessibilitySettingsOn()){
            open.setText(R.string.runing);
        }else {
            open.setText(R.string.open);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onClick(View v) {
        //辅助功能
        Intent intent =  new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
       // finish();
    }
}
