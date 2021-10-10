package cz.osu.ukol;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileReader;
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
    private TextField fixedField;
    @FXML
    private ComboBox codeChoiceList;
    @FXML
    private Label errorLabel;
    @FXML
    private Label fixedLabel;
    @FXML
    private GridPane matrixPane;
    @FXML
    private GridPane mainPane;
    @FXML
    private Button calculateButton;

    private ArrayList<CodeType> codeList;
    private TextField[][] matrix;

    @FXML
    public void initialize() {
        codeList = new ArrayList<CodeType>();
        codeList.add(new CodeType(6, 2));
        codeList.add(new CodeType(7, 4));
        codeList.add(new CodeType(8, 4));

        Pattern CodeInputPattern = Pattern.compile("^[0-1]+$|^$");

        TextFormatter inputFormatter = new TextFormatter(
                (UnaryOperator<TextFormatter.Change>)
                        change -> CodeInputPattern.matcher(change.getControlNewText()).matches() ? change : null);
        inputField.setTextFormatter(inputFormatter);

        codeChoiceList.setItems(FXCollections.observableArrayList(codeList));
        codeChoiceList.getSelectionModel().select(0);
        updateMatrix(codeList.get(0));
        matrixPane.setAlignment(Pos.CENTER);
    }

    @FXML
    public void onComboAction() {
        updateMatrix((CodeType) codeChoiceList.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void onFileButtonClick() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zvol soubor s daty.");
        File file = fileChooser.showOpenDialog(stage);

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            JsonElement jelement = JsonParser.parseReader(reader);
            JsonObject jobject = jelement.getAsJsonObject();

            String input = jobject.get("input").getAsString();
            int choice = jobject.get("option").getAsInt();
            int[][] arr = gson.fromJson(jobject.getAsJsonArray("array"), int[][].class);

            inputField.setText(input);
            codeChoiceList.getSelectionModel().select(choice);
            CodeType code = (CodeType) codeChoiceList.getSelectionModel().getSelectedItem();
            updateMatrix(code);

            for(int i = 0; i < matrix.length;i++) {
                for(int j = 0; j < matrix[0].length; j++) {
                    matrix[i][j].setText(Integer.toString(arr[i][j]));
                }
            }
            errorLabel.setText("");
            this.onCalculateButtonClick();
        }
        catch (Exception e) {
            errorLabel.setText("Soubor nelze načíst.");
            System.err.println(e.getMessage());
        }
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
        int syndrome;
        if(input.length() == code.getNumberOfInfoBits()) {
            result = calc.encode(input);
            outputField.setText(result);
            syndrome = calc.calcSyndrome(result);
        }
        else {
            result = calc.decode(input);
            outputField.setText(result);
            syndrome = calc.calcSyndrome(input);

        }
        synField.setText(StringUtils.leftPad(Integer.toString(syndrome), calc.getCode().getRedundancy(), '0'));
        if(syndrome != 0){
            errorLabel.setText("Nenulový syndrom!");
            CodeReport rep = calc.getCodereport(input);
            if(rep.isCanBeFixed()) {
                fixedLabel.setDisable(false);
                fixedField.setDisable(false);

                int pos = rep.getBitFlipPosition() - 1;
                if(input.charAt(rep.getBitFlipPosition() - 1) == '0')
                    input = input.substring(0, pos) + "1" + input.substring(pos + 1);
                else input = input.substring(0, pos) + "0" + input.substring(pos + 1);

                fixedField.setText(input);
            }
            else errorLabel.setText("Chybu v kódu nelze opravit.");
        }
        else {
            fixedLabel.setDisable(true);
            fixedField.clear();
            fixedField.setDisable(true);
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

    /**
     * Aktualizuji matici v UI.
     * @param code
     */
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

    /**
     * Kontroluji uživatelský vstup.
     * V případě chyby informuji uživatele prostřednictvím textového pole v UI.
     * @param input
     * @param code
     * @return
     */
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