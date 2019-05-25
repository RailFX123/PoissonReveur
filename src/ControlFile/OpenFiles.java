package ControlFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.FileChooser;

public class OpenFiles {

    public File abrir() {
        File archivo = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecci�n de Archivo");

        // Cambia el directorio dependiendo del sistema operativo
        String os = System.getProperty("os.name");
        if (!os.contains("Windows")) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } // end if

        try {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Archivo", "*.java", "*.c"));
            archivo = fileChooser.showOpenDialog(null);
        } catch (Exception e) {
            System.err.println("Archivo No seleccionado");
        }

        return archivo;
    }

    public BufferedReader readFiles(File archivo) {
        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(archivo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OpenFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        br = new BufferedReader(fr);
        
        return br;
    }
}
