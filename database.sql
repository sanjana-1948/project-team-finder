-- =====================================================
-- Project Team Finder - Database Script
-- =====================================================
DROP DATABASE IF EXISTS project_team_finder;
CREATE DATABASE project_team_finder;
USE project_team_finder;

-- ---------- STUDENTS ----------
CREATE TABLE students (
    student_id   VARCHAR(20)  PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    branch       VARCHAR(50)  NOT NULL,
    year         INT          NOT NULL,
    skills       VARCHAR(255),
    interests    VARCHAR(255),
    availability VARCHAR(50)  DEFAULT 'Available'
);

-- ---------- PROJECTS ----------
CREATE TABLE projects (
    project_id      VARCHAR(20)  PRIMARY KEY,
    project_name    VARCHAR(150) NOT NULL,
    description     TEXT,
    required_skills VARCHAR(255),
    max_team_size   INT          NOT NULL DEFAULT 4,
    domain          VARCHAR(100),
    created_by      VARCHAR(20)  NOT NULL,
    FOREIGN KEY (created_by) REFERENCES students(student_id)
        ON DELETE CASCADE
);

-- ---------- TEAMS ----------
CREATE TABLE teams (
    team_id    INT AUTO_INCREMENT PRIMARY KEY,
    team_name  VARCHAR(100) NOT NULL,
    project_id VARCHAR(20)  NOT NULL,
    leader_id  VARCHAR(20)  NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
        ON DELETE CASCADE,
    FOREIGN KEY (leader_id) REFERENCES students(student_id)
        ON DELETE CASCADE
);

-- ---------- TEAM MEMBERS ----------
CREATE TABLE team_members (
    team_id    INT         NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    PRIMARY KEY (team_id, student_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
        ON DELETE CASCADE
);

-- ---------- TEAM REQUESTS ----------
CREATE TABLE team_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id    INT         NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, ACCEPTED, REJECTED
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
        ON DELETE CASCADE
);

-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- 12 students (all passwords are shown plainly for testing)
INSERT INTO students (student_id, name, email, password, branch, year, skills, interests, availability) VALUES
('S101', 'Aarav Sharma',  'aarav@college.edu',  'pass123', 'CSE',  3, 'Java, SQL, Swing',               'Desktop Apps, Databases',   'Available'),
('S102', 'Priya Reddy',   'priya@college.edu',  'pass123', 'CSE',  2, 'Python, Machine Learning',       'AI, Data Science',          'Available'),
('S103', 'Rohan Mehta',   'rohan@college.edu',  'pass123', 'IT',   3, 'HTML, CSS, JavaScript, React',   'Web Development',           'Available'),
('S104', 'Sneha Iyer',    'sneha@college.edu',  'pass123', 'ECE',  4, 'Embedded C, Arduino, IoT',       'IoT, Robotics',             'Busy'),
('S105', 'Karthik Rao',   'karthik@college.edu','pass123', 'CSE',  3, 'Java, Spring, MySQL',            'Backend, Web Development',  'Available'),
('S106', 'Ananya Gupta',  'ananya@college.edu', 'pass123', 'IT',   2, 'UI Design, Figma, HTML',         'UI/UX, Mobile Apps',        'Available'),
('S107', 'Vikram Singh',  'vikram@college.edu', 'pass123', 'EEE',  4, 'MATLAB, Circuit Design, C',      'Smart Grid, Robotics',      'Available'),
('S108', 'Meera Nair',    'meera@college.edu',  'pass123', 'CSE',  4, 'Python, Django, SQL',            'Web Development, AI',       'Available'),
('S109', 'Arjun Patel',   'arjun@college.edu',  'pass123', 'MECH', 3, 'AutoCAD, SolidWorks, Arduino',   'Robotics, Automation',      'Available'),
('S110', 'Divya Menon',   'divya@college.edu',  'pass123', 'IT',   3, 'Android, Kotlin, Firebase',      'Mobile Apps',               'Available'),
('S111', 'Rahul Verma',   'rahul@college.edu',  'pass123', 'CSE',  2, 'C++, DSA, Java',                 'Competitive Programming',   'Available'),
('S112', 'Nisha Kapoor',  'nisha@college.edu',  'pass123', 'ECE',  3, 'Python, OpenCV, IoT',            'Computer Vision, IoT',      'Available');

-- 6 projects
INSERT INTO projects (project_id, project_name, description, required_skills, max_team_size, domain, created_by) VALUES
('P001', 'Library Management System',  'Desktop app to manage books, members and issue/return records.', 'Java, SQL, Swing',               4, 'Desktop Application', 'S101'),
('P002', 'Smart Attendance System',    'Face recognition based attendance marking for classrooms.',      'Python, OpenCV, Machine Learning', 4, 'Artificial Intelligence', 'S102'),
('P003', 'Campus Event Portal',        'Website for announcing and registering for college events.',     'HTML, CSS, JavaScript, React',     5, 'Web Development',     'S103'),
('P004', 'Smart Irrigation System',    'IoT system that waters plants based on soil moisture.',          'Arduino, IoT, Embedded C',         4, 'Internet of Things',  'S104'),
('P005', 'Student Expense Tracker',    'Android app to track daily student expenses with charts.',       'Android, Kotlin, Firebase',        3, 'Mobile Application',  'S110'),
('P006', 'Line Following Robot',       'Autonomous robot that follows a path using sensors.',            'Arduino, Circuit Design, C',       4, 'Robotics',            'S107');

-- Teams (one per project)
INSERT INTO teams (team_name, project_id, leader_id) VALUES
('Library Builders', 'P001', 'S101'),
('Vision Squad',     'P002', 'S102'),
('Web Wizards',      'P003', 'S103'),
('Green Thumbs',     'P004', 'S104'),
('Mobile Masters',   'P005', 'S110');

-- Team members (leaders are members of their own team)
INSERT INTO team_members (team_id, student_id) VALUES
(1, 'S101'), (1, 'S105'),
(2, 'S102'), (2, 'S112'),
(3, 'S103'), (3, 'S106'),
(4, 'S104'),
(5, 'S110');

-- Sample requests
INSERT INTO team_requests (team_id, student_id, status) VALUES
(1, 'S111', 'PENDING'),
(2, 'S108', 'PENDING'),
(3, 'S108', 'REJECTED'),
(4, 'S109', 'PENDING');