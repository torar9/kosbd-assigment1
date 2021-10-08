package cz.osu.ukol;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class MainController {
    @FXML
    private TextField inputField;
    @FXML
    private TextField outputField;
    @FXML
    private TextField dField;
    @FXML
    private TextField synField;
    @FXML
    private ComboBox codeChoiceList;
    @FXML
    private Label errorLabel;
    @FXML
    private GridPane matrixPane;
    @FXML
    private Button calculateButton;

    private ArrayList<CodeType> codeList;
    private TextField[][] matrix;

    @FXML
    public void initialize() {
        codeList = new ArrayList<CodeType>();
        codeList.add(new CodeType(7, 4));
        codeList.add(new CodeType(8, 4));
        codeList.add(new CodeType(15, 11));

        Pattern CodeInputPattern = Pattern.compile("^[0-1]+$|^$");

        TextFormatter inputFormatter = new TextFormatter(
                (UnaryOperator<TextFormatter.Change>)
                        change -> CodeInputPattern.matcher(change.getControlNewText()).matches() ? change : null);
        inputField.setTextFormatter(inputFormatter);

        codeChoiceList.setItems(FXCollections.observableArrayList(codeList));
        codeChoiceList.getSelectionModel().select(0);
        updateMatrix(codeList.get(0));
    }

    @FXML
    public void onComboAction() {
        updateMatrix((CodeType) codeChoiceList.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void onCalculateButtonClick() {
        String input = inputField.getText();
        CodeType code = (CodeType) codeChoiceList.getSelectionModel().getSelectedItem();

        if(!checkInput(input, code))
            return;

        int[][] matrix = getIntArray(code);
        Calculator calc = new Calculator(matrix, code);
        int d = calc.getMinHammingLen();
        dField.setText(Integer.toString(d));

        String result;
        if(input.length() == code.getNumberOfInfoBits()) {
            result = calc.encode(input);
            outputField.setText(result);
            synField.setText(Integer.toString(calc.calcSyndrome(result)));
        }
        else {
            result = calc.decode(input);
            outputField.setText(result);
            synField.setText(Integer.toString(calc.calcSyndrome(input)));
        }
    }

    @FXML
    public void onInputFieldTyped() {
        String input = inputField.getText();
        CodeType code = (CodeType) codeChoiceList.getSelectionModel().getSelectedItem();
        int len = input.length();

        if(len < code.getNumberOfInfoBits()) {
            calculateButton.setDisable(true);
            errorLabel.setText("Neplatná délka kódu.");
        }
        else if(len == code.getNumberOfInfoBits()) {
            calculateButton.setDisable(false);
            calculateButton.setText("Zakódovat");
            errorLabel.setText("");
        }
        else if(len == code.getLength()) {
            calculateButton.setDisable(false);
            calculateButton.setText("Dekódovat");
            errorLabel.setText("");
        }
        else {
            errorLabel.setText("Neplatná délka kódu.");
            calculateButton.setDisable(true);
        }
    }

    private void updateMatrix(CodeType code) {
        matrix = new TextField[code.getNumberOfInfoBits()][code.getLength()];
        matrixPane.getChildren().removeAll();
        matrixPane.getChildren().clear();

        for(int y = 0; y < code.getNumberOfInfoBits(); y++) {

            for(int x = 0; x < code.getLength(); x++) {
                TextField tf = new TextField();
                tf.setPrefSize(25, 25);
                tf.setMaxSize(25, 25);
                tf.setAlignment(Pos.CENTER);
                tf.setEditable(true);
                tf.setText("0");
                tf.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        tf.selectAll();
                    }
                });
                Pattern CodeInputPattern = Pattern.compile("^[0-1]+$|^$");

                TextFormatter inputFormatter = new TextFormatter(
                        (UnaryOperator<TextFormatter.Change>)
                                change -> CodeInputPattern.matcher(change.getControlNewText()).matches() ? change : null);
                tf.setTextFormatter(inputFormatter);

                matrixPane.add(tf, x, y, 1, 1);
                matrix[y][x] = tf;
            }
        }
    }

    private boolean checkInput(String input, CodeType code) {
        if(input.isEmpty()) {
            errorLabel.setText("Prázdný vstup");
            return false;
        }

        if(input.length() != code.getNumberOfInfoBits() && input.length() != code.getLength()) {
            errorLabel.setText("Neplatná délka kódu.");
            return false;
        }

        for(int y = 0; y < code.getNumberOfInfoBits(); y++) {
            for(int x = 0; x < code.getNumberOfInfoBits(); x++) {
                if(x == y){
                    if(!matrix[y][x].getText().equals("1")) {
                        errorLabel.setText("Matice není systematická.");
                        return false;
                    }
                }
                else if(!matrix[y][x].getText().equals("0")) {
                    errorLabel.setText("Matice není systematická.");
                    return false;
                }

            }
        }

        errorLabel.setText("");
        return true;
    }

    private int[][] getIntArray(CodeType code) {
        int[][] result = new int[code.getNumberOfInfoBits()][code.getLength()];

        for(int y = 0; y < code.getNumberOfInfoBits(); y++) {
            for(int x = 0; x < code.getLength(); x++) {
                result[y][x] = Integer.parseInt(matrix[y][x].getText());
            }
        }

        return result;
    }
}