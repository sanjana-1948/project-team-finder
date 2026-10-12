package gui;

import dao.StudentDAO;
import model.Student;

import javax.swing.*;
import java.awt.*;

public class ProfileFrame extends JFrame {

    private Student student;
    private StudentDAO dao = new StudentDAO();

    private JTextField txtId, txtEmail, txtName, txtSkills, txtInterests;
    private JPasswordField txtNewPassword;
    private JComboBox<String> cmbBranch, cmbYear, cmbAvailability;

    public ProfileFrame(Student student) {
        this.student = student;

        setTitle("Project Team Finder - My Profile");
        setSize(500, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("My Profile", JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        txtId = new JTextField();
        txtId.setEditable(false);
        txtEmail = new JTextField();
        txtEmail.setEditable(false);
        txtName = new JTextField();
        txtSkills = new JTextField();
        txtInterests = new JTextField();
        txtNewPassword = new JPasswordField();
        cmbBranch = new JComboBox<>(new String[]{"CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "Other"});
        cmbYear = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        cmbAvailability = new JComboBox<>(new String[]{"Available", "Busy"});

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        form.add(new JLabel("Student ID:"));                  form.add(txtId);
        form.add(new JLabel("Email:"));                       form.add(txtEmail);
        form.add(new JLabel("Name:"));                        form.add(txtName);
        form.add(new JLabel("Branch:"));                      form.add(cmbBranch);
        form.add(new JLabel("Year:"));                        form.add(cmbYear);
        form.add(new JLabel("Skills (comma separated):"));    form.add(txtSkills);
        form.add(new JLabel("Interests (comma separated):")); form.add(txtInterests);
        form.add(new JLabel("Availability:"));                form.add(cmbAvailability);
        form.add(new JLabel("New Password (optional):"));     form.add(txtNewPassword);
        add(form, BorderLayout.CENTER);

        JButton btnSave = new JButton("Save Changes");
        JButton btnReset = new JButton("Reset");
        JButton btnBack = new JButton("Back to Dashboard");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttons.add(btnSave);
        buttons.add(btnReset);
        buttons.add(btnBack);
        add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveProfile());
        btnReset.addActionListener(e -> loadFields());
        btnBack.addActionListener(e -> {
            new DashboardFrame(this.student).setVisible(true); // this.student = latest saved data
            dispose();
        });

        loadFields();
    }

    private void loadFields() {
        txtId.setText(student.getStudentId());
        txtEmail.setText(student.getEmail());
        txtName.setText(student.getName());
        txtSkills.setText(student.getSkills() == null ? "" : student.getSkills());
        txtInterests.setText(student.getInterests() == null ? "" : student.getInterests());
        txtNewPassword.setText("");
        selectOrAdd(cmbBranch, student.getBranch());
        cmbYear.setSelectedItem(String.valueOf(student.getYear()));
        selectOrAdd(cmbAvailability, student.getAvailability());
    }

    private void selectOrAdd(JComboBox<String> combo, String value) {
        if (value == null) {
            return;
        }
        boolean found = false;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(value)) {
                found = true;
                break;
            }
        }
        if (!found) {
            combo.addItem(value);
        }
        combo.setSelectedItem(value);
    }

    private void saveProfile() {
        String name = txtName.getText().trim();
        String newPassword = new String(txtNewPassword.getPassword());

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.",
                    "Check Your Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPassword.isEmpty() && newPassword.length() < 4) {
            JOptionPane.showMessageDialog(this, "New password must be at least 4 characters long.",
                    "Check Your Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student updated = new Student(
                student.getStudentId(), name, student.getEmail(), student.getPassword(),
                (String) cmbBranch.getSelectedItem(),
                Integer.parseInt((String) cmbYear.getSelectedItem()),
                txtSkills.getText().trim(),
                txtInterests.getText().trim(),
                (String) cmbAvailability.getSelectedItem());

        boolean ok = dao.updateProfile(updated);
        if (ok && !newPassword.isEmpty()) {
            ok = dao.updatePassword(student.getStudentId(), newPassword);
        }

        if (ok) {
            Student fresh = dao.getStudentById(student.getStudentId());
            if (fresh != null) {
                student = fresh;
            }
            loadFields();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not update the profile. Check the database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}