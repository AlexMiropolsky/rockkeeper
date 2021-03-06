/** FILENAME: DatabaseHelper.java
 *  CREATED: 2015
 *  AUTHORS:
 *    Alex Miropolsky
 *    Chris Berger
 *    Jesse Freitas
 *    Nicole Negedly
 *  LICENSE: GNU General Public License (Version 3)
 *    Please see the LICENSE file in the main project directory for more details.
 *
 *  DESCRIPTION:
 *    Sets up DB tables on app first time launch and retrieves a reference to the database object
 */
package transcend.rockeeper.sqlite;

import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.data.SettingsContract;
import transcend.rockeeper.data.StatContract;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@SuppressLint("NewApi")
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "rockeeper.db";
	public static final int DB_VERSION = 14;

	public final RouteContract routes = new RouteContract();
	public final GoalContract goals = new GoalContract();
	public final LocationContract locations = new LocationContract();
	public final SettingsContract settings = new SettingsContract();
	public final StatContract stats = new StatContract();
	
	public DatabaseHelper(Context context, CursorFactory c){
		super(context, DATABASE_NAME, c, DB_VERSION, new DatabaseErrorHandler(){
			public void onCorruption(SQLiteDatabase dbObj) {
				Log.e("DBH.java", "Corruption detected: " + dbObj.getPath());
				Log.e("DBH.java", "Closing Database");
				dbObj.close();
			}
		});
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(routes.createTable());
		db.execSQL(goals.createTable());
		db.execSQL(locations.createTable());
		db.execSQL(settings.createTable());
		db.execSQL(stats.createTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(routes.dropTable());
		db.execSQL(goals.dropTable());
		db.execSQL(locations.dropTable());
		db.execSQL(settings.dropTable());
		db.execSQL(stats.dropTable());
		onCreate(db);
	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(routes.dropTable());
		db.execSQL(goals.dropTable());
		db.execSQL(locations.dropTable());
		db.execSQL(settings.dropTable());
		db.execSQL(stats.dropTable());
		onCreate(db);
	}
}
