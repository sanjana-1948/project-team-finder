package model;

public class Team {
    private int teamId;
    private String teamName;
    private String projectId;
    private String leaderId;

    // Extra fields filled by DAO queries, used for display
    private String projectName;
    private int memberCount;
    private int maxTeamSize;

    public Team() {}

    public Team(int teamId, String teamName, String projectId, String leaderId) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.projectId = projectId;
        this.leaderId = leaderId;
    }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public int getMaxTeamSize() { return maxTeamSize; }
    public void setMaxTeamSize(int maxTeamSize) { this.maxTeamSize = maxTeamSize; }

    @Override
    public String toString() {
        return teamName + " (#" + teamId + ")";
    }
}