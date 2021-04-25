package com.ece420.lab6;

import org.jgrapht.Graph;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.*;

import java.util.List;
import java.util.Set;

public class GraphSegmenter {

    ImageGraphConverter graphConverter;

    public GraphSegmenter() {
        graphConverter = new ImageGraphConverter();
    }


    public byte[] segmentImage(byte[] image, int width, int height, List<IntPair> bkgSeeds, List<IntPair> objSeeds) {
        byte[][] image2d = new byte[height][width];
        byte[] newImage = new byte[width*height];
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                image2d[j][i] = image[j*width + i];
                newImage[j*width + i] = image[j*width+ i];
            }
        }
        System.out.println("Setting seeds");
        graphConverter.setBkgSeeds(bkgSeeds);
        graphConverter.setObjSeeds(objSeeds);
        System.out.println("Converting to graph");
        Graph<IntPair, DefaultWeightedEdge> graph = graphConverter.convertImageToGraph(image2d);


        // TODO: use a custom implementation for extra brownie points
        // Note that in version 1.0.1, Boykov-Kolmogorov does not exist
        // Thus, we would have to implement it ourselves
        System.out.println("Calculating minimum cut");
        PushRelabelMFImpl<IntPair, DefaultWeightedEdge> mincut = new PushRelabelMFImpl<>(graph);
        mincut.calculateMinCut(graphConverter.source, graphConverter.sink);
        Set<IntPair> objPartition, bkgPartition;
       // objPartition = mincut.getSourcePartition();
        bkgPartition = mincut.getSinkPartition();

        for (IntPair p : bkgPartition) {
            newImage[p.y*width + p.x] = 0; // super hacky way to delete the background
        }

        return newImage;
    }
}
