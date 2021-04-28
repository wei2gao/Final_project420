package com.ece420.lab6;

import org.jgrapht.graph.DefaultWeightedEdge;

public class ResidualGraphEdge extends DefaultWeightedEdge {
    double capacity, flow;

    public ResidualGraphEdge() {
        this(0,0);
    }

    public ResidualGraphEdge(double capacity, double flow) {
        super();
        this.capacity = capacity;
        this.flow = flow;
    }


    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(double flow) {
        this.flow = flow;
    }
}
