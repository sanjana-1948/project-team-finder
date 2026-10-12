package gui;

import dao.ProjectDAO;
import dao.StudentDAO;
import model.Project;
import model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindTeammatesFrame extends JFrame {

    private Student student;
    private StudentDAO studentDAO = new StudentDAO();

    private JTextField txtSkill, txtInterest;
    private JComboBox<String> cmbBranch, cmbYear;
    private JComboBox<Object> cmbProject; // first item is the text "-- Any project --"
    private DefaultTableModel tableModel;
    private JLabel lblCount;

    public FindTeammatesFrame(Student student) {
        this.student = student;

        setTitle("Project Team Finder - Find Teammates");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        search();
    }

    private JPanel buildFilterPanel() {
        txtSkill = new JTextField(12);
        txtInterest = new JTextField(12);
        cmbBranch = new JComboBox<>(new String[]{"All", "CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "Other"});
        cmbYear = new JComboBox<>(new String[]{"All", "1", "2", "3", "4"});

        cmbProject = new JComboBox<>();
        cmbProject.addItem("-- Any project --");
        for (Project p : new ProjectDAO().getAllProjects()) {
            cmbProject.addItem(p);
        }

        JButton btnSearch = new JButton("Search");
        JButton btnClear = new JButton("Clear Filters");
        btnSearch.addActionListener(e -> search());
        btnClear.addActionListener(e -> {
            txtSkill.setText("");
            txtInterest.setText("");
            cmbBranch.setSelectedIndex(0);
            cmbYear.setSelectedIndex(0);
            cmbProject.setSelectedIndex(0);
            search();
        });

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1.add(new JLabel("Skill:"));     row1.add(txtSkill);
        row1.add(new JLabel("Branch:"));    row1.add(cmbBranch);
        row1.add(new JLabel("Year:"));      row1.add(cmbYear);
        row1.add(new JLabel("Interest:"));  row1.add(txtInterest);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row2.add(new JLabel("Match for project:"));
        row2.add(cmbProject);
        row2.add(btnSearch);
        row2.add(btnClear);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
        panel.add(row1);
        panel.add(row2);
        return panel;
    }

    private JScrollPane buildTablePanel() {
        String[] cols = {"Student ID", "Name", "Email", "Branch", "Year", "Skills", "Interests",
                "Availability", "Skill Match"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return scroll;
    }

    private JPanel buildBottomPanel() {
        lblCount = new JLabel(" ");
        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> {
            new DashboardFrame(student).setVisible(true);
            dispose();
        });
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        panel.add(lblCount, BorderLayout.WEST);
        panel.add(btnBack, BorderLayout.EAST);
        return panel;
    }

    private void search() {
        String branch = (String) cmbBranch.getSelectedItem();
        String yearText = (String) cmbYear.getSelectedItem();
        int year = yearText.equals("All") ? 0 : Integer.parseInt(yearText);

        List<Student> results = studentDAO.searchStudents(
                txtSkill.getText(), branch, year, txtInterest.getText(), student.getStudentId());

        // Optional: rank students by how many of the project's required skills they have
        Map<String, Integer> matchCount = new HashMap<>();
        int requiredTotal = 0;
        Object selected = cmbProject.getSelectedItem();
        if (selected instanceof Project) {
            List<String> required = splitSkills(((Project) selected).getRequiredSkills());
            requiredTotal = required.size();

            List<Student> matched = new ArrayList<>();
            for (Student s : results) {
                int count = countMatches(s.getSkills(), required);
                if (count > 0) {
                    matchCount.put(s.getStudentId(), count);
                    matched.add(s);
                }
            }
            matched.sort((a, b) -> matchCount.get(b.getStudentId()) - matchCount.get(a.getStudentId()));
            results = matched;
        }

        tableModel.setRowCount(0);
        for (Student s : results) {
            String match = "-";
            if (matchCount.containsKey(s.getStudentId())) {
                match = matchCount.get(s.getStudentId()) + " / " + requiredTotal;
            }
            tableModel.addRow(new Object[]{s.getStudentId(), s.getName(), s.getEmail(), s.getBranch(),
                    s.getYear(), s.getSkills(), s.getInterests(), s.getAvailability(), match});
        }
        lblCount.setText(results.size() + " student(s) found");
    }

    private List<String> splitSkills(String text) {
        List<String> list = new ArrayList<>();
        if (text == null) {
            return list;
        }
        for (String part : text.split(",")) {
            String skill = part.trim().toLowerCase();
            if (!skill.isEmpty()) {
                list.add(skill);
            }
        }
        return list;
    }

    private int countMatches(String studentSkills, List<String> required) {
        if (studentSkills == null) {
            return 0;
        }
        String lower = studentSkills.toLowerCase();
        int count = 0;
        for (String skill : required) {
            if (lower.contains(skill)) {
                count++;
            }
        }
        return count;
    }
}