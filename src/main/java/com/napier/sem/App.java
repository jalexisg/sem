package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        // Create new Application
        App a = new App();

        // Connect to database
        if(args.length < 1){
            a.connect("localhost:33061", 0);
        }else{
            a.connect("db:3306", 30000);
        }

        // Test department lookup and salaries-by-department
        // Change the name below to a department that exists in your DB (e.g. "Sales").
        String testDeptName = "Sales";
        Department dept = a.getDepartment(testDeptName);
        if (dept == null) {
            System.out.println("Department '" + testDeptName + "' not found.");
        } else {
            System.out.println("Found department: " + dept.dept_no + " - " + dept.dept_name);

            ArrayList<Employee> deptEmployees = a.getSalariesByDepartment(dept);
            System.out.println("Employees in department '" + dept.dept_name + "': " + deptEmployees.size());

            // Print first up to 10 employees as a quick check
            int limit = Math.min(10, deptEmployees.size());
            for (int i = 0; i < limit; i++) {
                Employee e = deptEmployees.get(i);
                System.out.println(String.format("%d: %s %s (salary=%d)", e.emp_no, e.first_name, e.last_name, e.salary));
            }
        }

        // Disconnect from database
        a.disconnect();
    }

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect(String location, int delay) {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(delay);
                // Connect to database
                String dbUser = System.getenv("MYSQL_USER");
                String dbPassword = System.getenv("MYSQL_PASSWORD");

                if (dbUser == null || dbUser.isEmpty()) {
                    dbUser = "root";
                }
                if (dbPassword == null) {
                    dbPassword = "example";
                }

                con = DriverManager.getConnection("jdbc:mysql://" + location
                                + "/employees?allowPublicKeyRetrieval=true&useSSL=false",
                        dbUser, dbPassword);
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " +                                  Integer.toString(i));
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Expose connection for helper methods (minimal change to integrate Employee.getAllSalaries).
     */
    public Connection getCon() {
        return con;
    }

    public Employee getEmployee(int ID) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT emp_no, first_name, last_name "
                            + "FROM employees "
                            + "WHERE emp_no = " + ID;
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                return emp;
            } else
                return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * Gets all the current employees and salaries using this app's connection.
     * Prints a simple table to stdout and returns the list.
     */
    public ArrayList<Employee> getAllSalaries()
    {
        ArrayList<Employee> employees = new ArrayList<>();

        if (con == null)
        {
            System.err.println("No database connection");
            return employees;
        }

        String strSelect =
                "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                        + "FROM employees e JOIN salaries s ON e.emp_no = s.emp_no "
                        + "WHERE s.to_date = '9999-01-01' "
                        + "ORDER BY e.emp_no ASC";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(strSelect))
        {
            while (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.salary = rset.getInt("salary");
                employees.add(emp);
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to get salary details: " + e.getMessage());
        }

        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list and print
        for (Employee emp : employees)
        {
            String emp_string = String.format("%-10s %-15s %-20s %-8s",
                    emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }

        return employees;
    }

    /**
     * Return a Department by its name.
     * Uses a PreparedStatement to avoid SQL injection.
     */
    public Department getDepartment(String dept_name)
    {
        if (con == null)
        {
            System.err.println("No database connection");
            return null;
        }

        String sql = "SELECT dept_no, dept_name FROM departments WHERE dept_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, dept_name);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    Department d = new Department();
                    d.dept_no = rs.getString("dept_no");
                    d.dept_name = rs.getString("dept_name");
                    return d;
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to get department: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get current salaries (to_date = '9999-01-01') for employees in a given department.
     * Returns an empty list if none found or on error.
     */
    public ArrayList<Employee> getSalariesByDepartment(Department dept)
    {
        ArrayList<Employee> employees = new ArrayList<>();

        if (dept == null)
        {
            System.err.println("Department is null");
            return employees;
        }

        if (con == null)
        {
            System.err.println("No database connection");
            return employees;
        }

        String sql =
                "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                        + "FROM employees e "
                        + "JOIN dept_emp de ON e.emp_no = de.emp_no "
                        + "JOIN salaries s ON e.emp_no = s.emp_no "
                        + "JOIN departments d ON de.dept_no = d.dept_no "
                        + "WHERE d.dept_no = ? AND de.to_date = '9999-01-01' AND s.to_date = '9999-01-01' "
                        + "ORDER BY e.emp_no ASC";

        try (PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, dept.dept_no);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Employee emp = new Employee();
                    emp.emp_no = rs.getInt("emp_no");
                    emp.first_name = rs.getString("first_name");
                    emp.last_name = rs.getString("last_name");
                    emp.salary = rs.getInt("salary");
                    employees.add(emp);
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to get salaries by department: " + e.getMessage());
        }

        return employees;
    }

    public void displayEmployee(Employee emp) {
        if (emp != null) {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }

    public void printSalaries(ArrayList<Employee> employees)
    {
        // Check employees is not null
        if (employees == null)
        {
            System.out.println("No employees");
            return;
        }
        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list
        for (Employee emp : employees)
        {
            if (emp == null)
                continue;
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    public void addEmployee(Employee emp)
    {
        try
        {
            Statement stmt = con.createStatement();
            String strUpdate =
                    "INSERT INTO employees (emp_no, first_name, last_name, birth_date, gender, hire_date) " +
                            "VALUES (" + emp.emp_no + ", '" + emp.first_name + "', '" + emp.last_name + "', " +
                            "'9999-01-01', 'M', '9999-01-01')";
            stmt.execute(strUpdate);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to add employee");
        }
    }
}