package com.example.scanqrtogooglesheet;

public class Model_emp {
    private String EmployeeID;
    private String Name;


    public Model_emp(String EmployeeID, String Name){
        this.EmployeeID = EmployeeID;
        this.Name = Name;

    }

    public String getEmployeeID() {
        return EmployeeID;
    }

    public void setEmployeeID(String employeeID) {
        EmployeeID = employeeID;
    }



    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

}
