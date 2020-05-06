package com.heng.ku.lib.tree;


import java.util.ArrayList;
import java.util.List;

public class NodePrintTool {

    private static List<ArrayList<Node>> allLines = new ArrayList<>();
    private static List<Node> allNodes = new ArrayList<>();

    public static void print(Node root) {
        //分析 获取元素的行和列
        allLinesNode(root, 0);
        // 从最左侧元素开始  最左侧 父节点 右节点的最左侧 父节点 右焦点的最左侧
        getNode(root);
        for (ArrayList<Node> ls : allLines) {
            StringBuilder sb = new StringBuilder();
            for (Node n : ls) {
                int length = sb.length();
                for (int i = 0; i < allNodes.indexOf(n) * 3 - length + 3; i++) {
                    sb.append(" ");
                }
                sb.append(n);
            }
            System.out.println(sb);
        }
        allLines.clear();
        allNodes.clear();
    }

    private static void getNode(Node node) {
        if (node == null) return;
        getNode(node.left);
        allNodes.add(node);
        getNode(node.right);
    }

    private static void allLinesNode(Node node, int lines) {
        if (node == null) return;
        ArrayList<Node> line;
        if (allLines.size() > lines) {
            line = allLines.get(lines);
        } else {
            line = new ArrayList<>();
            allLines.add(line);
        }
        line.add(node);
        lines++;
        allLinesNode(node.left, lines);
        allLinesNode(node.right, lines);

    }

}
