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
 * VariableDotExp.java
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
import edu.clemson.cs.r2jt.collections.Iterator;

public class VariableDotExp extends VariableExp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The segments member. */
    private List<VariableExp> segments;

    /** The semanticExp member. */
    private VariableExp semanticExp;

    // ===========================================================
    // Constructors
    // ===========================================================

    public VariableDotExp() {};

    public VariableDotExp(Location location, List<VariableExp> segments,
            VariableExp semanticExp) {
        this.location = location;
        this.segments = segments;
        this.semanticExp = semanticExp;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        List<VariableExp> newSegments = new List<VariableExp>();
        for (VariableExp v : segments) {
            newSegments.add((VariableExp) substitute(v, substitutions));
        }

        return new VariableDotExp(location, newSegments,
                (VariableExp) substitute(semanticExp, substitutions));
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

    /** Returns the value of the segments variable. */
    public List<VariableExp> getSegments() {
        return segments;
    }

    /** Returns the value of the semanticExp variable. */
    public VariableExp getSemanticExp() {
        return semanticExp;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the segments variable to the specified value. */
    public void setSegments(List<VariableExp> segments) {
        this.segments = segments;
    }

    /** Sets the semanticExp variable to the specified value. */
    public void setSemanticExp(VariableExp semanticExp) {
        this.semanticExp = semanticExp;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitVariableDotExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getVariableDotExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("VariableDotExp\n");

        if (segments != null) {
            sb.append(segments.asString(indent + increment, increment));
        }

        if (semanticExp != null) {
            sb.append(semanticExp.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public String toString(int indent) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);

        printSpace(indent, sb);

        sb.append(segmentsToString(this.segments));

        if (semanticExp != null) {
            sb.append(semanticExp.toString(0));
        }

        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        Iterator<VariableExp> i = segments.iterator();
        while (i.hasNext()) {
            VariableExp temp = i.next();
            if (temp != null) {
                if (temp.containsVar(varName, IsOldExp)) {
                    return true;
                }
            }
        }
        if (semanticExp != null) {
            if (semanticExp.containsVar(varName, IsOldExp)) {
                return true;
            }
        }
        return false;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        Iterator<VariableExp> segmentsIt = segments.iterator();
        while (segmentsIt.hasNext()) {
            list.add((Exp) (segmentsIt.next()));
        }
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        segments.set(index, (VariableExp) e);
    }

    private String segmentsToString(List<VariableExp> segments) {
        StringBuffer sb = new StringBuffer();
        //Environment env = Environment.getInstance();
        if (segments != null) {
            Iterator<VariableExp> i = segments.iterator();

            while (i.hasNext()) {
                sb.append(i.next().toString(0));
                if (i.hasNext())// && !env.isabelle())
                    sb.append(".");
            }
        }
        return sb.toString();
    }

    public Exp copy() {
        Exp copy =
                new VariableDotExp(location, new List<VariableExp>(segments),
                        semanticExp);

        copy.setType(type);

        return copy;
    }

    public Object clone() {
        return Exp.copy(this);
    }

    public Exp replace(Exp old, Exp replacement) {
        if (old instanceof DotExp) {
            if (old.equals(this)) {
                return replacement;
            }
        }

        if ((old instanceof VarExp || old instanceof OldExp)) {
            Iterator<VariableExp> it = segments.iterator();

            if (it.hasNext()) {
                Exp name = it.next();
                if (old instanceof VarExp && name instanceof VarExp) {
                    if (((VarExp) old).getName().toString().equals(
                            ((VarExp) name).getName().toString())) {
                        segments.remove(0);
                        segments.add(0, (VariableExp) (Exp.clone(replacement)));

                        return this;
                    }
                }
                else if (old instanceof OldExp && name instanceof OldExp) {
                    name = Exp.replace(name, old, replacement);
                    if (name != null) {
                        segments.remove(0);
                        segments.add(0, (VariableExp) (Exp.clone(name)));
                        return this;
                    }
                }
            }

            if (it.hasNext()) {
                Exp name = it.next();
                name = Exp.replace(name, old, replacement);
                if (name != null && name instanceof VariableExp) {
                    segments.remove(1);
                    segments.add(1, (VariableExp) (Exp.clone(name)));
                    return this;
                }
            }
        }

        return this;
    }

}
