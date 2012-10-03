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
 * SelectionStmt.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.Mode;
import edu.clemson.cs.r2jt.data.PosSymbol;

public class SelectionStmt extends Statement {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The var member. */
    private ProgramExp var;

    /** The whenpairs member. */
    private List<ChoiceItem> whenpairs;

    /** The defaultclause member. */
    private List<Statement> defaultclause;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SelectionStmt() {};

    public SelectionStmt(ProgramExp var, List<ChoiceItem> whenpairs,
            List<Statement> defaultclause) {
        this.var = var;
        this.whenpairs = whenpairs;
        this.defaultclause = defaultclause;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    public Location getLocation() {
        return var.getLocation();
    }

    /** Returns the value of the var variable. */
    public ProgramExp getVar() {
        return var;
    }

    /** Returns the value of the whenpairs variable. */
    public List<ChoiceItem> getWhenpairs() {
        return whenpairs;
    }

    /** Returns the value of the defaultclause variable. */
    public List<Statement> getDefaultclause() {
        return defaultclause;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the var variable to the specified value. */
    public void setVar(ProgramExp var) {
        this.var = var;
    }

    /** Sets the whenpairs variable to the specified value. */
    public void setWhenpairs(List<ChoiceItem> whenpairs) {
        this.whenpairs = whenpairs;
    }

    /** Sets the defaultclause variable to the specified value. */
    public void setDefaultclause(List<Statement> defaultclause) {
        this.defaultclause = defaultclause;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitSelectionStmt(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("SelectionStmt\n");

        if (var != null) {
            sb.append(var.asString(indent + increment, increment));
        }

        if (whenpairs != null) {
            sb.append(whenpairs.asString(indent + increment, increment));
        }

        if (defaultclause != null) {
            sb.append(defaultclause.asString(indent + increment, increment));
        }

        return sb.toString();
    }
}
