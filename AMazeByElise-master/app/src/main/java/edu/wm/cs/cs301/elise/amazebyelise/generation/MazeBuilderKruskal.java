package edu.wm.cs.cs301.elise.amazebyelise.generation;

import java.util.ArrayList;

/** Summary of pseudocode:
 *
 * In Kruskal's algorithm for maze generation, edges ("walls") are unweighted.
 * In this implementation, we start with all possible walls ("candidates") thrown into a container (some data structure).
 * We will remove a random wall (rather than a wall with lowest weight) through a randomized selection process.
 * This will be our "candidate" for examination.
 *
 * We then determine whether the cells on either side of the wall are disjoint sets (i.e. not already connected).
 * If so, we need to merge these two cells by deleting the wall and note that these two cells now belong in the same set.
 * Else, these two cells are already part of the same set, so let's not remove this wall; pick a new one instead.
 * Repeat this process until there are no more possible candidates to consider.
 *
 * Reference source for Kruskal's algorithm: http://weblog.jamisbuck.org/2011/1/3/maze-generation-kruskal-s-algorithm.html
 * @author Elise
 */
public class MazeBuilderKruskal extends MazeBuilder implements Runnable {
    /** constructor for non-deterministic maze */
    public MazeBuilderKruskal() {
        super();
        System.out.println("MazeBuilderKruskal uses Kruskal's algorithm to generate maze.");
    }

    /** constructor for deterministic maze */
    public MazeBuilderKruskal(boolean det) {
        super(det);
        System.out.println("MazeBuilderKruskal uses Kruskal's algorithm to generate maze.");
    }

    @Override
    /** This method generates pathways by
     * (1) initializing a sets (one per cell on the maze grid),
     * (2) getting a list of walls that we can remove,
     * (3) selecting a random wall from that list for consideration,
     * (4) creating a tree representation of the current cell and its adjacent cell (which are separated by that wall),
     * (5) compare the current tree with the adjacent tree: if the wall connects these two disjoint trees, merge the two trees and delete the wall between them,
     * (6) otherwise, leave the wall there because the two trees on either side of it belong to the same set,
     * (7) repeat the process outlined in step 3-6 until we have no more walls to consider.
     */
    public void generatePathways() {

        /* use trees to represent sets
         * initialize the sets (one per cell on the maze grid)
         */
        Tree[][] sets = new Tree[width][height];
        for ( int x=0; x < width; x++ ) {
            for ( int y=0; y < height; y++ ) {
                sets[x][y] = new Tree();
            }
        }

        ArrayList<Wall> candidates = new ArrayList<Wall>();
        // get a list of walls that we can remove
        updateListOfWalls(candidates);

        /* repeat the following process until there are no more walls to consider
         * in this case, that means that there is only a single set left
         */
        while (!candidates.isEmpty()) {
            // select a random wall from the list of all walls
            Wall curWall = extractWallFromCandidateSetRandomly(candidates);

            // create tree representation of the current cell
            int curX = curWall.getX(); int curY = curWall.getY();
            Tree tree1 = sets[curX][curY];

            // create tree representation of the adjacent cell
            int neighborX = curWall.getNeighborX(); int neighborY = curWall.getNeighborY();
            Tree tree2 = sets[neighborX][neighborY];

            /* we will be comparing the current tree with the adjacent tree
             * if the wall connects these two disjoint trees, merge the two trees and delete the wall between them
             * otherwise, leave the wall there because the two trees on either side of it belong to the same set
             * so we discard the wall and loop again
             */
            if (!tree1.isConnected(tree2)) {
                tree1.merge(tree2);
                cells.deleteWall(curWall);
            }
        }
    }

    /**
     * Updates a list of all walls that could be removed from the maze based on walls towards new cells
     */
    protected void updateListOfWalls(ArrayList<Wall> candidates) {
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                for (CardinalDirection cd : CardinalDirection.values()) {
                    Wall candidate = new Wall(x, y, cd) ;
                    // if neighbor in the given direction is new and wall can be taken down, add that wall to the list of walls
                    if (cells.canGo(candidate) == true) {
                        candidates.add(candidate);
                    }
                }
            }
        }
    }

    /**
     * Pick a random position in the list of candidates, remove the candidate from the list and return it
     * @param candidates
     * @return candidate from the list, randomly chosen
     */
    protected Wall extractWallFromCandidateSetRandomly(final ArrayList<Wall> candidates) {
        return candidates.remove(random.nextIntWithinInterval(0, candidates.size()-1));
    }


    /**
     * This class is a data structure which represents each cell as a tree.
     * Each cell (x,y) belongs to a particular tree at any point in the algorithm.
     * The methods contained in this class helps us determine if two cells belong to the same tree (i.e. checking the root)
     * and also provides a way to merge two disjoint trees (i.e. updating the parent of the tree).
     * This Tree class will be useful in generating pathways for the maze using Kruskal's algorithm.
     */
    protected class Tree {

        private Tree parent = null;

        /**
         * empty constructor for tree
         */
        public Tree() {

        }

        /**
         * if two trees are connected, return the root
         * otherwise, return this tree
         * @return
         */
        public Tree root() {
            if (parent != null) {
                return parent.root();
            } else {
                return this;
            }
        }

        /**
         * check if two trees are connected
         * we know they are connected if the two trees share the same root
         * @param tree
         * @return
         */
        public boolean isConnected(Tree tree) {
            if (this.root() == tree.root()) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * connect to the tree by updating the parent
         * @param tree
         */
        public void merge(Tree tree) {
            tree.root().parent = this;
        }
    }
}

