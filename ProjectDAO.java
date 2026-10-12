package dao;

import db.DBConnection;
import model.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    private Project mapProject(ResultSet rs) throws SQLException {
        return new Project(
                rs.getString("project_id"),
                rs.getString("project_name"),
                rs.getString("description"),
                rs.getString("required_skills"),
                rs.getInt("max_team_size"),
                rs.getString("domain"),
                rs.getString("created_by"));
    }

    public boolean addProject(Project p) {
        String sql = "INSERT INTO projects (project_id, project_name, description, required_skills, "
                   + "max_team_size, domain, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getProjectId());
            ps.setString(2, p.getProjectName());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getRequiredSkills());
            ps.setInt(5, p.getMaxTeamSize());
            ps.setString(6, p.getDomain());
            ps.setString(7, p.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("addProject error: " + e.getMessage());
            return false;
        }
    }

    public boolean projectIdExists(String projectId) {
        String sql = "SELECT 1 FROM projects WHERE project_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("projectIdExists error: " + e.getMessage());
            return false;
        }
    }

    public List<Project> getAllProjects() {
        return queryList("SELECT * FROM projects ORDER BY project_id", null);
    }

    public List<Project> getProjectsByCreator(String studentId) {
        return queryList("SELECT * FROM projects WHERE created_by = ? ORDER BY project_id", studentId);
    }

    // Projects that do not have a team yet
    public List<Project> getProjectsWithoutTeam() {
        return queryList("SELECT * FROM projects WHERE project_id NOT IN "
                       + "(SELECT project_id FROM teams) ORDER BY project_id", null);
    }

    public Project getProjectById(String projectId) {
        List<Project> list = queryList("SELECT * FROM projects WHERE project_id = ?", projectId);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<Project> queryList(String sql, String param) {
        List<Project> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProject(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Project query error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateProject(Project p) {
        String sql = "UPDATE projects SET project_name = ?, description = ?, required_skills = ?, "
                   + "max_team_size = ?, domain = ? WHERE project_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getProjectName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getRequiredSkills());
            ps.setInt(4, p.getMaxTeamSize());
            ps.setString(5, p.getDomain());
            ps.setString(6, p.getProjectId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateProject error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProject(String projectId) {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, projectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteProject error: " + e.getMessage());
            return false;
        }
    }
}