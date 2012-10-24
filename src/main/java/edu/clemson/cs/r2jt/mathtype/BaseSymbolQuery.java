package edu.clemson.cs.r2jt.mathtype;

import java.util.List;

/**
 * <p>The most basic implementation of {@link SymbolQuery SymbolQuery}, which
 * pairs a {@link ScopeSearchPath ScopeSearchPath} with a 
 * {@link TableSearcher TableSearcher} to define a fully parameterized strategy
 * for searching a set of scopes.</p>
 */
public class BaseSymbolQuery<E extends SymbolTableEntry>
        implements
            SymbolQuery<E> {

    private final ScopeSearchPath mySearchPath;
    private final TableSearcher<E> mySearcher;

    public BaseSymbolQuery(ScopeSearchPath path, TableSearcher<E> searcher) {
        mySearchPath = path;
        mySearcher = searcher;
    }

    public List<E> searchFromContext(Scope source, ScopeRepository repo)
            throws DuplicateSymbolException {

        return mySearchPath.searchFromContext(mySearcher, source, repo);
    }
}