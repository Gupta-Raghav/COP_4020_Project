package edu.ufl.cise.plc;

import java.util.HashMap;
import java.util.Scanner;

public class Token implements IToken {

    private Kind kind;
    private String input;

    int lexemmelength;
    boolean done;
    int pos = 0;

    public enum State {
        START, IN_IDENT, HAVE_ZERO, HAVE_DOT,
        IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS, HAVE_PLUS;
    }

    State state = State.START;

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getText() {
        return input;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    public void Lexer() {
        int tok;

        done = false;

    }

    HashMap<Kind, State> rsrvd = new HashMap<>(); // hashmap for the reserved words of the programming language, we
                                                  // will use this after we have the identifier and we will compare it
                                                  // with the hashmap.

    // rsrvd.put(TYPE, 'int'), somehow create a hashmap of all the reserved words
    // like this

    Scanner s = new Scanner(System.in);
    char chars[] = s.next().toCharArray();

    public void step(){
        while(done!=true){
        char ch = chars[pos]; //Can i find some other way to iterate over the complete code, for loop ? - too tacky plus conditions will make it difficult
                           //while loop? 
        int             
        
        switch(state){ //implementing the DFA, this switch statement is to define the states of the DFA
            case START:
            int startPOS = pos;
                switch(ch){ //this switch case works on the characters of the string.
                    case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z':
                    case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z':
                    case '$':
                    case '_':
                    {
                        pos++;
                        state = State.IN_IDENT;
                        kind = Kind.PLUS;
                        //array list TOKENS<String "identfier",string "p">
                    }
                    //to do : Move to some different state 
                    //state = have 
                    //return identifier or save it somewhere to let the program know, save it in array list ? umm how about 
                    //we save it like ('string','token') but this will have to be done in the end of the execution of the dfa
                    //let's do it like this, create an array list(string name, token kind)
                    //where as soon as we hit the final state we add it into the array list. and array list can be named tokens? 
                    break;
                    default:
                    case '+':
                    {
                       kind = Kind.PLUS;
                        pos++;
                        state = State.HAVE_PLUS;
                    }
                }
            case IN_IDENT:
                switch(ch){
                case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y', 'z':
                case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z':
                case '$':
                case '_':
                case '1','2','3','4','5','6','7','8','9':
                {

                }
            }
            case HAVE_ZERO:
                {
                
                }
            case HAVE_DOT:
            {

            }
            case IN_FLOAT:
            {

            }

            default:
            throw new IllegalStateException("lexer bug");   
                
        }
    }
    }

    @Override
    public int getIntValue() {

        return 0;
    }

    @Override
    public float getFloatValue() {
        return 0;
    }

    @Override
    public boolean getBooleanValue() {
        return false;
    }

    @Override
    public String getStringValue() {
        return null;
    }

}
