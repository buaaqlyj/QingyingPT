package cn.edu.hit.pt.impl;

import android.content.Context;
import cn.edu.hit.pt.Params;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBase;

public class DatabaseUtil {
	public static DataBase systemDatabase(Context context) {
		return LiteOrm.newInstance(context, "SYSTEM");
	}
	
	public static DataBase userDatabase(Context context) {
		if(Params.CURUSER == null || Params.CURUSER.id == 0)
			return null;
		String name = MD5Util.MD5("Database:" + Params.CURUSER.id);
		return LiteOrm.newInstance(context, name);
	}
}
