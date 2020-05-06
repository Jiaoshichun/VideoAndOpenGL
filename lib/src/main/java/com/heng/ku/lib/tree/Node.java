package com.heng.ku.lib.tree;

/**
 * 红黑色
 * https://github.com/chun2012/The-Art-Of-Programming-By-July/blob/master/ebook/zh/03.01.md
 * 红黑树特性
 * 1）每个结点要么是红的，要么是黑的。
 * 2）根结点是黑的。
 * 3）每个叶结点（叶结点即指树尾端NIL指针或NULL结点）是黑的。
 * 4）如果一个结点是红的，那么它的俩个儿子都是黑的。
 * 5）对于任一结点而言，其到叶结点树尾端NIL指针的每一条路径都包含相同数目的黑结点。
 * <p>
 * 二叉树特性
 * 若任意结点的左子树不空，则左子树上所有结点的值均小于它的根结点的值；
 * 若任意结点的右子树不空，则右子树上所有结点的值均大于它的根结点的值；
 * 任意结点的左、右子树也分别为二叉查找树。
 * 没有键值相等的结点（no duplicate nodes）。
 */
public class Node {
    public int value;
    public Node left;
    public Node right;
    public Node parent;
    public boolean red = true;

    public Node(int value) {
        this.value = value;
    }

    /**
     * 获取根节点
     */
    public static Node root(Node node) {
        for (Node a = node, p; ; ) {
            if ((p = a.parent) == null) {
                return a;
            }
            a = p;
        }
    }

    /**
     * 插入数据
     */
    public static Node putNode(Node root, Node node) {
        if (root.red) root.red = false;
        Node p = null;
        Node c = root;
        while (c != null) {
            p = c;
            if (node.value < c.value) {
                c = c.left;
            } else if (node.value > c.value) {
                c = c.right;
            } else {
                return root;
            }
        }
        node.parent = p;
        if (p != null) {
            if (node.value > p.value) {
                p.right = node;
            } else {
                p.left = node;
            }
        } else {
            root = node;
        }

        return redBlackFix(root, node);
    }

    /**
     * 添加完元素后，修复红黑树，使之平衡
     *
     * @param root 根节点
     * @param node 添加的节点
     * @return 返回根节点
     * <p>
     * <p>
     * 1. 添加了一个4 默认是红色 |   2. 如果父节点和叔叔节点都是红色
     * | 父、叔节点 变黑 祖父节点变黑,当前节点指向祖父
     * --------------11(B)          |               11(B)
     * ----------/         \        |             /       \
     * --------2(R)         14(B)   |           2(R)         14(B)
     * ------/     \          \     |       /     \          \
     * --- 1(B)    7(B)       15(R) |     1(B)   >7(R)       15(R)
     * -----------/   \             |             /   \
     * ---------5(R)  8(R)          |           5(B)  8(B)
     * --------/                    |          /
     * ----->4(R)                   |        4(R)
     * -------------------------------------------------------------------------
     * 3.父节点红，叔叔节点黑，父是祖父|  4.父节点红，叔叔节点黑，父是祖父
     * -的左子节点，指向父节点，左旋   |   的左子节点，父变黑，祖父变红，
     * --------------------------------|   当前指向祖父，右旋
     * -----------------11(B)          |               >11(R)                           7(B)
     * -------------/         \        |             /       \                       /       \
     * ----------7(R)         14(B)    |           7(B)         14(B)              2(R)       >11(R)
     * ---------/     \          \     |       /     \           \              /     \       /    \
     * ------>2(R)   8(B)       15(R)  |     2(R)   8(B)        15(R)         1(B)    5(B)   8(B)  14(B)
     * ------/    \                    |    /   \                                     /              \
     * ----1(B)   5(B)                 |  1(B)  5(B)                                 4(R)            15(R)
     * --------- /                     |          /
     * --------4(R)                    |        4(R)
     */
    private static Node redBlackFix(Node root, Node node) {
        if (node.parent == null) {
            node.red = false;
            return root;
        } else if (!node.parent.red) {//如果父节点是黑色的话 不影响
            return root;
        }

        Node c = node;
        while (c.parent != null && c.parent.red) {
            if (c.parent == c.parent.parent.left) {//当前节点的父节点 是祖父节点的左节点
                if (c.parent.parent.right != null && c.parent.parent.right.red) {//如果叔叔节点是红色的
                    c.parent.red = false;//父节点变黑
                    c.parent.parent.right.red = false;//叔叔节点变黑
                    c.parent.parent.red = true;//祖父变红
                    c = c.parent.parent;//当前节点指到 祖父节点
                } else if (c == c.parent.right) {//如果当前节点是父节点的右子节点 并且叔叔节点是黑色的
                    c = c.parent;//将节点指向父节点
                    rotateLeft(root, c); //左转
                } else {//如果当前节点是父节点的左子节点 并且叔叔节点是黑色的
                    c.parent.red = false; //父节点变黑
                    c.parent.parent.red = true;//祖父节点变红
                    c = c.parent.parent;//当前节点 指向祖父节点
                    rotateRight(root, c);//右旋
                }
            } else {//当前节点的父节点 是祖父节点的右节点
                if (c.parent.parent.left != null && c.parent.parent.left.red) {//如果叔叔节点是红色的
                    c.parent.red = false;//父节点变黑
                    c.parent.parent.left.red = false;//叔叔节点变黑
                    c.parent.parent.red = true;//祖父变红
                    c = c.parent.parent;//当前节点指到 祖父节点
                } else if (c == c.parent.left) {//如果当前节点是父节点的左子节点 并且叔叔节点是黑色的
                    c = c.parent;//将节点指向父节点
                    rotateRight(root, c); //右转
                } else {//如果当前节点是父节点的右子节点 并且叔叔节点是黑色的
                    c.parent.red = false; //父节点变黑
                    c.parent.parent.red = true;//祖父节点变红
                    c = c.parent.parent;//当前节点 指向祖父节点
                    rotateLeft(root, c);//左旋
                }
            }
        }
        root.red = false;
        return root;
    }

    /**
     * 删除指定节点
     *
     * @param root 节点的根布局
     * @param node 要删除的节点
     */
    public static Node delete(Node root, Node node) {
        Node n = null;
        if (node.left == null || node.right == null) {
            if (node.left != null) {
                node.left.parent = node.parent;
                n = node.left;
            } else if (node.right != null) {
                //如果左边为空 右边不为空 则用它唯一的右节点替代他的位置
                node.right.parent = node.parent;
                n = node.right;
            }
        } else {
            //获取左边做大的数
            n = findMax(node.left);
            if (n != node.left) {
                n.parent.right = null;
                n.left = node.left;
                n.right = node.right;
            }
        }
        if (n != null) {
            n.parent = node.parent;
        }
        //将要删除的父节点与新的节点关联
        if (node.parent != null) {
            if (node == node.parent.left) {
                node.parent.left = n;
            } else {
                node.parent.right = n;
            }
        } else {
            root = n;
        }
        return deleteFix(root, n, node.red);
    }

    /**
     * 删除元素后进行修复
     *
     * @param root 根节点
     * @param n    删除后 替代该节点的元素
     * @param red  该节点原来的颜色
     *             1 while x ≠ root[T] and color[x] = BLACK
     *             2     do if x = left[p[x]]
     *             3           then w ← right[p[x]]
     *             4                if color[w] = RED
     *             5                   then color[w] ← BLACK                        ▹  Case 1
     *             6                        color[p[x]] ← RED                       ▹  Case 1
     *             7                        LEFT-ROTATE(T, p[x])                    ▹  Case 1
     *             8                        w ← right[p[x]]                         ▹  Case 1
     *             9                if color[left[w]] = BLACK and color[right[w]] = BLACK
     *             10                   then color[w] ← RED                          ▹  Case 2
     *             11                        x ← p[x]                                ▹  Case 2
     *             12                   else if color[right[w]] = BLACK
     *             13                           then color[left[w]] ← BLACK          ▹  Case 3
     *             14                                color[w] ← RED                  ▹  Case 3
     *             15                                RIGHT-ROTATE(T, w)              ▹  Case 3
     *             16                                w ← right[p[x]]                 ▹  Case 3
     *             17                         color[w] ← color[p[x]]                 ▹  Case 4
     *             18                         color[p[x]] ← BLACK                    ▹  Case 4
     *             19                         color[right[w]] ← BLACK                ▹  Case 4
     *             20                         LEFT-ROTATE(T, p[x])                   ▹  Case 4
     *             21                         x ← root[T]                            ▹  Case 4
     *             22        else (same as then clause with "right" and "left" exchanged)
     */
    private static Node deleteFix(Node root, Node n, boolean red) {
        //如果原来是红色不需要做处理  或者是黑色，但是删除的是根节点 也不需要处理
        //如果节点是唯一分支 也不需要处理
        if (red || n == root || n.left == null || n.right == null) return root;
        //如果 当前节点是红+黑 直接染黑即可
        if (n.red) n.red = false;
        Node w;//兄弟节点
        while (n != root && !n.red) {
            if (n == n.parent.left) {//当前节点是左子节点
                w = n.parent.right;
                if (w.red) {
                    //删除修复情况1：当前结点是黑+黑且兄弟结点为红色(此时父结点和兄弟结点的子结点分为黑)。
                    //如果兄弟节点是红的 则将 兄弟节点 变黑  父节点变红，以父节点为支点左旋
                    n.parent.red = true;
                    w.red = false;
                    rotateLeft(root, n.parent);
                    w = n.parent.right;
                }
                if (!((w.left != null && w.left.red) || (w.right != null && w.right.red))) {
                    //删除修复情况2：当前结点是黑加黑且兄弟是黑色且兄弟结点的两个子结点全为黑色
                    w.red = true;//兄弟节点变红
                    n = n.parent;//当前节点指向父节点
                } else if (w.left != null && w.left.red) {
                    //删除修复情况3：当前结点颜色是黑+黑，兄弟结点是黑色，兄弟的左子是红色，右子是黑色。`
                    //把兄弟结点染红，兄弟左子结点染黑，之后再在兄弟结点为支点解右旋
                    w.red = true;
                    w.left.red = false;
                    rotateRight(root, w);
                    w = n.parent.right;
                } else {
                    //删除修复情况4：当前结点颜色是黑-黑色，它的兄弟结点是黑色，但是兄弟结点的右子是红色，兄弟结点左子的颜色任意。
                    //把兄弟结点染成当前结点父结点的颜色，把当前结点父结点染成黑色，兄弟结点右子染成黑色，之后以当前结点的父结点为支点进行左旋`
                    w.red = n.parent.red;
                    n.parent.red = false;
                    w.right.red = false;
                    rotateLeft(root, n.parent);
                    n = root(n);
                }
            } else {
                w = n.parent.left;
                if (w.red) {
                    n.parent.red = true;
                    w.red = false;
                    rotateRight(root, n.parent);
                } else if (!w.left.red && !w.right.red) {
                    w.red = true;
                    n = n.parent;
                } else if (!w.left.red) {
                    w.red = true;
                    w.left.red = false;
                    rotateLeft(root, w);
                } else {
                    w.red = n.parent.red;
                    n.parent.red = false;
                    w.left.red = false;
                    rotateRight(root, n.parent);
                }
            }

        }

        return root(n);
    }

    private static Node findMax(Node node) {
        Node result = node;
        while (result.right != null) result = result.right;
        return result;
    }

    /**
     * 左旋转
     *
     * @param p 选择点
     *          左旋以p 到右子节点r 之间的链为“支轴”进行，它使右子节点r 成为p的父节点 并且r的左子节点成为 p的右子节点完成旋转操作
     */
    public static Node rotateLeft(Node root, Node p) {
        Node r, pp, rl;
        if (p != null && (r = p.right) != null) {
            if ((rl = p.right = r.left) != null) {
                rl.parent = p;
            }
            if ((pp = r.parent = p.parent) == null) {
                (root = r).red = false;
            } else if (pp.left == p) {
                pp.left = r;
            } else {
                pp.right = r;
            }
            r.left = p;
            p.parent = r;
        }
        return root;
    }

    /**
     * 左旋转
     *
     * @param p 选择点
     *          左旋以p 到左子节点l 之间的链为“支轴”进行，它使左子节点l 成为p的父节点 并且l的右子节点成为 p的左子节点完成旋转操作
     */
    public static Node rotateRight(Node root, Node p) {
        Node pp, l, lr;
        if (p != null && (l = p.left) != null) {
            if ((lr = p.left = l.right) != null) {
                lr.parent = p;
            }
            if ((pp = l.parent = p.parent) == null) {
                (root = l).red = false;
            } else if (pp.left == p) {
                pp.left = l;
            } else {
                pp.right = l;
            }
            l.right = p;
            p.parent = l;
        }
        return root;
    }

    @Override
    public String toString() {
        return value + (red ? "[R]" : "[B]");
    }

}
