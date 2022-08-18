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
import Scanner.Scanner;
import Scanner.Token;
import Scanner.Token.Tokens;
import Parser.TreeNode.TreeNodes;

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
    if (token == lookahead.getToken()) lookahead = s.getToken();
    else error("Weewoo", new Token(false));
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
    t.setLeft(nglob());
//    t.setMid(nfuncs());
//    t.setRight(nmain());
    return t;
  }

  private TreeNode nglob() {
  TreeNode t = new TreeNode(TreeNodes.NGLOB);
  t.setLeft(consts_spec());
        t.setMid(types_spec());
    t.setRight(arrays_spec());

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
    match(Tokens.TIDEN);
    match(Tokens.TEQUL);
    t.setLeft(expr());
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
    if (lookahead.getToken() == Tokens.TARRS || lookahead.getToken() == Tokens.TFUNC || lookahead.getToken() == Tokens.TMAIN) {
      return t1;
    }
    t2 = typelist();
    return new TreeNode(TreeNodes.NTYPEL, t1, t2);
  }

  private TreeNode type() {
    TreeNode t = new TreeNode();
    if (lookahead.getToken()  == Tokens.TIDEN) {
      t.setLeft(new TreeNode(TreeNodes.NIDEN, lookahead));
      match(Tokens.TIDEN);
    }else{
      error("Missing identifier");
    }
    match(Tokens.TTDEF);
    if (lookahead.getToken() == Tokens.TARAY) {
      // Array
      match(Tokens.TARAY);
      match(Tokens.TLBRK);
      t.setMid(expr());
      match(Tokens.TRBRK);
      match(Tokens.TTTOF);
      if (lookahead.getToken()  == Tokens.TIDEN) {
        t.setRight(new TreeNode(TreeNodes.NIDEN, lookahead));
        match(Tokens.TIDEN);
      }else{
        error("Missing identifier");
      }
      t.setNodeType(TreeNodes.NATYPE);
    } else if (lookahead.getToken() == Tokens.TIDEN) {
      // Struct
      t.setNodeType(TreeNodes.NRTYPE);
      t.setMid(nflist());
    }
    match(Tokens.TTEND);
    return t;
  }

  public TreeNode nflist() {
    TreeNode t1 = sdecl(), t2 = null;
    if (lookahead.getToken() == Tokens.TTEND) {
      return t1;
    } else if (lookahead.getToken() == Tokens.TCOMA) {
      match(Tokens.TCOMA);
      t2 = nflist();
    }
    return new TreeNode(TreeNodes.NFLIST, t1, t2);
  }

  public TreeNode sdecl() {
    TreeNode t = new TreeNode();
    if (lookahead.getToken()  == Tokens.TIDEN) {
      t.setLeft(new TreeNode(TreeNodes.NIDEN, lookahead));
      match(Tokens.TIDEN);
    }else{
      error("Missing identifier");
    }
    match(Tokens.TCOLN);
    if (lookahead.getToken() == Tokens.TIDEN) {
      //structid
      t.setMid(new TreeNode(TreeNodes.NIDEN, lookahead));
      t.setNodeType(TreeNodes.NTDECL);
      match(Tokens.TIDEN);
    } else if (lookahead.getToken() == Tokens.TINTG || lookahead.getToken() == Tokens.TFLOT || lookahead.getToken() == Tokens.TBOOL) {
      //stype
      switch (lookahead.getToken()) {
        case TINTG -> {
          t.setMid(new TreeNode(TreeNodes.NPRITYP, lookahead));
          match(Tokens.TINTG);
        }
        case TFLOT -> {
          t.setMid(new TreeNode(TreeNodes.NPRITYP, lookahead));
          match(Tokens.TFLOT);
        }
        case TBOOL -> {
          t.setMid(new TreeNode(TreeNodes.NPRITYP, lookahead));
          match(Tokens.TBOOL);
        }
      }
    }
    return t;
  }

  private TreeNode arrays_spec() {
    match(Tokens.TARRS);
    return null;
  }

  private TreeNode expr() {
    if (lookahead.getToken() == Tokens.TILIT ) {
      match(Tokens.TILIT);
      return new TreeNode(TreeNodes.NILIT);
    } else if (lookahead.getToken() == Tokens.TFLIT){
     match(Tokens.TFLIT);
     return new TreeNode(TreeNodes.NFLIT);
    }
    error("Not a number", new Token(true));
    return null;
  }
}
