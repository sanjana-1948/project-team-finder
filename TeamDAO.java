package dao;

import db.DBConnection;
import model.Student;
import model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {

    // Base query: team + project name + max size + current member count
    private static final String TEAM_SELECT =
            "SELECT t.team_id, t.team_name, t.project_id, t.leader_id, p.project_name, p.max_team_size, "
          + "(SELECT COUNT(*) FROM team_members m WHERE m.team_id = t.team_id) AS member_count "
          + "FROM teams t JOIN projects p ON t.project_id = p.project_id ";

    private Team mapTeam(ResultSet rs) throws SQLException {
        Team t = new Team(
                rs.getInt("team_id"),
                rs.getString("team_name"),
                rs.getString("project_id"),
                rs.getString("leader_id"));
        t.setProjectName(rs.getString("project_name"));
        t.setMaxTeamSize(rs.getInt("max_team_size"));
        t.setMemberCount(rs.getInt("member_count"));
        return t;
    }

    // Creates the team AND adds the leader as first member. Returns new team id, or -1 on failure.
    public int createTeam(Team team) {
        String checkSql = "SELECT COUNT(*) FROM teams WHERE project_id = ?";
        String insertTeam = "INSERT INTO teams (team_name, project_id, leader_id) VALUES (?, ?, ?)";
        String insertMember = "INSERT INTO team_members (team_id, student_id) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setString(1, team.getProjectId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            con.rollback();
                            return -1; // project already has a team
                        }
                    }
                }

                int newId;
                try (PreparedStatement ps = con.prepareStatement(insertTeam, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, team.getTeamName());
                    ps.setString(2, team.getProjectId());
                    ps.setString(3, team.getLeaderId());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            con.rollback();
                            return -1;
                        }
                        newId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertMember)) {
                    ps.setInt(1, newId);
                    ps.setString(2, team.getLeaderId());
                    ps.executeUpdate();
                }

                con.commit();
                return newId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("createTeam error: " + e.getMessage());
            return -1;
        }
    }

    public List<Team> getAllTeams() {
        return queryTeams(TEAM_SELECT + "ORDER BY t.team_name", null);
    }

    public List<Team> getTeamsForStudent(String studentId) {
        return queryTeams(TEAM_SELECT
                + "WHERE t.team_id IN (SELECT team_id FROM team_members WHERE student_id = ?) "
                + "ORDER BY t.team_name", studentId);
    }

    public Team getTeamById(int teamId) {
        List<Team> list = queryTeams(TEAM_SELECT + "WHERE t.team_id = ?", String.valueOf(teamId));
        return list.isEmpty() ? null : list.get(0);
    }

    public Team getTeamByProject(String projectId) {
        List<Team> list = queryTeams(TEAM_SELECT + "WHERE t.project_id = ?", projectId);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<Team> queryTeams(String sql, String param) {
        List<Team> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param); // MySQL converts "5" to a number automatically
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTeam(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Team query error: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getMembers(int teamId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.* FROM students s JOIN team_members m ON s.student_id = m.student_id "
                   + "WHERE m.team_id = ? ORDER BY s.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(StudentDAO.mapStudent(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getMembers error: " + e.getMessage());
        }
        return list;
    }

    public boolean isMember(int teamId, String studentId) {
        String sql = "SELECT 1 FROM team_members WHERE team_id = ? AND student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setString(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("isMember error: " + e.getMessage());
            return false;
        }
    }

    public boolean isTeamFull(int teamId) {
        String sql = "SELECT (SELECT COUNT(*) FROM team_members WHERE team_id = ?) AS cnt, p.max_team_size "
                   + "FROM teams t JOIN projects p ON t.project_id = p.project_id WHERE t.team_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") >= rs.getInt("max_team_size");
                }
            }
        } catch (SQLException e) {
            System.out.println("isTeamFull error: " + e.getMessage());
        }
        return true; // on error, treat as full (safe option)
    }

    // Returns "OK" or an error message.
    public String addMember(int teamId, String studentId) {
        if (isMember(teamId, studentId)) {
            return "This student is already in the team.";
        }
        if (isTeamFull(teamId)) {
            return "The team is already full.";
        }
        String sql = "INSERT INTO team_members (team_id, student_id) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setString(2, studentId);
            ps.executeUpdate();
            return "OK";
        } catch (SQLException e) {
            System.out.println("addMember error: " + e.getMessage());
            return "Database error while adding the member.";
        }
    }

    // Last member leaving deletes the team. If the leader leaves, another member becomes leader.
    public boolean leaveTeam(int teamId, String studentId) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int removed;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM team_members WHERE team_id = ? AND student_id = ?")) {
                    ps.setInt(1, teamId);
                    ps.setString(2, studentId);
                    removed = ps.executeUpdate();
                }
                if (removed == 0) {
                    con.rollback();
                    return false;
                }

                String nextMember = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT student_id FROM team_members WHERE team_id = ? LIMIT 1")) {
                    ps.setInt(1, teamId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            nextMember = rs.getString(1);
                        }
                    }
                }

                if (nextMember == null) {
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM teams WHERE team_id = ?")) {
                        ps.setInt(1, teamId);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE teams SET leader_id = ? WHERE team_id = ? AND leader_id = ?")) {
                        ps.setString(1, nextMember);
                        ps.setInt(2, teamId);
                        ps.setString(3, studentId);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("leaveTeam error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTeam(int teamId) {
        String sql = "DELETE FROM teams WHERE team_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteTeam error: " + e.getMessage());
            return false;
        }
    }
}