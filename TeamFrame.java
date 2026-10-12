package gui;

import dao.ProjectDAO;
import dao.StudentDAO;
import dao.TeamDAO;
import dao.TeamRequestDAO;
import model.Project;
import model.Student;
import model.Team;
import model.TeamRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TeamFrame extends JFrame {

    private Student student;
    private TeamDAO teamDAO = new TeamDAO();
    private TeamRequestDAO requestDAO = new TeamRequestDAO();
    private ProjectDAO projectDAO = new ProjectDAO();
    private StudentDAO studentDAO = new StudentDAO();

    private JTabbedPane tabs;

    // Tab 1: My Teams
    private DefaultTableModel myTeamsModel, membersModel;
    private JTable myTeamsTable;
    private List<Team> myTeams = new ArrayList<>();

    // Tab 2: Create Team
    private JComboBox<Project> cmbFreeProjects;
    private JTextField txtTeamName;

    // Tab 3: Browse Teams
    private DefaultTableModel browseModel;
    private JTable browseTable;
    private List<Team> allTeams = new ArrayList<>();

    // Tab 4: Requests
    private DefaultTableModel incomingModel, sentModel;
    private JTable incomingTable;
    private List<TeamRequest> incoming = new ArrayList<>();

    public TeamFrame(Student student) {
        this.student = student;

        setTitle("Project Team Finder - Team Management");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabs = new JTabbedPane();
        tabs.addTab("My Teams", buildMyTeamsTab());
        tabs.addTab("Create Team", buildCreateTab());
        tabs.addTab("Browse Teams", buildBrowseTab());
        tabs.addTab("Requests", buildRequestsTab());
        add(tabs, BorderLayout.CENTER);

        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> {
            new DashboardFrame(student).setVisible(true);
            dispose();
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        refreshAll();
    }

    private DefaultTableModel newModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    // ===== TAB 1: MY TEAMS =====
    private JPanel buildMyTeamsTab() {
        myTeamsModel = newModel(new String[]{"Team Name", "Project", "Members", "My Role"});
        myTeamsTable = new JTable(myTeamsModel);
        myTeamsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myTeamsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showMembersOfSelectedTeam();
            }
        });
        JScrollPane teamScroll = new JScrollPane(myTeamsTable);
        teamScroll.setBorder(BorderFactory.createTitledBorder("My Teams (select one to see its members)"));

        membersModel = newModel(new String[]{"Student ID", "Name", "Branch", "Year", "Skills", "Role"});
        JScrollPane memberScroll = new JScrollPane(new JTable(membersModel));
        memberScroll.setBorder(BorderFactory.createTitledBorder("Team Members"));

        JButton btnLeave = new JButton("Leave Selected Team");
        btnLeave.addActionListener(e -> leaveSelectedTeam());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(btnLeave);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 8));
        center.add(teamScroll);
        center.add(memberScroll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        panel.add(center, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void showMembersOfSelectedTeam() {
        membersModel.setRowCount(0);
        int row = myTeamsTable.getSelectedRow();
        if (row < 0 || row >= myTeams.size()) {
            return;
        }
        Team team = myTeams.get(row);
        for (Student s : teamDAO.getMembers(team.getTeamId())) {
            String role = s.getStudentId().equals(team.getLeaderId()) ? "Leader" : "Member";
            membersModel.addRow(new Object[]{s.getStudentId(), s.getName(), s.getBranch(),
                    s.getYear(), s.getSkills(), role});
        }
    }

    private void leaveSelectedTeam() {
        int row = myTeamsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a team first.",
                    "No Team Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Team team = myTeams.get(row);
        String extra = team.getLeaderId().equals(student.getStudentId())
                ? "\nYou are the leader. Leadership will pass to another member (or the team is deleted if you are the last one)."
                : "";
        int choice = JOptionPane.showConfirmDialog(this,
                "Leave the team \"" + team.getTeamName() + "\"?" + extra,
                "Leave Team", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (teamDAO.leaveTeam(team.getTeamId(), student.getStudentId())) {
            JOptionPane.showMessageDialog(this, "You have left the team.",
                    "Done", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Could not leave the team. Check the database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== TAB 2: CREATE TEAM =====
    private JPanel buildCreateTab() {
        cmbFreeProjects = new JComboBox<>();
        txtTeamName = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 15));
        form.add(new JLabel("Project (without a team):"));
        form.add(cmbFreeProjects);
        form.add(new JLabel("Team Name:"));
        form.add(txtTeamName);

        JButton btnCreate = new JButton("Create Team");
        btnCreate.addActionListener(e -> createTeam());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(btnCreate);

        JLabel hint = new JLabel("<html>You become the team leader and the first member. "
                + "Create a project first (Dashboard > Create Project) if the list is empty.</html>");

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        panel.add(hint, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void createTeam() {
        Project project = (Project) cmbFreeProjects.getSelectedItem();
        String teamName = txtTeamName.getText().trim();

        if (project == null) {
            JOptionPane.showMessageDialog(this, "There is no project available. Every project already has a team.",
                    "No Project", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (teamName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a team name.",
                    "Check Your Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Team team = new Team(0, teamName, project.getProjectId(), student.getStudentId());
        int newId = teamDAO.createTeam(team);
        if (newId > 0) {
            JOptionPane.showMessageDialog(this, "Team \"" + teamName + "\" created!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            txtTeamName.setText("");
            refreshAll();
            tabs.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "Could not create the team. The project may already have one.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            refreshAll();
        }
    }

    // ===== TAB 3: BROWSE TEAMS =====
    private JPanel buildBrowseTab() {
        browseModel = newModel(new String[]{"Team Name", "Project", "Members", "Leader"});
        browseTable = new JTable(browseModel);
        browseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(browseTable);
        scroll.setBorder(BorderFactory.createTitledBorder("All Teams"));

        JButton btnRequest = new JButton("Send Join Request");
        btnRequest.addActionListener(e -> sendJoinRequest());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(btnRequest);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void sendJoinRequest() {
        int row = browseTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a team first.",
                    "No Team Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Team team = allTeams.get(row);
        String result = requestDAO.sendRequest(team.getTeamId(), student.getStudentId());
        if (result.equals("OK")) {
            JOptionPane.showMessageDialog(this,
                    "Request sent to the leader of \"" + team.getTeamName() + "\".",
                    "Request Sent", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, result, "Cannot Send Request", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ===== TAB 4: REQUESTS =====
    private JPanel buildRequestsTab() {
        incomingModel = newModel(new String[]{"Student ID", "Student Name", "Team", "Status"});
        incomingTable = new JTable(incomingModel);
        incomingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane incomingScroll = new JScrollPane(incomingTable);
        incomingScroll.setBorder(BorderFactory.createTitledBorder("Join Requests to Teams I Lead (pending)"));

        JButton btnAccept = new JButton("Accept");
        JButton btnReject = new JButton("Reject");
        btnAccept.addActionListener(e -> handleRequest(true));
        btnReject.addActionListener(e -> handleRequest(false));
        JPanel incomingButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        incomingButtons.add(btnAccept);
        incomingButtons.add(btnReject);

        JPanel top = new JPanel(new BorderLayout());
        top.add(incomingScroll, BorderLayout.CENTER);
        top.add(incomingButtons, BorderLayout.SOUTH);

        sentModel = newModel(new String[]{"Team", "Status"});
        JScrollPane sentScroll = new JScrollPane(new JTable(sentModel));
        sentScroll.setBorder(BorderFactory.createTitledBorder("My Sent Requests"));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 8));
        center.add(top);
        center.add(sentScroll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void handleRequest(boolean accept) {
        int row = incomingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TeamRequest request = incoming.get(row);

        if (accept) {
            String result = requestDAO.acceptRequest(request.getRequestId());
            if (result.equals("OK")) {
                JOptionPane.showMessageDialog(this, request.getStudentName() + " has joined your team!",
                        "Request Accepted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result, "Cannot Accept", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            if (requestDAO.rejectRequest(request.getRequestId())) {
                JOptionPane.showMessageDialog(this, "Request rejected.",
                        "Request Rejected", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not reject the request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        refreshAll();
    }

    // ===== REFRESH: reloads every tab from the database =====
    private void refreshAll() {
        String myId = student.getStudentId();

        myTeams = teamDAO.getTeamsForStudent(myId);
        myTeamsModel.setRowCount(0);
        for (Team t : myTeams) {
            String role = t.getLeaderId().equals(myId) ? "Leader" : "Member";
            myTeamsModel.addRow(new Object[]{t.getTeamName(), t.getProjectName(),
                    t.getMemberCount() + " / " + t.getMaxTeamSize(), role});
        }
        membersModel.setRowCount(0);
        if (!myTeams.isEmpty()) {
            myTeamsTable.setRowSelectionInterval(0, 0);
        }

        cmbFreeProjects.removeAllItems();
        for (Project p : projectDAO.getProjectsWithoutTeam()) {
            cmbFreeProjects.addItem(p);
        }

        allTeams = teamDAO.getAllTeams();
        browseModel.setRowCount(0);
        for (Team t : allTeams) {
            Student leader = studentDAO.getStudentById(t.getLeaderId());
            String leaderName = (leader == null) ? t.getLeaderId() : leader.getName();
            browseModel.addRow(new Object[]{t.getTeamName(), t.getProjectName(),
                    t.getMemberCount() + " / " + t.getMaxTeamSize(), leaderName});
        }

        incoming = requestDAO.getPendingRequestsForLeader(myId);
        incomingModel.setRowCount(0);
        for (TeamRequest r : incoming) {
            incomingModel.addRow(new Object[]{r.getStudentId(), r.getStudentName(),
                    r.getTeamName(), r.getStatus()});
        }

        sentModel.setRowCount(0);
        for (TeamRequest r : requestDAO.getRequestsByStudent(myId)) {
            sentModel.addRow(new Object[]{r.getTeamName(), r.getStatus()});
        }
    }
}