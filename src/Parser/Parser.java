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

public class Parser {
  private Scanner s;
  private Token lookahead;
  public Parser(Scanner s_) {
    s = s_;
  }

  /**
   * Placeholder initialisation function
   */
  public void initialise() {}

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
    if (token == lookahead.getToken())
      lookahead = s.getToken();
    else
      error("Weewoo", new Token(false));
  }

  /**
   * Main run method of the parser
   */
  public void run() {
    lookahead = s.getToken();
    TreeNode syntaxTree = nprog();
    if (!lookahead.isEof()) {
      error("Not at eof", new Token(false));
    }
  }

  private TreeNode nprog() {
    TreeNode t = new TreeNode(TreeNodes.NPROG);
    match(Tokens.TCD22);
    match(Tokens.TIDEN);
    t.setNextChild(nglob());
    //    t.setNextChild(nfuncs());
    //    t.setNextChild(nmain());
    return t;
  }

  private TreeNode nglob() {
    TreeNode t = new TreeNode(TreeNodes.NGLOB);
    t.setNextChild(consts_spec());
    t.setNextChild(types_spec());
    t.setNextChild(arrays_spec());

    return t;
  }

  private TreeNode consts_spec() {
    if (lookahead.getToken() == Tokens.TCONS) {
      match(Tokens.TCONS);
      return initlist();
    }
    return null;
  }

  private TreeNode initlist() {
    TreeNode t1 = init(), t2 = null;
    if (lookahead.getToken() == Tokens.TTYPS || lookahead.getToken() == Tokens.TARRS
        || lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
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

  private TreeNode types_spec() {
    if (lookahead.getToken() == Tokens.TTYPS) {
      match(Tokens.TTYPS);
      return typelist();
    }
    return null;
  }

  private TreeNode typelist() {
    TreeNode t1 = type(), t2 = null;
    if (lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC
        || lookahead.getToken() == Tokens.TMAIN) {
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
      default -> error ("Invalid type");
    }
    return t;
  }

  private TreeNode arrays_spec() {
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
    if (lookahead.getToken()  == Tokens.TIDEN) {
      t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
      match(Tokens.TIDEN);
    }else{
      error("Missing identifier");
    }
    match(Tokens.TCOLN);
    if (lookahead.getToken()  == Tokens.TIDEN) {
      t.setNextChild(new TreeNode(TreeNodes.NIDEN, lookahead));
      match(Tokens.TIDEN);
    }else{
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
    if (lookahead.getToken() == Tokens.TILIT ) {
      match(Tokens.TILIT);
      return new TreeNode(TreeNodes.NILIT);
    } else if (lookahead.getToken() == Tokens.TFLIT){
     match(Tokens.TFLIT);
     return new TreeNode(TreeNodes.NFLIT);
    } else if (lookahead.getToken() == Tokens.TTRUE) {
      match(Tokens.TTRUE);
      return new TreeNode(TreeNodes.NTRUE);
    }else if (lookahead.getToken() == Tokens.TFALS) {
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

  private TreeNode fncall(Token nameIdenToken)
  {
    TreeNode t = new TreeNode(TreeNodes.NFCALL);
    t.setNextChild(new TreeNode(TreeNodes.NIDEN, nameIdenToken));
    match(Tokens.TLPAR);
    if (lookahead.getToken() != Tokens.TRPAR) {
      t.setNextChild(expr());
    }
    match(Tokens.TRPAR);
    return t;
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
}

