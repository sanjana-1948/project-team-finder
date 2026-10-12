package gui;

import dao.ProjectDAO;
import dao.StudentDAO;
import dao.TeamDAO;
import model.Project;
import model.Student;
import model.Team;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {

    private Student student;

    public DashboardFrame(Student student) {
        this.student = student;

        setTitle("Project Team Finder - Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTables(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JLabel lblWelcome = new JLabel("Welcome, " + student.getName() + "!");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel lblInfo = new JLabel(student.getStudentId() + "  |  " + student.getBranch()
                + "  |  Year " + student.getYear() + "  |  " + student.getAvailability());
        JLabel lblSkills = new JLabel("Skills: " + valueOrDash(student.getSkills()));
        JLabel lblInterests = new JLabel("Interests: " + valueOrDash(student.getInterests()));

        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 3));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        panel.add(lblWelcome);
        panel.add(lblInfo);
        panel.add(lblSkills);
        panel.add(lblInterests);
        return panel;
    }

    private JPanel buildTables() {
        String[] projectCols = {"ID", "Project Name", "Domain", "Required Skills", "Max Team Size"};
        DefaultTableModel projectModel = new DefaultTableModel(projectCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        List<Project> projects = new ProjectDAO().getAllProjects();
        for (Project p : projects) {
            projectModel.addRow(new Object[]{p.getProjectId(), p.getProjectName(), p.getDomain(),
                    p.getRequiredSkills(), p.getMaxTeamSize()});
        }
        JScrollPane projectScroll = new JScrollPane(new JTable(projectModel));
        projectScroll.setBorder(BorderFactory.createTitledBorder("Available Projects"));

        String[] teamCols = {"Team Name", "Project", "Members", "Leader"};
        DefaultTableModel teamModel = new DefaultTableModel(teamCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        List<Team> teams = new TeamDAO().getTeamsForStudent(student.getStudentId());
        StudentDAO studentDAO = new StudentDAO();
        for (Team t : teams) {
            Student leader = studentDAO.getStudentById(t.getLeaderId());
            String leaderName = (leader == null) ? t.getLeaderId() : leader.getName();
            teamModel.addRow(new Object[]{t.getTeamName(), t.getProjectName(),
                    t.getMemberCount() + " / " + t.getMaxTeamSize(), leaderName});
        }
        JScrollPane teamScroll = new JScrollPane(new JTable(teamModel));
        teamScroll.setBorder(BorderFactory.createTitledBorder("My Team"));

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        panel.add(projectScroll);
        panel.add(teamScroll);
        return panel;
    }

    private JPanel buildButtons() {
        JButton btnProfile = new JButton("My Profile");
        JButton btnProject = new JButton("Create Project");
        JButton btnFind = new JButton("Search Teammates");
        JButton btnTeam = new JButton("Team Management");
        JButton btnLogout = new JButton("Logout");

        btnProfile.addActionListener(e -> {
            new ProfileFrame(student).setVisible(true);
            dispose();
        });
        btnProject.addActionListener(e -> {
            new ProjectFrame(student).setVisible(true);
            dispose();
        });
        btnFind.addActionListener(e -> {
            new FindTeammatesFrame(student).setVisible(true);
            dispose();
        });
        btnTeam.addActionListener(e -> {
            new TeamFrame(student).setVisible(true);
            dispose();
        });
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Do you want to log out?",
                    "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(btnProfile);
        panel.add(btnProject);
        panel.add(btnFind);
        panel.add(btnTeam);
        panel.add(btnLogout);
        return panel;
    }

    private String valueOrDash(String text) {
        return (text == null || text.trim().isEmpty()) ? "-" : text;
    }
}