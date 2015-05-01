/** FILENAME: GoalsFragment.java
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
 *    Fragment for the goals page, showing the list of goals
 */

package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class GoalsFragment extends Fragment implements GoalDialogFragment.GoalDialogListener, AdapterView.OnItemClickListener{

	private ArrayList<Goal> goals = new ArrayList<Goal>();
	private DatabaseHelper dbh;
	private SQLiteDatabase db;
	private FragmentActivity mainActivity;
	private ListView listview;
    private int selectedItem = -1;      // the index of the list item selected

/****************************** LIFECYCLE METHODS *****************************/

    /** Returns a new instance of the fragment */
	public static GoalsFragment newInstance() {
		GoalsFragment fragment = new GoalsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
	}
	
	 public GoalsFragment() {
        // Required empty public constructor
    }

    /** Called when the fragment is created - handle initializations */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbh = new DatabaseHelper(this.getActivity(), null);
        db = dbh.getWritableDatabase();
        getGoals(db);
    }

    /** Sets up the view for the fragment */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    /** Called when the activity associated with the fragment has been created */
	@Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = this.getActivity();
        listview = (ListView) mainActivity.findViewById(R.id.listviewGoals);
        listview.setAdapter( new GoalListAdapter( this.getActivity(), goals ));
        listview.setOnItemClickListener( this );
    }

/********************************** DIALOG HANDLERS ***************************/

    /** Called when the user selects the positive button (i.e. 'OK') of the dialog */
    public void onGoalDialogPositiveClick(DialogFragment dialog, final Goal edit) {

        final Spinner verb = (Spinner) dialog.getDialog().findViewById( R.id.verbSpinner );
        final EditText value = (EditText) dialog.getDialog().findViewById( R.id.goalValue );
        final Spinner diff = (Spinner) dialog.getDialog().findViewById( R.id.goalDifficulty );
        final TextView noun = (TextView) dialog.getDialog().findViewById( R.id.nounView );
        final DatePicker date = (DatePicker) dialog.getDialog().findViewById( R.id.goalDatePicker );

        // Get the values from the fields
        final String verb_val = (String) verb.getSelectedItem();
        final int value_val = Integer.parseInt( value.getText().toString() );
        final String diff_val = (String) diff.getSelectedItem();
        final String noun_val = noun.getText().toString();
        final Calendar cal = Calendar.getInstance();
        cal.set( date.getYear(), date.getMonth(), date.getDayOfMonth() );
        final long date_val = cal.getTimeInMillis();

        // Determine the type of goal
        String goalType = "";
        if( verb_val.equals( "Earn" ) ) goalType = GoalContract.POINTS;
        else if( verb_val.equals( "Attempt" ) ) goalType = GoalContract.ATTEMPTS;
        else if( verb_val.equals( "Complete" ) ) goalType = GoalContract.COMPLETED;
        else if( verb_val.equals( "Climb a" ) ) goalType = GoalContract.DIFFICULTY;

        // Build the goal object and add/update it in the database
        final Goal g = (goalType == GoalContract.DIFFICULTY) ? dbh.goals.build( diff_val, date_val ) : dbh.goals.build( goalType, value_val, date_val );
        Transaction t = new Transaction(db) {
            public void task(SQLiteDatabase db) {
                if(edit == null){
                    dbh.routes.insert(g, db);
                }
                else{
                    dbh.routes.update(g, GoalContract._ID + "=" + edit.get(GoalContract._ID), null, db);
                }
            }
            public void onComplete() {
                if(edit != null){
                    if(selectedItem == -1)
                        return;
                    goals.set(selectedItem, g);
                }
                else{
                    goals.add(g);
                    click(listview, selectedItem);
                }

                ((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true, true);
    }

/**************************** BUTTON HANDLERS ***************************/

    /** Displays the goals dialog in add mode */
    public void addGoal( View v ){
        Bundle args = new Bundle();
        args.putInt( "selectedItem", -1 );
        GoalDialogFragment d = new GoalDialogFragment();
        d.setArguments( args );
        d.setTargetFragment( this, 1 );
        d.show( getFragmentManager(), "GoalDialog" );
    }

    /** Displays the goals dialog in edit mode */
    public void editGoal( View v ){
        //final Goal edit = (Goal) listview.getAdapter().getItem(selectedItem);
        Bundle args = new Bundle();
        args.putInt( "selectedItem", selectedItem );
        GoalDialogFragment d = new GoalDialogFragment();
        d.setArguments( args );
        d.setTargetFragment( this, 1 );
        d.show( getFragmentManager(), "GoalDialog" );
    }

    /** Deletes the selected goal from the list and the database */
    public void deleteGoal( View v ){
        final GoalContract.Goal delete = (GoalContract.Goal) listview.getAdapter().getItem(selectedItem);
        goals.remove(delete);
        Transaction t = new Transaction(db){
            public void task(SQLiteDatabase db) {
                dbh.goals.delete(GoalContract._ID + "=" + delete.get(GoalContract._ID), null, db);
            }
            public void onComplete() {
                goals.remove(delete);
                click(listview, selectedItem);
                ((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true, true);
    }

/******************************* LIST HANDLERS ******************************/

    /** Called when the user selects an item in the list */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        click(view, position);
    }

    /** Handle what happens when a list item is clicked */
    private void click(View view, int position){
        Button editB = (Button) getActivity().findViewById(R.id.editGoalButton);
        Button deleteB = (Button) getActivity().findViewById(R.id.deleteGoalButton);
        if( position == selectedItem ) {
            view.setSelected(false);
            view.setActivated(false);
            editB.setEnabled(false);
            deleteB.setEnabled(false);
            listview.clearChoices();
            selectedItem = -1;
        } else {
            view.setSelected( true );
            view.setActivated( true );
            editB.setEnabled( true );
            deleteB.setEnabled( true );
            selectedItem = position;
        }
    }

/***************************** GOAL METHODS ******************************/

    /** Queries the database for goals */
    private void getGoals(SQLiteDatabase db2) {
        Transaction t = new Transaction(db){
            public void task(SQLiteDatabase db) {
                Cursor c = dbh.goals.query(null, null, null, GoalContract.DUE_DATE, false, null, db);
                c.moveToFirst();
                while(c.getCount() > 0 && !c.isAfterLast()){
                    goals.add(dbh.goals.build(c));
                    c.moveToNext();
                }
            }
            public void onComplete(){
                Log.i("GoalsFragment", "Goals Loaded.");
                ((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true, true);
    }

    /** Refreshes the list of goals */
    public void refresh() {
        goals.clear();
        getGoals(db);
        ((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
    }

/********************************* ADAPTERS *******************************************/
    
    /** Custom adapter for the list of goals */
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
                CheckBox completed = (CheckBox) convertView.findViewById(R.id.checkboxComplete);
                if(g.get(GoalContract.TYPE) == GoalContract.DIFFICULTY){
                	completed.setChecked(Long.parseLong(g.get(GoalContract.STATUS)) > 0); 
                } else {
                	completed.setChecked(Long.parseLong(g.get(GoalContract.STATUS)) >= Long.parseLong(g.get(g.get(GoalContract.TYPE))));
                }
                
                String goal = dbh.goals.verbs.get(g.get(GoalContract.TYPE)) + 
                		g.get(g.get(GoalContract.TYPE)) + 
                		dbh.goals.nouns.get(g.get(GoalContract.TYPE));
                
                goalText.setText(goal.toUpperCase(Locale.US));
                started.setText("Due on " + DateFormat.getDateFormat(context).format(Long.parseLong(g.get(GoalContract.DUE_DATE))));
            }
            return convertView;
        }
    }



}