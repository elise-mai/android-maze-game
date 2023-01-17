package edu.wm.cs.cs301.elise.amazebyelise.gui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.wm.cs.cs301.elise.amazebyelise.R;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazePanel;
import edu.wm.cs.cs301.elise.amazebyelise.generation.StatePlaying;

/**
 * Class: PlayAnimationActivity.
 *
 * Responsibilities:
 * (1) Allow the user to watch a robot automatically solve the maze (can start and pause robot),
 * (2) Allow the user to toggle walls, the map, and the solution, if they want,
 * (3) Allow the user to return to the menu with the back button,
 * (4) Have a shortcut button which takes us directly to the finishing screen (temporary).
 *
 * Collaborators: GeneratingActivity (which passes information to PlayAnimationActivity),
 * FinishActivity (which occurs when the game is over).
 *
 * @author Elise
 */
public class PlayAnimationActivity extends AppCompatActivity {

    private MazePanel mazepanel;
    private StatePlaying maze;
    private int energyLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_animation);

        ProgressBar energyBar = (ProgressBar) findViewById(R.id.energy_progressbar);
        TextView energyText = (TextView) findViewById(R.id.energy_text);
        energyLevel = 2500;
        energyBar.setMax(2500);
        energyBar.setProgress(energyLevel);

        mazepanel = findViewById(R.id.panel);
        maze = new StatePlaying();
        maze.setMazeConfiguration(GeneratingActivity.mazeConfig);
        maze.start(null, mazepanel);
        mazepanel.update();
    }

    /**
     * If the back button is pressed, stop everything and return to AMazeActivity.
     */
    @Override
    public void onBackPressed() {
        if(AMazeActivity.music != null)
        {
            try{
                AMazeActivity.music.stop();
                AMazeActivity.music.release();
            }finally {
                AMazeActivity.music = null;
            }
        }
        Log.v("onBackPressed", "Switching to AMazeActivity");
        Toast.makeText(getBaseContext(), "Returning to menu", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AMazeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Determines what happens when buttons are pressed.
     * Here, we have the wall, map, and solution toggle buttons, which can be turned on and off.
     * We also have the ability to start and pause the robot.
     * Lastly, we have a back button (takes us to AMazeActivity)
     * and a shortcut button (takes us directly to FinishActivity). For now, the shortcut
     * button takes us to the winning screen, but the losing screen can be activated by adjusting
     * the boolean isWinning.
     * @param v
     */
    public void onButtonClick(View v) {
        if (v.getId() == R.id.wall_toggle) {
            Log.v("Wall Toggle", "User pressed Wall Toggle");
            Toast.makeText(this, "Toggle Walls", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.map_toggle) {
            Log.v("Map Toggle", "User pressed Map Toggle");
            Toast.makeText(this, "Toggle Map", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.solution_toggle) {
            Log.v("Solution Toggle", "User pressed Solution Toggle");
            Toast.makeText(this, "Toggle Solution", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.back_button) {
            Log.v("Back Button", "User pressed Back Button");
            Toast.makeText(this, "Returning to menu", Toast.LENGTH_SHORT).show();
            if(AMazeActivity.music != null)
            {
                try{
                    AMazeActivity.music.stop();
                    AMazeActivity.music.release();
                }finally {
                    AMazeActivity.music = null;
                }
            }
            Intent i = new Intent(PlayAnimationActivity.this, AMazeActivity.class);
            startActivity(i);
        }
        if (v.getId() == R.id.shortcut) {
            Log.v("Shortcut Button", "User pressed Shortcut Button");
            Toast.makeText(this, "Finished the maze", Toast.LENGTH_SHORT).show();
            Log.v("Activity", "Switching to FinishActivity");
            Intent i = new Intent(PlayAnimationActivity.this, FinishActivity.class);
            i.putExtra("driver", "WallFollower");
            // for now, the isWinning boolean value is true
            // this will show the winning screen
            // if it is false, it will show the losing screen
            i.putExtra("isWinning", true);
            // for now, path length is 0
            i.putExtra("path length", 0);
            // for now, energy consumed is 0
            i.putExtra("energy consumed", 0);
            startActivity(i);
        }
        if (v.getId() == R.id.play_toggle) {
            Log.v("Play Toggle", "User pressed Play Toggle");
            Toast.makeText(this, "Toggle Robot", Toast.LENGTH_SHORT).show();
        }
    }
}
