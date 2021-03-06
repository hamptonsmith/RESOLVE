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
 * IfExp.java
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

public class IfExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The test member. */
    private Exp test;

    /** The thenclause member. */
    private Exp thenclause;

    /** The elseclause member. */
    private Exp elseclause;

    // ===========================================================
    // Constructors
    // ===========================================================

    public IfExp() {};

    public IfExp(Location location, Exp test, Exp thenclause, Exp elseclause) {
        this.location = location;
        this.test = test;
        this.thenclause = thenclause;
        this.elseclause = elseclause;
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

    /** Returns the value of the test variable. */
    public Exp getTest() {
        return test;
    }

    /** Returns the value of the thenclause variable. */
    public Exp getThenclause() {
        return thenclause;
    }

    /** Returns the value of the elseclause variable. */
    public Exp getElseclause() {
        return elseclause;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the test variable to the specified value. */
    public void setTest(Exp test) {
        this.test = test;
    }

    /** Sets the thenclause variable to the specified value. */
    public void setThenclause(Exp thenclause) {
        this.thenclause = thenclause;
    }

    /** Sets the elseclause variable to the specified value. */
    public void setElseclause(Exp elseclause) {
        this.elseclause = elseclause;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        return new IfExp(location, substitute(test, substitutions), substitute(
                thenclause, substitutions), substitute(elseclause,
                substitutions));
    }

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitIfExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getIfExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("IfExp\n");

        if (test != null) {
            sb.append(test.asString(indent + increment, increment));
        }

        if (thenclause != null) {
            sb.append(thenclause.asString(indent + increment, increment));
        }

        if (elseclause != null) {
            sb.append(elseclause.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public String toString(int indent) {
        //Environment   env	= Environment.getInstance();
        //if(env.isabelle()){return toIsabelleString(indent);};    	
        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("If ");
        sb.append(test.toString(0));

        sb.append(" then ");

        sb.append("(" + thenclause.toString(0) + ")");

        if (elseclause != null) {
            sb.append("else ");
            sb.append("(" + elseclause.toString(0) + ")");
        }

        return sb.toString();
    }

    public String toIsabelleString(int indent) {
        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);

        sb.append(test.toString(0));

        sb.append(" --> ");

        sb.append("(" + thenclause.toString(0) + ")");

        if (elseclause != null) {
            sb.append("& not(" + test.toString(0) + ")");
            sb.append(" --> ");
            sb.append("(" + elseclause.toString(0) + ")");
        }

        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        Boolean found = false;
        if (test != null) {
            found = test.containsVar(varName, IsOldExp);
        }
        if (!found && thenclause != null) {
            found = thenclause.containsVar(varName, IsOldExp);
        }
        if (!found && elseclause != null) {
            found = elseclause.containsVar(varName, IsOldExp);
        }
        return found;
    }

    public Object clone() {
        IfExp clone = new IfExp();
        if (test != null)
            clone.setTest((Exp) Exp.clone(this.getTest()));
        if (elseclause != null)
            clone.setElseclause((Exp) Exp.clone(this.getElseclause()));
        if (thenclause != null)
            clone.setThenclause((Exp) Exp.clone(this.getThenclause()));
        clone.setLocation(this.getLocation());
        clone.setType(getType());
        return clone;
    }

    public Exp replace(Exp old, Exp replacement) {
        if (!(old instanceof IfExp)) {
            if (test != null) {
                Exp testcl = Exp.replace(test, old, replacement);
                if (testcl != null)
                    this.setTest(testcl);
            }
            if (thenclause != null) {
                Exp then = Exp.replace(thenclause, old, replacement);
                if (then != null)
                    this.setThenclause(then);
            }
            if (elseclause != null) {
                Exp elsecl = Exp.replace(elseclause, old, replacement);

                if (elsecl != null)
                    this.setElseclause(elsecl);
            }

            return this;
        }
        else
            return this;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(test);
        list.add(thenclause);
        list.add(elseclause);
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        switch (index) {
        case 0:
            test = e;
            break;
        case 1:
            thenclause = e;
            break;
        case 2:
            elseclause = e;
            break;
        }
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof IfExp)) {
            return false;
        }
        return true;
    }

    public Exp remember() {
        if (test instanceof OldExp)
            this.setTest(((OldExp) (test)).getExp());
        if (elseclause instanceof OldExp)
            this.setElseclause(((OldExp) (elseclause)).getExp());
        if (thenclause instanceof OldExp)
            this.setThenclause(((OldExp) (thenclause)).getExp());

        if (test != null)
            test = test.remember();
        if (elseclause != null)
            elseclause = elseclause.remember();
        if (thenclause != null)
            thenclause = thenclause.remember();

        return this;
    }

    public void prettyPrint() {
        System.out.print("if ");
        test.prettyPrint();
        System.out.println();
        System.out.print("then ");
        thenclause.prettyPrint();
        if (elseclause != null) {
            System.out.println();
            System.out.print("else ");
            elseclause.prettyPrint();
        }
    }

    public Exp copy() {
        Exp newTest = Exp.copy(test);
        Exp newThenclause = Exp.copy(thenclause);

        Exp newElseclause = null;
        if (elseclause != null) {
            newElseclause = Exp.copy(elseclause);
        }

        Exp result = new IfExp(null, newTest, newThenclause, newElseclause);
        result.setType(getType());
        return result;
    }

}
