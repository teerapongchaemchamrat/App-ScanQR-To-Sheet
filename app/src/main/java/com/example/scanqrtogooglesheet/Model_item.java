package com.example.scanqrtogooglesheet;

public class Model_item {
    private String Part_No;
    private String Part_Name;
    private String Process;
    private String PRO_Name;

    public Model_item(String Part_No, String Part_Name, String Process, String PRO_Name){
        this.Part_No = Part_No;
        this.Part_Name = Part_Name;
        this.Process = Process;
        this.PRO_Name = PRO_Name;
    }

    public String getPart_No() {
        return Part_No;
    }

    public void setPart_No(String part_No) {
        Part_No = part_No;
    }

    public String getPart_Name() {
        return Part_Name;
    }

    public void setPart_Name(String part_Name) {
        Part_Name = part_Name;
    }

    public String getProcess() {
        return Process;
    }

    public void setProcess(String process) {
        Process = process;
    }

    public String getPRO_Name() {
        return PRO_Name;
    }

    public void setPRO_Name(String PRO_Name) {
        this.PRO_Name = PRO_Name;
    }
}
