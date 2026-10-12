package gui;

import dao.StudentDAO;
import model.Student;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private JTextField txtId, txtName, txtEmail, txtSkills, txtInterests;
    private JPasswordField txtPassword, txtConfirm;
    private JComboBox<String> cmbBranch, cmbYear;

    public RegisterFrame() {
        setTitle("Project Team Finder - Registration");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Student Registration", JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        txtId = new JTextField();
        txtName = new JTextField();
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();
        txtConfirm = new JPasswordField();
        cmbBranch = new JComboBox<>(new String[]{"CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "Other"});
        cmbYear = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        txtSkills = new JTextField();
        txtInterests = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        form.add(new JLabel("Student ID:"));          form.add(txtId);
        form.add(new JLabel("Name:"));                form.add(txtName);
        form.add(new JLabel("Email:"));               form.add(txtEmail);
        form.add(new JLabel("Password:"));            form.add(txtPassword);
        form.add(new JLabel("Confirm Password:"));    form.add(txtConfirm);
        form.add(new JLabel("Branch:"));              form.add(cmbBranch);
        form.add(new JLabel("Year:"));                form.add(cmbYear);
        form.add(new JLabel("Skills (comma separated):"));    form.add(txtSkills);
        form.add(new JLabel("Interests (comma separated):")); form.add(txtInterests);
        add(form, BorderLayout.CENTER);

        JButton btnRegister = new JButton("Register");
        JButton btnBack = new JButton("Back to Login");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttons.add(btnRegister);
        buttons.add(btnBack);
        add(buttons, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> doRegister());
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void doRegister() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());
        String branch = (String) cmbBranch.getSelectedItem();
        int year = Integer.parseInt((String) cmbYear.getSelectedItem());
        String skills = txtSkills.getText().trim();
        String interests = txtInterests.getText().trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showWarning("Student ID, Name, Email and Password are required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showWarning("Please enter a valid email address.");
            return;
        }
        if (password.length() < 4) {
            showWarning("Password must be at least 4 characters long.");
            return;
        }
        if (!password.equals(confirm)) {
            showWarning("Password and Confirm Password do not match.");
            return;
        }

        StudentDAO dao = new StudentDAO();
        if (dao.studentIdExists(id)) {
            showWarning("This Student ID is already registered.");
            return;
        }
        if (dao.emailExists(email)) {
            showWarning("This email is already registered.");
            return;
        }

        Student s = new Student(id, name, email, password, branch, year, skills, interests, "Available");
        if (dao.registerStudent(s)) {
            JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Check the database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Check Your Input", JOptionPane.WARNING_MESSAGE);
    }
}