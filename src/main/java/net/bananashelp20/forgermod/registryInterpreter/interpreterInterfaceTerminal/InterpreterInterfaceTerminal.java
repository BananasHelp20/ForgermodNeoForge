package net.bananashelp20.forgermod.registryInterpreter.interpreterInterfaceTerminal;

import net.bananashelp20.forgermod.registryInterpreter.WilliCodeGenerator.WilliCodeGenerator;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InterpreterInterfaceTerminal extends JFrame {

    private static InterpreterInterfaceTerminal instance;

    private JTextArea terminalArea;
    private List<String> history = new ArrayList<>();
    private int historyIndex = -1;

    private File currentDirectory = new File(System.getProperty("user.dir"));

    public boolean williInterpreterMode = false;
    public boolean registryInterpreterMode = false;

    private String PROMPT;
    private int inputStartPosition;
    public static String systemPromt = "";
    public static String currentPromt = "";

    public InterpreterInterfaceTerminal() {
        instance = this;

        setTitle("Custom PowerShell Terminal");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        terminalArea = new JTextArea();
        terminalArea.setBackground(Color.BLACK);

        // ===== Text jetzt hellgrau =====
        terminalArea.setForeground(new Color(200, 200, 200));
        terminalArea.setCaretColor(new Color(200, 200, 200));
        // =================================

        terminalArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        terminalArea.setLineWrap(true);
        terminalArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        terminalArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                int caret = terminalArea.getCaretPosition();

                if (caret < inputStartPosition) {
                    terminalArea.setCaretPosition(terminalArea.getText().length());
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (caret <= inputStartPosition) {
                        e.consume();
                        return;
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    handleEnter();
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    e.consume();
                    showPreviousCommand();
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.consume();
                    showNextCommand();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(terminalArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        printWelcomeMessage();
        printPrompt();
    }

    public static void terminalMessage(String message, boolean systemMessage) {
        SwingUtilities.invokeLater(() -> {

            if (instance == null) {
                instance = new InterpreterInterfaceTerminal();
                instance.setVisible(true);
            }

            instance.printExternalMessage(message, new Color(200, 200, 200));
        });
    }

    public static void terminalMessage(String message, Color messageColor, boolean systemMessage) {
        final String msg = (systemMessage) ? systemPromt + message : message;
        SwingUtilities.invokeLater(() -> {
            if (instance == null) {
                instance = new InterpreterInterfaceTerminal();
                instance.setVisible(true);
            }
            instance.printExternalMessage(msg, messageColor);
        });
    }

    private void printExternalMessage(String message, Color MessageColor) {
        if (!terminalArea.getText().endsWith("\n")) {
            terminalArea.append("\n");
        }

        terminalArea.append(message + "\n");
        printPrompt();
    }

    private void printWelcomeMessage() {
        terminalArea.append("Windows PowerShell\n");
        terminalArea.append("Custom Java Terminal\n\n");
    }

    private void printPrompt() {
        if (!registryInterpreterMode && !williInterpreterMode) {
            PROMPT = currentDirectory.getAbsolutePath() + "> ";
        } else {
            PROMPT = currentPromt;
        }
        terminalArea.append(PROMPT);
        inputStartPosition = terminalArea.getText().length();
        terminalArea.setCaretPosition(inputStartPosition);
    }

    private void handleEnter() {
        String fullText = terminalArea.getText();

        if (inputStartPosition > fullText.length()) {
            inputStartPosition = fullText.length();
        }

        String command = fullText.substring(inputStartPosition).trim();

        history.add(command);
        historyIndex = history.size();

        String[] parts = command.split(" ");
        try {
            if (parts[0].equalsIgnoreCase("run") || parts[0].equalsIgnoreCase("start")) {
                if (parts[1].toLowerCase().contains("reg")) {
                    RegistryInterpreter.main(new String[]{});
                    registryInterpreterMode = true;
                } else if (parts[1].toLowerCase().contains("willi")) {
                    WilliCodeGenerator.main(new String[]{});
                    williInterpreterMode = true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }

        if (command.equalsIgnoreCase("clear")) {
            terminalArea.setText("");
            history.clear();
            historyIndex = -1;
            printPrompt();
            return;
        }

        terminalArea.append("\n");

        executeCommand(command);

        printPrompt();
    }

    private void executeCommand(String command) {

        if (command.equals("ls")) {
            listFiles(false);
            return;
        }

        if (command.equals("ls -a")) {
            listFiles(true);
            return;
        }

        if (command.startsWith("cd ")) {
            changeDirectory(command.substring(3).trim());
            return;
        }

        if (command.equals("cd")) {
            return;
        }
    }

    private void listFiles(boolean showHidden) {

        File[] files = currentDirectory.listFiles();
        if (files == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (File file : files) {

            if (!showHidden && file.isHidden()) continue;

            String type = file.isDirectory() ? "d" : "-";
            String size = String.format("%10d", file.length());
            String date = sdf.format(new Date(file.lastModified()));
            String name = file.getName();

            terminalArea.append(type + " " + size + " " + date + " " + name + "\n");
        }
    }

    private void changeDirectory(String path) {

        File newDir;

        if (path.equals("..")) {
            newDir = currentDirectory.getParentFile();
            if (newDir == null) return;
        } else {
            File temp = new File(path);
            if (!temp.isAbsolute()) {
                temp = new File(currentDirectory, path);
            }
            newDir = temp;
        }

        if (newDir.exists() && newDir.isDirectory()) {
            currentDirectory = newDir;
        } else {
            terminalArea.append("Directory not found\n");
        }
    }

    private void showPreviousCommand() {
        if (history.isEmpty()) return;

        if (historyIndex > 0) {
            historyIndex--;
            replaceCurrentInput(history.get(historyIndex));
        }
    }

    private void showNextCommand() {
        if (history.isEmpty()) return;

        if (historyIndex < history.size() - 1) {
            historyIndex++;
            replaceCurrentInput(history.get(historyIndex));
        } else {
            historyIndex = history.size();
            replaceCurrentInput("");
        }
    }

    private void replaceCurrentInput(String command) {
        String text = terminalArea.getText();
        String newText = text.substring(0, inputStartPosition) + command;
        terminalArea.setText(newText);
        terminalArea.setCaretPosition(terminalArea.getText().length());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (instance == null) {
                new InterpreterInterfaceTerminal().setVisible(true);
            }
        });
    }
}
