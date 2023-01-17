package edu.wm.cs.cs301.elise.amazebyelise.generation;

import edu.wm.cs.cs301.elise.amazebyelise.generation.Distance;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Robot.Direction;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Robot.Turn;

/**
 * Class: WallFollower
 *
 * Responsibilities: Use a basic robot with left and forward sensors to
 * drive to the exit of the maze. Follows the wall on the robot's left.
 *
 * Collaborators: RobotDriver.java (which WallFollower implements) and
 * BasicRobot.java (which contains functionality for the robot driver)
 *
 * @author Elise
 */
public class WallFollower implements RobotDriver {

    private BasicRobot robot;

    /**
     * Empty constructor with no parameters.
     * Need to call setRobot(Robot r) in order to give the WallFollower a robot to work with!
     */
    public WallFollower() {

    }

    /**
     * Assigns a robot platform to the driver.
     * The driver uses a robot to perform, this method provides it with this necessary information.
     * @param r robot to operate
     */
    @Override
    public void setRobot(Robot r) {
        robot = (BasicRobot) r;
    }

    /**
     * Provides the robot driver with information on the dimensions of the 2D maze
     * measured in the number of cells in each direction.
     * @param width of the maze
     * @param height of the maze
     * @precondition 0 <= width, 0 <= height of the maze.
     */
    @Override
    public void setDimensions(int width, int height) {
        // Method not needed for WallFollower
    }

    /**
     * Provides the robot driver with information on the distance to the exit.
     * Only some drivers such as the wizard rely on this information to find the exit.
     * @param distance gives the length of path from current position to the exit.
     * @precondition null != distance, a full functional distance object for the current maze.
     */
    @Override
    public void setDistance(Distance distance) {
        // Method not needed for WallFollower
    }

    /**
     * Drives the robot towards the exit given it exists and
     * given the robot's energy supply lasts long enough.
     * @return true if driver successfully reaches the exit, false otherwise
     */
    @Override
    public boolean drive2Exit() throws Exception {

        // a robot must exist for WallFollower to work
        if (robot == null) {
            throw new Exception("No robot to work with!");
        }

        // check to see if robot has the appropriate sensors
        if (!robot.hasDistanceSensor(Robot.Direction.LEFT) || !robot.hasDistanceSensor(Robot.Direction.FORWARD)) {
            throw new Exception("Missing left or forward distance sensor!");
        }

        // loop that drives the robot through the maze and guides it towards exit
        while (!robot.isAtExit() && !robot.hasStopped()) {
            // exit ahead, robot should move towards it
            if (robot.canSeeExit(Robot.Direction.FORWARD)) {
                robot.move(1, false);
                // exit on left, robot should turn left and move towards it
            } else if (robot.canSeeExit(Robot.Direction.LEFT)) {
                robot.rotate(Turn.LEFT);
                robot.move(1, false);
            } else if ((robot.distanceToObstacle(Direction.LEFT) == 0) && (robot.distanceToObstacle(Direction.FORWARD) != 0)) {
                robot.move(1, false);
            } else if ((robot.distanceToObstacle(Direction.LEFT) == 0) && (robot.distanceToObstacle(Direction.FORWARD) == 0)) {
                robot.rotate(Turn.RIGHT);
            } else if ((robot.distanceToObstacle(Direction.LEFT) != 0)) {
                robot.rotate(Turn.LEFT);
                robot.move(1, false);
            }
        }

        // if the robot is at the exit position, move the robot through the exit
        if (robot.isAtExit()) {
            while (!robot.canSeeExit(Robot.Direction.FORWARD)) {
                robot.rotate(Robot.Turn.LEFT);
            }
            robot.move(1, false);
            return true; // true, we got to the exit in the end!
        } else {
            return false; // false, we were not able to get to the exit after all
        }
    }

    /**
     * Returns the total energy consumption of the journey, i.e.,
     * the difference between the robot's initial energy level at
     * the starting position and its energy level at the exit position.
     * This is used as a measure of efficiency for a robot driver.
     */
    @Override
    public float getEnergyConsumption() {
        return (robot.INIT_ENERGY_LEVEL - robot.getBatteryLevel());
    }

    /**
     * Returns the total length of the journey in number of cells traversed.
     * Being at the initial position counts as 0.
     * This is used as a measure of efficiency for a robot driver.
     */
    @Override
    public int getPathLength() {
        return robot.getOdometerReading();
    }

}
