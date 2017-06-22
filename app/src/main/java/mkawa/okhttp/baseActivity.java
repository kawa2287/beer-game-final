package mkawa.okhttp;

import android.content.ClipData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by mattkawahara on 7/6/16.
 */
public class baseActivity extends AppCompatActivity {


    //menu bar?
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_items,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this,settings.class);
                this.startActivity(settingsIntent);
                break;
            case R.id.market:
                Intent marketIntent = new Intent(this,Market.class);
                this.startActivity(marketIntent);
                break;
            case R.id.viewStats:
                Intent viewStatsIntent = new Intent(this,UserStatstics.class);
                this.startActivity(viewStatsIntent);
                break;
            case R.id.searchBeer:
                Intent searchBeerIntent = new Intent(this,beerSearch.class);
                this.startActivity(searchBeerIntent);
                break;
            case R.id.viewLeaderBoard:
                Intent leaderBoardIntent = new Intent(this,LeaderBoard.class);
                this.startActivity(leaderBoardIntent);
            case R.id.dashboard:
                Intent dashboardIntent = new Intent(this,Dashboard.class);
                this.startActivity(dashboardIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


}
