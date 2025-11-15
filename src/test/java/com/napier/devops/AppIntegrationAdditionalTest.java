package com.napier.devops;

import com.napier.sem.App;
import com.napier.sem.Department;
import com.napier.sem.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppIntegrationAdditionalTest {

    static App app;

    @BeforeAll
    static void init() {
        app = new App();
        // connect to the same test DB mapping used elsewhere
        app.connect("localhost:33061", 30000);
    }

    @Test
    void testGetDepartmentNotFound() {
        Department d = app.getDepartment("NON_EXISTENT_DEPT_12345");
        assertNull(d, "Expected no department for a non-existent name");
    }

    @Test
    void testGetAllSalariesNonEmpty() {
        List<Employee> salaries = app.getAllSalaries();
        assertNotNull(salaries, "Salaries list should not be null");
        assertFalse(salaries.isEmpty(), "Expected at least one current salary in the test DB");
        Employee e = salaries.get(0);
        assertTrue(e.salary > 0, "Employee salary should be positive");
    }

    @Test
    void testGetEmployeeInvalidId() {
        Employee emp = app.getEmployee(-1);
        assertNull(emp, "Expected null for an invalid employee id");
    }
}
