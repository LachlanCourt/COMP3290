package Parser;

import Scanner.Token;

public class TreeNode {
    enum TreeNodes {
        NPROG,
        NGLOB,
        NILIST,
        NINIT,
        NILIT,
        NFLIT,
        NTYPEL,
        NATYPE,
        NRTYPE,
        NFLIST,
        NSDECL,
        NTDECL,
        NPRITYP,
        NALIST,
        NARRD,
        NADD,
        NSUB,
        NMUL,
        NDIV,
        NMOD,
        NPOW,
        NTRUE,
        NFALS,
        NSIMV,
        NARRV,
        NAELT,
        NFCALL,
        NBOOL,
        NAND,
        NOR,
        NXOR,
        NNOT,
        NEQL,
        NNEQ,
        NGRT,
        NLSS,
        NLEQ,
        NGEQ,
        NEXPL,
        NMAIN,
        NSDLST,
        NSTATS,
        NREPT,
        NASGNS,
        NASGN,
        NPLEQ,
        NMNEQ,
        NSTEA,
        NDVEQ,
        NFORL,
        NIFTH,
        NIFTE,
        NIFEF,
        NINPUT,
        NPRINT,
        NPRLN,
        NVLIST,
        NPRLST,

        NSTRG,
        NCALL,
        NRETN,
        NFUNCS,
        NFUND,

        NPLIST,

        NSIMP,
        NARRP,
        NARRC,

        NDLIST,
    }

    private TreeNodes nodeType;
    private TreeNode left;
    private TreeNode mid;
    private TreeNode right;
    private Token token;

    public TreeNode() {}

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

    public TreeNode(TreeNodes type_, TreeNode left_) {
        nodeType = type_;
        token = null;
        left = left_;
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

    public TreeNode(TreeNodes type_, TreeNode left_, TreeNode mid_, TreeNode right_) {
        nodeType = type_;
        token = null;
        left = left_;
        mid = mid_;
        right = right_;
    }

    public void setNextChild(TreeNode child) {
        if (left == null) {
            left = child;
        } else if (mid == null) {
            mid = child;
        } else {
            right = child;
        }
    }

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode getMid() {
        return mid;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setNodeType(TreeNodes type_) {
        nodeType = type_;
    }

    public TreeNodes getNodeType() {
        return nodeType;
    }

    public void setToken(Token token_) {
        token = token_;
    }
}
