package com.napier.devops;

import com.napier.sem.App;
import com.napier.sem.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppIntegrationTest
{
    static App app;

    @BeforeAll
    static void init()
    {
        app = new App();
        app.connect("localhost:33060", 30000);
    }

    @Test
    void testGetEmployee()
    {
        Employee emp = app.getEmployee(255530);
        assertNotNull(emp, "Expected employee 255530 to be present in the test DB");
        assertEquals(255530, emp.emp_no);
        assertEquals("Ronghao", emp.first_name);
        assertEquals("Garigliano", emp.last_name);
    }

    @Test
    void testAddEmployee()
    {
        Employee emp = new Employee();
        emp.emp_no = 500000;
        emp.first_name = "Kevin";
        emp.last_name = "Chalmers";
        try {
            // Ensure clean state: remove any pre-existing test employee with same id
            try {
                app.getCon().createStatement().executeUpdate("DELETE FROM employees WHERE emp_no = 500000");
            } catch (Exception ignored) {}

            app.addEmployee(emp);
            emp = app.getEmployee(500000);
            assertNotNull(emp, "Expected newly added employee to be present");
            assertEquals(500000, emp.emp_no);
            assertEquals("Kevin", emp.first_name);
            assertEquals("Chalmers", emp.last_name);
        } finally {
            // Clean up test data so repeated runs stay deterministic
            try {
                app.getCon().createStatement().executeUpdate("DELETE FROM employees WHERE emp_no = 500000");
            } catch (Exception ignored) {}
        }
    }
}