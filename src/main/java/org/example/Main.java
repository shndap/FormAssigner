package org.example;

import org.example.Assigner.Assigner;
import org.example.Assigner.Form.Form;

public class Main {
    public static void main(String[] args) {
        try {
            String bpmnPath = "src/main/resources/Formless.bpmn";
            String ID = "task_1";
            String formPath = "src/main/resources/Form1.form";
            String savePath = "src/main/resources/formed.bpmn";
            Form form = new Form(formPath);
            Assigner assigner = new Assigner(bpmnPath);
            assigner.assignFormToID(ID, form);
            assigner.assignFormToID("Activity_0f0fuap", form);
            assigner.saveAs(savePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ridi");
        }
        System.out.println("Done");
    }
}