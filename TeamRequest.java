package model;

public class TeamRequest {
    private int requestId;
    private int teamId;
    private String studentId;
    private String status; // PENDING, ACCEPTED, REJECTED

    // Extra fields filled by DAO queries, used for display
    private String studentName;
    private String teamName;

    public TeamRequest() {}

    public TeamRequest(int requestId, int teamId, String studentId, String status) {
        this.requestId = requestId;
        this.teamId = teamId;
        this.studentId = studentId;
        this.status = status;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
}