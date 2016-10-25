package a.w.w.t.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by duhuanhuan on 2016/9/30.
 */

public class SP {
    private Context context;
    private String hb="hb";
    SharedPreferences.Editor editor;
    SharedPreferences sp;
    public SP(Context context){
        this.context = context;
        sp = context.getSharedPreferences(hb, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    //保存红包个数
    public void saveHB(int i){
        editor.putInt("totle_hb",i);
        editor.commit();
    }
    //获取红包个数
    public int getHBTotle(){
        return sp.getInt("totle_hb",0);
    }
    //保存红包金额
    public void saveHB_JL(String x){
        editor.putString("totle_jl",x);
        editor.commit();
    }
    //获取红包金额
    public String getHBTotle_JL(){
        return sp.getString("totle_jl","0.00");
    }

    public void cleraHB(){
        editor.clear();
        editor.commit();
    }

}
