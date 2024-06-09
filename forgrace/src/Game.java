import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Game extends JFrame {
    private final ArrayList<JButton> buttons = new ArrayList<>();
    private final HashSet<Integer> selectedIndices = new HashSet<>();
    private final Border defaultBorder = BorderFactory.createLineBorder(new Color(190, 100, 150), 3, true);
    private final Border selectedBorder = BorderFactory.createLineBorder(new Color(75, 0, 130), 5, true);
    private final Color disabledColor = Color.GRAY;
    private int connectionsFound = 0;
    private final int totalConnections = 4;
    private String[] connectionThemes = new String[totalConnections];
    private ArrayList<ArrayList<String>> originalGroups = new ArrayList<>();
    private JTextArea foundConnectionsTextArea;

    public Game() {
        setTitle("Connections Game");
        setSize(500, 640); //height increase for new panels
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); //use BorderLayout for main frame

        JPanel gamePanel = new JPanel(new GridLayout(4, 4)); //panel for game buttons
        JPanel bottomPanel = new JPanel(new BorderLayout()); //panel for new game and hint buttons

        //load words from file
        String[] items = loadWordsAndThemesFromFile();

        if (items == null) {
            JOptionPane.showMessageDialog(this, "Failed to load words from file.");
            System.exit(1);
        } //error message if could not load

        //add each block of 4 words to a group and assign them the theme
        for (int i = 0; i < totalConnections; i++) {
            ArrayList<String> group = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                group.add(items[i * 4 + j]);
            }
            originalGroups.add(group); //add the group to list1
            connectionThemes[i] = items[16 + i]; //assign theme
        }

        //shuffle all words on the board
        ArrayList<String> shuffledItems = new ArrayList<>();
        for (ArrayList<String> group : originalGroups) {
            shuffledItems.addAll(group);
        }
        Collections.shuffle(shuffledItems);

        //designing the words
        Font font = new Font("Comic Sans MS", Font.BOLD, 14);

        //create buttons and store words
        for (int i = 0; i < 16; i++) {
            JButton button = new JButton(shuffledItems.get(i));
            button.addActionListener(new ButtonClickListener(i));
            button.setBackground(new Color(100, 104, 230)); //muted purple background
            button.setForeground(Color.BLACK); //font color
            button.setFont(font); //custom font
            button.setFocusable(false); //remove focus indication
            button.setBorder(defaultBorder); //default rounded border
            buttons.add(button);
            gamePanel.add(button);
        }

        //panel for displaying found connections
        JPanel foundConnectionsPanel = new JPanel(new BorderLayout());
        foundConnectionsTextArea = new JTextArea();
        foundConnectionsTextArea.setEditable(false);
        foundConnectionsTextArea.setLineWrap(true);
        foundConnectionsTextArea.setWrapStyleWord(true);
        foundConnectionsTextArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(foundConnectionsTextArea);
        scrollPane.setPreferredSize(new Dimension(400, 92));
        foundConnectionsPanel.add(scrollPane, BorderLayout.CENTER);
        foundConnectionsPanel.setBorder(BorderFactory.createTitledBorder("Found Connections"));

        //create "New Game" button
        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> startNewGame());

        //create "Hint" button
        JButton hintButton = new JButton("Hint");
        hintButton.addActionListener(e -> giveHint());

        //add buttons to bottom panel
        bottomPanel.add(newGameButton, BorderLayout.WEST);
        bottomPanel.add(hintButton, BorderLayout.EAST);

        add(gamePanel, BorderLayout.CENTER);
        add(foundConnectionsPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.NORTH);

        getContentPane().setBackground(new Color(255, 255, 255)); //set background color of JFrame

        setVisible(true);
    }

    /**
     * loads words
     */
    private String[] loadWordsAndThemesFromFile() {
        ArrayList<String> lines = new ArrayList<>();

        //randomg choose a list
        int randomFileNumber = (int) (Math.random() * 9) + 1;
        String filePath = "/Users/tanyaqu/Downloads/untitled/forgrace/src/list" + randomFileNumber;

        File file = new File(filePath);

        //CHECK: is it printing out correct file?
        System.out.println("Attempting to load file from: " + file.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); // Trim any leading/trailing whitespace
                if (!line.isEmpty()) {
                    lines.add(line); // Add non-empty lines to list1
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        //check if there are enough lines
        if (lines.size() < 20) {
            System.out.println("Not enough lines in the file.");
            return null;
        }

        //first 16 lines as items for the game board
        String[] items = new String[20];
        for (int i = 0; i < 20; i++) {
            items[i] = lines.get(i);
        }

        return items;
    }

    /**
     * button click implementation
     */
    private class ButtonClickListener implements ActionListener {
        private final int index;
        public ButtonClickListener(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = buttons.get(index);
            if (selectedIndices.contains(index)) {
                // If button is already selected, deselect it
                selectedIndices.remove(index);
                button.setBorder(defaultBorder); // Reset border to default
            } else {
                // If button is not selected, select it
                selectedIndices.add(index);
                button.setBorder(selectedBorder); // Set border to selected
            }
            if (selectedIndices.size() == 4) {
                checkConnections();
            }
        }
    }

    /**
     * checks for connection - gameplay
     */
    private void checkConnections() {
        HashSet<String> selectedItems = new HashSet<>();
        for (Integer index : selectedIndices) {
            selectedItems.add(buttons.get(index).getText());
        }

        boolean foundConnection = false;
        for (int i = 0; i < totalConnections; i++) {
            HashSet<String> group = new HashSet<>(originalGroups.get(i));
            if (selectedItems.equals(group)) {
//                JOptionPane.showMessageDialog(this, "You found a connection: " + connectionThemes[i]);

                Icon customIcon = new ImageIcon("/sad.png");
                JOptionPane.showMessageDialog(this, "You found a connection: " + connectionThemes[i], "Connection Found", JOptionPane.INFORMATION_MESSAGE, customIcon);

                disableSelectedButtons(); // Disable buttons for the selected squares
                connectionsFound++;
                foundConnection = true;
                foundConnectionsTextArea.append(connectionThemes[i] + ": " + group.toString() + "\n");
                break;
            }

//            System.out.println("Checking group: " + group); //troubleshooting
        }

        if (!foundConnection) {
            JOptionPane.showMessageDialog(this, "No valid connection found.");
        }

        if (connectionsFound == totalConnections) {
            JOptionPane.showMessageDialog(this, "Congratulations! You found all connections.");
        }

        //reset selection
        selectedIndices.clear();
        updateButtonAppearance(); //update button appearance after checking connections
    }

    /**
     * button functionality
     */
    private void updateButtonAppearance() {
        for (int i = 0; i < buttons.size(); i++) {
            JButton button = buttons.get(i);
            if (selectedIndices.contains(i)) {
                button.setBorder(selectedBorder); //set border to selected
            } else {
                button.setBorder(defaultBorder); //set border to default
            }
            if (!button.isEnabled()) {
                button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3, true)); //set border color to gray for disabled buttons
                button.setForeground(disabledColor); //set text color to gray for disabled buttons
            }
        }
    }

    /**
     * button functionality
     */
    private void disableSelectedButtons() {
        for (int index : selectedIndices) {
            JButton button = buttons.get(index);
            button.setEnabled(false); // Disable button
            button.setBorder(defaultBorder); // Reset border to default
        }
    }

    /**
     * starts a new game
     */
    private void startNewGame() {
        dispose(); //close the current game window
        SwingUtilities.invokeLater(Game::new); //start a new game
    }

    /**
     * gives a hint by revealing one connection
     */
    private void giveHint() {
        for (int i = 0; i < totalConnections; i++) {
            ArrayList<String> group = originalGroups.get(i);
            boolean allEnabled = true;

            for (String word : group) {
                for (JButton button : buttons) {
                    if (button.getText().equals(word) && !button.isEnabled()) {
                        allEnabled = false;
                        break;
                    }
                }
                if (!allEnabled) break;
            }

            if (allEnabled) {
                for (String word : group) {
                    for (JButton button : buttons) {
                        if (button.getText().equals(word)) {
                            button.setEnabled(false); // Disable button
                            button.setBorder(defaultBorder); // Reset border to default
                            button.setForeground(disabledColor); // Set text color to gray
                            button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3, true)); // Set border color to gray for disabled buttons
                        }
                    }
                }
                foundConnectionsTextArea.append(connectionThemes[i] + ": " + group.toString() + "\n");
                connectionsFound++;

                JOptionPane.showMessageDialog(this, "A connection has been revealed: " + connectionThemes[i]);
                if (connectionsFound == totalConnections) {
                    JOptionPane.showMessageDialog(this, "Congratulations! You found all connections.");
                }
                break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }

}
