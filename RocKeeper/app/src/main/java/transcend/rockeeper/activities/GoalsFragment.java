package transcend.rockeeper.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GoalsFragment extends Fragment implements AdapterView.OnItemClickListener{

	private ArrayList<Goal> goals = new ArrayList<Goal>();
	private DatabaseHelper dbh;
	private SQLiteDatabase db;
	private FragmentActivity mainActivity;
	private ListView listview;
	
	public void addGoal(View v) {
	}

	public void editGoal(View v) {
	}

	public void deleteGoal(View v) {
	}

	public static GoalsFragment newInstance() {
		GoalsFragment fragment = new GoalsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
	}
	
	 public GoalsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbh = new DatabaseHelper(this.getActivity(), null);
        db = dbh.getWritableDatabase();
        getGoals(db);
    }

    private void getGoals(SQLiteDatabase db2) {
    	Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				Cursor c = dbh.goals.query(null, null, null, GoalContract.DUE_DATE, true, null, db);
                c.moveToFirst();
				while(c.getCount() > 0 && !c.isAfterLast()){
					goals.add(dbh.goals.build(c));
                    c.moveToNext();
				}
			}
			public void onComplete(){Log.i("GoalsFragment", "Goals Loaded.");}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}

	@Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = this.getActivity();
        listview = (ListView) mainActivity.findViewById(R.id.listviewGoals);
        listview.setAdapter( new GoalListAdapter( this.getActivity(), goals ));
        listview.setOnItemClickListener( this );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }
    
    /* Custom adapter for the list of goals */
    public class GoalListAdapter extends ArrayAdapter<Goal> {

        Context context;
        LayoutInflater inflater = null;

        public GoalListAdapter( Context context, ArrayList<Goal> goals ) {
            super( context, 0, goals );
            this.context = context;
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }
        public int getCount() {
            return goals.size();
        }
        public Goal getItem( int position ) {
            return goals.get( position );
        }
        public long getItemId( int position ) {
            return position;
        }
        public View getView( final int position, View convertView, ViewGroup parent ) {
            if(convertView == null){
        		convertView = inflater.inflate(R.layout.goal_row, null);
        		Goal g = getItem(position);
                TextView goalText = (TextView) convertView.findViewById(R.id.goalName);
                TextView started = (TextView) convertView.findViewById(R.id.dueDate);
                
                String goal = dbh.goals.verbs.get(g.get(GoalContract.TYPE)) + 
                		g.get(g.get(GoalContract.TYPE)) + 
                		dbh.goals.nouns.get(g.get(GoalContract.TYPE));
                
                goalText.setText(goal.toUpperCase(Locale.US));
                started.setText("Due on " + DateFormat.getDateFormat(context).format(Long.parseLong(g.get(GoalContract.DUE_DATE))));
            }
            return convertView;
        }
    }
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}
}
