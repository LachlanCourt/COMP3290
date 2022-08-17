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
    //    t.setMid(types_spec());
//    t.setRight(arrays_spec());

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
