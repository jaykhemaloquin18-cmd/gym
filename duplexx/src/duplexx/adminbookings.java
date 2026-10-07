package duplexx;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Trainer Panel: view/cancel bookings, add/remove trainers, bookings per trainer,
 * with live search on both tabs.
 * Class name is still "adminbookings" so your other forms keep working.
 */
public class adminbookings extends JFrame {

    private final DefaultTableModel bookingsModel = new DefaultTableModel(
            new String[]{"ID", "Client", "Trainer", "Date", "Time"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class; // so ID sorts numerically
        }
    };

    private final DefaultTableModel trainersModel = new DefaultTableModel(
            new String[]{"Trainer", "Total Bookings"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 1 ? Integer.class : String.class;
        }
    };

    private final JTable bookingsTable = new JTable(bookingsModel);
    private final JTable trainersTable = new JTable(trainersModel);

    private final TableRowSorter<DefaultTableModel> bookingsSorter = new TableRowSorter<>(bookingsModel);
    private final TableRowSorter<DefaultTableModel> trainersSorter = new TableRowSorter<>(trainersModel);

    private final JTextField trainerField = new JTextField(15);
    private final JTextField bookingSearchField = new JTextField(20);
    private final JTextField trainerSearchField = new JTextField(20);
    private final JLabel bookingCountLabel = new JLabel(" ");

    public adminbookings() {
        setTitle("Trainer Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Admin Panel", JLabel.CENTER);
        title.setFont(new java.awt.Font("Cambria", java.awt.Font.BOLD | java.awt.Font.ITALIC, 26));
        add(title, BorderLayout.NORTH);

        // Sorting + searching
        bookingsTable.setRowSorter(bookingsSorter);
        trainersTable.setRowSorter(trainersSorter);
        bookingSearchField.getDocument().addDocumentListener(
                new SearchListener(bookingSearchField, bookingsSorter, true));
        trainerSearchField.getDocument().addDocumentListener(
                new SearchListener(trainerSearchField, trainersSorter, false));

        // ---- Bookings tab ----
        JButton clearBookingSearch = new JButton("Clear");
        clearBookingSearch.addActionListener(e -> bookingSearchField.setText(""));
        JPanel bookingSearchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookingSearchBar.add(new JLabel("Search (client, trainer, date, time):"));
        bookingSearchBar.add(bookingSearchField);
        bookingSearchBar.add(clearBookingSearch);
        bookingSearchBar.add(bookingCountLabel);

        JButton cancelBtn = new JButton("Cancel Selected Booking");
        cancelBtn.addActionListener(e -> cancelSelectedBooking());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadBookings();
            loadTrainers();
        });
        JPanel bookingButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookingButtons.add(cancelBtn);
        bookingButtons.add(refreshBtn);

        JPanel bookingsTab = new JPanel(new BorderLayout(4, 4));
        bookingsTab.add(bookingSearchBar, BorderLayout.NORTH);
        bookingsTab.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        bookingsTab.add(bookingButtons, BorderLayout.SOUTH);

        // ---- Trainers tab ----
        JButton clearTrainerSearch = new JButton("Clear");
        clearTrainerSearch.addActionListener(e -> trainerSearchField.setText(""));
        JPanel trainerSearchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trainerSearchBar.add(new JLabel("Search trainer:"));
        trainerSearchBar.add(trainerSearchField);
        trainerSearchBar.add(clearTrainerSearch);

        JButton addBtn = new JButton("Add Trainer");
        addBtn.addActionListener(e -> addTrainer());
        JButton removeBtn = new JButton("Remove Selected Trainer");
        removeBtn.addActionListener(e -> removeSelectedTrainer());
        JPanel trainerControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trainerControls.add(new JLabel("Name:"));
        trainerControls.add(trainerField);
        trainerControls.add(addBtn);
        trainerControls.add(removeBtn);

        JPanel trainersTab = new JPanel(new BorderLayout(4, 4));
        trainersTab.add(trainerSearchBar, BorderLayout.NORTH);
        trainersTab.add(new JScrollPane(trainersTable), BorderLayout.CENTER);
        trainersTab.add(trainerControls, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bookings", bookingsTab);
        tabs.addTab("Trainers", trainersTab);
        add(tabs, BorderLayout.CENTER);

        // ---- Log out button ----
        JButton logoutBtn = new JButton("LOG OUT");
        logoutBtn.addActionListener(e -> {
            new login().setVisible(true);
            dispose();
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(logoutBtn);
        add(bottom, BorderLayout.SOUTH);

        loadBookings();
        loadTrainers();
    }

    // ---------------------------------------------------------------
    // Live search: filters as you type, case-insensitive, all columns
    // ---------------------------------------------------------------

    private class SearchListener implements DocumentListener {
        private final JTextField field;
        private final TableRowSorter<DefaultTableModel> sorter;
        private final boolean updateCount;

        SearchListener(JTextField field, TableRowSorter<DefaultTableModel> sorter, boolean updateCount) {
            this.field = field;
            this.sorter = sorter;
            this.updateCount = updateCount;
        }

        private void apply() {
            String text = field.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                // Pattern.quote so characters like ( or * are treated as plain text
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
            }
            if (updateCount) {
                updateBookingCount();
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            apply();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            apply();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            apply();
        }
    }

    private void updateBookingCount() {
        int shown = bookingsTable.getRowCount();
        int total = bookingsModel.getRowCount();
        bookingCountLabel.setText(shown == total
                ? total + " booking(s)"
                : shown + " of " + total + " booking(s)");
    }

    // ---------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------

    /** All bookings, soonest first. */
    private void loadBookings() {
        bookingsModel.setRowCount(0);
        String sql = "SELECT b.id, COALESCE(b.username, '-') AS client, tr.name AS trainer, "
                   + "b.booking_date, TIME_FORMAT(b.booking_time, '%H:%i') AS t "
                   + "FROM bookings b JOIN trainers tr ON tr.id = b.trainer_id "
                   + "ORDER BY b.booking_date, b.booking_time";
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showDbError();
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookingsModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("client"),
                        rs.getString("trainer"),
                        rs.getDate("booking_date").toString(),
                        rs.getString("t")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load bookings: " + ex.getMessage());
        }
        updateBookingCount();
    }

    /** Every trainer with how many bookings they have. */
    private void loadTrainers() {
        trainersModel.setRowCount(0);
        String sql = "SELECT tr.name, COUNT(b.id) AS total "
                   + "FROM trainers tr LEFT JOIN bookings b ON b.trainer_id = tr.id "
                   + "GROUP BY tr.id, tr.name ORDER BY tr.name";
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showDbError();
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trainersModel.addRow(new Object[]{rs.getString("name"), rs.getInt("total")});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load trainers: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    private void cancelSelectedBooking() {
        int viewRow = bookingsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking in the table first.");
            return;
        }
        // Convert because the table can be sorted/filtered
        int row = bookingsTable.convertRowIndexToModel(viewRow);
        int id = (Integer) bookingsModel.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel booking #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showDbError();
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM bookings WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not cancel booking: " + ex.getMessage());
        }
        loadBookings();
        loadTrainers();
    }

    private void addTrainer() {
        String name = trainerField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type the trainer's name first.");
            return;
        }
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showDbError();
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO trainers (name) VALUES (?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            trainerField.setText("");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) { // duplicate name
                JOptionPane.showMessageDialog(this, "A trainer with that name already exists.");
            } else {
                JOptionPane.showMessageDialog(this, "Could not add trainer: " + ex.getMessage());
            }
        }
        loadTrainers();
    }

    private void removeSelectedTrainer() {
        int viewRow = trainersTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a trainer in the table first.");
            return;
        }
        int row = trainersTable.convertRowIndexToModel(viewRow);
        String name = (String) trainersModel.getValueAt(row, 0);
        int total = (Integer) trainersModel.getValueAt(row, 1);
        String msg = "Remove trainer \"" + name + "\"?";
        if (total > 0) {
            msg += "\nThis will also delete their " + total + " booking(s).";
        }
        int ok = JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showDbError();
                return;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement delBookings = conn.prepareStatement(
                        "DELETE FROM bookings WHERE trainer_id = (SELECT id FROM trainers WHERE name = ?)");
                 PreparedStatement delTrainer = conn.prepareStatement(
                        "DELETE FROM trainers WHERE name = ?")) {
                delBookings.setString(1, name);
                delBookings.executeUpdate();
                delTrainer.setString(1, name);
                delTrainer.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not remove trainer: " + ex.getMessage());
        }
        loadBookings();
        loadTrainers();
    }

    private void showDbError() {
        JOptionPane.showMessageDialog(this,
                "Cannot connect to the database. Is MySQL running?",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new adminbookings().setVisible(true));
    }
}