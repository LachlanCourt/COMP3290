/*******************************************************************************
 ****    COMP3290 Assignment 1
 ****    c3308061
 ****    Lachlan Court
 ****    10/08/2022
 ****    This class is a placeholder for a Parser for the CD22 programming language.
 ****    in this project it runs a debug routine to output tokens found by the scanner
 ****    until EOF
 *******************************************************************************/
package Parser;

import Parser.TreeNode.TreeNodes;
import Scanner.Scanner;
import Scanner.Token;
import Scanner.Token.Tokens;
import com.sun.source.tree.Tree;

public class Parser {
    private Scanner s;
    private Token lookahead;
    private Token previousLookahead;

    public Parser(Scanner s_) {
        s = s_;
    }

    /**
     * Placeholder initialisation function
     */
    public void initialise() {
    }

    private void error(String message, Token token) {
        System.err.println("ERROR");
        System.err.println(message);
        System.exit(1);
    }

    private void error(String message) {
        System.err.println("ERROR");
        System.err.println(message);
        System.exit(1);
    }

    private void match(Tokens token) {
        if (token == lookahead.getToken()) {
            previousLookahead = lookahead;
            lookahead = s.getToken();
        } else
            error("Failed to match " + token + " around " + previousLookahead.getRow() + ":" + previousLookahead.getCol());
    }

    /**
     * Main run method of the parser
     */
    public void run() {
        lookahead = s.getToken();
        TreeNode syntaxTree = program();
        if (!lookahead.isEof()) {
            error("Not at eof", new Token(false));
        }
        System.out.println(syntaxTree);
    }

    private TreeNode program() {
        TreeNode t = new TreeNode(TreeNodes.NPROG);
        match(Tokens.TCD22);
        match(Tokens.TIDEN);
        t.setNextChild(globals());
        //    t.setNextChild(nfuncs());
        t.setNextChild(mainbody());
        return t;
    }

    private TreeNode globals() {
        TreeNode t = new TreeNode(TreeNodes.NGLOB);
        t.setNextChild(consts());
        t.setNextChild(types());
        t.setNextChild(arrays());

        return t;
    }

    private TreeNode consts() {
        if (lookahead.getToken() == Tokens.TCONS) {
            match(Tokens.TCONS);
            return initlist();
        }
        return null;
    }

    private TreeNode initlist() {
        TreeNode t1 = init(), t2 = null;
        if (lookahead.getToken() == Tokens.TTYPS || lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = initlist();
        }
        return new TreeNode(TreeNodes.NILIST, t1, t2);
    }

    private TreeNode init() {
        TreeNode t = new TreeNode(TreeNodes.NINIT);
        if (lookahead.getToken() == Tokens.TIDEN) {
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        match(Tokens.TEQUL);
        t.setNextChild(expr());
        return t;
    }

    private TreeNode types() {
        if (lookahead.getToken() == Tokens.TTYPS) {
            match(Tokens.TTYPS);
            return typelist();
        }
        return null;
    }

    private TreeNode typelist() {
        TreeNode t1 = type(), t2 = null;
        if (lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        t2 = typelist();
        return new TreeNode(TreeNodes.NTYPEL, t1, t2);
    }

    private TreeNode type() {
        TreeNode t = new TreeNode();
        if (lookahead.getToken() == Tokens.TIDEN) {
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        match(Tokens.TTDEF);
        if (lookahead.getToken() == Tokens.TARAY) {
            // Array
            match(Tokens.TARAY);
            match(Tokens.TLBRK);
            t.setNextChild(expr());
            match(Tokens.TRBRK);
            match(Tokens.TTTOF);
            if (lookahead.getToken() == Tokens.TIDEN) {
                t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
                match(Tokens.TIDEN);
            } else {
                error("Missing identifier");
            }
            t.setNodeType(TreeNodes.NATYPE);
        } else if (lookahead.getToken() == Tokens.TIDEN) {
            // Struct
            t.setNodeType(TreeNodes.NRTYPE);
            t.setNextChild(fields());
        }
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode fields() {
        TreeNode t1 = sdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TTEND) {
            return t1;
        } else if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = fields();
        }
        return new TreeNode(TreeNodes.NFLIST, t1, t2);
    }

    private TreeNode sdecl() {
        TreeNode t = new TreeNode();
        if (lookahead.getToken() == Tokens.TIDEN) {
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        match(Tokens.TCOLN);
        if (lookahead.getToken() == Tokens.TIDEN) {
            // structid
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
            t.setNodeType(TreeNodes.NTDECL);
            match(Tokens.TIDEN);
        } else {
            // stype
            t.setNextChild(stype());
            t.setNodeType(TreeNodes.NTDECL);
        }
        return t;
    }

    private TreeNode stype() {
        TreeNode t = new TreeNode(TreeNodes.NPRITYP);
        switch (lookahead.getToken()) {
            case TINTG -> {
                t.setToken(lookahead);
                match(Tokens.TINTG);
            }
            case TFLOT -> {
                t.setToken(lookahead);
                match(Tokens.TFLOT);

            }
            case TBOOL -> {
                t.setToken(lookahead);
                match(Tokens.TBOOL);
            }
            default -> error("Invalid type");
        }
        return t;
    }

    private TreeNode arrays() {
        if (lookahead.getToken() == Tokens.TARRS) {
            match(Tokens.TARRS);
            return arrdecls();
        }
        return null;
    }

    private TreeNode arrdecls() {
        TreeNode t1 = arrdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = arrdecls();
        }
        return new TreeNode(TreeNodes.NALIST, t1, t2);
    }

    private TreeNode arrdecl() {
        TreeNode t = new TreeNode(TreeNodes.NARRD);
        if (lookahead.getToken() == Tokens.TIDEN) {
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        match(Tokens.TCOLN);
        if (lookahead.getToken() == Tokens.TIDEN) {
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
            match(Tokens.TIDEN);
        } else {
            error("Missing identifier");
        }
        return t;
    }

    private TreeNode expr() {
        TreeNode t = new TreeNode();
        t.setNextChild(term());
        // Sets node type, if it is not epsilon
        t.setNextChild(exprr(t));
        if (t.getNodeType() == null) {
            // Epsilon path of recursive exprr rule. If no following token was found, just return the term as its own node
            return t.getLeft();
        }
        return t;
    }

    private TreeNode exprr(TreeNode t) {
        if (lookahead.getToken() == Tokens.TPLUS) {
            match(Tokens.TPLUS);
            t.setNodeType(TreeNodes.NADD);
            t.setNextChild(term());
        } else if (lookahead.getToken() == Tokens.TMINS) {
            match(Tokens.TMINS);
            t.setNodeType(TreeNodes.NSUB);
            t.setNextChild(term());
        }
        return t;
    }

    private TreeNode term() {
        TreeNode t = new TreeNode();
        t.setNextChild(fact());
        // Sets node type, if it is not epsilon
        t.setNextChild(termr(t));
        if (t.getNodeType() == null) {
            // Epsilon path of recursive termr rule. If no following token was found, just return the term as its own node
            return t.getLeft();
        }
        return t;
    }

    private TreeNode termr(TreeNode t) {
        if (lookahead.getToken() == Tokens.TSTAR) {
            match(Tokens.TSTAR);
            t.setNodeType(TreeNodes.NMUL);
            t.setNextChild(fact());
        } else if (lookahead.getToken() == Tokens.TDIVD) {
            match(Tokens.TDIVD);
            t.setNodeType(TreeNodes.NDIV);
            t.setNextChild(fact());

        } else if (lookahead.getToken() == Tokens.TPERC) {
            match(Tokens.TPERC);
            t.setNodeType(TreeNodes.NMOD);
            t.setNextChild(fact());
        }
        return t;
    }

    private TreeNode fact() {
        TreeNode t = new TreeNode();
        t.setNextChild(exponent());
        // Sets node type, if it is not epsilon
        t.setNextChild(factr(t));
        if (t.getNodeType() == null) {
            // Epsilon path of recursive factr rule. If no following token was found, just return the term as its own node
            return t.getLeft();
        }
        return t;
    }

    private TreeNode factr(TreeNode t) {
        if (lookahead.getToken() == Tokens.TCART) {
            match(Tokens.TCART);
            t.setNodeType(TreeNodes.NPOW);
            t.setNextChild(exponent());
        }
        return t;
    }

    private TreeNode exponent() {
        if (lookahead.getToken() == Tokens.TILIT) {
            match(Tokens.TILIT);
            return new TreeNode(TreeNodes.NILIT);
        } else if (lookahead.getToken() == Tokens.TFLIT) {
            match(Tokens.TFLIT);
            return new TreeNode(TreeNodes.NFLIT);
        } else if (lookahead.getToken() == Tokens.TTRUE) {
            match(Tokens.TTRUE);
            return new TreeNode(TreeNodes.NTRUE);
        } else if (lookahead.getToken() == Tokens.TFALS) {
            match(Tokens.TFALS);
            return new TreeNode(TreeNodes.NFALS);
        } else if (lookahead.getToken() == Tokens.TLPAR) {
            match(Tokens.TLPAR);
            TreeNode t = bool();
            match(Tokens.TRPAR);
            return t;
        } else if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            if (lookahead.getToken() == Tokens.TLPAR) {
                return fncall(token);
            }
            // If the node is not a function call, it's either an ID or an array accessor. Either way start by passing the id
            // token straight to the overridden function
            return var(token);

        }
        error("Not a number");
        return null;
    }

    private TreeNode bool() {
        TreeNode t = new TreeNode(TreeNodes.NBOOL);
        t.setNextChild(rel());
        boolr(t);
        return t;
    }

    private TreeNode boolr(TreeNode t) {
        t.setNextChild(logop());
        if (t.getMid() != null) {
            t.setNextChild(rel());
        }
        return t;
    }

    private TreeNode logop() {
        if (lookahead.getToken() == Tokens.TTAND) {
            match(Tokens.TTAND);
            return new TreeNode(TreeNodes.NAND);
        } else if (lookahead.getToken() == Tokens.TTTOR) {
            match(Tokens.TTTOR);
            return new TreeNode(TreeNodes.NOR);
        } else if (lookahead.getToken() == Tokens.TTXOR) {
            match(Tokens.TTXOR);
            return new TreeNode(TreeNodes.NXOR);
        }
        return null;
    }

    private TreeNode rel() {
        if (lookahead.getToken() == Tokens.TNOTT) {
            return new TreeNode(TreeNodes.NNOT, expr(), relop(), expr());
        }
        TreeNode exprNode = expr();
        TreeNode relopNode = relop();
        if (relopNode == null) {
            return new TreeNode(TreeNodes.NREL, exprNode);
        }
        return new TreeNode(TreeNodes.NREL, exprNode, relopNode, expr());
    }

    private TreeNode relop() {
        if (lookahead.getToken() == Tokens.TEQEQ) {
            match(Tokens.TEQEQ);
            return new TreeNode(TreeNodes.NEQL);
        } else if (lookahead.getToken() == Tokens.TNEQL) {
            match(Tokens.TNEQL);
            return new TreeNode(TreeNodes.NNEQ);
        } else if (lookahead.getToken() == Tokens.TGRTR) {
            match(Tokens.TGRTR);
            return new TreeNode(TreeNodes.NGRT);
        } else if (lookahead.getToken() == Tokens.TLESS) {
            match(Tokens.TLESS);
            return new TreeNode(TreeNodes.NLSS);
        } else if (lookahead.getToken() == Tokens.TLEQL) {
            match(Tokens.TLEQL);
            return new TreeNode(TreeNodes.NLEQ);
        } else if (lookahead.getToken() == Tokens.TGEQL) {
            match(Tokens.TGEQL);
            return new TreeNode(TreeNodes.NGEQ);
        }
        return null;
    }

    private TreeNode fncall() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return fncall(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode fncall(Token nameIdenToken) {
        TreeNode t = new TreeNode(TreeNodes.NFCALL);
        t.setNextChild(new TreeNode(TreeNodes.NIDEN, nameIdenToken));
        match(Tokens.TLPAR);
        if (lookahead.getToken() != Tokens.TRPAR) {
            t.setNextChild(elist());
        }
        match(Tokens.TRPAR);
        return t;
    }

    private TreeNode elist() {
        TreeNode t1 = bool(), t2 = null;
        if (lookahead.getToken() == Tokens.TRPAR) {
            return t1;
        }
        if (lookahead.getToken() == Tokens.TCOMA) {
            match(Tokens.TCOMA);
            t2 = elist();
        }
        return new TreeNode(TreeNodes.NEXPL, t1, t2);
    }

    private TreeNode var() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return var(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode var(Token nameIdenToken) {
        if (lookahead.getToken() == Tokens.TLBRK) {
            TreeNode t = new TreeNode();
            match(Tokens.TLBRK);
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, nameIdenToken));
            t.setNextChild(expr());
            match(Tokens.TRBRK);
            // Access struct field
            if (lookahead.getToken() == Tokens.TDOTT) {
                match(Tokens.TDOTT);
                if (lookahead.getToken() == Tokens.TIDEN) {
                    Token fieldIdenToken = lookahead;
                    match(Tokens.TIDEN);
                    t.setNextChild(new TreeNode(TreeNodes.NIDEN, fieldIdenToken));
                }
            }
            // Either return with whole struct or individual field if the above if ran
            return t;
        }
        // Simple variable
        return new TreeNode(TreeNodes.NSIMV, new TreeNode(TreeNodes.NIDEN, nameIdenToken));
    }

    private TreeNode mainbody() {
        TreeNode t = new TreeNode(TreeNodes.NMAIN);
        match(Tokens.TMAIN);
        t.setNextChild(slist());
        match(Tokens.TBEGN);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        match(Tokens.TCD22);
        if (lookahead.getToken() == Tokens.TIDEN) {
            match(Tokens.TIDEN);
            t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
        } else {
            error("Missing identifier");
        }
        return t;
    }

    private TreeNode slist() {
        TreeNode t1 = sdecl(), t2 = null;
        if (lookahead.getToken() == Tokens.TBEGN) {
            return t1;
        }
        match(Tokens.TCOMA);
        t2 = slist();
        return new TreeNode(TreeNodes.NSDLST, t1, t2);
    }

    private TreeNode stats() {
        TreeNode t = null;
        // Assign t1
        if (lookahead.getToken() == Tokens.TTFOR || lookahead.getToken() == Tokens.TIFTH) {
            t = strstat();
        } else if (lookahead.getToken() == Tokens.TREPT || lookahead.getToken() == Tokens.TIDEN || lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT || lookahead.getToken() == Tokens.TPRLN || lookahead.getToken() == Tokens.TRETN) {
            t = stat();
            match(Tokens.TSEMI);
        }
        // Epsilon path
        if (lookahead.getToken() != Tokens.TTFOR && lookahead.getToken() != Tokens.TIFTH && lookahead.getToken() != Tokens.TREPT && lookahead.getToken() != Tokens.TIDEN && lookahead.getToken() != Tokens.TINPT && lookahead.getToken() != Tokens.TPRNT && lookahead.getToken() != Tokens.TPRLN && lookahead.getToken() != Tokens.TRETN) {
            return t;
        }
        return new TreeNode(TreeNodes.NSTATS, t, stats());
    }


    private TreeNode stat() {
        if (lookahead.getToken() == Tokens.TREPT) {
            return reptstat();
        } else if (lookahead.getToken() == Tokens.TINPT || lookahead.getToken() == Tokens.TPRNT || lookahead.getToken() == Tokens.TPRLN) {
            return iostat();
        } else if (lookahead.getToken() == Tokens.TRETN) {
            return returnstat();
        }
        Token token = lookahead;
        match(Tokens.TIDEN);
        if (lookahead.getToken() == Tokens.TLPAR) {
            return callstat(token);
        } else {
            return asgnstat(token);
        }
    }

    private TreeNode reptstat() {
        TreeNode t = new TreeNode(TreeNodes.NREPT);
        match(Tokens.TREPT);
        match(Tokens.TLPAR);
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null) t.setNextChild(asgnlistNode);
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        match(Tokens.TUNTL);
        t.setNextChild(bool());
        return t;
    }

    private TreeNode iostat() {
        if (lookahead.getToken() == Tokens.TINPT) {
            match(Tokens.TINPT);
            return new TreeNode(TreeNodes.NINPUT, vlist());
        } else if (lookahead.getToken() == Tokens.TPRNT) {
            match(Tokens.TPRNT);
            return new TreeNode(TreeNodes.NPRINT, prlist());
        } else {
            match(Tokens.TPRLN);
            return new TreeNode(TreeNodes.NPRLN, prlist());
        }
    }

    private TreeNode returnstat() {
        TreeNode t = new TreeNode(TreeNodes.NRETN);
        match(Tokens.TRETN);
        if (lookahead.getToken() == Tokens.TVOID) {
            match(Tokens.TVOID);
        } else {
            t.setNextChild(expr());
        }
        return t;
    }

    private TreeNode strstat() {
        if (lookahead.getToken() == Tokens.TTFOR) {
            return forstat();
        }
        return ifstat();
    }

    private TreeNode callstat() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return callstat(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode callstat(Token nameIdenToken) {
        TreeNode t = new TreeNode(TreeNodes.NCALL, nameIdenToken);
        match(Tokens.TLPAR);
        if (lookahead.getToken() != Tokens.TRPAR) {
            t.setNextChild(elist());
        }
        match(Tokens.TRPAR);
        return t;
    }

    private TreeNode asgnstat() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            Token token = lookahead;
            match(Tokens.TIDEN);
            return asgnstat(token);
        }
        error("Expected identifier");
        return null;
    }

    private TreeNode asgnstat(Token nameIdenToken) {
        TreeNode varNode = var(nameIdenToken);
        TreeNode t = asgnop();
        t.setNextChild(varNode);
        t.setNextChild(bool());
        return t;
    }

    private TreeNode forstat() {
        TreeNode t = new TreeNode(TreeNodes.NFORL);
        match(Tokens.TTFOR);
        match(Tokens.TLPAR);
        TreeNode asgnlistNode = asgnlist();
        if (asgnlistNode != null) t.setNextChild(asgnlistNode);
        match(Tokens.TSEMI);
        t.setNextChild(bool());
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        match(Tokens.TTEND);
        return t;
    }

    private TreeNode ifstat() {
        TreeNode t = new TreeNode();
        match(Tokens.TIFTH);
        match(Tokens.TLPAR);
        t.setNextChild(bool());
        match(Tokens.TRPAR);
        t.setNextChild(stats());
        if (lookahead.getToken() == Tokens.TTEND) {
            t.setNodeType(TreeNodes.NIFTH);
            match(Tokens.TTEND);
        } else if (lookahead.getToken() == Tokens.TELSE) {
            t.setNodeType(TreeNodes.NIFTE);
            match(Tokens.TELSE);
            t.setNextChild(stats());
            match(Tokens.TTEND);
        } else {
            t.setNodeType(TreeNodes.NIFEF);
            match(Tokens.TELIF);
            match(Tokens.TLPAR);
            TreeNode childNode = new TreeNode(TreeNodes.NIFTH);
            childNode.setNextChild(bool());
            match(Tokens.TRPAR);
            childNode.setNextChild(stats());
            match(Tokens.TTEND);
            t.setNextChild(childNode);
        }
        return t;
    }

    private TreeNode asgnlist() {
        if (lookahead.getToken() == Tokens.TIDEN) {
            return alist();
        }
        return null;
    }

    private TreeNode alist() {
        TreeNode t1 = asgnstat(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = alist();
        }
        return new TreeNode(TreeNodes.NASGNS, t1, t2);
    }

    private TreeNode asgnop() {
        if (lookahead.getToken() == Tokens.TEQUL) {
            match(Tokens.TEQUL);
            return new TreeNode(TreeNodes.NASGN);
        } else if (lookahead.getToken() == Tokens.TPLEQ) {
            match(Tokens.TPLEQ);
            return new TreeNode(TreeNodes.NPLEQ);
        } else if (lookahead.getToken() == Tokens.TMNEQ) {
            match(Tokens.TMNEQ);
            return new TreeNode(TreeNodes.NMNEQ);
        } else if (lookahead.getToken() == Tokens.TSTEQ) {
            match(Tokens.TSTEQ);
            return new TreeNode(TreeNodes.NSTEA);
        } else if (lookahead.getToken() == Tokens.TDVEQ) {
            match(Tokens.TDVEQ);
            return new TreeNode(TreeNodes.NDVEQ);
        }
        error("Expected assignment operator");
        return null;
    }

    private TreeNode vlist() {
        TreeNode t1 = var(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = vlist();
        }
        return new TreeNode(TreeNodes.NVLIST, t1, t2);
    }

    private TreeNode prlist() {
        TreeNode t1 = printitem(), t2;
        if (lookahead.getToken() != Tokens.TCOMA) {
            return t1;
        } else {
            match(Tokens.TCOMA);
            t2 = prlist();
        }
        return new TreeNode(TreeNodes.NPRLST, t1, t2);
    }

    private TreeNode printitem() {
        if (lookahead.getToken() == Tokens.TSTRG) {
            Token token = lookahead;
            match(Tokens.TSTRG);
            return new TreeNode(TreeNodes.NSTRG, token);
        }
        return expr();
    }
}
