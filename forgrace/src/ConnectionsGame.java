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

public class ConnectionsGame extends JFrame {
    private final ArrayList<JButton> buttons = new ArrayList<>();
    private final HashSet<Integer> selectedIndices = new HashSet<>();
    private final Border defaultBorder = BorderFactory.createLineBorder(new Color(190, 145, 215), 3, true); // Muted purple rounded border
    private final Border selectedBorder = BorderFactory.createLineBorder(new Color(75, 0, 130), 5, true); // Darker purple with larger rounded border
    private final Color disabledColor = Color.GRAY; // Color for disabled text
    private int connectionsFound = 0;
    private final int totalConnections = 4; // Total number of connections to be found
    private String[] connectionThemes = new String[totalConnections]; // Array to store connection themes
    private ArrayList<ArrayList<String>> originalGroups = new ArrayList<>(); // Store the original groups

    public ConnectionsGame() {
        setTitle("Connections Game");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 4)); // 4x4 grid for 16 items

        // 1. load words from file
        String[] items = loadWordsAndThemesFromFile();

        if (items == null) {
            JOptionPane.showMessageDialog(this, "Failed to load words from file.");
            System.exit(1);
        } //error message if could not load

        // 2. add each block of 4 words to a group and assign them the theme
        for (int i = 0; i < totalConnections; i++) {
            ArrayList<String> group = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                group.add(items[i * 4 + j]);
            }
            originalGroups.add(group); //add the group to list1
            connectionThemes[i] = items[16 + i]; //assign theme
        }

        // Step 3: Shuffle all words on the board
        ArrayList<String> shuffledItems = new ArrayList<>();
        for (ArrayList<String> group : originalGroups) {
            shuffledItems.addAll(group);
        }
        Collections.shuffle(shuffledItems);

        //design element
        Font font = new Font("Comic Sans MS", Font.BOLD, 14);

        // create buttons and store words
        for (int i = 0; i < 16; i++) {
            JButton button = new JButton(shuffledItems.get(i));
            button.addActionListener(new ButtonClickListener(i));
            button.setBackground(new Color(190, 145, 215)); // Muted purple background
            button.setForeground(Color.BLACK); // Black font color
            button.setFont(font); // Set custom font
            button.setFocusable(false); // Remove focus indication
            button.setBorder(defaultBorder); // Set default rounded border
            buttons.add(button);
            add(button);
        }

        getContentPane().setBackground(new Color(255, 255, 255)); // Set background color of JFrame

        setVisible(true);
    }

    /**
     * loads words
     */
    private String[] loadWordsAndThemesFromFile() {
        ArrayList<String> lines = new ArrayList<>();

        // Generate a random number between 1 and 10 to select a file
        int randomFileNumber = (int) (Math.random() * 2) + 1;
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

        // Check if there are enough lines
        if (lines.size() < 20) {
            System.out.println("Not enough lines in the file.");
            return null;
        }

        // first 16 lines as items for the game board
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
                JOptionPane.showMessageDialog(this, "You found a connection: " + connectionThemes[i]);
                disableSelectedButtons(); // Disable buttons for the selected squares
                connectionsFound++;
                foundConnection = true;
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

        // Reset selection
        selectedIndices.clear();
        updateButtonAppearance(); // Update button appearance after checking connections
    }

    /**
     * button functionality
     */
    private void updateButtonAppearance() {
        for (int i = 0; i < buttons.size(); i++) {
            JButton button = buttons.get(i);
            if (selectedIndices.contains(i)) {
                button.setBorder(selectedBorder); // Set border to selected
            } else {
                button.setBorder(defaultBorder); // Set border to default
            }
            if (!button.isEnabled()) {
                button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3, true)); // Set border color to gray for disabled buttons
                button.setForeground(disabledColor); // Set text color to gray for disabled buttons
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConnectionsGame::new);
    }

}
