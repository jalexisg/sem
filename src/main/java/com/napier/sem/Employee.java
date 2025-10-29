package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
* Gets all the current employees and salaries.
* @return A list of all employees and salaries, or null if there is an error.
*/
public class Employee
{
    public int emp_no;
    public String first_name;
    public String last_name;
    public String title;
    public String dept_name;
    public String manager;
    public int salary;

    /**
     * Gets all the current employees and salaries.
     * @param con Connection to DB
     * @return A list of all employees and salaries (empty list on error)
     */
    public static ArrayList<Employee> getAllSalaries(Connection con)
    {
        ArrayList<Employee> employees = new ArrayList<>();
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
        catch (Exception e)
        {
            System.err.println("Failed to get salary details: " + e.getMessage());
        }
        return employees;
    }
}