package com.ece420.lab6;

import android.media.Image;
import android.util.ArraySet;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoykovKolmogorov {

    public Set<IntPair> sPartition, tPartition;
    private Set<IntPair> activeNodes, orphans;
    private Map<IntPair, IntPair> parents; // Probably a HashMap
    private Map<IntPair, String> _tree;
    private double flow;

    // TODO: residual graph
    private Graph<IntPair, ResidualGraphEdge> residualGraph;

    public BoykovKolmogorov (Graph<IntPair, DefaultWeightedEdge> graph) {
        sPartition = new HashSet<IntPair>();
        tPartition = new HashSet<IntPair>();
        activeNodes = new HashSet<IntPair>();
        orphans = new HashSet<IntPair>();
        parents = new HashMap<>();
        _tree = new HashMap<IntPair, String>();

        activeNodes.add(ImageGraphConverter.source);
        activeNodes.add(ImageGraphConverter.sink);

        residualGraph = new SimpleDirectedWeightedGraph<IntPair, ResidualGraphEdge>(ResidualGraphEdge.class);
        Graphs.addAllVertices(residualGraph, graph.vertexSet());

        // Realistically we would probably want to give source and sink as arguments, but whatever
        _tree.put(ImageGraphConverter.source, "S");
        _tree.put(ImageGraphConverter.sink, "T");

        // TODO: Add in the edges as ResidualGraphEdges and also we need to track both capacity and flow
        // Construct the residual graph
        for (DefaultWeightedEdge e : graph.edgeSet()) {
            IntPair p, q;
            double weight;
            p = graph.getEdgeSource(e);
            q = graph.getEdgeTarget(e);
            weight = graph.getEdgeWeight(e);

            // Initializing with
            ResidualGraphEdge newEdge = new ResidualGraphEdge(weight, 0);
            residualGraph.addEdge(p, q, newEdge);

            // Also add back-edges for source and sink with zero capacity
            /// This is pretty clunky but it will have to do for now
            IntPair source = ImageGraphConverter.source;
            IntPair sink = ImageGraphConverter.sink;

            if (p.equals(source) || q.equals(sink)) {
                ResidualGraphEdge backEdge = new ResidualGraphEdge(0,0);
                residualGraph.addEdge(q, p, backEdge);
            }

        }
    }

    public void calculate () {
        while (true) {
            System.out.println("Grow");
            List<IntPair> P = grow();
            System.out.println("Path length: " + P.size());
            if (P.isEmpty()) break;
            System.out.println("Augment");
            augment(P);
            System.out.println("Adopt");
            adopt();
        }
        reconstructTrees();
    }

    private String tree(IntPair p) {
        if (_tree.containsKey(p)) {
            return _tree.get(p);
        } else {
            return "";
        }
    }

    private double treeCapacity(IntPair p, IntPair q) {
        if (tree(p).equals("S")) {
            return residualGraph.getEdge(p,q).getCapacity() - residualGraph.getEdge(p,q).getFlow();
        }
        else if (tree(p).equals("T")) {
            return residualGraph.getEdge(q,p).getCapacity() - residualGraph.getEdge(q,p).getFlow();
        }
        else {
            return 0;
        }
    }

    private double edgeCapacity(IntPair p, IntPair q) {
        if (residualGraph.containsEdge(p, q)) {
            return residualGraph.getEdge(p,q).getCapacity();
        }
        else return 0;
    }

    private List<IntPair> grow() {

        List<IntPair> path = new ArrayList<>();
        while (!activeNodes.isEmpty()) {
            IntPair p = activeNodes.toArray(new IntPair[activeNodes.size()])[0];
            List<IntPair> neighbors = Graphs.neighborListOf(residualGraph, p);
            for (IntPair q : neighbors) {
                if (treeCapacity(p, q) > 0) {
                    if (!_tree.containsKey(q) || _tree.getOrDefault(q, "").equals("")) {
                        activeNodes.add(q);
                        _tree.put(q, _tree.getOrDefault(p, ""));
                        parents.put(q, p);
                    }
                    else
                    {
                        if (!_tree.getOrDefault(p, "").equals(_tree.get(q))) {
                            IntPair node_before, node_after;
                            // TODO: reconstruct the path and return it

                            if (tree(p).equals("S")) {
                                node_before = p;
                                node_after = q;
                            } else /*if (tree(p).equals("T"))*/ {
                                node_before = q;
                                node_after = p;
                            }

                            path.add(node_before);
                            path.add(node_after);

                            IntPair parentP, parentQ;
                            parentP = parents.getOrDefault(node_before, null);
                            while (parentP != null) {
                                path.add(0, parentP);
                                if (parentP.equals(ImageGraphConverter.source)) break;
                                parentP = parents.getOrDefault(parentP, null);
                            }

                            parentQ = parents.getOrDefault(node_after, null);
                            while (parentQ != null) {
                                path.add(parentQ);
                                if (parentQ.equals(ImageGraphConverter.sink)) break;
                                parentQ = parents.getOrDefault(parentQ, null);
                            }

                            return path;
                        }
                    }
                }

            }
            activeNodes.remove(p);
        }
        return path;
    }

    private void augment(List<IntPair> path) {
        double bottleneck = Double.MAX_VALUE;
        for (int i = 0; i < path.size() - 1; ++i) {
            double capacity = edgeCapacity(path.get(i), path.get(i+1))
                    - residualGraph.getEdge(path.get(i), path.get(i+1)).getFlow();

            if (capacity < bottleneck) {
                bottleneck = capacity;
            }
        }

        for (int i = 0; i < path.size() - 1; ++i) {
            residualGraph.getEdge(path.get(i), path.get(i+1)).flow =  residualGraph.getEdge(path.get(i), path.get(i+1)).flow + bottleneck;
            residualGraph.getEdge(path.get(i+1), path.get(i)).flow =  residualGraph.getEdge(path.get(i+1), path.get(i)).flow - bottleneck;

            if (residualGraph.getEdge(path.get(i), path.get(i+1)).getFlow() == residualGraph.getEdge(path.get(i), path.get(i+1)).getCapacity()) {
                IntPair p, q;
                p = path.get(i);
                q = path.get(i+1);
                if (tree(p).equals("S") && tree(q).equals("S")) {
                    parents.remove(q);
                    orphans.add(q);
                }
                else if (tree(p).equals("T") && tree(q).equals("T")) {
                    parents.remove(p);
                    orphans.add(p);
                }
            }
        }
    }

    private void adopt() {
        while (!orphans.isEmpty()) {
            IntPair p = orphans.toArray(new IntPair[0])[0];
            orphans.remove(p);
            IntPair parent = null;

            for (IntPair q : Graphs.neighborListOf(residualGraph, p)) {
                boolean same_tree, pos_cap, origin_source_sink;

                same_tree = (tree(p).equals(tree(q)));
                pos_cap = (treeCapacity(q,p) > 0);

                origin_source_sink = false;
                IntPair q_parent = q;
                while (q_parent != null) {
                    if (q_parent.equals(ImageGraphConverter.source) || q_parent.equals(ImageGraphConverter.sink)) {
                        origin_source_sink = true;
                        break;
                    }
                    q_parent = parents.getOrDefault(q_parent, null);
                }


                if (same_tree && pos_cap && origin_source_sink) {
                    parent = q;
                    break;
                }
            }

            if (parent != null) {
                parents.put(p, parent);
            } else {
                for (IntPair q : Graphs.neighborListOf(residualGraph, p)) {
                    if (tree(p).equals(tree(q))) {
                        if (treeCapacity(q,p) > 0) {
                            activeNodes.add(q);
                        }
                        if (parents.getOrDefault(q, null) != null && parents.getOrDefault(q, null).equals(p)) {
                            orphans.add(q);
                            parents.remove(q);
                        }
                        _tree.remove(p);
                        if (activeNodes.contains(p)) {
                            activeNodes.remove(p);
                        }
                    }
                }
            }

        }
    }

    private void reconstructTrees() {
        for (IntPair node : _tree.keySet()) {
            if (_tree.getOrDefault(node, "").equals("S")) {
                sPartition.add(node);
            }
            else if (_tree.getOrDefault(node,"").equals("T")) {
                tPartition.add(node);
            }
        }
    }

}
