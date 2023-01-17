package edu.wm.cs.cs301.elise.amazebyelise.generation;

import edu.wm.cs.cs301.elise.amazebyelise.generation.CardinalDirection;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Cells;
import edu.wm.cs.cs301.elise.amazebyelise.generation.MazeConfiguration;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Constants.UserInput;

/**
 * Class: BasicRobot
 *
 * Responsibilities: This class contains methods that allow the robot to
 * rotate and move within a maze. It can sense the distance to obstacles (walls),
 * inform the user about whether it is in a room or at an exit position,
 * keeps tracks of the path length through the odometer counter, and
 * provide information about the battery level of the robot (certain actions require energy).
 * The main purpose of BasicRobot is to work with a robot driver algorithm
 * to solve the maze (find the exit).
 *
 * Collaborators: Robot.java (which BasicRobot implements),
 * Controller.java (which holds the maze to be explored),
 * MazeConfiguration.java (which gives us relevant information about the current maze),
 * Cells.java (which encodes the walls and borders within the maze),
 * CardinalDirection.java (which provides us with directional information),
 * and a RobotDriver class (which will operate the BasicRobot).
 *
 * @author Elise
 *
 */
public class BasicRobot implements Robot {

    // other classes that I can use to get information
    protected StatePlaying mazeController;
    private MazeConfiguration mazeConfig;
    protected Cells mazeCells;
    /* initialize a battery and the constants related
     * to energy requirements for certain operations such as
     * sensing distance, rotating, and moving forward
     */
    private float batteryLevel;
    protected final float INIT_ENERGY_LEVEL = 2500;
    private final float ENERGY_TO_SENSE_DISTANCE = 1;
    private final float ENERGY_TO_ROTATE_90DEG = 3;
    private final float ENERGY_TO_ROTATE_180DEG = 6;
    private final float ENERGY_TO_ROTATE_360DEG = 12;
    private final float ENERGY_TO_STEP_FORWARD = 5;

    // attributes
    private int odometerCounter;
    private boolean hasStopped;
    private boolean roomSensor;
    private boolean forwardSensor;
    private boolean backwardSensor;
    private boolean rightSensor;
    private boolean leftSensor;
    private CardinalDirection adjDir;

    /**
     * Constructor where all sensors are on.
     */
    public BasicRobot() {

        batteryLevel = INIT_ENERGY_LEVEL;
        odometerCounter = 0;
        hasStopped = false;

        this.roomSensor = true;
        this.forwardSensor = true;
        this.backwardSensor = true;
        this.rightSensor = true;
        this.leftSensor = true;
    }

    /**
     * Constructor where you can adjust the settings of BasicRobot
     * (i.e. turn on and off certain sensors).
     * @param forwardSensor
     * @param backwardSensor
     * @param rightSensor
     * @param leftSensor
     */
    public BasicRobot(boolean forwardSensor, boolean backwardSensor,
                      boolean rightSensor, boolean leftSensor) {

        batteryLevel = INIT_ENERGY_LEVEL;
        odometerCounter = 0;
        hasStopped = false;

        this.roomSensor = true;
        this.forwardSensor = forwardSensor;
        this.backwardSensor = backwardSensor;
        this.rightSensor = rightSensor;
        this.leftSensor = leftSensor;
    }

    /**
     * Turn robot on the spot for amount of degrees.
     * If robot runs out of energy, it stops,
     * which can be checked by hasStopped() == true and by checking the battery level.
     * @param turn direction to turn and relative to current forward direction.
     */
    @Override
    public void rotate(Turn turn) {
        switch(turn) {
            case LEFT:
                if (getBatteryLevel() >= ENERGY_TO_ROTATE_90DEG) {
                    batteryLevel = batteryLevel - ENERGY_TO_ROTATE_90DEG;
                    mazeController.keyDown(UserInput.Left, 0);
                } else {
                    hasStopped = true;
                }
                break;
            case RIGHT:
                if (getBatteryLevel() >= ENERGY_TO_ROTATE_90DEG) {
                    batteryLevel = batteryLevel - ENERGY_TO_ROTATE_90DEG;
                    mazeController.keyDown(UserInput.Right, 0);
                } else {
                    hasStopped = true;
                }
                break;
            case AROUND:
                if (getBatteryLevel() >= ENERGY_TO_ROTATE_180DEG) {
                    batteryLevel = batteryLevel - ENERGY_TO_ROTATE_180DEG;
                    mazeController.keyDown(UserInput.Left, 0);
                    mazeController.keyDown(UserInput.Left, 0);
                } else {
                    hasStopped = true;
                }
                break;
        }
    }

    /**
     * Moves robot forward a given number of steps. A step matches a single cell.
     * If the robot runs out of energy somewhere on its way, it stops,
     * which can be checked by hasStopped() == true and by checking the battery level.
     * @param distance is the number of cells to move in the robot's current forward direction
     * @param manual is true if robot is operated manually by user, false otherwise
     * @precondition distance >= 0
     */
    @Override
    public void move(int distance, boolean manual) {
        while (!hasStopped() && (distance != 0)){
            batteryLevel = batteryLevel - ENERGY_TO_STEP_FORWARD;
            if (batteryLevel < 0) {
                hasStopped = true;
            } else {
                mazeController.keyDown(UserInput.Up, 0);
                odometerCounter = odometerCounter + 1;
                distance = distance - 1;
            }
        }
    }

    /**
     * Provides the current position as (x,y) coordinates for the maze cell as an array of length 2 with [x,y].
     * @postcondition 0 <= x < width, 0 <= y < height of the maze.
     * @return array of length 2, x = array[0], y=array[1]
     * @throws Exception if position is outside of the maze
     */
    @Override
    public int[] getCurrentPosition() throws Exception {
        int[] currentPosition = mazeController.getCurrentPosition();
        int x = currentPosition[0]; int y = currentPosition[1];
        if (!mazeConfig.isValidPosition(x, y)){
            throw new Exception("Current position is out of bounds.");
        }
        return currentPosition;
    }

    /**
     * Provides the robot with a reference to the controller to cooperate with.
     * The robot memorizes the controller such that this method is most likely called only once
     * and for initialization purposes. The controller serves as the main source of information
     * for the robot about the current position, the presence of walls, the reaching of an exit.
     * The controller is assumed to be in the playing state.
     * @param controller is the communication partner for robot
     * @precondition controller != null, controller is in playing state and has a maze
     */
    @Override
    public void setMaze(StatePlaying controller) {
        this.mazeController = controller;
        this.mazeConfig = this.mazeController.getMazeConfiguration();
        this.mazeCells = this.mazeConfig.getMazecells();
    }

    /**
     * Tells if current position (x,y) is right at the exit but still inside the maze.
     * Used to recognize termination of a search.
     * @return true if robot is at the exit, false otherwise
     */
    @Override
    public boolean isAtExit() {
        int[] currentPosition = mazeController.getCurrentPosition();
        int x = currentPosition[0]; int y = currentPosition[1];
        return mazeCells.isExitPosition(x, y);
    }

    /**
     * Tells if a sensor can identify the exit in given direction relative to
     * the robot's current forward direction from the current position.
     * @return true if the exit of the maze is visible in a straight line of sight
     * @throws UnsupportedOperationException if robot has no sensor in this direction
     */
    @Override
    public boolean canSeeExit(Direction direction) throws UnsupportedOperationException {
        if (hasDistanceSensor(direction) == true) {
            if (distanceToObstacle(direction) == Integer.MAX_VALUE) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Tells if current position is inside a room.
     * @return true if robot is inside a room, false otherwise
     * @throws UnsupportedOperationException if not supported by robot
     */
    @Override
    public boolean isInsideRoom() throws UnsupportedOperationException {
        if (hasRoomSensor() == true) {
            int[] currentPosition = mazeController.getCurrentPosition();
            int x = currentPosition[0]; int y = currentPosition[1];
            return mazeCells.isInRoom(x, y);
        } else {
            throw new UnsupportedOperationException("No room sensor!");
        }
    }

    /**
     * Tells if the robot has a room sensor.
     */
    @Override
    public boolean hasRoomSensor() {
        return roomSensor;
    }

    /**
     * Provides the current cardinal direction.
     * @return cardinal direction is robot's current direction in absolute terms
     */
    @Override
    public CardinalDirection getCurrentDirection() {
        return mazeController.getCurrentDirection();
    }

    /**
     * Returns the current battery level.
     * The robot has a given battery level (energy level)
     * that it draws energy from during operations.
     * The particular energy consumption is device dependent such that a call
     * for distance2Obstacle may use less energy than a move forward operation.
     * If battery level <= 0 then robot stops to function and hasStopped() is true.
     * @return current battery level, level is > 0 if operational.
     */
    @Override
    public float getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Sets the current battery level.
     * The robot has a given battery level (energy level)
     * that it draws energy from during operations.
     * The particular energy consumption is device dependent such that a call
     * for distance2Obstacle may use less energy than a move forward operation.
     * If battery level <= 0 then robot stops to function and hasStopped() is true.
     * @param level is the current battery level
     * @precondition level >= 0
     */
    @Override
    public void setBatteryLevel(float level) {
        batteryLevel = level;
    }

    /**
     * Gets the distance traveled by the robot.
     * The robot has an odometer that calculates the distance the robot has moved.
     * Whenever the robot moves forward, the distance
     * that it moves is added to the odometer counter.
     * The odometer reading gives the path length if its setting is 0 at the start of the game.
     * The counter can be reset to 0 with resetOdomoter().
     * @return the distance traveled measured in single-cell steps forward
     */
    @Override
    public int getOdometerReading() {
        return odometerCounter;
    }

    /**
     * Resets the odomoter counter to zero.
     * The robot has an odometer that calculates the distance the robot has moved.
     * Whenever the robot moves forward, the distance
     * that it moves is added to the odometer counter.
     * The odometer reading gives the path length if its setting is 0 at the start of the game.
     */
    @Override
    public void resetOdometer() {
        odometerCounter = 0;
    }

    /**
     * Gives the energy consumption for a full 360 degree rotation.
     * @return energy for a full rotation
     */
    @Override
    public float getEnergyForFullRotation() {
        return ENERGY_TO_ROTATE_360DEG;
    }

    /**
     * Gives the energy consumption for moving forward for a distance of 1 step.
     * @return energy for a single step forward
     */
    @Override
    public float getEnergyForStepForward() {
        return ENERGY_TO_STEP_FORWARD;
    }

    /**
     * Tells if the robot has stopped for reasons like lack of energy, hitting an obstacle, etc.
     * @return true if the robot has stopped, false otherwise
     */
    @Override
    public boolean hasStopped() {
        return hasStopped;
    }

    /**
     * Tells the distance to an obstacle (a wall or border)
     * in a direction as given and relative to the robot's current forward direction.
     * Distance is measured in the number of cells towards that obstacle,
     * e.g. 0 if current cell has a wall in this direction,
     * 1 if it is one step forward before directly facing a wall,
     * Integer.MaxValue if one looks through the exit into eternity.
     * @return number of steps towards obstacle if obstacle is visible
     * in a straight line of sight, Integer.MAX_VALUE otherwise
     * @throws UnsupportedOperationException if not supported by robot
     */
    @Override
    public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
        if (hasDistanceSensor(direction)) {

            batteryLevel = batteryLevel - ENERGY_TO_SENSE_DISTANCE;
            if (batteryLevel < 0) {
                hasStopped = true;
                throw new UnsupportedOperationException("No more battery!");
            }


			/* Compare with Cells.java for consistency
			Directions: right=east, down=south, left=west, up=north
			public static int[] DIRS_X = { 1, 0, -1, 0 };
			public static int[] DIRS_Y = { 0, 1, 0, -1 };
			 */
            CardinalDirection cd = getCurrentDirection();
            //System.out.println(cd);
            CardinalDirection adjDir = getAdjustedDirection(cd, direction);
			/*
			int[] dir = cd.getDirection();
			int dirX = dir[0]; int dirY = dir[1];

			// adjust the relative direction
			switch(direction) {
			case LEFT:
				int temp1 = dirX;
				dirX = -dirY;
				dirY = temp1;
				break;
			case RIGHT:
				int temp2 = dirX;
				dirX = dirY;
				dirY = -temp2;
				break;
			case BACKWARD:
				dirX = -dirX;
				dirY = -dirY;
				break;
			case FORWARD: default:
				break;
			}

			cd = CardinalDirection.getDirection(dirX, dirY);
			*/
            boolean obstacleFound = false;
            int distance = 0;
            int[] currentPosition = mazeController.getCurrentPosition();
            int curX = currentPosition[0]; int curY = currentPosition[1];

            /* Current mapping between cardinal directions and (dx,dy)
             * east  = (1,0)
             * south = (0,1)
             * west  = (-1,0)
             * north = (0,-1)
             */
            while (obstacleFound == false) {

                if (adjDir == CardinalDirection.West) {
                    if (mazeCells.hasWall(curX, curY, CardinalDirection.West)) {
                        obstacleFound = true;
                    } else {
                        curX = curX - 1;
                        distance = distance + 1;
                    }

                } else if (adjDir == CardinalDirection.East) {
                    if (mazeCells.hasWall(curX, curY, CardinalDirection.East)) {
                        obstacleFound = true;
                    } else {
                        curX = curX + 1;
                        distance = distance + 1;
                    }

                } else if (adjDir == CardinalDirection.North){
                    if (mazeCells.hasWall(curX, curY, CardinalDirection.North)) {
                        obstacleFound = true;
                    } else {
                        curY = curY - 1;
                        distance = distance + 1;
                    }

                } else if (adjDir == CardinalDirection.South) {
                    if (mazeCells.hasWall(curX, curY, CardinalDirection.South)) {
                        obstacleFound = true;
                    } else {
                        curY = curY + 1;
                        distance = distance + 1;
                    }
                }
                if (curX < 0 || curX >= mazeConfig.getWidth() || curY < 0 || curY >= mazeConfig.getHeight()) {
                    return Integer.MAX_VALUE;
                }
            }

            return distance;

        } else {
            throw new UnsupportedOperationException("No distance sensor in this direction!");
        }
    }

    public CardinalDirection getAdjustedDirection(CardinalDirection cd, Direction direction) {
        // TODO Auto-generated method stub
        int[] dir = cd.getDirection();
        int dirX = dir[0]; int dirY = dir[1];

        // adjust the relative direction
        switch(direction) {
            case LEFT:
                int temp1 = dirX;
                dirX = -dirY;
                dirY = temp1;
                break;
            case RIGHT:
                int temp2 = dirX;
                dirX = dirY;
                dirY = -temp2;
                break;
            case BACKWARD:
                dirX = -dirX;
                dirY = -dirY;
                break;
            case FORWARD: default:
                break;
        }

        adjDir = CardinalDirection.getDirection(dirX, dirY);
        return adjDir;
    }

    /**
     * Tells if the robot has a distance sensor for the given direction.
     */
    @Override
    public boolean hasDistanceSensor(Direction direction) {
        if ((direction == Direction.LEFT && leftSensor == true) ||
                (direction == Direction.RIGHT && rightSensor == true) ||
                (direction == Direction.BACKWARD && backwardSensor == true) ||
                (direction == Direction.FORWARD && forwardSensor == true)) {
            return true;
        } else {
            return false;
        }
    }

}

