import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

/**
 * GUI application for adding, editing, and deleting rows in {@code History.csv}.
 *
 * <p>Displays the CSV as an editable table. Edit cells in place, use
 * Add Row / Delete Row (or the Delete key) to modify the list, then Save
 * to write back to disk. Run the pipeline (BuildAll) afterwards to regenerate
 * the site.
 */
public class EditHistory extends JFrame implements HistoryFileProcessor {

	private static final String[] COLUMN_NAMES =
			{ "SeniorPerson", "JuniorPerson", "Relationship", "SeniorUrl" };

	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JTextField pathField;

	public EditHistory() {
		super("Edit History");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 600);
		setLocationRelativeTo(null);

		// Path bar (top)
		pathField = new JTextField(DEFAULT_PATH + "History.csv", 50);
		JButton loadBtn = new JButton("Load");
		loadBtn.addActionListener(e -> loadCsv());

		JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pathPanel.add(new JLabel("CSV:"));
		pathPanel.add(pathField);
		pathPanel.add(loadBtn);

		// Editable table (centre)
		tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
			@Override public boolean isCellEditable(int row, int col) { return true; }
		};
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(22);
		table.getTableHeader().setReorderingAllowed(false);
		table.getColumnModel().getColumn(SENIOR_PERSON).setPreferredWidth(200);
		table.getColumnModel().getColumn(JUNIOR_PERSON).setPreferredWidth(200);
		table.getColumnModel().getColumn(RELATIONSHIP).setPreferredWidth(120);
		table.getColumnModel().getColumn(SENIOR_URL).setPreferredWidth(350);

		// Delete key removes the selected row
		table.getInputMap(JComponent.WHEN_FOCUSED)
		     .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
		table.getActionMap().put("deleteRow", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) { deleteRow(); }
		});

		// Button bar (bottom)
		JButton addBtn    = new JButton("Add Row");
		JButton deleteBtn = new JButton("Delete Row");
		JButton saveBtn   = new JButton("Save");
		addBtn.addActionListener(e -> addRow());
		deleteBtn.addActionListener(e -> deleteRow());
		saveBtn.addActionListener(e -> saveCsv());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(addBtn);
		buttonPanel.add(deleteBtn);
		buttonPanel.add(Box.createHorizontalStrut(20));
		buttonPanel.add(saveBtn);

		setLayout(new BorderLayout());
		add(pathPanel, BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		loadCsv();
	}

	private void loadCsv() {
		String path = pathField.getText().trim();
		tableModel.setRowCount(0);
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(path)).build()) {
			reader.readNext(); // skip header
			for (String[] row : reader.readAll()) {
				String[] padded = new String[4];
				for (int i = 0; i < 4; i++)
					padded[i] = (i < row.length) ? row[i].trim() : "";
				tableModel.addRow(padded);
			}
		} catch (IOException | CsvException ex) {
			JOptionPane.showMessageDialog(this, "Failed to load: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveCsv() {
		if (table.isEditing())
			table.getCellEditor().stopCellEditing();

		String path = pathField.getText().trim();
		try (PrintStream out = new PrintStream(new File(path))) {
			out.println(String.join(",", COLUMN_NAMES));
			for (int row = 0; row < tableModel.getRowCount(); row++) {
				String senior = tableModel.getValueAt(row, SENIOR_PERSON).toString().trim();
				if (senior.isEmpty()) continue; // skip blank rows
				StringBuilder line = new StringBuilder();
				for (int col = 0; col < 4; col++) {
					if (col > 0) line.append(",");
					line.append(csvQuote(tableModel.getValueAt(row, col).toString().trim()));
				}
				out.println(line);
			}
			JOptionPane.showMessageDialog(this, "Saved to " + path);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addRow() {
		tableModel.addRow(new String[] { "", "", "", "" });
		int newRow = tableModel.getRowCount() - 1;
		table.scrollRectToVisible(table.getCellRect(newRow, 0, true));
		table.setRowSelectionInterval(newRow, newRow);
		table.editCellAt(newRow, 0);
		Component editor = table.getEditorComponent();
		if (editor != null) editor.requestFocusInWindow();
	}

	private void deleteRow() {
		int selected = table.getSelectedRow();
		if (selected >= 0)
			tableModel.removeRow(selected);
	}

	private static String csvQuote(String value) {
		if (value.contains("\"") || value.contains(",") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new EditHistory().setVisible(true));
	}
}
