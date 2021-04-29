package com.ece420.lab6;

import org.jgrapht.Graph;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GraphSegmenter {

    ImageGraphConverter graphConverter;
    static final int downsampleFactor = 16;

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

        // TODO: downsample the image by a factor of 4
        byte[][] image_downsample = new byte[height/downsampleFactor][width/downsampleFactor];

        for (int i = 0; i < width/downsampleFactor; i ++) {
            for (int j = 0; j < height/downsampleFactor; j ++) {
                image_downsample[j][i] = image[(j*downsampleFactor)*width + i*downsampleFactor];
            }
        }

        List<IntPair> bkgSeeds_downsampled, objSeeds_downsampled;
        bkgSeeds_downsampled = new ArrayList<IntPair>();
        objSeeds_downsampled = new ArrayList<IntPair>();

        // TODO: convert seeds to downsampled coordinates
        for (IntPair seed : bkgSeeds) {
            IntPair seed_downsampled = new IntPair(seed.x/downsampleFactor, seed.y/downsampleFactor);
            if (!bkgSeeds_downsampled.contains(seed_downsampled))
                bkgSeeds_downsampled.add(seed_downsampled);
        }

        for (IntPair seed : objSeeds) {
            IntPair seed_downsampled = new IntPair(seed.x/downsampleFactor, seed.y/downsampleFactor);
            if (!objSeeds_downsampled.contains(seed_downsampled))
                objSeeds_downsampled.add(seed_downsampled);
        }

        System.out.println("Setting seeds");
        graphConverter.setBkgSeeds(bkgSeeds_downsampled);
        graphConverter.setObjSeeds(objSeeds_downsampled);
        System.out.println("Converting to graph");
        Graph<IntPair, DefaultWeightedEdge> graph = graphConverter.convertImageToGraph(image_downsample);
        System.out.println("Conversion done");

        // TODO: use a custom implementation for extra brownie points
        // Note that in version 1.0.1, Boykov-Kolmogorov does not exist
        // Thus, we would have to implement it ourselves
        System.out.println("Calculating minimum cut");
//        PushRelabelMFImpl<IntPair, DefaultWeightedEdge> mincut = new PushRelabelMFImpl<>(graph);
//        mincut.calculateMinCut(graphConverter.source, graphConverter.sink);
        BoykovKolmogorov bk = new BoykovKolmogorov(graph);
        bk.calculate();
        Set<IntPair> objPartition, bkgPartition;
        objPartition = bk.sPartition;//mincut.getSourcePartition();
        bkgPartition = bk.tPartition; //.getSinkPartition();
        System.out.println("Cut computed");
        for (IntPair p : bkgPartition) {
            if (p.x >= 0 && p.y >= 0) {
                for (int i = 0; i < downsampleFactor; i++) {
                    for (int j = 0; j < downsampleFactor; j++) {

                        newImage[(p.y * downsampleFactor + j) * width + (p.x * downsampleFactor + i)] = 0; // super hacky way to delete the background
                    }
                }
            }


        }

        return newImage;
    }
}
