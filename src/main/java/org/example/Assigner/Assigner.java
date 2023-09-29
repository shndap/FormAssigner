package org.example.Assigner;

import org.example.Assigner.BPMNLoader.BPMNLoader;
import org.example.Assigner.Form.Form;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;

public class Assigner {
    private Document bpmn;
    private final String bpmnPath;
    private boolean isOkay;

    public Assigner(String bpmnPath) {
        this.bpmnPath = bpmnPath;
        try {
            bpmn = BPMNLoader.getBPMN(bpmnPath);
            isOkay = true;
        } catch (Exception e) {
            isOkay = false;
        }
    }

    public void assignFormToID(String ID, Form form) {
        assert isOkay;

        if (!appendForm(form)) {
            System.out.println("form failed");
            return;
        }
        if (!assignToID(ID, form)) {
            System.out.println("Assignment failed");
        }
    }

    private boolean assignToID(String ID, Form form) {
        Node task = getUserTaskByID(ID);
        if (task == null) return false;
        Node elements = getElements(task);
        Element formNode = bpmn.createElement("zeebe:formDefinition");
        formNode.setAttribute("formKey", "camunda-forms:bpmn:" + form.getID());
        elements.appendChild(formNode);
        return true;
    }

    private Node getElements(Node task) {
        for (int i = 0; i < task.getChildNodes().getLength(); i++)
            if (task.getChildNodes().item(i).getNodeName().equals("bpmn:extensionElements")) {
                Node item = task.getChildNodes().item(i);
                if (item.hasChildNodes())
                    item.removeChild(item.getFirstChild());
                return item;
            }

        Element elements = bpmn.createElement("bpmn:extensionElements");
        task.appendChild(elements);
        return elements;
    }

    private boolean appendForm(Form form) {
        Node extensionElements = getExtensionElements();
        if (extensionElements == null) return false;

        if (!hasForm(extensionElements.getChildNodes(), form))
            extensionElements.insertBefore(form.getNode(bpmn), extensionElements.getFirstChild());
        return true;
    }

    private boolean hasForm(NodeList list, Form form) {
        for (int i = 0; i < list.getLength(); i++) {
            Element item = (Element) list.item(i);
            if (item.hasAttribute("id") && item.getAttribute("id").equals(form.getID())) {
                return true;
            }
        }

        return false;
    }

    private Node getExtensionElements() {
        NodeList rootChildNodes = getRootChildNodes();

        for (int i = 0; i < rootChildNodes.getLength(); i++) {
            Node node = rootChildNodes.item(i);
            if (node.getNodeName().equals("bpmn:process")) {
                return getNode(node);
            }
        }

        return null;
    }

    private NodeList getRootChildNodes() {
        Element root = bpmn.getDocumentElement();
        return root.getChildNodes();
    }

    private Node getNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
            if (childNodes.item(j).getNodeName().equals("bpmn:extensionElements"))
                return childNodes.item(j);

        Element extensionElements = bpmn.createElement("bpmn:extensionElements");
        node.insertBefore(extensionElements, node.getFirstChild());
        return extensionElements;
    }

    private Node getUserTaskByID(String ID) {
        NodeList rootChildNodes = getRootChildNodes();
        for (int i = 0; i < rootChildNodes.getLength(); i++) {
            Node node = rootChildNodes.item(i);
            if (node.getNodeName().equals("bpmn:process")) {
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Element item = getElement(ID, childNodes, j);
                    if (item != null) return item;
                }
            }
        }

        return null;
    }

    private static Element getElement(String ID, NodeList childNodes, int j) {
        try {
            Element item = (Element) childNodes.item(j);
            if (item.getNodeName().equals("bpmn:userTask") &&
                    item.hasAttribute("id") &&
                    item.getAttribute("id").equals(ID)) {
                return item;
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public void saveAs(String path) {
        assert isOkay;

        try (FileOutputStream output = new FileOutputStream(path)) {
            writeXml(output);
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        saveAs(bpmnPath);
    }

    private void writeXml(FileOutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(bpmn);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }

    public boolean isOkay() {
        return isOkay;
    }
}
