package edu.ufl.cise.plc;

import java.util.HashMap;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;

import java.util.*;

public class Token implements IToken {

    private Kind kind;
    private String input;

    int lexemmelength;
    static int state;
    static int done;
    static int finalState;
    int pos = 0;

    public enum State {
        START, IN_IDENT, HAVE_ZERO, HAVE_DOT,
        IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS;
    }

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

    }

    public Lexer(){
        int tok;

        done = 0;
        state = 0;
        finalState = -1;

        double x = 0.123;
        

        while(done != 0){
            step();
            setFinal();
        }
        if(finalState<0){

        }
    }

    HashMap<Kind, string> rsrvd = new HashMap<>(); // hashmap for the reserved words of the programming language, we
                                                   // will use this after we have the identifier and we will compare it
                                                   // with the hashmap.

    // rsrvd.put(TYPE, 'int'), somehow create a hashmap of all the reserved words
    // like this

    public void moveToState(State st) {
        state = st;
    }

    Scanner s = new Scanner(System.in);
    char chars[] = s.next().toCharArray();

    public void step(){
        char ch = chars[pos]; //Can i find some other way to iterate over the complete code, for loop ? - too tacky plus conditions will make it difficult
                           //while loop? 
        switch(state){ //implementing the DFA, this switch statement is to define the states of the DFA
            case START:
            int startPOS = pos;
                switch(ch){ //this switch case works on the characters of the string.
                    case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z', '$', '_':
                    //return identifier or save it somewhere to let the program know, save it in array list ? umm how about 
                    //we save it like ('string','token') but this will have to be done in the end of the execution of the dfa
                    //let's do it like this, create an array list(string name, token kind)
                    //where as soon as we hit the final state we add it into the array list. and array list can be named tokens? 
                    moveToState(IN_IDENT,ch);
                    break;
                    default:
                    done =1;

                    case '+':
                        kind = Kind.PLUS;
                        pos++

                }
            case State.IN_IDENT:
                switch(ch){
                case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y', 'z':
                case '1','2','3','4','5','6','7','8','9':
                {

                }
            }
            case State.HAVE_ZERO:
                {
                
                }
            case State.HAVE_DOT:
            {

            }
            case IN_FLOAT:
            {

            }

            default:
            throw new 
                
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
