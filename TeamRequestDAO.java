package dao;

import db.DBConnection;
import model.TeamRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeamRequestDAO {

    private static final String REQ_SELECT =
            "SELECT r.request_id, r.team_id, r.student_id, r.status, s.name AS student_name, t.team_name "
          + "FROM team_requests r "
          + "JOIN students s ON r.student_id = s.student_id "
          + "JOIN teams t ON r.team_id = t.team_id ";

    private final TeamDAO teamDAO = new TeamDAO();

    private TeamRequest mapRequest(ResultSet rs) throws SQLException {
        TeamRequest r = new TeamRequest(
                rs.getInt("request_id"),
                rs.getInt("team_id"),
                rs.getString("student_id"),
                rs.getString("status"));
        r.setStudentName(rs.getString("student_name"));
        r.setTeamName(rs.getString("team_name"));
        return r;
    }

    // Returns "OK" or an error message.
    public String sendRequest(int teamId, String studentId) {
        if (teamDAO.isMember(teamId, studentId)) {
            return "You are already a member of this team.";
        }
        if (teamDAO.isTeamFull(teamId)) {
            return "This team is already full.";
        }
        if (hasPendingRequest(teamId, studentId)) {
            return "You already have a pending request for this team.";
        }
        String sql = "INSERT INTO team_requests (team_id, student_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setString(2, studentId);
            ps.executeUpdate();
            return "OK";
        } catch (SQLException e) {
            System.out.println("sendRequest error: " + e.getMessage());
            return "Database error while sending the request.";
        }
    }

    public boolean hasPendingRequest(int teamId, String studentId) {
        String sql = "SELECT 1 FROM team_requests WHERE team_id = ? AND student_id = ? AND status = 'PENDING'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setString(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("hasPendingRequest error: " + e.getMessage());
            return false;
        }
    }

    public List<TeamRequest> getPendingRequestsForLeader(String leaderId) {
        return queryRequests(REQ_SELECT
                + "WHERE t.leader_id = ? AND r.status = 'PENDING' ORDER BY r.request_id", leaderId);
    }

    public List<TeamRequest> getRequestsByStudent(String studentId) {
        return queryRequests(REQ_SELECT
                + "WHERE r.student_id = ? ORDER BY r.request_id DESC", studentId);
    }

    private List<TeamRequest> queryRequests(String sql, String param) {
        List<TeamRequest> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Request query error: " + e.getMessage());
        }
        return list;
    }

    // Adds the student to the team and marks the request ACCEPTED. Returns "OK" or an error message.
    public String acceptRequest(int requestId) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int teamId;
                String studentId;
                String status;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT team_id, student_id, status FROM team_requests WHERE request_id = ?")) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "Request not found.";
                        }
                        teamId = rs.getInt("team_id");
                        studentId = rs.getString("student_id");
                        status = rs.getString("status");
                    }
                }
                if (!"PENDING".equals(status)) {
                    con.rollback();
                    return "This request has already been handled.";
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT (SELECT COUNT(*) FROM team_members WHERE team_id = ?) AS cnt, p.max_team_size "
                      + "FROM teams t JOIN projects p ON t.project_id = p.project_id WHERE t.team_id = ?")) {
                    ps.setInt(1, teamId);
                    ps.setInt(2, teamId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt("cnt") >= rs.getInt("max_team_size")) {
                            con.rollback();
                            return "The team is full. Cannot accept more members.";
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO team_members (team_id, student_id) VALUES (?, ?)")) {
                    ps.setInt(1, teamId);
                    ps.setString(2, studentId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE team_requests SET status = 'ACCEPTED' WHERE request_id = ?")) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }

                con.commit();
                return "OK";
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("acceptRequest error: " + e.getMessage());
            return "Database error while accepting the request.";
        }
    }

    public boolean rejectRequest(int requestId) {
        String sql = "UPDATE team_requests SET status = 'REJECTED' WHERE request_id = ? AND status = 'PENDING'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("rejectRequest error: " + e.getMessage());
            return false;
        }
    }
}