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

    private void grow() {
        while (!activeNodes.isEmpty()) {
            IntPair p = activeNodes.toArray(new IntPair[activeNodes.size()])[0];
            List<IntPair> neighbors = Graphs.neighborListOf(residualGraph, p);
            for (IntPair q : neighbors) {
                if (treeCapacity(p, q) > 0) {
                    if (tree(p).equals("")) {
                        activeNodes.add(q);
                        if (_tree.getOrDefault(p, "") == null) {
                            _tree.put(q, "");
                        } else {
                            _tree.put(q, _tree.getOrDefault(p, ""));
                        }
                        parents.put(q, p);
                    }
                    else
                    {
                        if (!_tree.getOrDefault(p, "").equals(_tree.get(q))) {
                            IntPair node_before, node_after;
                            // TODO: reconstruct the path and return it
                        }
                    }
                }

            }
        }
    }

    private void augment() {

    }

    private void adopt() {

    }

    private void reconstructTrees() {

    }

}
