package edu.wm.cs.cs301.elise.amazebyelise.generation;


/**
 * An order describes functionality needed to order a maze from
 * the maze factory.
 * @author Elise
 */
public class StubOrder implements Order {

    private Builder whichAlgorithm;
    private int skillLevel;
    private boolean isPerfect;
    private MazeConfiguration mazeConfig;
    int percentage;

    /**
     * Data container which communicates input parameters such as
     * which algorithm (DFS, Prim's, or Kruskal's), what skill level, etc...
     * for maze generation
     * @param whichAlgorithm
     * @param skillLevel
     * @param isPerfect
     */
    public StubOrder(Builder whichAlgorithm, int skillLevel, boolean isPerfect) {
        this.whichAlgorithm = whichAlgorithm;
        this.skillLevel = skillLevel;
        this.isPerfect = isPerfect;
    }

    @Override
    /**
     * Gives the required skill level, range of values 0,1,2,...,15
     */
    public int getSkillLevel() {
        return skillLevel;
    }

    @Override
    /**
     * Gives the requested builder algorithm, possible values
     * are listed in the Builder enum type.
     */
    public Builder getBuilder() {
        return whichAlgorithm;
    }

    @Override
    /**
     * Describes if the ordered maze should be perfect, i.e. there are
     * no loops and no isolated areas, which also implies that
     * there are no rooms as rooms can imply loops
     */
    public boolean isPerfect() {
        return isPerfect;
    }

    @Override
    /**
     * Delivers the produced maze.
     * This method is called by the factory to provide the
     * resulting maze as a MazeConfiguration.
     * @param the maze
     */
    public void deliver(MazeConfiguration mazeConfig) {
        this.mazeConfig = mazeConfig;

    }

    /**
     * Extract the maze configuration from the stub order.
     * This allows us to test various elements of the maze.
     * @return
     */
    public MazeConfiguration getMazeConfiguration() {
        return mazeConfig;
    }

    @Override
    /**
     * Provides an update on the progress being made on
     * the maze production. This method is called occasionally
     * during production, there is no guarantee on particular values.
     * Percentage will be delivered in monotonously increasing order,
     * the last call is with a value of 100 after delivery of product.
     * @param current percentage of job completion
     */
    public void updateProgress(int percentage) {
        this.percentage = percentage;
    }

    @Override
    public int getProgress() { return this.percentage; }
}
