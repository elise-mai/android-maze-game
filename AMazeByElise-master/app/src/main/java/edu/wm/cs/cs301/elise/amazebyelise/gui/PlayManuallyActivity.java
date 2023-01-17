package edu.wm.cs.cs301.elise.amazebyelise.gui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import edu.wm.cs.cs301.elise.amazebyelise.R;
import edu.wm.cs.cs301.elise.amazebyelise.generation.BasicRobot;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Constants;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Controller;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazePanel;
import edu.wm.cs.cs301.elise.amazebyelise.generation.RobotDriver;
import edu.wm.cs.cs301.elise.amazebyelise.generation.StatePlaying;
import edu.wm.cs.cs301.elise.amazebyelise.generation.WallFollower;

/**
 * Class: PlayManuallyActivity.
 *
 * Responsibilities:
 * (1) Allow the user to manually explore the maze using buttons for movement,
 * (2) Allow the user to toggle walls, the map, and the solution, if they want,
 * (3) Allow the user to return to the menu with the back button,
 * (4) Have a shortcut button which takes us directly to the finishing screen (temporary).
 *
 * Collaborators: GeneratingActivity (which passes information to PlayManuallyActivity),
 * FinishActivity (which occurs when the game is over).
 *
 * @author Elise
 */
public class PlayManuallyActivity extends AppCompatActivity {
    private MazePanel mazepanel;
    private StatePlaying maze;
    public WallFollower driver;
    public BasicRobot robot;
    private int pathLength;
    private int energyLevel;
    private ProgressBar energyBar;
    private TextView energyText;

    /**
     * Sets up the layout for PlayManuallyActivity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_manually);

        mazepanel = findViewById(R.id.panel);
        maze = new StatePlaying();
        maze.setMazeConfiguration(GeneratingActivity.mazeConfig);
        maze.start(null, mazepanel);
        mazepanel.update();

        energyBar = findViewById(R.id.energy_progressbar);
        energyText = findViewById(R.id.energy_text);
        energyLevel = 2500;
        energyBar.setMax(2500);
        energyBar.setProgress(energyLevel);
        energyText.setText("Energy: " + energyLevel + "/" + energyBar.getMax());

        pathLength = 0;
    }

    /**
     * If the back button is pressed, stop everything and return to AMazeActivity.
     */
    @Override
    public void onBackPressed() {
        Log.v("onBackPressed", "Switching to AMazeActivity");
        Toast.makeText(getBaseContext(), "Returning to menu", Toast.LENGTH_SHORT).show();
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

    /**
     * Determines what happens when buttons are pressed.
     * Here, we have the wall, map, and solution toggle buttons, which can be turned on and off.
     * We also have the up, down, left, right buttons, which control manual movement throughout
     * the maze. Lastly, we have a back button (takes us to AMazeActivity)
     * and a shortcut button (takes us directly to FinishActivity). For now, the shortcut
     * button takes us to the winning screen, but the losing screen can be activated by adjusting
     * the boolean isWinning.
     * @param v
     */
    public void onButtonClick(View v) {
        if (v.getId() == R.id.wall_toggle) {
            Log.v("Wall Toggle", "User pressed Wall Toggle");
            Toast.makeText(this, "Toggle Walls", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.ToggleLocalMap, 0);
        }
        if (v.getId() == R.id.map_toggle) {
            Log.v("Map Toggle", "User pressed Map Toggle");
            Toast.makeText(this, "Toggle Map", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.ToggleFullMap, 0);
        }
        if (v.getId() == R.id.solution_toggle) {
            Log.v("Solution Toggle", "User pressed Solution Toggle");
            Toast.makeText(this, "Toggle Solution", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.ToggleSolution, 0);
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
            Intent i = new Intent(PlayManuallyActivity.this, AMazeActivity.class);
            startActivity(i);
        }
        if (v.getId() == R.id.shortcut) {
            Log.v("Shortcut Button", "User pressed Shortcut Button");
            Toast.makeText(this, "Finished the maze", Toast.LENGTH_SHORT).show();
            Log.v("Activity", "Switching to FinishActivity");
            Intent i = new Intent(PlayManuallyActivity.this, FinishActivity.class);
            i.putExtra("driver", "Manual");
            i.putExtra("isWinning", true);
            i.putExtra("path length", pathLength);
            i.putExtra("energy consumed", 2500 - energyLevel);
            startActivity(i);
        }
        if (v.getId() == R.id.up_button) {
            Log.v("Up Button", "User pressed Up Button");
            Toast.makeText(this, "Up", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.Up, 0);
            pathLength++;
            if ((energyLevel - 5) > 0) {
                energyLevel -= 5;
                energyBar.setProgress(energyLevel);
                energyText.setText("Energy: " + energyLevel + "/" + energyBar.getMax());
            } else {
                switchToFinishActivity();
            }


            if (StatePlaying.isFinished) {
                Toast.makeText(this, "Finished the maze", Toast.LENGTH_SHORT).show();
                Log.v("Activity", "Switching to FinishActivity");
                Intent i = new Intent(PlayManuallyActivity.this, FinishActivity.class);
                i.putExtra("driver", "Manual");
                i.putExtra("isWinning", true);
                i.putExtra("path length", pathLength);
                i.putExtra("energy consumed", 2500 - energyLevel);
                startActivity(i);
            }
        }
        if (v.getId() == R.id.down_button) {
            Log.v("Down Button", "User pressed Down Button");
            Toast.makeText(this, "Down", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.Down, 0);
            pathLength++;
            if ((energyLevel - 5) > 0) {
                energyLevel -= 5;
                energyBar.setProgress(energyLevel);
                energyText.setText("Energy: " + energyLevel + "/" + energyBar.getMax());
            } else {
                switchToFinishActivity();
            }


            if (StatePlaying.isFinished) {
                Toast.makeText(this, "Finished the maze", Toast.LENGTH_SHORT).show();
                Log.v("Activity", "Switching to FinishActivity");
                Intent i = new Intent(PlayManuallyActivity.this, FinishActivity.class);
                i.putExtra("driver", "Manual");
                i.putExtra("isWinning", true);
                i.putExtra("path length", pathLength);
                i.putExtra("energy consumed", 2500 - energyLevel);
                startActivity(i);
            }
        }

        if (v.getId() == R.id.left_button) {
            Log.v("Left Button", "User pressed Left Button");
            Toast.makeText(this, "Left", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.Left, 0);
            if ((energyLevel - 3) > 0) {
                energyLevel -= 3;
                energyBar.setProgress(energyLevel);
                energyText.setText("Energy: " + energyLevel + "/" + energyBar.getMax());
            } else {
                switchToFinishActivity();
            }

        }

        if (v.getId() == R.id.right_button) {
            Log.v("Right Button", "User pressed Right Button");
            Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show();
            maze.keyDown(Constants.UserInput.Right, 0);
            if ((energyLevel - 3) > 0) {
                energyLevel -= 3;
                energyBar.setProgress(energyLevel);
                energyText.setText("Energy: " + energyLevel + "/" + energyBar.getMax());
            } else {
                switchToFinishActivity();
            }
        }
    }
    public void switchToFinishActivity() {
        Toast.makeText(this, "Out of energy", Toast.LENGTH_SHORT).show();
        Log.v("Activity", "Switching to FinishActivity");
        Intent i = new Intent(PlayManuallyActivity.this, FinishActivity.class);
        i.putExtra("isWinning", false);
        i.putExtra("path length", pathLength);
        i.putExtra("energy consumed", 2500 - energyLevel);
        startActivity(i);
    }
}
