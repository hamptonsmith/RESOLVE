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
 * FacilityOperationDec.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.PosSymbol;

public class FacilityOperationDec extends Dec {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The name member. */
    private PosSymbol name;

    /** The parameters member. */
    private List<ParameterVarDec> parameters;

    /** The returnTy member. */
    private Ty returnTy;

    /** The stateVars member. */
    private List<AffectsItem> stateVars;

    /** The requires member. */
    private Exp requires;

    /** The ensures member. */
    private Exp ensures;

    /** The decreasing member. */
    private Exp decreasing;

    /** The facilities member. */
    private List<FacilityDec> facilities;

    /** The variables member. */
    private List<VarDec> variables;

    /** The variables member. */
    private List<AuxVarDec> aux_variables;

    /** The statements member. */
    private List<Statement> statements;

    /** The recursive member. */
    private boolean recursive;

    // ===========================================================
    // Constructors
    // ===========================================================

    public FacilityOperationDec() {};

    public FacilityOperationDec(PosSymbol name,
            List<ParameterVarDec> parameters, Ty returnTy,
            List<AffectsItem> stateVars, Exp requires, Exp ensures,
            Exp decreasing, List<FacilityDec> facilities,
            List<VarDec> variables, List<AuxVarDec> aux_variables,
            List<Statement> statements) {
        this.name = name;
        this.parameters = parameters;
        this.returnTy = returnTy;
        this.stateVars = stateVars;
        this.requires = requires;
        this.ensures = ensures;
        this.decreasing = decreasing;
        this.facilities = facilities;
        this.variables = variables;
        this.aux_variables = aux_variables;
        this.statements = statements;
        this.recursive = recursive;
    }

    public FacilityOperationDec(PosSymbol name,
            List<ParameterVarDec> parameters, Ty returnTy,
            List<AffectsItem> stateVars, Exp requires, Exp ensures,
            Exp decreasing, List<FacilityDec> facilities,
            List<VarDec> variables, List<AuxVarDec> aux_variables,
            List<Statement> statements, boolean recursive) {
        this.name = name;
        this.parameters = parameters;
        this.returnTy = returnTy;
        this.stateVars = stateVars;
        this.requires = requires;
        this.ensures = ensures;
        this.decreasing = decreasing;
        this.facilities = facilities;
        this.variables = variables;
        this.aux_variables = aux_variables;
        this.statements = statements;
        this.recursive = recursive;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    /** Returns the value of the parameters variable. */
    public List<ParameterVarDec> getParameters() {
        return parameters;
    }

    /** Returns the value of the returnTy variable. */
    public Ty getReturnTy() {
        return returnTy;
    }

    /** Returns the value of the stateVars variable. */
    public List<AffectsItem> getStateVars() {
        return stateVars;
    }

    /** Returns the value of the requires variable. */
    public Exp getRequires() {
        return requires;
    }

    /** Returns the value of the ensures variable. */
    public Exp getEnsures() {
        return ensures;
    }

    /** Returns the value of the decreasing variable. */
    public Exp getDecreasing() {
        return decreasing;
    }

    /** Returns the value of the facilities variable. */
    public List<FacilityDec> getFacilities() {
        return facilities;
    }

    /** Returns the value of the variables variable. */
    public List<VarDec> getVariables() {
        return variables;
    }

    /** Returns the value of the variables variable. */
    public List<AuxVarDec> getAuxVariables() {
        return aux_variables;
    }

    /** Returns the value of the statements variable. */
    public List<Statement> getStatements() {
        return statements;
    }

    /** Returns the value of the recursive variable. */
    public boolean getRecursive() {
        return recursive;
    }

    public List<VarDec> getAllVariables() {
        List<VarDec> vars = new List<VarDec>();

        Iterator<VarDec> j;
        j = variables.iterator();
        while (j.hasNext()) {
            VarDec tmpAV = j.next();
            VarDec tmp = new VarDec(tmpAV.getName(), tmpAV.getTy());
            vars.add(tmp);
        }
        Iterator<AuxVarDec> i;
        i = aux_variables.iterator();
        while (i.hasNext()) {
            AuxVarDec tmpAV = i.next();
            VarDec tmp = new VarDec(tmpAV.getName(), tmpAV.getTy());
            vars.add(tmp);
        }
        return vars;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    /** Sets the parameters variable to the specified value. */
    public void setParameters(List<ParameterVarDec> parameters) {
        this.parameters = parameters;
    }

    /** Sets the returnTy variable to the specified value. */
    public void setReturnTy(Ty returnTy) {
        this.returnTy = returnTy;
    }

    /** Sets the stateVars variable to the specified value. */
    public void setStateVars(List<AffectsItem> stateVars) {
        this.stateVars = stateVars;
    }

    /** Sets the requires variable to the specified value. */
    public void setRequires(Exp requires) {
        this.requires = requires;
    }

    /** Sets the ensures variable to the specified value. */
    public void setEnsures(Exp ensures) {
        this.ensures = ensures;
    }

    /** Sets the decreasing variable to the specified value. */
    public void setDecreasing(Exp decreasing) {
        this.decreasing = decreasing;
    }

    /** Sets the facilities variable to the specified value. */
    public void setFacilities(List<FacilityDec> facilities) {
        this.facilities = facilities;
    }

    /** Sets the variables variable to the specified value. */
    public void setVariables(List<VarDec> variables) {
        this.variables = variables;
    }

    /** Sets the variables variable to the specified value. */
    public void setAuxVariables(List<AuxVarDec> aux_variables) {
        this.aux_variables = aux_variables;
    }

    /** Sets the statements variable to the specified value. */
    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitFacilityOperationDec(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("FacilityOperationDec\n");

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (parameters != null) {
            sb.append(parameters.asString(indent + increment, increment));
        }

        if (returnTy != null) {
            sb.append(returnTy.asString(indent + increment, increment));
        }

        if (stateVars != null) {
            sb.append(stateVars.asString(indent + increment, increment));
        }

        if (requires != null) {
            sb.append(requires.asString(indent + increment, increment));
        }

        if (ensures != null) {
            sb.append(ensures.asString(indent + increment, increment));
        }

        if (decreasing != null) {
            sb.append(decreasing.asString(indent + increment, increment));
        }

        if (facilities != null) {
            sb.append(facilities.asString(indent + increment, increment));
        }

        if (variables != null) {
            sb.append(variables.asString(indent + increment, increment));
        }

        if (statements != null) {
            sb.append(statements.asString(indent + increment, increment));
        }

        return sb.toString();
    }
}
