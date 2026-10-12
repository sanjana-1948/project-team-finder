# Project Team Finder

A Java desktop application that helps college students find teammates for projects.
Students can register, create projects, search for teammates by skill, form teams,
send and accept join requests, and manage their team.

## Technologies
- Java (Core, OOP, Collections, Exception Handling)
- Java Swing (GUI)
- JDBC + MySQL (database)
- VS Code

## Features
- Student registration and login
- Dashboard and profile editing
- Create projects with required skills
- Find teammates by skill, branch, year and interest
- Create a team, send join requests, accept or reject requests, leave a team

## Project structure
```
project_team_finder/
├── src/
│   ├── Main.java            (starts the app)
│   ├── db/DBConnection.java (MySQL connection)
│   ├── model/               (Student, Project, Team, TeamRequest)
│   ├── dao/                 (database operations)
│   └── gui/                 (Swing windows)
├── database/database.sql    (tables + sample data)
└── lib/                     (mysql-connector-j jar)
```

## How the layers connect
GUI -> DAO -> DBConnection -> MySQL. The GUI never writes SQL; every query lives in a DAO class.

## How to run
1. Install JDK 17 or newer and MySQL.
2. Run `database/database.sql` in MySQL to create the `project_team_finder` database.
3. Download `mysql-connector-j` and place the jar inside `lib/`.
4. Open `src/db/DBConnection.java` and set your MySQL username and password.
5. Compile and run from the project root (Windows):
```
dir /s /b src\*.java > sources.txt
javac -d bin -cp "lib/*" @sources.txt
java -cp "bin;lib/*" Main
```

## Database tables
students, projects, teams, team_members, team_requests

## Author
Your Name, Your College, Year
