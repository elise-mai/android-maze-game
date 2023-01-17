package edu.wm.cs.cs301.elise.amazebyelise.gui;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import edu.wm.cs.cs301.elise.amazebyelise.R;

/**
 * Class: AMazeActivity.
 *
 * Responsibilities:
 * (1) Serves as the main menu screen for users when they open the app,
 * (2) Allows users to pick a skill level, maze builder, and robot driver,
 * (3) Passes information about users' maze to the GeneratingActivity.
 *
 * Collaborators: GeneratingActivity (which receives information from MazeActivity).
 *
 * @author Elise
 */
public class AMazeActivity extends AppCompatActivity {
    public static MediaPlayer music;
    private String builder = "DFS";
    private String driver = "Manual";
    private int skillLevel = 0;

    /**
     * Sets up the layout for AMazeActivity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amaze);
        if (music == null) {
            music = MediaPlayer.create(AMazeActivity.this, R.raw.instumental);
            music.setLooping(true);
            music.start();
        }
        // Initiate  views
        SeekBar seekbar_for_skillLevel = findViewById(R.id.seek_bar);
        // Perform seek bar change listener event used for getting the progress value
        seekbar_for_skillLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * The seek bar's progress is set as the skill level (maze complexity).
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                skillLevel = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             * When the user stops moving the seek bar, we know a skill level has been selected.
             * We will show the user his/her selected skill level using a toast.
             * @param seekBar
             */
            public void onStopTrackingTouch(SeekBar seekBar) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);
                Log.v("Skill Level", "A skill level has been selected");
                Toast.makeText(AMazeActivity.this, "Skill level: " + skillLevel, Toast.LENGTH_SHORT).show();
            }
        });

        Spinner spinner_for_builder = findViewById(R.id.spinner_builder);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_for_builder = ArrayAdapter.createFromResource(this, R.array.builders, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter_for_builder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_for_builder.setAdapter(adapter_for_builder);
        spinner_for_builder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * User selects a maze builder from a drop down menu.
             * The item that is selected is the user's desired maze builder.
             * Inform the user of his/her choice with a toast.
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);
                builder = parent.getItemAtPosition(position).toString();
                Log.v("Builder", "A builder has been selected");
                Toast.makeText(getBaseContext(), "Builder: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        Spinner spinner_for_driver = findViewById(R.id.spinner_driver);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_for_driver = ArrayAdapter.createFromResource(this, R.array.drivers, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter_for_driver.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_for_driver.setAdapter(adapter_for_driver);
        spinner_for_driver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * User selects a robot driver from a drop down menu.
             * The item that is selected is the user's desired robot driver.
             * Inform the user of his/her choice with a toast.
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);
                driver = parent.getItemAtPosition(position).toString();
                Log.v("Driver", "A driver has been selected");
                Toast.makeText(getBaseContext(), "Driver: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Button button = findViewById(R.id.enter_button);
        button.setOnClickListener(new View.OnClickListener() {
            /**
             * When the user presses the enter button, we will switch to a new activity,
             * the generating screen. We will need to pass the user's choices made here
             * to the next activity.
             * @param v
             */
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v.getId() == R.id.enter_button) {
                    vibe.vibrate(100);
                    Toast.makeText(getBaseContext(), "Generating Maze", Toast.LENGTH_SHORT).show();
                    Log.v("Activity", "Switching to GeneratingActivity");
                    Intent i = new Intent(AMazeActivity.this, GeneratingActivity.class);
                    i.putExtra("Driver", driver);
                    i.putExtra("Builder", builder);
                    i.putExtra("Skill Level", skillLevel);
                    startActivity(i);
                }
            }
        });
    }
}

