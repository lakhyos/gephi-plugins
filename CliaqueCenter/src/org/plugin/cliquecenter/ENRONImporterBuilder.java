/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.plugin.cliquecenter;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import javax.swing.JFileChooser;
import org.openide.util.Exceptions;

/**
 *
 * @author lakhyos
 */

//@ServiceProvider(service = FileImporterBuilder.class)
public final class ENRONImporterBuilder {

    ENRONImporter importer;
    
    public ENRONImporterBuilder() {
        try {
            openFile();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void openFile() throws Exception{
    
        importer = new ENRONImporter();

        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);
        String filePath;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            filePath = file.getAbsolutePath();
            System.out.println(filePath);
            importer.importData(new LineNumberReader(new FileReader(filePath)));
          }
    }
}
