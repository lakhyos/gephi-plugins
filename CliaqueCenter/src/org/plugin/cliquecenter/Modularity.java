/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugin.cliquecenter;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import org.gephi.data.attributes.api.AttributeModel;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;


/**
 *
 * @author ayoub
 */
public class Modularity {

    public static final String MODULARITY_CLASS = "modularity_class";
    private ProgressTicket progress;
    private boolean isCanceled;
    private CommunityStructure structure;
    private double modularity;
    private double modularityResolution;
    private boolean isRandomized = false;
    private boolean useWeight = true;
    private double resolution = 1.;


   
    
    public void startCalc(GraphModel graphModel) {
        Graph hgraph = graphModel.getUndirectedGraphVisible();
        execute(hgraph);
    }

    public void execute(Graph hgraph) {
        isCanceled = false;

        hgraph.readLock();

        structure = new CommunityStructure(hgraph, structure);
        int[] comStructure = new int[hgraph.getNodeCount()];

        HashMap<String, Double> computedModularityMetrics = computeModularity(hgraph, structure, comStructure, resolution, isRandomized, useWeight);

        modularity = computedModularityMetrics.get("modularity");
        modularityResolution = computedModularityMetrics.get("modularityResolution");

        hgraph.readUnlock();
    }

    protected HashMap<String, Double> computeModularity(Graph hgraph, CommunityStructure theStructure, int[] comStructure,
            double currentResolution, boolean randomized, boolean weighted) {
        isCanceled = false;
        Progress.start(progress);
        Random rand = new Random();

        double totalWeight = theStructure.graphWeightSum;
        double[] nodeDegrees = theStructure.weights.clone();

        HashMap<String, Double> results = new HashMap<String, Double>();

        if (isCanceled) {
            hgraph.readUnlockAll();
            return results;
        }
        boolean someChange = true;
        while (someChange) {
            someChange = false;
            boolean localChange = true;
            while (localChange) {
                localChange = false;
                int start = 0;
                if (randomized) {
                    start = Math.abs(rand.nextInt()) % theStructure.N;
                }
                int step = 0;
                for (int i = start; step < theStructure.N; i = (i + 1) % theStructure.N) {
                    step++;
                    Community bestCommunity = updateBestCommunity(theStructure, i, currentResolution);
                    if ((theStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        theStructure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                    if (isCanceled) {
                        hgraph.readUnlockAll();
                        return results;
                    }
                }
                someChange = localChange || someChange;
                if (isCanceled) {
                    hgraph.readUnlockAll();
                    return results;
                }
            }

            if (someChange) {
                theStructure.zoomOut();
            }
        }

        fillComStructure(hgraph, theStructure, comStructure);
        double[] degreeCount = fillDegreeCount(hgraph, theStructure, comStructure, nodeDegrees, weighted);

        double computedModularity = finalQ(comStructure, degreeCount, hgraph, theStructure, totalWeight, 1., weighted);
        double computedModularityResolution = finalQ(comStructure, degreeCount, hgraph, theStructure, totalWeight, currentResolution, weighted);

        results.put("modularity", computedModularity);
        results.put("modularityResolution", computedModularityResolution);

        return results;
    }

    Community updateBestCommunity(CommunityStructure theStructure, int i, double currentResolution) {
        double best = 0.;
        Community bestCommunity = null;
        Set<Community> iter = theStructure.nodeConnectionsWeight[i].keySet();
        for (Community com : iter) {
            double qValue = q(i, com, theStructure, currentResolution);
            if (qValue > best) {
                best = qValue;
                bestCommunity = com;
            }
        }
        return bestCommunity;
    }

    int[] fillComStructure(Graph hgraph, CommunityStructure theStructure, int[] comStructure) {
//        int[] comStructure = new int[hgraph.getNodeCount()];
        int count = 0;

        for (Community com : theStructure.communities) {
            for (Integer node : com.nodes) {
                Community hidden = theStructure.invMap.get(node);
                for (Integer nodeInt : hidden.nodes) {
                    comStructure[nodeInt] = count;
                }
            }
            count++;
        }
        return comStructure;
    }

    double[] fillDegreeCount(Graph hgraph, CommunityStructure theStructure, int[] comStructure, double[] nodeDegrees, boolean weighted) {
        double[] degreeCount = new double[theStructure.communities.size()];

        for (Node node : hgraph.getNodes()) {
            int index = theStructure.map.get(node);
            if (weighted) {
                degreeCount[comStructure[index]] += nodeDegrees[index];
            } else {
                degreeCount[comStructure[index]] += hgraph.getDegree(node);
            }

        }
        return degreeCount;
    }

    private double finalQ(int[] struct, double[] degrees, Graph hgraph,
            CommunityStructure theStructure, double totalWeight, double usedResolution, boolean weighted) {

        double res = 0;
        double[] internal = new double[degrees.length];
        for (Node n : hgraph.getNodes()) {
            int n_index = theStructure.map.get(n);
            for (Node neighbor : hgraph.getNeighbors(n)) {
                if (n == neighbor) {
                    continue;
                }
                int neigh_index = theStructure.map.get(neighbor);
                if (struct[neigh_index] == struct[n_index]) {
                    if (weighted) {
                        internal[struct[neigh_index]] += hgraph.getEdge(n, neighbor).getWeight();
                    } else {
                        internal[struct[neigh_index]]++;
                    }
                }
            }
        }
        for (int i = 0; i < degrees.length; i++) {
            internal[i] /= 2.0;
            res += usedResolution * (internal[i] / totalWeight) - Math.pow(degrees[i] / (2 * totalWeight), 2);//HERE
        }
        return res;
    }

    

    public double getModularity() {
        return modularity;
    }


    public int getCommunitiesNumber() {
        return structure.communities.size();
    }

    private double q(int node, Community community, CommunityStructure theStructure, double currentResolution) {
        Float edgesToFloat = theStructure.nodeConnectionsWeight[node].get(community);
        double edgesTo = 0;
        if (edgesToFloat != null) {
            edgesTo = edgesToFloat.doubleValue();
        }
        double weightSum = community.weightSum;
        double nodeWeight = theStructure.weights[node];
        double qValue = currentResolution * edgesTo - (nodeWeight * weightSum) / (2.0 * theStructure.graphWeightSum);
        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() > 1)) {
            qValue = currentResolution * edgesTo - (nodeWeight * (weightSum - nodeWeight)) / (2.0 * theStructure.graphWeightSum);
        }
        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() == 1)) {
            qValue = 0.;
        }
        return qValue;
    }
}