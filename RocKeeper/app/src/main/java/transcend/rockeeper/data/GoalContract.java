// I want to get XXXXX points all time. I'm currently at XXXXXX points.
// I want to atttempt XX routes and one day. I want to complete at least XX of them.
// I want to climb a VX by XX/XX/XXXX .

// Alex start start XML - get us an idea of what we're looking it - visualize it
// Jesse think about how the skelleton framework (null values in the database, column with climb/attempt/get...)
// Dropdown to choose question format and interchangeable verbs
// column to indicate type (difficulty, points, completed). THe columsn will be null except for column indicated by thte type.

package transcend.rockeeper.data;

import android.database.Cursor;

public class GoalContract extends Contract {

	public static final String TYPE = "type";
	public static final String ATTEMPTS = RouteContract.NUM_ATTEMPTS;
	public static final String COMPLETED = RouteContract.COMPLETED;
	public static final String POINTS = RouteContract.POINTS;
	public static final String DIFFICULTY = RouteContract.DIFFICULTY;
	public static final String DUE_DATE = "due_date";
	
	public GoalContract(){
		super();
		colTypes.put(TYPE, TEXT);
		colTypes.put(ATTEMPTS, INT);
		colTypes.put(COMPLETED, INT);
		colTypes.put(POINTS, INT);
		colTypes.put(DUE_DATE, INT);
	}
	
	@Override
	public String tableName() { return "goals";}
	
	public class Goal extends Unit {
		public Goal(String type, long val, long due){
			put(TYPE, type);
			put(type, val);
			put(DUE_DATE, due);
		}
		public Goal(String difficulty, long due){
			put(TYPE, DIFFICULTY);
			put(DIFFICULTY, difficulty);
			put(DUE_DATE, due);
		}
	}
	public Goal build(Cursor c){
		return this.new Goal(
				c.getString(c.getColumnIndex(TYPE)),
				c.getLong(c.getColumnIndex(c.getString(c.getColumnIndex(TYPE)))),
				c.getLong(c.getColumnIndex(DUE_DATE)));
	}
	public Goal build(String type, long val, long due){
		return this.new Goal(type, val, due);
	}
	public Goal build(String difficulty, long due){
		return this.new Goal(difficulty, due);
	}
}
