package Parser;

import Scanner.Token;

public class TreeNode {
    enum TreeNodes {
        NPROG, NGLOB, NILIST, NINIT, NILIT, NFLIT, NTYPEL, NIDEN, NATYPE, NRTYPE, NFLIST, NSDECL, NTDECL, NPRITYP, NALIST, NARRD
    }

    private TreeNodes nodeType;
    private TreeNode left;
    private TreeNode mid;
    private TreeNode right;
    private Token token;

    public TreeNode() {

    }

    public TreeNode(TreeNodes type_, Token token_, TreeNode left_, TreeNode mid_, TreeNode right_) {
        nodeType = type_;
        token = token_;
        left = left_;
        mid = mid_;
        right = right_;
    }

    public TreeNode(TreeNodes type_, Token token_) {
        nodeType = type_;
        token = token_;
        left = null;
        mid = null;
        right = null;
    }

    public TreeNode(TreeNodes type_) {
        nodeType = type_;
        token = null;
        left = null;
        mid = null;
        right = null;
    }

    public TreeNode(TreeNodes type_, TreeNode left_, TreeNode mid_) {
        nodeType = type_;
        token = null;
        left = left_;
        mid = mid_;
        right = null;
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left_) {
        left = left_;
    }

    public TreeNode getMid() {
        return mid;
    }

    public void setMid(TreeNode mid_) {
        mid = mid_;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right_) {
        right = right_;
    }

    public void setNodeType(TreeNodes type_) {
        nodeType = type_;
    }
}
