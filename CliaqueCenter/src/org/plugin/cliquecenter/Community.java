/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugin.cliquecenter;

import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author lakhyos
 */
class Community {

    double weightSum;
    CommunityStructure structure;
    LinkedList<Integer> nodes;
    HashMap<Community, Float> connectionsWeight;
    HashMap<Community, Integer> connectionsCount;

    public int size() {
        return nodes.size();
    }

    public Community(Community com) {
        structure = com.structure;
        connectionsWeight = new HashMap<Community, Float>();
        connectionsCount = new HashMap<Community, Integer>();
        nodes = new LinkedList<Integer>();
        //mHidden = pCom.mHidden;
    }

    public Community(CommunityStructure structure) {
        this.structure = structure;
        connectionsWeight = new HashMap<Community, Float>();
        connectionsCount = new HashMap<Community, Integer>();
        nodes = new LinkedList<Integer>();
    }

    public void seed(int node) {
        nodes.add(node);
        weightSum += structure.weights[node];
    }

    public boolean add(int node) {
        nodes.addLast(node);
        weightSum += structure.weights[node];
        return true;
    }

    public boolean remove(int node) {
        boolean result = nodes.remove(new Integer(node));
        weightSum -= structure.weights[node];
        if (nodes.size() == 0) {
            structure.communities.remove(this);
        }
        return result;
    }
}
