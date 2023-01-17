package edu.wm.cs.cs301.elise.amazebyelise.gui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.wm.cs.cs301.elise.amazebyelise.R;
import edu.wm.cs.cs301.elise.amazebyelise.generation.BasicRobot;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Controller;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazeBuilder;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazeConfiguration;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazeFactory;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazePanel;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Order;
import edu.wm.cs.cs301.elise.amazebyelise.generation.StubOrder;
import edu.wm.cs.cs301.elise.amazebyelise.generation.WallFollower;

/**
 * Class: GeneratingActivity.
 *
 * Responsibilities:
 * (1) Receive information about the user's desired maze from MazeActivity,
 * (2) Use that information to prepare a maze,
 * (3) Show the progress of the maze generation to the user,
 * (4) Switch to a new activity, depending on user's selection, and pass on information to that activity.
 *
 * Collaborators: AMazeActivity (which gives GeneratingActivity information),
 * PlayManuallyActivity (which is the next activity if the user picked manual play mode,
 * PlayAnimationActivity (which is the next activity if the user picked automatic play mode.
 *
 * @author Elise
 */
public class GeneratingActivity extends AppCompatActivity {

    private int curProgress = 0;
    private Handler handler = new Handler();
    public static String driver;
    public static String builder;
    public static int skillLevel;
    public static Boolean robotExists = false;
    public static Boolean driverExists = false;
    public volatile boolean isStopped = false;
    private MazeFactory mazeFactory;
    private StubOrder stubOrder;
    public static MazeConfiguration mazeConfig;
    public static WallFollower wallFollower;

    /**
     * Sets up the layout for GeneratingActivity.
     * Automatically runs a progress bar, which imitates the delay needed for maze generation.
     * Once the progress bar reaches its max value, it will switch to either PlayManuallyActivity
     * or PlayAnimationActivity, depending on the user's choice of robot driver in AMazeActivity,
     * if the process is not stopped by the user pressing the back button.
     * Sends necessary information to the next activity as well.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generating);

        extractExtras();
        generateMaze();
        trackProgress();

        final Button button = findViewById(R.id.back_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);
                isStopped = true;
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
                Intent i = new Intent(GeneratingActivity.this, AMazeActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            builder = extras.getString("Builder");
            driver = extras.getString("Driver");
            skillLevel = extras.getInt("Skill Level");
        }
    }

    private void generateMaze() {
        mazeFactory = new MazeFactory();

        if (builder.equalsIgnoreCase("Prim")) {
            stubOrder = new StubOrder(Order.Builder.Prim, skillLevel, false);
            Log.v("GeneratingActivity", "Creating maze using Prim's algorithm");
        } else if (builder.equalsIgnoreCase("Kruskal")) {
            stubOrder = new StubOrder(Order.Builder.Kruskal, skillLevel, false);
            Log.v("GeneratingActivity", "Creating maze using Kruskal's algorithm");
        } else {
            stubOrder = new StubOrder(Order.Builder.DFS, skillLevel, false);
            Log.v("GeneratingActivity", "Creating maze using Default algorithm");
        }

        mazeFactory.order(stubOrder);
        //mazeFactory.waitTillDelivered();
        //mazeConfig = stubOrder.getMazeConfiguration();
    }

    private void trackProgress() {
        // Reference source: https://www.journaldev.com/9629/android-progressbar-example
        // Initiate progress bar and start button
        final ProgressBar progress_bar = findViewById(R.id.progress_bar);
        final TextView view_progress = findViewById(R.id.textView);
        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                Log.v("Progress bar", "Updating progress");
                while (curProgress < 100 && !isStopped) {
                    curProgress = stubOrder.getProgress();
                    // Update the progress bar and display the current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progress_bar.setProgress(curProgress);
                            view_progress.setText(curProgress + "/" + progress_bar.getMax());
                        }
                    });
                    try {
                        // Sleep for 1000 milliseconds.
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mazeFactory.waitTillDelivered();
                mazeConfig = stubOrder.getMazeConfiguration();
                proceedToNextActivity();
            }
        }).start();
    }

    private void proceedToNextActivity() {
        if(driver.equalsIgnoreCase("Manual") && !isStopped){
            Intent i = new Intent(GeneratingActivity.this, PlayManuallyActivity.class);
            i.putExtra("driver", getIntent().getExtras().getString("driver"));
            Log.v("Activity", "Switching to PlayManually");
            startActivity(i);
        } else{
            if (!isStopped) {
                Intent i = new Intent(GeneratingActivity.this, PlayAnimationActivity.class);
                i.putExtra("driver", getIntent().getExtras().getString("driver"));
                //robotExists = true;
                //driverExists = true;
                Log.v("Activity", "Switching to PlayAnimation");
                startActivity(i);
            }
        }
    }


    /**
     * If the back button is pressed, stop everything and return to AMazeActivity.
     */
    @Override
    public void onBackPressed() {
        isStopped = true;
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

}
