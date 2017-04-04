
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.lang.Math;


public class AStarDiv implements AIModule
{

    public class PointCostComparator implements Comparator<Point>
    {
        final Map<Point, Double> pointToCost;
        final TerrainMap map;
        final double end_point_height;
        final Point end_point;

        PointCostComparator(Map<Point, Double> ptc, TerrainMap m)
        {
            pointToCost = ptc;
            map = m;
            end_point = map.getEndPoint();
            end_point_height = map.getTile(end_point);
        }

        @Override
        public int compare(Point p1, Point p2)
        {
            double p1_cost = pointToCost.get(p1) + getHeuristic(p1);
            double p2_cost = pointToCost.get(p2) + getHeuristic(p2);
            if (p1_cost < p2_cost)
            {
                return -1;
            }
            if (p1_cost > p2_cost)
            {
                return 1;
            }
            return 0;
        }

        private double getHeuristic(final Point pt)
        {
            double pt_height = map.getTile(pt);
            double delta_d = Math.max(Math.abs(end_point.x - pt.x), 
                                      Math.abs(end_point.y - pt.y));
            if (pt_height == 0)
                return 0.0;
            return delta_d/2;
            
        }
    }

    /// Creates the path to the goal.
    public List<Point> createPath(final TerrainMap map)
    {
        // Holds the resulting path
        final Point startPoint = map.getStartPoint(); 
        
        final Set<Point> explored = new HashSet<Point>();
        final Map<Point, Double> pointToCost = new HashMap<Point, Double>();
        final Comparator<Point> comparator = new PointCostComparator(pointToCost, map);
        final PriorityQueue<Point> frontier = new PriorityQueue<Point>(100, comparator);

        final Map<Point, ArrayList<Point>> frontierPaths = new HashMap<Point, ArrayList<Point>>();

        pointToCost.put(startPoint, 0.0);
        frontier.add(startPoint);
        ArrayList<Point> newPath = new ArrayList<Point>();
        newPath.add(startPoint);
        frontierPaths.put(startPoint, newPath);
        while(true)
        {
            Point node = frontier.poll();
            //if found end point return solution
            if (node.equals(map.getEndPoint()))
                return frontierPaths.get(node);

            explored.add(node);
            Point[] neighbors = map.getNeighbors(node);
            for(final Point neighbor : neighbors)
            {
                if (!explored.contains(neighbor))
                {
                    if (!frontier.contains(neighbor))
                    {
                        pointToCost.put(neighbor, map.getCost(node, neighbor) + pointToCost.get(node));
                        frontier.add(neighbor);

                        ArrayList<Point> prevPath = frontierPaths.get(node);
                        ArrayList<Point> copyPrevPath = new ArrayList<>(prevPath);
                        copyPrevPath.add(neighbor);
                        frontierPaths.put(neighbor, copyPrevPath);
                    }
                    else if (pointToCost.get(node) + map.getCost(node, neighbor) < pointToCost.get(neighbor))
                    {    
                        pointToCost.put(neighbor, pointToCost.get(node) + map.getCost(node, neighbor));
                        frontier.remove(neighbor);
                        frontier.add(neighbor);
                        
                        ArrayList<Point> prevPath = frontierPaths.get(node);
                        ArrayList<Point> copyPrevPath = new ArrayList<>(prevPath);
                        copyPrevPath.add(neighbor);
                        frontierPaths.put(neighbor, copyPrevPath);
                    }
                }
            }
        }
    }
}