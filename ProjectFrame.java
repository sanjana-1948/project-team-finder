package gui;

import dao.ProjectDAO;
import dao.TeamDAO;
import model.Project;
import model.Student;
import model.Team;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjectFrame extends JFrame {

    private Student student;
    private ProjectDAO projectDAO = new ProjectDAO();
    private TeamDAO teamDAO = new TeamDAO();

    private JTabbedPane tabs;

    // "Create Project" tab
    private JTextField txtId, txtName, txtSkills;
    private JTextArea txtDescription;
    private JComboBox<Integer> cmbSize;
    private JComboBox<String> cmbDomain;

    // "Project Dashboard" tab
    private JComboBox<Project> cmbProjects;
    private JLabel lblName, lblSkills, lblSize, lblTeam;
    private JTextArea areaDescription;
    private DefaultTableModel memberModel;
    private boolean loading = false; // stops combo events while we refill it

    public ProjectFrame(Student student) {
        this.student = student;

        setTitle("Project Team Finder - Projects");
        setSize(850, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabs = new JTabbedPane();
        tabs.addTab("Create Project", buildCreatePanel());
        tabs.addTab("Project Dashboard", buildDashboardPanel());
        add(tabs, BorderLayout.CENTER);

        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> {
            new DashboardFrame(student).setVisible(true);
            dispose();
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        reloadProjects(null);
    }

    // ===== TAB 1: CREATE PROJECT =====
    private JPanel buildCreatePanel() {
        txtId = new JTextField(suggestProjectId());
        txtName = new JTextField();
        txtSkills = new JTextField();
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        cmbSize = new JComboBox<>(new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10});
        cmbSize.setSelectedItem(4);
        cmbDomain = new JComboBox<>(new String[]{"Web Development", "Mobile Application", "Desktop Application",
                "Artificial Intelligence", "Internet of Things", "Robotics", "Data Science", "Other"});
        cmbDomain.setEditable(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.add(new JLabel("Project ID:"));                         form.add(txtId);
        form.add(new JLabel("Project Name:"));                       form.add(txtName);
        form.add(new JLabel("Description:"));                        form.add(new JScrollPane(txtDescription));
        form.add(new JLabel("Required Skills (comma separated):"));  form.add(txtSkills);
        form.add(new JLabel("Maximum Team Size:"));                  form.add(cmbSize);
        form.add(new JLabel("Project Domain:"));                     form.add(cmbDomain);

        JButton btnCreate = new JButton("Create Project");
        JButton btnClear = new JButton("Clear");
        btnCreate.addActionListener(e -> createProject());
        btnClear.addActionListener(e -> clearCreateForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.add(btnCreate);
        buttons.add(btnClear);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 50, 10, 50));
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private String suggestProjectId() {
        int n = projectDAO.getAllProjects().size() + 1;
        String id = String.format("P%03d", n);
        while (projectDAO.projectIdExists(id)) {
            n++;
            id = String.format("P%03d", n);
        }
        return id;
    }

    private void clearCreateForm() {
        txtId.setText(suggestProjectId());
        txtName.setText("");
        txtDescription.setText("");
        txtSkills.setText("");
        cmbSize.setSelectedItem(4);
        cmbDomain.setSelectedIndex(0);
    }

    private void createProject() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        String skills = txtSkills.getText().trim();
        int size = (Integer) cmbSize.getSelectedItem();
        Object domainObj = cmbDomain.getSelectedItem();
        String domain = (domainObj == null) ? "" : domainObj.toString().trim();

        if (id.isEmpty() || name.isEmpty() || skills.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Project ID, Name and Required Skills are required.",
                    "Check Your Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (projectDAO.projectIdExists(id)) {
            JOptionPane.showMessageDialog(this, "This Project ID already exists. Please use another one.",
                    "Check Your Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Project p = new Project(id, name, description, skills, size, domain, student.getStudentId());
        if (projectDAO.addProject(p)) {
            JOptionPane.showMessageDialog(this,
                    "Project created! Go to Team Management to create a team for it.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearCreateForm();
            reloadProjects(id);
            tabs.setSelectedIndex(1);
        } else {
            JOptionPane.showMessageDialog(this, "Could not create the project. Check the database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== TAB 2: PROJECT DASHBOARD =====
    private JPanel buildDashboardPanel() {
        cmbProjects = new JComboBox<>();
        cmbProjects.addActionListener(e -> {
            if (!loading) {
                showSelectedProject();
            }
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Select Project:"));
        top.add(cmbProjects);

        lblName = new JLabel();
        lblName.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblSkills = new JLabel();
        lblSize = new JLabel();
        lblTeam = new JLabel();
        JPanel labels = new JPanel(new GridLayout(4, 1, 0, 4));
        labels.add(lblName);
        labels.add(lblSkills);
        labels.add(lblSize);
        labels.add(lblTeam);

        areaDescription = new JTextArea(3, 20);
        areaDescription.setEditable(false);
        areaDescription.setLineWrap(true);
        areaDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(areaDescription);
        descScroll.setBorder(BorderFactory.createTitledBorder("Description"));

        JPanel details = new JPanel(new BorderLayout(0, 8));
        details.add(labels, BorderLayout.NORTH);
        details.add(descScroll, BorderLayout.CENTER);

        String[] cols = {"Student ID", "Name", "Branch", "Year", "Skills", "Role"};
        memberModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JScrollPane memberScroll = new JScrollPane(new JTable(memberModel));
        memberScroll.setBorder(BorderFactory.createTitledBorder("Team Members"));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 10));
        center.add(details);
        center.add(memberScroll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void reloadProjects(String selectId) {
        loading = true;
        cmbProjects.removeAllItems();
        List<Project> projects = projectDAO.getAllProjects();
        Project toSelect = null;
        for (Project p : projects) {
            cmbProjects.addItem(p);
            if (p.getProjectId().equals(selectId)) {
                toSelect = p;
            }
        }
        if (toSelect != null) {
            cmbProjects.setSelectedItem(toSelect);
        }
        loading = false;
        showSelectedProject();
    }

    private void showSelectedProject() {
        memberModel.setRowCount(0);
        Project p = (Project) cmbProjects.getSelectedItem();
        if (p == null) {
            lblName.setText("No projects available.");
            lblSkills.setText("");
            lblSize.setText("");
            lblTeam.setText("");
            areaDescription.setText("");
            return;
        }

        lblName.setText("Project: " + p.getProjectName() + "  (" + p.getProjectId() + ")");
        lblSkills.setText("Required Skills: " + p.getRequiredSkills() + "     |     Domain: " + p.getDomain());
        areaDescription.setText(p.getDescription());
        areaDescription.setCaretPosition(0);

        Team team = teamDAO.getTeamByProject(p.getProjectId());
        if (team == null) {
            lblSize.setText("Team Size: 0 / " + p.getMaxTeamSize());
            lblTeam.setText("Team: No team created yet");
            return;
        }

        lblSize.setText("Team Size: " + team.getMemberCount() + " / " + p.getMaxTeamSize());
        lblTeam.setText("Team: " + team.getTeamName());
        List<Student> members = teamDAO.getMembers(team.getTeamId());
        for (Student s : members) {
            String role = s.getStudentId().equals(team.getLeaderId()) ? "Leader" : "Member";
            memberModel.addRow(new Object[]{s.getStudentId(), s.getName(), s.getBranch(),
                    s.getYear(), s.getSkills(), role});
        }
    }
}