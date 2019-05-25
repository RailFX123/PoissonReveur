/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poissonreveur;

import ControlFile.OpenFiles;
import MainExecution.ExecutionTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

/**
 *
 * @author georg
 */
public class ViewController implements Initializable {

    @FXML
    private TextArea txtCode;

    @FXML
    private TextArea txtResult;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        File este = null;
        try {
            este = archivos.abrir();

            if (este != null) {
                extension = este.getAbsolutePath();
                if (extension.contains(".java")) {
                    alertar("Java");
                } else if (extension.contains(".c")) {
                    alertar("C");
                }

                BufferedReader br = archivos.readFiles(este);
                String linea = "";
                while ((linea = br.readLine()) != null) {
                    txtCode.appendText(linea + "\n");
                }
            }

        } catch (Exception e) {
        }
    }

    @FXML
    private void analizar(ActionEvent eventF) {
        if (extension != null) {
            String v = test.iniciar(extension);
            txtResult.setText(v);
        } else {
            alertar("Error", "No hay código para analizar");
        }

    }

    @FXML
    private void limpiar(ActionEvent evt) {
        txtCode.setText("");
        txtResult.setText("");
    }

    private void alertar(String formato) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Poisson Rêveur");
        alert.setHeaderText("El Archivo es de tipo");
        alert.setContentText(formato);
        alert.showAndWait();
    }

    private void alertar(String formato, String aviso) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Poisson Rêveur");
        alert.setHeaderText(formato);
        alert.setContentText(aviso);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtCode.setEditable(false);
        txtResult.setEditable(false);
    }

//    Varibales que no se deben manosear
    private final OpenFiles archivos = new OpenFiles();
    private final ExecutionTest test = new ExecutionTest();
    private String extension = null;
}
