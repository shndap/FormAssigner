package org.example.Assigner.Form;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Form {
    private final String path;

    public Form(String path) {
        this.path = path;
    }

    public String getJson() {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader buffer = new BufferedReader(new FileReader(path))) {
            String str;
            while ((str = buffer.readLine()) != null)
                builder.append(str).append("\n");
        } catch (IOException e) {
            return null;
        }

        return builder.toString();
    }

    public Node getNode(Document doc) {
        Element element = doc.createElement("zeebe:userTaskForm");
        element.setAttribute("id", getID());
        Text text = doc.createTextNode(getJson());
        element.appendChild(text);
        return element;
    }

    public String getID() {
        return "form_" + Integer.toHexString(this.hashCode()).toUpperCase();
    }
}
