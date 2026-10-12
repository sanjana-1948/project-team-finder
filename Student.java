package model;

public class Student {
    private String studentId;
    private String name;
    private String email;
    private String password;
    private String branch;
    private int year;
    private String skills;
    private String interests;
    private String availability;

    public Student() {}

    public Student(String studentId, String name, String email, String password,
                   String branch, int year, String skills, String interests, String availability) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.branch = branch;
        this.year = year;
        this.skills = skills;
        this.interests = interests;
        this.availability = availability;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override
    public String toString() {
        return name + " (" + studentId + ")";
    }
}