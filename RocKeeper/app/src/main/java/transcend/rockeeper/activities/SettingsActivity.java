package transcend.rockeeper.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class SettingsActivity extends Activity
{
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        getFragmentManager().beginTransaction().replace( android.R.id.content, new SettingsFragment() ).commit();
    }

    /*public MainActivity getMainActivity() {

    }*/
}
