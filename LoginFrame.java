package gui;

import dao.StudentDAO;
import model.Student;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Project Team Finder - Login");
        setSize(420, 330);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Project Team Finder", JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 5, 10));
        JLabel lblSub = new JLabel("Student Login", JLabel.CENTER);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(lblTitle);
        top.add(lblSub);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        form.add(new JLabel("Email / Student ID:"));
        txtUser = new JTextField();
        form.add(txtUser);
        form.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        form.add(txtPassword);
        add(form, BorderLayout.CENTER);

        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        buttons.add(btnLogin);
        buttons.add(btnRegister);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLogin); // Enter key = Login

        btnLogin.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
    }

    private void doLogin() {
        String user = txtUser.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (user.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email/student ID and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StudentDAO dao = new StudentDAO();
        Student student = dao.login(user, password);

        if (student == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid login details (or the database is not reachable).",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        } else {
            new DashboardFrame(student).setVisible(true);
            dispose();
        }
    }
}