package edu.wm.cs.cs301.elise.amazebyelise.generation;

//import java.awt.Graphics;

import android.widget.ProgressBar;

import edu.wm.cs.cs301.elise.amazebyelise.R;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Constants.StateGUI;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Constants.UserInput;
import edu.wm.cs.cs301.elise.amazebyelise.generation.CardinalDirection;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Cells;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazeConfiguration;
import edu.wm.cs.cs301.elise.amazebyelise.gui.GeneratingActivity;
import edu.wm.cs.cs301.elise.amazebyelise.gui.PlayManuallyActivity;


/**
 * Class handles the user interaction
 * while the game is in the third stage
 * where the user plays the game.
 * This class is part of a state pattern for the
 * Controller class. It is a ConcreteState.
 *
 * It implements a state-dependent behavior that controls the display and reacts to key board input from a user.
 * At this point user keyboard input is first dealt with a key listener (SimpleKeyListener)
 * and then handed over to a Controller object by way of the keyDown method.
 *
 * Responsibilities
 * Show the first person view and the map view
 * Accept input for manual operation (left, right, up, down etc)
 * Update the graphics, recognize termination
 *
 * This code is refactored code from Maze.java by Paul Falstad,
 * www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class StatePlaying extends DefaultState {
    FirstPersonDrawer firstPersonView;
    MapDrawer mapView;
    MazePanel panel;
    Controller control;

    MazeConfiguration mazeConfig ;

    private boolean showMaze;           // toggle switch to show overall maze on screen
    private boolean showSolution;       // toggle switch to show solution in overall maze on screen
    private boolean mapMode; // true: display map of maze, false: do not display map of maze
    // map_mode is toggled by user keyboard input, causes a call to draw_map during play mode

    // current position and direction with regard to MazeConfiguration
    private int px, py ; // current position on maze grid (x,y)
    private int dx, dy;  // current direction

    // current position and direction with regard to graphics view
    // graphics has intermediate views for a smoother experience of turns
    private int viewx, viewy; // current position
    private int viewdx, viewdy; // current view direction, more fine grained than (dx,dy)
    private int angle; // current viewing angle, east == 0 degrees
    //static final int viewz = 50;
    private int walkStep; // counter for intermediate steps within a single step forward or backward
    private Cells seencells; // a matrix with cells to memorize which cells are visible from the current point of view
    // the FirstPersonDrawer obtains this information and the MapDrawer uses it for highlighting currently visible walls on the map
    private RangeSet rset;
    // debug stuff
    private boolean deepdebug = false;
    //private boolean allVisible = false;
    //private boolean newGame = false;
    ProgressBar progressBar;


    boolean started;
    private BasicRobot robot;
    private RobotDriver driver;
    public static Boolean isFinished = false;

    public StatePlaying() {
        rset = new RangeSet();
        started = false;
    }
    @Override
    public void setMazeConfiguration(MazeConfiguration config) {
        mazeConfig = config;
    }
    /**
     * Start the game by showing the title screen.
     * If the panel is null, all drawing operations are skipped.
     * This mode of operation is useful for testing purposes,
     * i.e., a dryrun of the game without the graphics part.
     * @param controller provides access to the controller this state resides in
     * @param panel is part of the UI and visible on the screen, needed for drawing
     */
    public void start(Controller controller, MazePanel panel) {
        started = true;
        // keep the reference to the controller to be able to call method to switch the state
        //control = controller;
        // keep the reference to the panel for drawing
        this.panel = panel;
        //
        // adjust internal state of maze model
        // visibility settings
        showMaze = false ;
        showSolution = false ;
        mapMode = false;
        // init data structure for visible walls
        seencells = new Cells(mazeConfig.getWidth()+1,mazeConfig.getHeight()+1) ;
        // set the current position and direction consistently with the viewing direction
        setPositionDirectionViewingDirection();
        walkStep = 0; // counts incremental steps during move/rotate operation

        if (panel != null) {
            // init mazeview, controller not needed for title
            // reset map_scale in mapdrawer to a value of 10
            firstPersonView = new FirstPersonDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT, Constants.MAP_UNIT,
                    Constants.STEP_SIZE, seencells, mazeConfig.getRootnode()) ;

            // order of registration matters, code executed in order of appearance!
            mapView = new MapDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,Constants.MAP_UNIT,
                    Constants.STEP_SIZE, seencells, 100, this) ;

            // if given a filename, show a message and move to the loading screen
            // otherwise, show message that we wait for the skill level for input
            notifyViewerRedraw();

            if (GeneratingActivity.robotExists && GeneratingActivity.driverExists) {
                System.out.println("Creating the selected robot: " + GeneratingActivity.driver);
                robot = new BasicRobot();
                robot.setMaze(StatePlaying.this);
                driver = new WallFollower();
                driver.setRobot(robot);
                driver.setDistance(controller.getMazeConfiguration().getMazedists());
                try {
                    System.out.print("Trying to drive out of maze now!");
                    driver.drive2Exit() ;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            // else: dry-run without graphics
            printWarning();
        }
    }
    /**
     * Internal method to set the current position, the direction
     * and the viewing direction to values consistent with the
     * given maze.
     */
    private void setPositionDirectionViewingDirection() {
        // obtain starting position
        int[] start = mazeConfig.getStartingPosition() ;
        setCurrentPosition(start[0],start[1]) ;
        // set current view direction and angle
        setCurrentDirection(1, 0) ; // east direction
        viewdx = dx<<16;
        viewdy = dy<<16;
        angle = 0; // angle matches with east direction, hidden consistency constraint!
    }


    /**
     * Method incorporates all reactions to keyboard input in original code,
     * The simple key listener calls this method to communicate input.
     */
    public boolean keyDown(UserInput key, int value) {
        if (!started)
            return false;

        // react to input for directions and interrupt signal (ESCAPE key)
        // react to input for displaying a map of the current path or of the overall maze (on/off toggle switch)
        // react to input to display solution (on/off toggle switch)
        // react to input to increase/reduce map scale
        switch (key) {
            case Start: // misplaced, do nothing
                break;
            case Up: // move forward
                walk(1);
                if (isOutside(px,py)) {
                    isFinished = true;
                }
                break;
            case Left: // turn left
                rotate(1);
                break;
            case Right: // turn right
                rotate(-1);
                break;
            case Down: // move backward
                walk(-1);
                if (isOutside(px,py)) {
                    isFinished = true;
                }
                break;
            case ReturnToTitle: // escape to title screen
                control.switchToTitle();
                break;
            case Jump: // make a step forward even through a wall
                // go to position if within maze
                if (mazeConfig.isValidPosition(px + dx, py + dy)) {
                    setCurrentPosition(px + dx, py + dy) ;
                    notifyViewerRedraw() ;
                }
                break;
            case ToggleLocalMap: // show local information: current position and visible walls
                // precondition for showMaze and showSolution to be effective
                // acts as a toggle switch
                mapMode = !mapMode;
                notifyViewerRedraw();
                break;
            case ToggleFullMap: // show the whole maze
                // acts as a toggle switch
                showMaze = !showMaze;
                notifyViewerRedraw();
                break;
            case ToggleSolution: // show the solution as a yellow line towards the exit
                // acts as a toggle switch
                showSolution = !showSolution;
                notifyViewerRedraw() ;
                break;
            case ZoomIn: // zoom into map
                notifyViewerIncrementMapScale() ;
                notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
                break ;
            case ZoomOut: // zoom out of map
                notifyViewerDecrementMapScale() ;
                notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
                break ;
        } // end of internal switch statement for playing state
        return true;
    }
    protected void notifyViewerRedraw() {
        if (panel == null) {
            printWarning();
            return;
        }
        MazePanel g = panel ;
        // viewers draw on the buffer graphics
        if (null == g) {
            System.out.println("Maze.notifierViewerRedraw: can't get graphics object to draw on, skipping redraw operation") ;
        }
        else {
            firstPersonView.redraw(g, StateGUI.STATE_PLAY, px, py, viewdx, viewdy, walkStep, Constants.VIEW_OFFSET, rset, angle) ;
            mapView.redraw(g, StateGUI.STATE_PLAY, px, py, viewdx, viewdy, walkStep, Constants.VIEW_OFFSET, rset, angle) ;
        }

        // update the screen with the buffer graphics
        panel.update() ;
    }
    /**
     * Notify all registered viewers to increment the map scale
     */
    private void notifyViewerIncrementMapScale() {
        if (panel == null) {
            printWarning();
            return;
        }
        mapView.incrementMapScale() ;
        // update the screen with the buffer graphics
        panel.update() ;
    }
    /**
     * Notify all registered viewers to decrement the map scale
     */
    private void notifyViewerDecrementMapScale() {
        if (panel == null) {
            printWarning();
            return;
        }
        mapView.decrementMapScale() ;
        // update the screen with the buffer graphics
        panel.update() ;
    }
    /**
     * Print the warning about a missing panel only once
     */
    boolean printedWarning = false;
    private void printWarning() {
        if (printedWarning)
            return;
        System.out.println("StatePlaying.start: warning: no panel, dry-run game without graphics!");
        printedWarning = true;
    }
    ////////////////////////////// set methods ///////////////////////////////////////////////////////////////
    ////////////////////////////// Actions that can be performed on the maze model ///////////////////////////
    protected void setCurrentPosition(int x, int y) {
        px = x ;
        py = y ;
    }
    private void setCurrentDirection(int x, int y) {
        dx = x ;
        dy = y ;
    }
    ////////////////////////////// get methods ///////////////////////////////////////////////////////////////
    protected int[] getCurrentPosition() {
        int[] result = new int[2];
        result[0] = px;
        result[1] = py;
        return result;
    }
    protected CardinalDirection getCurrentDirection() {
        return CardinalDirection.getDirection(dx, dy);
    }
    boolean isInMapMode() {
        return mapMode ;
    }
    boolean isInShowMazeMode() {
        return showMaze ;
    }
    boolean isInShowSolutionMode() {
        return showSolution ;
    }
    public MazeConfiguration getMazeConfiguration() {
        return mazeConfig ;
    }
    //////////////////////// Methods for move and rotate operations ///////////////
    final double radify(int x) {
        return x*Math.PI/180;
    }
    /**
     * Helper method for walk()
     * @param dir
     * @return true if there is no wall in this direction
     */
    protected boolean checkMove(int dir) {
        CardinalDirection cd = null;
        switch (dir) {
            case 1: // forward
                cd = getCurrentDirection();
                break;
            case -1: // backward
                cd = getCurrentDirection().oppositeDirection();
                break;
            default:
                throw new RuntimeException("Unexpected direction value: " + dir);
        }
        return !mazeConfig.hasWall(px, py, cd);
    }
    /**
     * Redraw and wait, used to obtain a smooth appearance for rotate and move operations
     */
    private void slowedDownRedraw() {
        notifyViewerRedraw() ;
        try {
            Thread.sleep(25);
        } catch (Exception e) {
            // may happen if thread is interrupted
            // no reason to do anything about it, ignore exception
        }
    }
    /**
     * Intermediate step during rotation, updates the screen
     */
    private void rotateStep() {
        angle = (angle+1800) % 360;
        viewdx = (int) (Math.cos(radify(angle))*(1<<16));
        viewdy = (int) (Math.sin(radify(angle))*(1<<16));
        slowedDownRedraw();
    }
    /**
     * Performs a rotation with 4 intermediate views,
     * updates the screen and the internal direction
     * @param dir for current direction
     */
    synchronized private void rotate(int dir) {
        final int originalAngle = angle;
        final int steps = 4;

        for (int i = 0; i != steps; i++) {
            // add 1/4 of 90 degrees per step
            // if dir is -1 then subtract instead of addition
            angle = originalAngle + dir*(90*(i+1))/steps;
            rotateStep();
        }
        setCurrentDirection((int) Math.cos(radify(angle)), (int) Math.sin(radify(angle))) ;
        logPosition(); // debugging
    }
    /**
     * Moves in the given direction with 4 intermediate steps,
     * updates the screen and the internal position
     * @param dir, only possible values are 1 (forward) and -1 (backward)
     */
    synchronized private void walk(int dir) {
        if (!checkMove(dir))
            return;
        // walkStep is a parameter of the redraw method in FirstPersonDrawer
        // it is used there for scaling steps
        // so walkStep is implicitly used in slowedDownRedraw which triggers the redraw
        // operation on all listed viewers
        for (int step = 0; step != 4; step++) {
            walkStep += dir;
            slowedDownRedraw();
        }
        setCurrentPosition(px + dir*dx, py + dir*dy) ;
        walkStep = 0; // reset counter for next time
        logPosition(); // debugging
    }

    /**
     * checks if the given position is outside the maze
     * @param x
     * @param y
     * @return true if position is outside, false otherwise
     */
    private boolean isOutside(int x, int y) {
        return !mazeConfig.isValidPosition(x, y) ;
    }
    /////////////////////// Methods for debugging ////////////////////////////////
    private void dbg(String str) {
        //System.out.println(str);
    }

    private void logPosition() {
        if (!deepdebug)
            return;
        dbg("x="+viewx/Constants.MAP_UNIT+" ("+
                viewx+") y="+viewy/Constants.MAP_UNIT+" ("+viewy+") ang="+
                angle+" dx="+dx+" dy="+dy+" "+viewdx+" "+viewdy);
    }
}



