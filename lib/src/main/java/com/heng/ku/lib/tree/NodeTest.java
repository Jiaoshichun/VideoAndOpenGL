package com.heng.ku.lib.tree;

public class NodeTest {
    public static void main(String[] args) {
        Node root = new Node(13);
        Node node8 = new Node(8);
        Node.putNode(root, node8);
        Node.putNode(root, new Node(15));
        Node.putNode(root, new Node(1));
        Node.putNode(root, new Node(11));
        Node.putNode(root, new Node(25));
        Node node17 = new Node(17);
        Node.putNode(root, node17);
        Node.putNode(root, new Node(22));
        Node.putNode(root, new Node(27));
        Node.putNode(root, new Node(4));
        Node.putNode(root, new Node(10));
        Node.putNode(root, new Node(21));
        Node.putNode(root, new Node(18));
        Node.putNode(root, new Node(23));
        Node.putNode(root, new Node(5));
        Node.putNode(root, new Node(7));
        Node.putNode(root, new Node(0));
        Node.putNode(root, new Node(-1));
        Node.putNode(root, new Node(9));

        NodePrintTool.print(root);
        System.out.println("------------------");
        Node.delete(root, node8);
        NodePrintTool.print(root);
    }
}
