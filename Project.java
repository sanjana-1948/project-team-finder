package model;

public class Project {
    private String projectId;
    private String projectName;
    private String description;
    private String requiredSkills;
    private int maxTeamSize;
    private String domain;
    private String createdBy;

    public Project() {}

    public Project(String projectId, String projectName, String description,
                   String requiredSkills, int maxTeamSize, String domain, String createdBy) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.maxTeamSize = maxTeamSize;
        this.domain = domain;
        this.createdBy = createdBy;
    }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public int getMaxTeamSize() { return maxTeamSize; }
    public void setMaxTeamSize(int maxTeamSize) { this.maxTeamSize = maxTeamSize; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @Override
    public String toString() {
        return projectName + " (" + projectId + ")";
    }
}