package edu.clemson.cs.r2jt.parsing;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;

public class ColsASTAdaptor extends CommonTreeAdaptor {

    @Override
    public Object create(Token payload) {
        return new ColsAST(payload);
    }

    /*@Override
    public Object dupNode(Object old) {
    	return (old==null)? null : ((ColsAST)old).dupNode();
    }*/
    @Override
    public Object errorNode(TokenStream input, Token start, Token stop,
            RecognitionException e) {
        return new ColsASTErrorNode(input, start, stop, e);

    }
}