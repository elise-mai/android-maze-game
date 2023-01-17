package edu.wm.cs.cs301.elise.amazebyelise.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.wm.cs.cs301.elise.amazebyelise.R;
import edu.wm.cs.cs301.elise.amazebyelise.generation.StatePlaying;

import static edu.wm.cs.cs301.elise.amazebyelise.generation.StatePlaying.isFinished;

/**
 * Class: FinishActivity.
 *
 * Responsibilities:
 * (1) Display messages for success or failure (i.e. did the user complete the maze or not?),
 * (2) Display path length and energy consumption,
 * (3) Allow the user to go back to the menu.
 *
 * Collaborators: PlayManuallyActivity (which gives information to FinishActivity),
 * PlayAnimationActivity (which gives information to FinishActivity),
 * AMazeActivity (which we can return to).
 *
 * @author Elise
 */
public class FinishActivity extends AppCompatActivity {
    private boolean isWinning;
    private String driver;
    private int path_length_num;
    private int energy_consumed_num;

    /**
     * Set up the layout for FinishActivity.
     * Depending on whether the user won or lost, display the messages "you win!" or "you lose!"
     * and the reason for success or failure. Display path length and energy consumption.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        // Get information from the previous activity.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isWinning = extras.getBoolean("isWinning");
            driver = extras.getString("driver");
            path_length_num = extras.getInt("path length");
            energy_consumed_num = extras.getInt("energy consumed");
        }

        // If the user didn't win, then we change the UI.
        if(!isWinning) {
            TextView finish_message1 = findViewById(R.id.finish_message1);
            finish_message1.setText(getString(R.string.you_lose));
            TextView finish_message2 = findViewById(R.id.finish_message2);
            finish_message2.setText(getString(R.string.you_ran_out_of_energy));
        }

        TextView path_length = findViewById(R.id.path_length_num);
        TextView energy_consumed = findViewById(R.id.energy_consumed_num);
        path_length.setText(" " + path_length_num);
        energy_consumed.setText(" " + energy_consumed_num);


        final Button button = findViewById(R.id.menu_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);
                StatePlaying.isFinished = false;
                if(AMazeActivity.music != null)
                {
                    try{
                        AMazeActivity.music.stop();
                        AMazeActivity.music.release();
                    }finally {
                        AMazeActivity.music = null;
                    }
                }
                Log.v("Activity", "Switching to AMazeActivity");
                Toast.makeText(getBaseContext(), "Returning to menu", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(FinishActivity.this, AMazeActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    /**
     * If the back button is pressed, stop everything and return to AMazeActivity.
     */
    @Override
    public void onBackPressed() {
        Log.v("onBackPressed", "Switching to AMazeActivity");
        Toast.makeText(getBaseContext(), "Returning to menu", Toast.LENGTH_SHORT).show();
        StatePlaying.isFinished = false;
        if(AMazeActivity.music != null)
        {
            try{
                AMazeActivity.music.stop();
                AMazeActivity.music.release();
            }finally {
                AMazeActivity.music = null;
            }
        }
        Intent intent = new Intent(this, AMazeActivity.class);
        startActivity(intent);
        finish();
    }
}
