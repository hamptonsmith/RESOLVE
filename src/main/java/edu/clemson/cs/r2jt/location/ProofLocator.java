/*
 * This software is released under the new BSD 2006 license.
 * 
 * Note the new BSD license is equivalent to the MIT License, except for the
 * no-endorsement final clause.
 * 
 * Copyright (c) 2007, Clemson University
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Clemson University nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This sofware has been developed by past and present members of the
 * Reusable Sofware Research Group (RSRG) in the School of Computing at
 * Clemson University. Contributors to the initial version are:
 * 
 * Steven Atkinson
 * Greg Kulczycki
 * Kunal Chopra
 * John Hunt
 * Heather Keown
 * Ben Markle
 * Kim Roche
 * Murali Sitaraman
 */
/*
 * DefinitionLocator.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.location;

import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.collections.Stack;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.entry.*;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.Environment;
import edu.clemson.cs.r2jt.scope.*;
import edu.clemson.cs.r2jt.type.*;

public class ProofLocator {

    // ===========================================================
    // Variables
    // ===========================================================

    private ErrorHandler err;

    //private Environment env = Environment.getInstance();

    private OldSymbolTable table;

    private TypeMatcher tm;

    // ===========================================================
    // Constructors
    // ===========================================================

    public ProofLocator(OldSymbolTable table, TypeMatcher tm, ErrorHandler err) {
        this.table = table;
        this.tm = tm;
        this.err = err;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public ProofEntry locateProof(PosSymbol name) throws SymbolSearchException {
        List<ProofEntry> proofs = locateProofsInStack(name);
        if (proofs.size() == 0) {
            proofs = locateProofsInImports(name);
        }
        if (proofs.size() > 1) {
            List<Location> locs = getLocationList(proofs);
            String msg = ambigProofRefMessage(name.toString(), locs.toString());
            err.error(name.getLocation(), msg);
            throw new SymbolSearchException();
        }
        else if (proofs.size() == 0) {
            String msg = cantFindProofMessage(name.toString());
            err.error(name.getLocation(), msg);
            throw new SymbolSearchException();
        }
        else {
            return proofs.get(0);
        }
    }

    public ProofEntry locateProof(PosSymbol qual, PosSymbol name)
            throws SymbolSearchException {
        if (qual == null) {
            return locateProof(name);
        }
        QualifierLocator qlocator = new QualifierLocator(table, err);
        ModuleScope scope;
        scope = qlocator.locateMathModule(qual);
        if (scope.containsProof(name.getSymbol())) {
            ProofEntry p = scope.getProof(name.getSymbol());
            return p;
        }
        else {
            String msg =
                    cantFindProofInModMessage(name.toString(), qual.toString());
            err.error(qual.getLocation(), msg);
            throw new SymbolSearchException();
        }
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    private List<ProofEntry> locateProofsInStack(PosSymbol name)
            throws SymbolSearchException {
        List<ProofEntry> proofs = new List<ProofEntry>();
        Stack<Scope> stack = table.getStack();
        Stack<Scope> hold = new Stack<Scope>();
        try {
            while (!stack.isEmpty()) {
                Scope scope = stack.pop();
                hold.push(scope);
                if (scope instanceof ModuleScope) {
                    ModuleScope mscope = (ModuleScope) scope;
                    if (mscope.containsProof(name.getSymbol())) {
                        proofs.add(mscope.getProof(name.getSymbol()));
                    }
                    // FIX: Check for recursive operation
                    // should be added here.
                }
            }
            return proofs;
        }
        finally {
            while (!hold.isEmpty()) {
                stack.push(hold.pop());
            }
        }
    }

    private List<ProofEntry> locateProofsInImports(PosSymbol name)
            throws SymbolSearchException {
        List<ProofEntry> proofs = new List<ProofEntry>();
        Iterator<ModuleScope> i =
                table.getModuleScope().getMathVisibleModules();
        while (i.hasNext()) {
            ModuleScope iscope = i.next();
            if (iscope.containsProof(name.getSymbol())) {
                proofs.add(iscope.getProof(name.getSymbol()));
            }
        }
        return proofs;
    }

    private List<Location> getLocationList(List<ProofEntry> entries) {
        List<Location> locs = new List<Location>();
        Iterator<ProofEntry> i = entries.iterator();
        while (i.hasNext()) {
            ProofEntry entry = i.next();
            locs.add(entry.getLocation());
        }
        return locs;
    }

    // -----------------------------------------------------------
    // Error Related Methods
    // -----------------------------------------------------------

    private String cantFindProofInModMessage(String name, String module) {
        return "Cannot find a proof named " + name + " in module " + module
                + ".";
    }

    private String cantFindProofMessage(String name) {
        return "Cannot find a proof named " + name + ".";
    }

    private String ambigProofRefMessage(String name, String mods) {
        return "The proof named " + name + " is found in more than one "
                + "module visible from this scope: " + mods + ".";
    }

}
