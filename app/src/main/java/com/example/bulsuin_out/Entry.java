package com.example.bulsuin_out;

public class Entry {
    private String name;
    private String role;
    private String departmentOrPurpose;
    private String vehicleModel;

    public Entry(String name, String role, String department, String plate) {
        this.name = name;
        this.role = role;
        this.departmentOrPurpose = department;
        this.vehicleModel = plate;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getDepartmentOrPurpose() {
        return departmentOrPurpose;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }
}

