/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package duplexx;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;


public class booking extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(booking.class.getName());

    /**
     * Creates new form booking
     */
    public booking() {
        initComponents();
    loadTrainers();
    loadDates();
    refreshTimes();

    // refresh the available times whenever trainer or date changes
    jComboBox1.addActionListener(e -> refreshTimes());
    jComboBox2.addActionListener(e -> refreshTimes());

    }
private static final String[] TIME_SLOTS = {
    "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
    "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"
};
private void loadTrainers() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    try (Connection conn = new DBConnection().connect()) {
        if (conn == null) {
            showDbError();
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM trainers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addElement(rs.getString("name"));
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Could not load trainers: " + ex.getMessage());
    }
    jComboBox1.setModel(model);
}

/** Fill the date combo box with today + the next 13 days. */
private void loadDates() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    LocalDate today = LocalDate.now();
    for (int i = 0; i < 14; i++) {
        model.addElement(today.plusDays(i).toString()); // yyyy-MM-dd
    }
    jComboBox2.setModel(model);
}

/** Show only the time slots that are still free for the chosen trainer + date. */
private void refreshTimes() {
    String trainer = (String) jComboBox1.getSelectedItem();
    String date = (String) jComboBox2.getSelectedItem();

    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    if (trainer == null || date == null) {
        jComboBox3.setModel(model);
        return;
    }

    Set<String> taken = new HashSet<>();
    String sql = "SELECT TIME_FORMAT(b.booking_time, '%H:%i') AS t "
               + "FROM bookings b JOIN trainers tr ON tr.id = b.trainer_id "
               + "WHERE tr.name = ? AND b.booking_date = ?";
    try (Connection conn = new DBConnection().connect()) {
        if (conn == null) {
            showDbError();
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainer);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    taken.add(rs.getString("t"));
                }
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Could not load times: " + ex.getMessage());
    }

    for (String slot : TIME_SLOTS) {
        if (!taken.contains(slot)) {
            model.addElement(slot);
        }
    }
    jComboBox3.setModel(model);
}

private void showDbError() {
    JOptionPane.showMessageDialog(this,
            "Cannot connect to the database. Is MySQL running?",
            "Database error", JOptionPane.ERROR_MESSAGE);
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 3, 21)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("TIME");
        jLabel7.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jLabel7ComponentHidden(evt);
            }
        });
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 340, 50, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 3, 21)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Select Date");
        jLabel8.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jLabel8ComponentHidden(evt);
            }
        });
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 260, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 3, 21)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Select Trainer");
        jLabel9.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jLabel9ComponentHidden(evt);
            }
        });
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 180, 140, -1));

        jLabel3.setFont(new java.awt.Font("Cambria", 3, 28)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Book a Gym Session ");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 60, -1, -1));

        jButton1.setBackground(new java.awt.Color(204, 51, 0));
        jButton1.setFont(new java.awt.Font("Segoe UI Variable", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Back to Home");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 10, -1, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Choose your trainer, data, and time");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 130, 220, -1));

        jButton2.setBackground(new java.awt.Color(255, 0, 0));
        jButton2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("CONFIRM BOOKING");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 440, -1, -1));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Alex Reyes,", "Maria Santos", "John Cruz", "Ana Lopez" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);
        getContentPane().add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 220, -1, -1));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(jComboBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 310, -1, -1));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(jComboBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 380, -1, -1));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/4.png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 800, 500));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
home home1 = new home();

home1.setVisible(true);

this.dispose();    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
 String trainer = (String) jComboBox1.getSelectedItem();
    String date = (String) jComboBox2.getSelectedItem();
    String time = (String) jComboBox3.getSelectedItem();

    if (trainer == null || date == null || time == null) {
        JOptionPane.showMessageDialog(this, "Please choose a trainer, date and time.");
        return;
    }

    String sql = "INSERT INTO bookings (trainer_id, booking_date, booking_time) "
               + "SELECT id, ?, ? FROM trainers WHERE name = ?";
    try (Connection conn = new DBConnection().connect()) {
        if (conn == null) {
            showDbError();
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time + ":00"));
            ps.setString(3, trainer);
            ps.executeUpdate();
        }
        JOptionPane.showMessageDialog(this,
                "Booking confirmed!\n" + trainer + "\n" + date + " at " + time);
        refreshTimes(); // the slot you just booked disappears from the list
    } catch (SQLException ex) {
        if (ex.getErrorCode() == 1062) { // duplicate entry: someone booked it first
            JOptionPane.showMessageDialog(this,
                    "Sorry, that slot was just taken. Please pick another time.");
            refreshTimes();
        } else {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage());
        }
    }


        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jLabel9ComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jLabel9ComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel9ComponentHidden

    private void jLabel8ComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jLabel8ComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel8ComponentHidden

    private void jLabel7ComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jLabel7ComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel7ComponentHidden

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new booking().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
}
