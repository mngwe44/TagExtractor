import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

public class TagExtractorFrame extends JFrame {
    private JTextArea textArea;
    private File textFile;
    private Set<String> stopWords;
    private Map<String, Integer> tagMap;

    public TagExtractorFrame() {
        setTitle("Tag Extractor");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with buttons
        JPanel topPanel = new JPanel();
        JButton openTextBtn = new JButton("Open Text File");
        JButton openStopWordsBtn = new JButton("Open Stop Words File");
        JButton extractBtn = new JButton("Extract Tags");
        JButton saveBtn = new JButton("Save Tags");

        topPanel.add(openTextBtn);
        topPanel.add(openStopWordsBtn);
        topPanel.add(extractBtn);
        topPanel.add(saveBtn);

        add(topPanel, BorderLayout.NORTH);

        // Center scrollable text area
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Button actions
        openTextBtn.addActionListener(this::handleOpenText);
        openStopWordsBtn.addActionListener(this::handleOpenStopWords);
        extractBtn.addActionListener(this::handleExtract);
        saveBtn.addActionListener(this::handleSave);

        setVisible(true);
    }

    private void handleOpenText(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textFile = chooser.getSelectedFile();
            textArea.append("Selected text file: " + textFile.getName() + "\n");
        }
    }

    private void handleOpenStopWords(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File stopWordsFile = chooser.getSelectedFile();
            stopWords = loadStopWords(stopWordsFile);
            textArea.append("Loaded stop words from: " + stopWordsFile.getName() + "\n");
        }
    }

    private Set<String> loadStopWords(File file) {
        Set<String> stopSet = new HashSet<>();
        try (Scanner in = new Scanner(file)) {
            while (in.hasNextLine()) {
                stopSet.add(in.nextLine().trim().toLowerCase());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load stop words.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return stopSet;
    }

    private void handleExtract(ActionEvent e) {
        if (textFile == null || stopWords == null) {
            JOptionPane.showMessageDialog(this, "Please select both a text file and a stop words file.", "Missing Files", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tagMap = new TreeMap<>();

        try (Scanner in = new Scanner(textFile)) {
            while (in.hasNext()) {
                String word = in.next().toLowerCase().replaceAll("[^a-z]", "");
                if (!stopWords.contains(word) && !word.isEmpty()) {
                    tagMap.put(word, tagMap.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to read text file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Display results
        textArea.append("\n--- Extracted Tags ---\n");
        for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
            textArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    private void handleSave(ActionEvent e) {
        if (tagMap == null || tagMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags to save.", "Nothing to Save", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(outputFile)) {
                for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                    out.println(entry.getKey() + "," + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Tags saved to " + outputFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving tags.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TagExtractorFrame::new);
    }
}
