package edu.wm.cs.cs301.elise.amazebyelise.generation;

import android.graphics.Point;

import edu.wm.cs.cs301.elise.amazebyelise.generation.Constants.StateGUI;
import edu.wm.cs.cs301.elise.amazebyelise.generation.BSPBranch;
import edu.wm.cs.cs301.elise.amazebyelise.generation.BSPLeaf;
import edu.wm.cs.cs301.elise.amazebyelise.generation.BSPNode;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Cells;
import edu.wm.cs.cs301.elise.amazebyelise.generation.Seg;
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Point;
import java.util.ArrayList;

/**
 * This class encapsulates all functionality for drawing the current view
 * at the maze from a first person perspective.
 * It is an drawing agent with redraw as its public method to redraw
 * the maze while the user plays, i.e. navigates through the maze.
 *
 * This code is refactored code from Maze.java by Paul Falstad,
 * www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class FirstPersonDrawer {
    // keep local copies of values determined in StatePlaying.java,
    // all values are set in the constructor call
    // values are basically constants or shared data structures across
    // StatePlaying, MapDrawer and FirstPersonDrawer
    // constants, i.e. set in constructor call with values
    // that are not subject to change in maze
    private int view_width = 400;
    private int view_height = 400;
    private int map_unit = 128;
    private int step_size = map_unit/4;
    // map scale may be adjusted by user input, controlled in StatePlaying
    private Cells seencells ; // cells whose walls are currently visible
    // node is determined in MazeBuilder when creating the maze, helps to decide visibility
    private BSPNode bsp_root ;

    // angle, used in rotations
    private int angle = 0 ;  // set in redraw_play

    // used in bounding box
    private int zscale = view_height/2;

    MazePanel mazePanel ; // the graphics object for the buffer image this class draws on
    // note: updating the panel that is on screen with the buffer image
    // is the responsibility of the StatePlaying class

    final int viewz = 50;  // constant from StatePlaying.java

    // current position (px,py) scaled by map_unit and
    // modified by view direction is stored in (viewx, viewy)
    private int viewx ; // current position for view, x coordinate, calculated in redraw_play
    private int viewy ; // current position for view, y coordinate, calculated in redraw_play
    // view direction (view_dx,view_dy)
    private int view_dx ; // set in redraw_play
    private int view_dy ; // set in redraw_play
    // set of ranges
    private RangeSet rset ; // set in redraw_play

    // debug stuff
    private boolean deepdebug = false;
    private boolean all_visible = false;
    private int traverse_node_ct;
    private int traverse_ssector_ct;
    private int drawrect_ct ;
    private int drawrect_late_ct ;
    private int drawrect_segment_ct ;
    private int nesting = 0;

    /**
     * Constructor
     * @param width of display
     * @param height of display
     * @param map_unit
     * @param step_size
     * @param seencells
     * @param bsp_root
     */
    public FirstPersonDrawer(int width, int height, int map_unit, int step_size, Cells seencells, BSPNode bsp_root) {
        // store given parameter values
        view_width = width ;
        view_height = height ;
        this.map_unit = map_unit ;
        this.step_size = step_size ;
        this.seencells = seencells ;
        this.bsp_root = bsp_root ;
        // constants and derived values
        angle = 0 ; // angle for initial setting of direction is 0 == East, hidden constraint across classes
        zscale = view_height/2;
    }

    /**
     * Draws the first person view on the screen during the game
     * @param mazePanel graphics handler for the buffer image that this class draws on
     * @param state the current state of the GUI
     * @param px x coordinate of current position, only used to get viewx
     * @param py y coordinate of current position, only used to get viewy
     * @param view_dx view direction, x coordinate
     * @param view_dy view direction, y coordinate
     * @param rset
     * @param ang
     * @param walk_step, only used to get viewx and viewy
     * @param view_offset, only used to get viewx and viewy
     */
    public void redraw(MazePanel mazePanel, StateGUI state, int px, int py, int view_dx,
                       int view_dy, int walk_step, int view_offset, RangeSet rset, int ang) {
        // if notified by model that state has changed
        // Query model for parameters
        //dbg("viewer.redraw called");
        this.mazePanel = mazePanel;
        this.rset = rset ;
        this.view_dx = view_dx ;
        this.view_dy = view_dy ;
        this.angle = ang ;

        // calculate view
        viewx = (px*map_unit+map_unit/2) + viewd_unscale(view_dx*(step_size*walk_step-view_offset));
        viewy = (py*map_unit+map_unit/2) + viewd_unscale(view_dy*(step_size*walk_step-view_offset));
        // update graphics
        // draw background figure: black on bottom half, grey on top half
        drawBackground(mazePanel);
        // set color to white and draw what ever can be seen from the current position
        mazePanel.setColor("White");
        rset.set(0, view_width-1); // reset set of ranges to set with single new element (0,width-1)
        // debug: reset counters
        traverse_node_ct = traverse_ssector_ct =
                drawrect_ct = drawrect_late_ct = drawrect_segment_ct = 0;
        //
        drawAllVisibleSectors(bsp_root);
    }



    ////////////////////////////// internal, private methods ///////////////////////////////
    /**
     * Draws a black and a grey rectangle to provide a background.
     * Note that this also erases previous drawings of maze or map.
     * @param mazePanel
     */
    private void drawBackground(MazePanel mazePanel) {
        mazePanel.setColor("Black");
        mazePanel.fillRect(0, 0, view_width, view_height/2);
        mazePanel.setColor("DarkGray");
        mazePanel.fillRect(0, view_height/2, view_width, view_height/2);
    }
    /**
     * Recursive method to explore tree of BSP nodes and draw all segments in leaf nodes
     * where the bounding box is visible
     * @param nn is the current node of interest
     */
    private void drawAllVisibleSectors(BSPNode nn) {
        traverse_node_ct++; // debug

        // Anchor, stop recursion at leaf nodes
        if (nn.isIsleaf()) {
            drawAllSegmentsOfASector((BSPLeaf) nn);
            return;
        }

        // for intermediate nodes proceed recursively through all visible branches
        BSPBranch n = (BSPBranch) nn;

        // debug code
        if (deepdebug) {
            dbg("                               ".substring(0, nesting) +
                    "traverse_node "+n.getX()+" "+n.getY()+" "+n.getDx()+" "+n.getDy()+" "+
                    n.getLowerBoundX()+" "+n.getLowerBoundY()+" "+n.getUpperBoundX()+" "+n.getUpperBoundY());
        }
        nesting++; // debug

        int dot = (viewx-n.getX())*n.getDy()-(viewy-n.getY())*n.getDx();
        BSPNode lch = n.getLeftBranch();
        BSPNode rch = n.getRightBranch();
        // The type of tree traversal depends on the value of dot
        // if dot >= 0 consider right node before left node
        if ((dot >= 0) && (boundingBoxIsVisible(rch))) {
            drawAllVisibleSectors(rch);
        }
        // consider left node
        if (boundingBoxIsVisible(lch))
            drawAllVisibleSectors(lch);
        // if dot < 0 consider right node now (after left node)
        if ((dot < 0) && (boundingBoxIsVisible(rch))) {
            drawAllVisibleSectors(rch);
        }
        nesting--; // debug
    }
    /**
     * Decide if the bounding box is visible
     * @return
     */
    private boolean boundingBoxIsVisible(BSPNode b) {
        int ymax = b.getUpperBoundY();
        int ymin = b.getLowerBoundY();
        int xmin = b.getLowerBoundX();
        int xmax = b.getUpperBoundX();


        if (all_visible) // unused feature, presumably for debugging
            return true;
        // check a few simple cases up front
        if (rset.isEmpty())
            return false;
        if (angle >= 45 && angle <= 135 && viewy > ymax)
            return false;
        if (angle >= 225 && angle <= 315 && viewy < ymin)
            return false;
        if (angle >= 135 && angle <= 225 && viewx < xmin)
            return false;
        if ((angle >= 315 || angle <= 45) && viewx > xmax)
            return false;

        xmin -= viewx;
        ymin -= viewy;
        xmax -= viewx;
        ymax -= viewy;

        int p1x = xmin;
        int p2x = xmax;
        int p1y = ymin;
        int p2y = ymax;
        if (ymin < 0 && ymax > 0) {
            p1y = ymin;
            p2y = ymax;
            if (xmin < 0) {
                if (xmax > 0)
                    return true;
                p1x = p2x = xmax;
            } else
                p1x = p2x = xmin;
        } else if (xmin < 0 && xmax > 0) {
            if (ymin < 0)
                p1y = p2y = ymax;
            else
                p1y = p2y = ymin;
        } else if ((xmin > 0 && ymin > 0) || (xmin < 0 && ymin < 0)) {
            p1x = xmax;
            p2x = xmin;
        }
        int rp1x = -viewd_unscale(view_dy*p1x-view_dx*p1y);
        int rp1z = -viewd_unscale(view_dx*p1x+view_dy*p1y);
        int rp2x = -viewd_unscale(view_dy*p2x-view_dx*p2y);
        int rp2z = -viewd_unscale(view_dx*p2x+view_dy*p2y);

        RangePair rp = new RangePair(rp1x, rp1z, rp2x, rp2z);
        if (!rp.clip3d(rp))
            return false;
        int x1 = rp.x1*zscale/rp.z1+(view_width/2);
        int x2 = rp.x2*zscale/rp.z2+(view_width/2);
        if (x1 > x2) { //switch if necessary
            int xj = x1;
            x1 = x2;
            x2 = xj;
        }
        // constraint: x1 <= x2
        int[] p = new int[2];
        p[0] = x1; p[1] = x2;
        return (rset.intersect(p));
    }

    /**
     * Traverses all segments of this leaf and draws corresponding rectangles on screen
     * @param n is the leaf node
     */
    private void drawAllSegmentsOfASector(BSPLeaf n) {
        ArrayList<Seg> sl = n.getSlist();
        // debug
        traverse_ssector_ct++;
        if (deepdebug) {
            dbg("                               ".substring(0, nesting) +
                    "traverse_ssector "+n.getLowerBoundX()+" "+n.getLowerBoundY()+" "+n.getUpperBoundX()+" "+n.getUpperBoundY());
        }
        // for all segments of this node
        for (int i = 0; i != sl.size(); i++) {
            Seg seg = (Seg) sl.get(i);
            // draw rectangle
            // before: drawSegment(seg, seg.getStartPositionX(), seg.getStartPositionY(), seg.getEndPositionX(), seg.getEndPositionY());
            drawSegment(seg);
            // debug
            if (deepdebug) {
                dbg("                               ".substring(0, nesting) +
                        " traverse_ssector(" + i +") "+
                        seg.getStartPositionX()+" "+seg.getStartPositionY()+" "+
                        seg.getExtensionX()+" "+seg.getExtensionY());
            }

        }
    }


    /**
     * Draws segment on screen via graphics attribute gc
     * Helper method for traverse_ssector
     * @param seg whose seen attribute may be set to true
     */
    private void drawSegment(Seg seg) {
        int ox1 = seg.getStartPositionX();
        int y1 = seg.getStartPositionY() ;
        int ox2 = seg.getEndPositionX();
        int y2 = seg.getEndPositionY();
        int z1 = 0;
        int z2 = 100;

        drawrect_ct++; // debug, counter
        ox1 -= viewx;
        y1 -= viewy;
        z1 -= viewz;
        ox2 -= viewx;
        y2 -= viewy;
        z2 -= viewz;

        int y11, y12, y21, y22;
        y11 = y21 = -z1;
        y12 = y22 = -z2;

        int x1 ;
        int x2 ;
        x1 = -viewd_unscale(view_dy*ox1-view_dx*y1);
        z1 = -viewd_unscale(view_dx*ox1+view_dy*y1);
        x2 = -viewd_unscale(view_dy*ox2-view_dx*y2);
        z2 = -viewd_unscale(view_dx*ox2+view_dy*y2);

        RangePair rp = new RangePair(x1, z1, x2, z2);
        if (!rp.clip3d(rp))
            return;

        y11 = y11*zscale/rp.z1+(view_height/2); // constant from here
        y12 = y12*zscale/rp.z1+(view_height/2); // constant from here
        y21 = y21*zscale/rp.z2+(view_height/2); // constant from here
        y22 = y22*zscale/rp.z2+(view_height/2); // constant from here
        x1 = rp.x1*zscale/rp.z1+(view_width/2); // constant from here
        x2 = rp.x2*zscale/rp.z2+(view_width/2); // constant from here
        if (x1 >= x2) /* reject backfaces */
            return;
        int x1i = x1;
        int xd = x2-x1;
        mazePanel.setColor(seg.color);
        boolean drawn = false;
        drawrect_late_ct++; // debug, counter
        // loop variable is x1i, upper limit x2 is fixed
        while (x1i <= x2) {
            // check if there is an intersection,
            // if there is none proceed exit the loop,
            // if there is one, get it as (x1i,x2i)
            int[] p = {x1i, x2};
            if (!rset.intersect(p))
                break;
            x1i = p[0];
            int x2i = p[1];
            // let's work on the intersection (x1i,x2i)
            int[] xps = { x1i, x1i, x2i+1, x2i+1 };
            int[] yps = { y11+(x1i-x1)*(y21-y11)/xd,
                    y12+(x1i-x1)*(y22-y12)/xd+1,
                    y22+(x2i-x2)*(y22-y12)/xd+1,
                    y21+(x2i-x2)*(y21-y11)/xd };
            // debug
            //System.out.println("polygon-x: " + xps[0] + ", " + xps[1] + ", " + xps[2] + ", " + xps[3]) ;
            //System.out.println("polygon-y: " + yps[0] + ", " + yps[1] + ", " + yps[2] + ", " + yps[3]) ;
            mazePanel.fillPolygon(xps, yps, 4);
            // for debugging purposes, code will draw a red line around polygon
            // this makes individual segments visible
			/*
			gc.setColor(new Color(240,20,20));
			gc.drawPolygon(xps, yps, 4);
			gc.setColor(seg.getColor());
			*/
            // end debugging
            drawn = true;
            rset.remove(x1i, x2i);
            x1i = x2i+1;
            drawrect_segment_ct++; // debug, counter
        }
        if (drawn && !seg.isSeen()) {
            seg.setSeen(true); // updates the segment
            // set the seencells bit for all cells of a segment
            // seg is not modified
            seencells.addWallsForSegment(seg, map_unit); // updates seencells
        }
    }

    ////////////////////////////// static methods that do not rely on instance fields //////
    /**
     * Unscale given vale
     * @param x
     * @return
     */
    final int viewd_unscale(int x) {
        return x >> 16;
    }
    /**
     * Helper method for debugging
     * @param str
     */
    private void dbg(String str) {
        // TODO: change this into a logger
        System.out.println("FirstPersonDrawer:"+ str);
    }

    /**
     * Trivial class to hold 4 integer values. Used only in FirstPersonDrawer.
     */
    class RangePair {
        public int x1, z1, x2, z2;
        RangePair(int xx1, int zz1, int xx2, int zz2) {
            x1 = xx1;
            z1 = zz1;
            x2 = xx2;
            z2 = zz2;
        }
        /**
         * Helper method for bbox_visible and drawrect
         * @param rp may be modified
         * @return
         */
        public boolean clip3d(RangePair rp) {
            final int x1 = rp.x1, z1 = rp.z1, x2 = rp.x2, z2 = rp.z2;

            if (z1 > -4 && z2 > -4)
                return false;
            if (x1 > -z1 && x2 > -z2)
                return false;
            if (-x1 > -z1 && -x2 > -z2)
                return false;
            final int dx = x2-x1;
            final int dz = z2-z1;
            FloatPair fp = new FloatPair(0, 1);
            if (!fp.clipt(-dx-dz,x1+z1))
                return false;
            if (!fp.clipt(dx-dz,-x1+z1))
                return false;
            if (!fp.clipt(-dz,z1-4))
                return false;
            // update internals of parameter rp
            if (fp.p2 < 1) {
                rp.x2 = (int) (x1 + fp.p2*dx);
                rp.z2 = (int) (z1 + fp.p2*dz);
            }
            if (fp.p1 > 0) {
                rp.x1 += fp.p1*dx;
                rp.z1 += fp.p1*dz;
            }
            return true;
        }
    }
    /**
     * Trivial class to hold to double values. Used only in FirstPersonDrawer.
     *
     */
    class FloatPair {
        public double p1, p2;
        FloatPair(double pp1, double pp2) {
            p1 = pp1;
            p2 = pp2;
        }
        /**
         * Helper method for clip3d
         * @param denom
         * @param num
         * @return
         */
        public boolean clipt(int denom, int num) {
            if (denom > 0) {
                double t = num * 1.0 / denom;
                if (t > p2)
                    return false;
                if (t > p1)
                    p1 = t;   // update fp
            } else if (denom < 0) {
                double t = num * 1.0 / denom;
                if (t < p1)
                    return false;
                if (t < p2)
                    p2 = t; // update fp
            } else if (num > 0)
                return false;
            return true;

        }
    }
}

