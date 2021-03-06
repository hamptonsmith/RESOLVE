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
 * OldExp.java
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
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;

public class OldExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The exp member. */
    private Exp exp;

    // ===========================================================
    // Constructors
    // ===========================================================

    public OldExp() {};

    public OldExp(Location location, Exp exp) {
        this.location = location;
        this.exp = exp;

        if (exp.getMathType() != null) {
            setMathType(exp.getMathType());
        }

        if (exp.getMathTypeValue() != null) {
            setMathTypeValue(exp.getMathTypeValue());
        }
    }

    @Override
    public void setMathType(MTType t) {
        super.setMathType(t);
        exp.setMathType(t);
    }

    @Override
    public void setMathTypeValue(MTType t) {
        super.setMathTypeValue(t);
        exp.setMathTypeValue(t);
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        return new OldExp(location, substitute(exp, substitutions));
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the location variable. */
    public Location getLocation() {
        return location;
    }

    /** Returns the value of the exp variable. */
    public Exp getExp() {
        return exp;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the exp variable to the specified value. */
    public void setExp(Exp exp) {
        this.exp = exp;
        setType(exp.getType());
        setMathType(exp.getMathType());
        setMathTypeValue(exp.getMathTypeValue());
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitOldExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getOldExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("OldExp\n");

        if (exp != null) {
            sb.append(exp.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String toString(int indent) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);

        if (exp != null) {
            sb.append("#" + exp.toString(0));
        }

        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        if (exp != null) {
            if (IsOldExp) {
                return exp.containsVar(varName, false);
            }
            else {
                return false;
            }
        }
        return false;
    }

    public Object clone() {
        OldExp clone = new OldExp();
        clone.setExp((Exp) Exp.clone(this.getExp()));
        clone.setLocation(this.getLocation());
        clone.setType(getType());
        return clone;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(exp);
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        exp = e;
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof OldExp)) {
            return false;
        }
        return true;
    }

    public Exp replace(Exp old, Exp replacement) {
        if (old instanceof OldExp) {
            if (replacement instanceof OldExp) {
                Exp tmp =
                        Exp.replace(exp, ((OldExp) old).getExp(),
                                ((OldExp) replacement).getExp());
                if (tmp != null) {
                    exp = tmp;
                    return this;
                }
            }
            else {
                Exp tmp =
                        Exp.replace(exp, ((OldExp) old).getExp(), replacement);
                if (tmp != null)
                    return tmp;
            }
        }
        else {
            if (exp instanceof FunctionExp) {
                if (old instanceof VarExp
                        && !((FunctionExp) exp).getName().equals(
                                ((VarExp) old).getName().toString())) {
                    if (!(replacement instanceof VarExp && (((VarExp) replacement)
                            .getName().getName().startsWith("?") || ((VarExp) replacement)
                            .getName().getName().startsWith("_")))) {
                        exp = Exp.replace(exp, old, replacement);
                    }
                    else {
                        List<FunctionArgList> paramList =
                                ((FunctionExp) exp)
                                        .replaceVariableInParamListWithExp(
                                                ((FunctionExp) exp)
                                                        .getParamList(), old,
                                                replacement);
                        ((FunctionExp) exp).setParamList(paramList);
                    }
                }
                return this;
            }
        }
        return this;
    }

    public Exp remember() {
        return (exp).remember();
    }

    public void prettyPrint() {
        System.out.print("#");
        exp.prettyPrint();
    }

    public Exp copy() {
        Exp newExp = Exp.copy(exp);
        newExp = new OldExp(getLocation(), newExp);
        newExp.setType(getType());
        return newExp;
    }
}
