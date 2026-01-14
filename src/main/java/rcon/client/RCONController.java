package rcon.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.glavo.rcon.Rcon;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RCONController {

    private final Rcon rcon = new Rcon();

    private final List<String> commandHistory = new ArrayList<>();

    private int commandIdx = 0;

    @FXML
    private Button disconnectButton;

    @FXML
    private Button connectButton;

    @FXML
    private TextField commandLineField;

    @FXML
    private TextField hostField;

    @FXML
    private TextField portField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextArea commandOutputArea;

    @FXML
    private Button sendButton;

    public void initialize() {
        hostField.textProperty().addListener((old, oldChar, newChar) -> validateHostField());
        portField.textProperty().addListener((old, oldChar, newChar) -> validatePortField());
        passwordField.textProperty().addListener((old, oldChar, newChar) -> validatePasswordField());
    }

    @FXML
    protected void onDisconnect() throws IOException {
        rcon.disconnect();
        onRCONDisconnect();
        commandOutputArea.appendText("RCON disconnected\n");
    }

    @FXML
    protected void onConnect() {
        if (!validateFields()) {
            return;
        }
        connectButton.setDisable(true);
        passwordField.setDisable(true);
        hostField.setDisable(true);
        portField.setDisable(true);

        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());
        String password = passwordField.getText();

        commandOutputArea.setText("Connecting...\n");
        commandOutputArea.clear();
        try {
            rcon.connect(host, port, password.getBytes(StandardCharsets.UTF_8));
            commandOutputArea.appendText("RCON connected\n");
            disconnectButton.setDisable(false);
            commandLineField.setDisable(false);
            sendButton.setDisable(false);
        } catch (Exception e) {
            onRCONDisconnect();
            commandOutputArea.setText(e.getMessage() + "\n" + toStringExceptionStackTrace(e));
        }
    }

    @FXML
    protected void sendCommand() {
        String command = commandLineField.getText();
        commandLineField.clear();

        commandOutputArea.appendText("> " + command + "\n");
        if (command != null && !command.isBlank()) {
            try {
                commandHistory.add(command);
                commandIdx = commandHistory.size();
                String output = rcon.command(command);
                commandOutputArea.appendText(output + "\n");
            } catch (IOException e) {
                onRCONDisconnect();
                commandOutputArea.setText(e.getMessage() + "\n" + toStringExceptionStackTrace(e));
            }
        }
    }

    @FXML
    protected void onClear() {
        commandOutputArea.clear();
    }

    public void handleInputCommand(KeyEvent e) {
        if (commandHistory.isEmpty()) {
            return;
        }

        if (e.getCode() == KeyCode.DOWN) {
            commandIdx = setCommandFromHistory(commandIdx + 1);
            return;
        }

        if (e.getCode() == KeyCode.UP) {
            commandIdx = setCommandFromHistory(commandIdx - 1);
            return;
        }

        commandIdx = commandHistory.size();
    }

    private void onRCONDisconnect() {
        hostField.setEditable(true);
        portField.setEditable(true);

        disconnectButton.setDisable(true);
        connectButton.setDisable(false);

        commandLineField.setDisable(true);
        sendButton.setDisable(true);

        passwordField.setDisable(false);
        hostField.setDisable(false);
        portField.setDisable(false);

        commandHistory.clear();
        commandIdx = 0;
    }

    private int setCommandFromHistory(int idx) {
        if (idx < 0) {
            return  0;

        } else if (idx >= commandHistory.size()) {
            return commandHistory.size();
        }

        commandLineField.setText(commandHistory.get(idx));
        return idx;
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        String host = hostField.getText().trim();
        if (host.isEmpty()) {
            errors.append("• Host cannot be empty\n");
            setFieldErrorStyle(hostField, true);
            isValid = false;
        } else if (!isValidHost(host)) {
            errors.append("• Invalid host format\n");
            setFieldErrorStyle(hostField, true);
            isValid = false;
        } else {
            setFieldErrorStyle(hostField, false);
        }

        String portText = portField.getText().trim();
        if (portText.isEmpty()) {
            errors.append("• Port cannot be empty\n");
            setFieldErrorStyle(portField, true);
            isValid = false;
        } else {
            try {
                int port = Integer.parseInt(portText);
                if (port < 1 || port > 65535) {
                    errors.append("• Port must be between 1 and 65535\n");
                    setFieldErrorStyle(portField, true);
                    isValid = false;
                } else {
                    setFieldErrorStyle(portField, false);
                }
            } catch (NumberFormatException e) {
                errors.append("• Port must be a valid number\n");
                setFieldErrorStyle(portField, true);
                isValid = false;
            }
        }

        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            errors.append("• Password cannot be empty\n");
            setFieldErrorStyle(passwordField, true);
            isValid = false;
        } else {
            setFieldErrorStyle(passwordField, false);
        }

        if (!isValid) {
            showValidationError(errors.toString());
        }

        return isValid;
    }

    private boolean isValidHost(String host) {
        if (host.equalsIgnoreCase("localhost")) {
            return true;
        }

        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (host.matches(ipv4Pattern)) {
            return true;
        }

        String domainPattern = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)+([A-Za-z]{2,}|[A-Za-z0-9-]{2,}\\.[A-Za-z]{2,})$";
        if (host.matches(domainPattern)) {
            return true;
        }

        return !host.isEmpty();
    }

    private void setFieldErrorStyle(TextField field, boolean hasError) {
        Platform.runLater(() -> {
            if (hasError) {
                field.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; " +
                        "-fx-border-color: #ff4444; -fx-border-width: 2; -fx-border-radius: 3;");
            } else {
                field.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; " +
                        "-fx-prompt-text-fill: #666666; -fx-border-color: transparent;");
            }
        });
    }

    private void showValidationError(String errorMessage) {
        Platform.runLater(() -> {
            commandOutputArea.appendText("Validation Errors:\n" + errorMessage + "\n");
        });
    }

    private void validateHostField() {
        String host = hostField.getText().trim();
        if (host.isEmpty()) {
            setFieldErrorStyle(hostField, true);
        } else {
            setFieldErrorStyle(hostField, false);
        }
    }

    private void validatePortField() {
        String portText = portField.getText().trim();
        if (portText.isEmpty()) {
            setFieldErrorStyle(portField, true);
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                setFieldErrorStyle(portField, true);
            } else {
                setFieldErrorStyle(portField, false);
            }
        } catch (NumberFormatException e) {
            setFieldErrorStyle(portField, true);
        }
    }

    private void validatePasswordField() {
        String password = passwordField.getText().trim();
        if (password.isEmpty() || password.length() < 3) {
            setFieldErrorStyle(passwordField, true);
        } else {
            setFieldErrorStyle(passwordField, false);
        }
    }

    private String toStringExceptionStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}