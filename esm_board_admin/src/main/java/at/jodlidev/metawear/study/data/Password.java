package at.jodlidev.metawear.study.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by JodliDev on 18.09.2019.
 * If somebody wants to "hack" a foreign board, they can do that anyway. So there is no point in making this class super secure.
 */
public class Password {
	public static final String TABLE = "board_password";
	public static final String KEY_PASSWORD = "board_password";
	
	private static int password = 0; //it is assumed that is_same() is called right after connecting to a board and also that password will bot be 0 after that anymore
	
	
	public static void init(SQLiteDatabase db) {
		Cursor c = db.query(TABLE,
				new String[]{KEY_PASSWORD},
				null,
				null,
				null,
				null,
				null,
				"1");
		
		password = c.moveToFirst() ? c.getInt(0) : 0;
		
		c.close();
		
		if(password == 0) {
			password = (int) (Math.random() * Integer.MAX_VALUE);
			
			db.delete(Password.TABLE, null, null);
			ContentValues values = new ContentValues();
			values.put(KEY_PASSWORD, password);
			db.insert(TABLE, null, values);
		}
	}
	
	public static int get() {
		if(password == 0)
			Log.d("qwe", "password is 0");
		return password;
	}
//	public static int get(SQLiteDatabase db) {
//		if(password != 0)
//			return password;
//
//		Cursor c = db.query(TABLE,
//				new String[]{KEY_PASSWORD},
//				null,
//				null,
//				null,
//				null,
//				null,
//				"1");
//
//		password = c.moveToFirst() ? c.getInt(0) : 0;
//
//		c.close();
//		return password;
//	}
	
//	public static int get_or_create(SQLiteDatabase db) {
//		int pass = get(db);
//		if(pass == 0) {
//			pass = (int) (Math.random() * Integer.MAX_VALUE);
//
//			db.delete(Password.TABLE, null, null);
//			ContentValues values = new ContentValues();
//			values.put(KEY_PASSWORD, pass);
//			db.insert(TABLE, null, values);
//
//			return pass;
//		}
//		else
//			return pass;
//	}
	
	
	public static boolean is_same(int pass) {
		return password == pass;
	}
}
