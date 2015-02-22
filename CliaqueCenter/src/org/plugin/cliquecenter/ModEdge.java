/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugin.cliquecenter;

/**
 *
 * @author lakhyos
 */
class ModEdge {

    public int source;
    public int target;
    public float weight;

    public ModEdge(int s, int t, float w) {
        source = s;
        target = t;
        weight = w;
    }
}
