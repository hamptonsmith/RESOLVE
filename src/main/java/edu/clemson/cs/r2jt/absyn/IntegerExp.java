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
 * IntegerExp.java
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
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;

public class IntegerExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    private PosSymbol qualifier;

    /** The value member. */
    private int value;

    // ===========================================================
    // Constructors
    // ===========================================================

    public IntegerExp() {};

    public IntegerExp(Location location, PosSymbol qualifier, int value) {
        this.location = location;
        this.qualifier = qualifier;
        this.value = value;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        Exp retval = new IntegerExp(location, qualifier, value);
        retval.setType(type);
        retval.setMathType(getMathType());
        retval.setMathTypeValue(getMathTypeValue());
        return retval;
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

    public PosSymbol getQualifier() {
        return qualifier;
    }

    /** Returns the value of the value variable. */
    public int getValue() {
        return value;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    public void setQualifier(PosSymbol qualifier) {
        this.qualifier = qualifier;
    }

    /** Sets the value variable to the specified value. */
    public void setValue(int value) {
        this.value = value;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitIntegerExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getIntegerExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("IntegerExp\n");

        if (qualifier != null) {
            printSpace(indent + increment, sb);
            sb.append(qualifier + "\n");
        }

        printSpace(indent + increment, sb);
        sb.append(value + "\n");

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String toString(int indent) {

        StringBuffer sb = new StringBuffer();

        if (qualifier != null) {
            sb.append(qualifier + ".");
        }

        sb.append(value);

        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        return false;
    }

    public Object clone() {
        IntegerExp clone = new IntegerExp();
        clone.setQualifier(this.qualifier);
        clone.setValue(this.value);
        clone.setLocation(this.getLocation());
        clone.setType(type);
        clone.setMathType(getMathType());
        clone.setMathTypeValue(getMathTypeValue());
        return clone;
    }

    public List<Exp> getSubExpressions() {
        return new List<Exp>();
    }

    public void setSubExpression(int index, Exp e) {

    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof IntegerExp)) {
            return false;
        }
        if (qualifier != null && ((IntegerExp) (e2)).getQualifier() != null) {
            if (!(qualifier.equals(((IntegerExp) e2).getQualifier().getName()))) {
                return false;
            }
        }
        if (value != ((IntegerExp) e2).getValue()) {
            return false;
        }
        return true;
    }

    public Exp replace(Exp old, Exp replace) {
        if (!(old instanceof IntegerExp)) {
            return null;
        }
        else if (((IntegerExp) old).getValue() == value)
            return replace;
        else
            return null;
    }

    public void prettyPrint() {
        if (qualifier != null) {
            System.out.print(qualifier.getName() + ".");
        }
        System.out.print(value);
    }

    public Exp copy() {
        Exp retval = new IntegerExp(null, qualifier, value);
        retval.setType(type);
        retval.setMathType(getMathType());
        retval.setMathTypeValue(getMathTypeValue());
        return retval;
    }

    public boolean equivalent(Exp e) {
        boolean retval = e instanceof IntegerExp;

        if (retval) {
            IntegerExp eAsIntegerExp = (IntegerExp) e;
            retval = (value == eAsIntegerExp.value);
        }

        return retval;
    }
}
