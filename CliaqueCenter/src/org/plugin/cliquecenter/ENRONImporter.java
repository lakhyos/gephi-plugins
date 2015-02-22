/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugin.cliquecenter;

import java.io.LineNumberReader;
import java.io.Reader;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 *
 * @author lakhyos
 */
public class ENRONImporter {

    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    int nNodes;
    int nEdges;

    public ENRONImporter() {
        Container ctn = (Container) Lookup.getDefault().lookup(ContainerFactory.class)
                .newContainer();
        container = ctn.getLoader();
    }

    public void importData(LineNumberReader reader) throws Exception {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        // read type code initial line
        String line = reader.readLine();
       
        String[] str = line.split("\t");
        nNodes = (Integer.valueOf(str[0].trim()));
        nEdges = (Integer.valueOf(str[1].trim()));

        System.out.println(nNodes);
        System.out.println(nEdges);

        // read comment lines if any
        boolean comment = true;
        while (comment) {
            line = reader.readLine();
            comment = line.startsWith("#");
        }

        //String[] str = line.split("\t");
        //int nRows = (Integer.valueOf(str[0].trim())).intValue();
        //int nColumns = (Integer.valueOf(str[1].trim())).intValue();
        //int nNonZeros = (Integer.valueOf(str[2].trim())).intValue();
        //report.log("Number of edges: " + nEdges);
        //report.log("Number of nodes: " + nNodes);
        //report.log("Number of non zeros: " + nNonZeros);
        while ((line = reader.readLine()) != null) {
            //Read coordinates and value
            str = line.split("\t");
            int node1Index = (Integer.valueOf(str[0].trim()));
            int node2Index = (Integer.valueOf(str[1].trim()));
            float weight = 1f;
            if (str.length > 2) {
                weight = (Double.valueOf(str[2].trim())).floatValue();
            }
            //System.out.println(node1Index + "+" + node2Index);

            //Get or create node
            NodeDraft node1 = null;
            if (container.nodeExists(String.valueOf(node1Index))) {
                node1 = container.getNode(String.valueOf(node1Index));
            } else {
                node1 = container.factory().newNodeDraft();
                node1.setId(String.valueOf(node1Index));

                //Don't forget to add the node
                container.addNode(node1);
            }
            NodeDraft node2 = null;
            if (container.nodeExists(String.valueOf(node2Index))) {
                node2 = container.getNode(String.valueOf(node2Index));
            } else {
                node2 = container.factory().newNodeDraft();
                node2.setId(String.valueOf(node2Index));

                //Don't forget to add the node
                container.addNode(node2);
            }

            //Create edge
            EdgeDraft edgeDraft = container.factory().newEdgeDraft();
            edgeDraft.setSource(node1);
            edgeDraft.setTarget(node2);
            edgeDraft.setWeight(weight);
            container.addEdge(edgeDraft);
        }
            ImportController importController = Lookup.getDefault().lookup(ImportController.class);
            importController.process((Container) container, new DefaultProcessor(), workspace);
    }

}
