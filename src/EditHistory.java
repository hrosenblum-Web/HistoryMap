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

	/**
	 * Header values written to the first line of the saved CSV.
	 * Order must match the column index constants in {@link HistoryFileProcessor}
	 * so that {@link RelationshipReader} accepts the file without error.
	 */
	private static final String[] COLUMN_NAMES =
			{ "SeniorPerson", "JuniorPerson", "Relationship", "SeniorUrl" };

	/** Model backing the table; rows map 1-to-1 with CSV data rows. */
	private final DefaultTableModel tableModel;

	/** Table component that renders and edits {@link #tableModel}. */
	private final JTable table;

	/** Editable field holding the absolute path to {@code History.csv}. */
	private final JTextField pathField;

	/**
	 * Builds the frame, wires up all controls, and loads the CSV immediately
	 * so the table is populated on open.
	 */
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
			// All cells are editable so the table behaves like a spreadsheet.
			@Override public boolean isCellEditable(int row, int col) { return true; }
		};
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(22);
		// Column order must stay fixed to match the CSV column indices.
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

	/**
	 * Reads the CSV at the path shown in {@link #pathField} and populates the
	 * table, replacing any previously loaded data. The header row is skipped.
	 *
	 * <p>Rows shorter than four columns are padded with empty strings so the
	 * table always has a cell for every column, including the optional
	 * {@code SeniorUrl} column that older CSV files may omit.
	 *
	 * <p>Shows an error dialog if the file cannot be read or parsed.
	 */
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

	/**
	 * Writes the current table contents back to the CSV file at the path shown
	 * in {@link #pathField}, then shows a confirmation dialog on success.
	 *
	 * <p>Any cell that is still being edited is committed before the file is
	 * written, so the active edit is never silently discarded. Rows whose
	 * {@code SeniorPerson} cell is blank are skipped, consistent with how
	 * {@link HistoryClean} handles empty entries.
	 *
	 * <p>Shows an error dialog if the file cannot be written.
	 */
	private void saveCsv() {
		// Commit any cell that is still open in the editor before reading the model.
		if (table.isEditing())
			table.getCellEditor().stopCellEditing();

		String path = pathField.getText().trim();
		try (PrintStream out = new PrintStream(new File(path))) {
			out.println(String.join(",", COLUMN_NAMES));
			for (int row = 0; row < tableModel.getRowCount(); row++) {
				String senior = tableModel.getValueAt(row, SENIOR_PERSON).toString().trim();
				if (senior.isEmpty()) continue; // skip blank rows added but never filled in
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

	/**
	 * Appends a blank row to the table, scrolls it into view, selects it, and
	 * immediately opens the {@code SeniorPerson} cell for editing so the user
	 * can start typing without an extra click.
	 */
	private void addRow() {
		tableModel.addRow(new String[] { "", "", "", "" });
		int newRow = tableModel.getRowCount() - 1;
		table.scrollRectToVisible(table.getCellRect(newRow, 0, true));
		table.setRowSelectionInterval(newRow, newRow);
		table.editCellAt(newRow, 0);
		Component editor = table.getEditorComponent();
		if (editor != null) editor.requestFocusInWindow();
	}

	/**
	 * Removes the currently selected row from the table.
	 * Does nothing if no row is selected.
	 */
	private void deleteRow() {
		int selected = table.getSelectedRow();
		if (selected >= 0)
			tableModel.removeRow(selected);
	}

	/**
	 * Wraps {@code value} in double quotes and escapes any embedded quotes if
	 * the value contains a double quote, a comma, or a newline — the three
	 * characters that would otherwise break CSV parsing. Identical to the logic
	 * in {@link HistoryClean} so files remain round-trippable through that stage.
	 *
	 * @param value raw cell value
	 * @return {@code value} unchanged, or {@code value} wrapped in {@code "..."} with
	 *         internal {@code "} doubled
	 */
	private static String csvQuote(String value) {
		if (value.contains("\"") || value.contains(",") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}

	/**
	 * Launches the application on the Swing event-dispatch thread.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new EditHistory().setVisible(true));
	}
}
