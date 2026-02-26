package net.bananashelp20.forgermod.registryInterpreter.interpreterInterfaceTerminal;

import org.checkerframework.checker.units.qual.C;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreterHelperMethods.warning;

public class InterpreterInterfaceTerminal extends JFrame {

    private static InterpreterInterfaceTerminal instance;

    private JTextPane terminalPane;
    private StyledDocument document;

    private Style defaultStyle;
    private Style systemStyle;

    private List<String> history = new ArrayList<>();
    private int historyPointer = -1;

    private File currentDirectory = new File(System.getProperty("user.dir"));

    public boolean editorMode = false;
    private File mountedFile = null;

    private String PROMPT;
    private int inputStartPosition;
    private int editorContentStart = 0;

    public InterpreterInterfaceTerminal() {
        instance = this;

        setTitle("Interpreter Terminal");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        terminalPane = new JTextPane();
        terminalPane.setBackground(Color.BLACK);
        terminalPane.setCaretColor(new Color(200, 200, 200));
        terminalPane.setFont(new Font("Consolas", Font.PLAIN, 16));
        terminalPane.setBorder(new EmptyBorder(8, 8, 8, 8));

        document = terminalPane.getStyledDocument();

        defaultStyle = terminalPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, new Color(200, 200, 200));

        systemStyle = terminalPane.addStyle("system", null);
        StyleConstants.setForeground(systemStyle, Color.RED);

        terminalPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (editorMode) {
                    handleEditorKeys(e);
                    return;
                }

                int caret = terminalPane.getCaretPosition();

                if (caret < inputStartPosition) {
                    terminalPane.setCaretPosition(document.getLength());
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && caret <= inputStartPosition) {
                    e.consume();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    handleEnter();
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    e.consume();
                    historyUp();
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.consume();
                    historyDown();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(terminalPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        printWelcomeMessage();
        printPrompt();
    }

    private void historyUp() {
        if (history.isEmpty()) return;

        if (historyPointer == -1) {
            historyPointer = history.size() - 1;
        } else if (historyPointer > 0) {
            historyPointer--;
        }

        replaceCurrentInput(history.get(historyPointer));
    }

    private void historyDown() {
        if (history.isEmpty()) return;

        if (historyPointer >= history.size() - 1) {
            historyPointer = -1;
            replaceCurrentInput("");
        } else {
            historyPointer++;
            replaceCurrentInput(history.get(historyPointer));
        }
    }

    private void replaceCurrentInput(String text) {
        try {
            document.remove(inputStartPosition,
                    document.getLength() - inputStartPosition);

            document.insertString(document.getLength(),
                    text, defaultStyle);

            terminalPane.setCaretPosition(document.getLength());
        } catch (Exception ignored) {}
    }

    /* ========================= */

    public void terminalMessage(String message, boolean lineBreak) {
        appendStyled(message + "\n", defaultStyle);
    }

    public void terminalMessage(String message) {
        terminalMessage(message, true);
    }

    public void terminalMessage(String message, Color messageColor) {
        terminalMessage(message, messageColor, true);
    }

    public void terminalMessage(String message, Color messageColor, boolean lineBreak) {
        Style custom = terminalPane.addStyle("custom_" + System.nanoTime(), null);
        StyleConstants.setForeground(custom, messageColor);
        appendStyled(message + (lineBreak ? "\n" : ""), custom);
    }

    public void terminalMessage(String message, boolean systemMessage, boolean lineBreak) {
        if (systemMessage) {
            appendStyled("#System> " + message + (lineBreak ? "\n" : ""), systemStyle);
        } else {
            terminalMessage(message);
        }
    }

    private void appendStyled(String text, Style style) {
        try {
            document.insertString(document.getLength(), text, style);
            terminalPane.setCaretPosition(document.getLength());
        } catch (BadLocationException ignored) {}
    }

    private final Color DARK_CYAN = new Color(0, 204, 204);

    private void printWelcomeMessage() {
        terminalMessage("Interpreter Interface Terminal", DARK_CYAN);
        terminalMessage("Type in ? to look at all commands currently available", Color.gray);
        terminalMessage("");
    }

    private String currentPromt = "";

    private void printPrompt() {
        if (!williMode && !registryMode) {
            PROMPT = currentDirectory.getAbsolutePath() + "> ";
            appendStyled(PROMPT, defaultStyle);
        }
        inputStartPosition = document.getLength();
        terminalPane.setCaretPosition(inputStartPosition);
    }

    private void handleEnter() {
        try {
            String fullText = document.getText(0, document.getLength());
            String command = fullText.substring(inputStartPosition).trim();

            if (!command.isEmpty()) {
                history.add(command);
            }

            historyPointer = -1;

            appendStyled("\n", defaultStyle);

            if (command.equalsIgnoreCase("clear")) {
                document.remove(0, document.getLength());
                printPrompt();
                return;
            }

            executeCommand(command);

            if (!editorMode) {
                printPrompt();
            }

        } catch (Exception ignored) {}
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

        if (command.startsWith("mkdir ")) {
            createFileOrDirectory(command.substring(6).trim());
            return;
        }

        if (command.startsWith("mount ")) {
            mountFile(command.substring(6).trim());
            return;
        }

        String[] parts = command.split(" ");
        if (parts.length >= 2 && (parts[0].equalsIgnoreCase("run") || parts[0].equalsIgnoreCase("start"))) {
            String target = parts[1].toLowerCase();
            new Thread(() -> {
                try {
                    if (target.contains("reg")) {
                        startRegistryInterpreterDialoge();
                    } else if (target.contains("willi")) {
                        startWilliCodeGeneratorDialoge();
                    } else {
                        terminalMessage("Unknown target for run/start: " + target, true);
                    }
                } catch (Exception e) {
                    terminalMessage("Error running " + target + ": " + e.getMessage(), true);
                }
            }).start();
        }
    }

    private boolean registryMode = false;
    private boolean williMode = false;

    private void startWilliCodeGeneratorDialoge() {

    }

    private final Color PURPLE = new Color(213, 109, 220);
    private final Color YELLOW = new Color(222, 197, 67);
    private final Color GREEN = new Color(60, 131, 45);
    private final Color RED = new Color(203, 73, 83);

    private void startRegistryInterpreterDialoge() {
        terminalMessage("****************************************************************************************************************************************", YELLOW);
        terminalMessage("* Generating the code means OVERRIDING ALL CURRENT CODE that's been written to: all datagen files, ModItems, ModBlocks, RegistryClass, *", YELLOW);
        terminalMessage("* ModToolTiers and ModCreativeModeTabs. Other Files might also be affected, and there is no guarantee the code works as it should.     *", YELLOW);
        terminalMessage("* Please make sure to ", YELLOW, false);
        terminalMessage("//!PRESERVE ", PURPLE, false);
        terminalMessage("every important code line that shall not be overridden                                               *", YELLOW);
        terminalMessage("* If you wish to continue anyways, type in ", YELLOW, false);
        terminalMessage("\"!START\"", GREEN, false);
        terminalMessage(".                                                                                   *", YELLOW);
        terminalMessage("* If you want to stop without any code being generated, type in the command ", YELLOW, false);
        terminalMessage("\"!STOP\".", RED, false);
        terminalMessage("                                                   *", YELLOW);
        terminalMessage("****************************************************************************************************************************************", YELLOW);
    }


    private void createFileOrDirectory(String path) {
        try {
            File target = new File(currentDirectory, path);

            if (path.contains(".")) {
                File parent = target.getParentFile();
                if (parent != null) parent.mkdirs();

                if (target.createNewFile()) {
                    terminalMessage("File created");
                } else {
                    terminalMessage("File already exists");
                }
            } else {
                if (target.mkdirs()) {
                    terminalMessage("Directory created");
                } else {
                    terminalMessage("Directory already exists");
                }
            }

        } catch (IOException e) {
            terminalMessage("Error creating file/directory", true);
        }
    }

    private void mountFile(String name) {
        File target = new File(currentDirectory, name);

        if (!target.exists() || !target.isFile()) {
            terminalMessage("Invalid file", true);
            return;
        }

        try {
            mountedFile = target;
            editorMode = true;

            String content = new String(java.nio.file.Files.readAllBytes(target.toPath()));

            document.remove(0, document.getLength());

            appendStyled("-- MOUNT MODE (" + name + ") --\n", defaultStyle);
            appendStyled("CTRL+S = Save | ESC or CTRL+C = Exit\n\n", defaultStyle);

            editorContentStart = document.getLength();

            appendStyled(content, defaultStyle);
            terminalPane.setCaretPosition(document.getLength());

        } catch (Exception e) {
            terminalMessage("Error opening file", true);
        }
    }

    private void handleEditorKeys(KeyEvent e) {
        int caret = terminalPane.getCaretPosition();

        if (caret < editorContentStart) {
            terminalPane.setCaretPosition(editorContentStart);
        }

        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && caret <= editorContentStart) {
            e.consume();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_DELETE && caret < editorContentStart) {
            e.consume();
            return;
        }

        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
            e.consume();
            saveMountedFile();
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C)) {
            e.consume();
            exitEditor();
        }
    }

    private void saveMountedFile() {
        try {
            String content = document.getText(editorContentStart,
                    document.getLength() - editorContentStart);
            java.nio.file.Files.write(mountedFile.toPath(), content.getBytes());
        } catch (Exception ignored) {}
    }

    private void exitEditor() {
        editorMode = false;
        mountedFile = null;
        try { document.remove(0, document.getLength()); } catch (Exception ignored) {}
        printWelcomeMessage();
        printPrompt();
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

            terminalMessage(type + " " + size + " " + date + " " + name);
        }
    }

    private void changeDirectory(String path) {
        File newDir;

        if (path.equals("..")) {
            newDir = currentDirectory.getParentFile();
            if (newDir == null) return;
        } else {
            File temp = new File(path);
            if (!temp.isAbsolute()) temp = new File(currentDirectory, path);
            newDir = temp;
        }

        if (newDir.exists() && newDir.isDirectory()) {
            currentDirectory = newDir;
        } else {
            terminalMessage("Directory not found", true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (instance == null) {
                new InterpreterInterfaceTerminal().setVisible(true);
            }
        });
    }
}
