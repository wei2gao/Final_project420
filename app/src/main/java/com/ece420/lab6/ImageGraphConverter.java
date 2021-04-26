package com.ece420.lab6;
import org.jgrapht.*;
import org.jgrapht.Graphs;
import org.jgrapht.graph.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ImageGraphConverter {

    Map<IntPair, Boolean> seeds;
    private byte[][] image;
    private int[] histogramBkg, histogramObj;
    public IntPair source, sink;
    int objSum, bkgSum;

    private static final boolean bkgVal = false;
    private static final boolean objVal = true;

    private static final double cost_lambda = 0.9;
    private static final double cost_sigma = 5;

    public ImageGraphConverter() {
        histogramObj = new int[256];
        histogramBkg = new int[256];
        seeds = new HashMap<IntPair, Boolean>();
    }

    public void setBkgSeeds(List<IntPair> seedList) {
        for (IntPair p : seedList) {
            seeds.put(p, bkgVal);
        }
    }

    public void setObjSeeds(List<IntPair> seedList) {
        for (IntPair p : seedList) {
            seeds.put(p, objVal);
        }
    }

    public void addBkgSeed(IntPair px) {
        seeds.put(px, bkgVal);
    }

    public void addObjSeed(IntPair px) {
        seeds.put(px, objVal);
    }

    private void makeHistogram(byte[][] image) {
        int size = image.length * image[0].length;
        for (int i = 0; i < image.length; ++i) {
            for (int j = 0; j < image[0].length; ++j) {
                int dataVal = image[i][j] & 0x00FF;

                //System.out.println(i + " " + j);
                try {
                    IntPair p = new IntPair(j,i);
                    if (seeds.get(p) == bkgVal) {
                        histogramBkg[dataVal]++;
                        bkgSum++;
                    } else {
                        histogramObj[dataVal]++;
                        objSum++;
                    }
                } catch (NullPointerException e) {
                    // nothing
                }

            }
        }
    }

    public Graph<IntPair, DefaultWeightedEdge> convertImageToGraph(byte[][] image_in) {


       this.image = image_in;

        Graph<IntPair, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<IntPair, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        // TODO: add every pixel as an integer pair
        // TODO: add source and sink, perhaps s = (-1, -1) and t = (-2, -2)
        int width, height;
        width = image[0].length;
        height = image.length;
        System.out.println("Making histogram");
        makeHistogram(image);

        double weight_K = Integer.MIN_VALUE;
        System.out.println("Making nodes");
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                IntPair p =  new IntPair(i,j);
                for (int u = i - 1; u <= i + 1; u++) {
                    for (int v = j - 1; v <= j + 1; v++) {
                        if (u >= 0 && u < width && v >= 0 && v < height) {
                            if (u != i && v != j) {
                                IntPair q = new IntPair(u, v);
                                double candidate = costBpq(p, q);
                                if (candidate > weight_K) {
                                    weight_K = candidate;
                                }
                            }
                        }
                    }
                }
            }
        }


        source = new IntPair(-1, -1);
        sink = new IntPair(-2, -2);
        graph.addVertex(source);
        graph.addVertex(sink);
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                IntPair p = new IntPair(i,j);
                graph.addVertex(p);
                //DefaultWeightedEdge e1 = graph.addEdge(p, sink);
                //DefaultWeightedEdge e2 = graph.addEdge(source, p);
                double sinkWeight, sourceWeight;
                try {
                    if (seeds.get(p) == bkgVal) {
                        sourceWeight = 0;
                        sinkWeight = weight_K;
                    } else if (seeds.get(p) == objVal) {
                        sourceWeight = weight_K;
                        sinkWeight = 0;
                    } else {
                        sourceWeight = cost_lambda * costRp(p, false);
                        sinkWeight = cost_lambda * costRp(p, true);
                    }
                    Graphs.addEdge(graph, p, sink, sinkWeight); // TODO
                    Graphs.addEdge(graph, source, p, sourceWeight); // TODO
                } catch (NullPointerException e) {

                }

            }
        }

        // Once all the vertices are added. connect everything to its neighbors
        System.out.println("Making edges");
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                IntPair p = new IntPair(i, j);
                for (int u = i - 1; u <= i + 1; u++) {
                    for (int v = j - 1; v <= j + 1; v++) {
                        if (u >= 0 && u < width && v >= 0 && v < height) {
                            if (u != i && v != j) {
                                IntPair neighbor = new IntPair(u, v);
                                //DefaultWeightedEdge e1 = graph.addEdge(p, neighbor);
                                //DefaultWeightedEdge e2 = graph.addEdge(neighbor, p);
                                // graph.setEdgeWeight(...)
//                                graph.setEdgeWeight(e1, costBpq(p, neighbor)); // TODO
//                                graph.setEdgeWeight(e2, costBpq(p, neighbor)); // TODO
                                Graphs.addEdge(graph, p, neighbor, costBpq(p, neighbor));
                                Graphs.addEdge(graph, neighbor, p, costBpq(p, neighbor));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Done making edges");
        return graph; // The graph should now be ready for segmentation using a minimum cut algorithm.
    }

    private double costBpq(IntPair p, IntPair q) {
        int Ip,Iq;
        Ip = image[p.y][p.x];
        Iq = image[q.y][q.x];
        double distance = Math.sqrt(Math.pow(p.x - q.x, 2) + Math.pow(p.y - q.y, 2));
        return Math.exp(-Math.pow(Ip - Iq, 2) / 2 / Math.pow(cost_sigma, 2));
    }

    private double costRp(IntPair p, boolean isBkg) {
        int intensity = this.image[p.y][p.x];
        if (isBkg) {
            if (histogramBkg[intensity] == 0) {
                return -Math.log(-0.00001);
            } else {
                return -Math.log(histogramBkg[intensity]/(double)bkgSum);
            }
        } else {
            if (histogramObj[intensity] == 0) {
                return -Math.log(-0.00001);
            } else {
                return -Math.log(histogramObj[intensity]/(double)objSum);
            }
        }
    }
}
