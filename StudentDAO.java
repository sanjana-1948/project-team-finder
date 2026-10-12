package dao;

import db.DBConnection;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public static Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("branch"),
                rs.getInt("year"),
                rs.getString("skills"),
                rs.getString("interests"),
                rs.getString("availability"));
    }

    public boolean registerStudent(Student s) {
        String sql = "INSERT INTO students (student_id, name, email, password, branch, year, skills, interests, availability) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPassword());
            ps.setString(5, s.getBranch());
            ps.setInt(6, s.getYear());
            ps.setString(7, s.getSkills());
            ps.setString(8, s.getInterests());
            ps.setString(9, s.getAvailability() == null ? "Available" : s.getAvailability());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("registerStudent error: " + e.getMessage());
            return false;
        }
    }

    public boolean studentIdExists(String studentId) {
        return exists("SELECT 1 FROM students WHERE student_id = ?", studentId);
    }

    public boolean emailExists(String email) {
        return exists("SELECT 1 FROM students WHERE email = ?", email);
    }

    private boolean exists(String sql, String value) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("exists error: " + e.getMessage());
            return false;
        }
    }

    // Accepts email OR student ID. Returns the Student, or null if login fails.
    public Student login(String emailOrId, String password) {
        String sql = "SELECT * FROM students WHERE (email = ? OR student_id = ?) AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailOrId);
            ps.setString(2, emailOrId);
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("login error: " + e.getMessage());
        }
        return null;
    }

    public Student getStudentById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("getStudentById error: " + e.getMessage());
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllStudents error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateProfile(Student s) {
        String sql = "UPDATE students SET name = ?, branch = ?, year = ?, skills = ?, interests = ?, availability = ? "
                   + "WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getBranch());
            ps.setInt(3, s.getYear());
            ps.setString(4, s.getSkills());
            ps.setString(5, s.getInterests());
            ps.setString(6, s.getAvailability());
            ps.setString(7, s.getStudentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateProfile error: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(String studentId, String newPassword) {
        String sql = "UPDATE students SET password = ? WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updatePassword error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteStudent error: " + e.getMessage());
            return false;
        }
    }

    // Empty or "All" filters are ignored. year = 0 means any year.
    public List<Student> searchStudents(String skill, String branch, int year,
                                        String interest, String excludeId) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (!isBlank(skill)) {
            sql.append(" AND skills LIKE ?");
            params.add("%" + skill.trim() + "%");
        }
        if (!isBlank(branch)) {
            sql.append(" AND branch = ?");
            params.add(branch.trim());
        }
        if (year > 0) {
            sql.append(" AND year = ?");
            params.add(year);
        }
        if (!isBlank(interest)) {
            sql.append(" AND interests LIKE ?");
            params.add("%" + interest.trim() + "%");
        }
        if (!isBlank(excludeId)) {
            sql.append(" AND student_id <> ?");
            params.add(excludeId);
        }
        sql.append(" ORDER BY name");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStudent(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("searchStudents error: " + e.getMessage());
        }
        return list;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || s.equalsIgnoreCase("All");
    }
}