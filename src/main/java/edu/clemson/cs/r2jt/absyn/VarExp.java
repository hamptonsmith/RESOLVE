/*
 * This softare is released under the new BSD 2006 license.
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
 * VarExp.java
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
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;

public class VarExp extends Exp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The qualifier member. */
    private PosSymbol qualifier;

    /** The name member. */
    private PosSymbol name;

    private boolean local;

    public static final int NONE = 0;
    public static final int FORALL = 1;
    public static final int EXISTS = 2;
    public static final int UNIQUE = 3;

    private int quantification = NONE;

    // ===========================================================
    // Constructors
    // ===========================================================

    public VarExp() {};

    public VarExp(Location location, PosSymbol qualifier, PosSymbol name) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        local = false;
    }

    public VarExp(Location location, PosSymbol qualifier, PosSymbol name,
            int quantifier) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        local = false;
        this.quantification = quantifier;
    }

    // special constructor to use when we can determine the statement return 
    // type while building the symbol table in RBuilder.g
    public VarExp(Location location, PosSymbol qualifier, PosSymbol name,
            Type bType) {
        this.location = location;
        this.qualifier = qualifier;
        this.name = name;
        local = false;
        super.bType = bType;
    }

    public boolean equivalent(Exp e) {
        boolean retval;

        if (e instanceof VarExp) {
            VarExp eAsVarExp = (VarExp) e;
            retval =
                    (posSymbolEquivalent(qualifier, eAsVarExp.qualifier) && (posSymbolEquivalent(
                            name, eAsVarExp.name)));
        }
        else {
            retval = false;
        }

        return retval;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        Exp retval = new VarExp(location, qualifier, name, quantification);
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

    /** Returns the value of the qualifier variable. */
    public PosSymbol getQualifier() {
        return qualifier;
    }

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    public boolean getIsLocal() {
        return local;
    }

    public int getQuantification() {
        return quantification;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the qualifier variable to the specified value. */
    public void setQualifier(PosSymbol qualifier) {
        this.qualifier = qualifier;
    }

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    public void setIsLocal(boolean local) {
        this.local = local;
    }

    public void setQuantification(int quantification) {
        this.quantification = quantification;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitVarExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getVarExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("VarExp\n");

        if (qualifier != null) {
            sb.append(qualifier.asString(indent + increment, increment));
        }

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String toString(int indent) {
        // 	Environment   env	= Environment.getInstance();
        //  	if(env.isabelle()){return toIsabelleString(indent);};

        StringBuffer sb = new StringBuffer();
        String strName = "";
        if (name != null)
            strName = name.toString();

        printSpace(indent, sb);
        //    if (name != null) {
        //        sb.append(name.toString());
        //    }

        if (name != null) {
            int index = 0;
            int num = 0;
            while ((strName.charAt(index)) == '?') {
                num++;
                index++;
            }
            if (strName.substring(num).startsWith("Conc_")) {
                strName = strName.replace("Conc_", "Conc.");
            }
            sb.append(strName.substring(index, strName.length()));
            for (int i = 0; i < num; i++) {
                sb.append("'");
            }
        }

        return sb.toString();
    }

    /*public String toIsabelleString(int indent) {
        Environment env = Environment.getInstance();
        StringBuffer sb = new StringBuffer();
    	printSpace(indent, sb);
    	String strName = name.toString();
        if (name != null) {
        	int index = 0;
        	int num = 0;
        	while((strName.charAt(index))== '?'){
        		num++;
        		index++;
        	}
        	sb.append(strName.substring(index, strName.length()));
        	if(num > 0)
        		sb.append(num);
        }
    //    if(env.pretty()){
            try{
            	Type type = null; //this.getType();
            	String typeStr = null;
            	if(type == null){
            		type = AssertiveCode.getCurrVar(this.name).getType();
    	    		typeStr = type.asString();

            	}
            	if(type == null){
            		// added the null (for ErrorHandler) after changing it to not be abstract
    	        	MathExpTypeResolver METR = new MathExpTypeResolver(env.getSymbolTable(env.getModuleID(env.getTargetFile())),
    	        												new TypeMatcher(), null);
    	        	type = METR.getVarExpType(this);
    	        		  
    	    		typeStr = type.toMath().toString().substring(0, (type.toMath()).toString().lastIndexOf("_Theory"));
            	}
            	if(typeStr == null) return sb.toString();
            	
            
        		if(typeStr.equals("Integer") || typeStr.equals("*Z")){typeStr = "int";}
        		else if(typeStr.equals("*String") || typeStr.equals("*Str(*Entry)")){typeStr = "'obj string";}
        		else if(typeStr.equals("*Entry")  || typeStr.equals("*Entry")){typeStr = "'obj";}
        		else return sb.toString();
            	
            	sb.append("::" + typeStr);
                sb.append(")");
                sb.insert(0, "(");
            } catch(Exception e){
            
            }
    //        }


        return sb.toString();
    }*/

    public boolean containsVar(String varName, boolean IsOldExp) {
        if (this.getName() != null) {
            if (!IsOldExp) {
                if (varName.equals(name.getName())) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                /* If IsOldExp is true, it means we were looking for an OldExp class
                   above this and have not yet found one. */
                return false;
            }
        }
        else
            return false;
    }

    public Object clone() {
        PosSymbol newName = null;
        if (this.getName() != null) {
            newName = createPosSymbol((this.getName().toString()));
        }

        VarExp clone = new VarExp(location, qualifier, newName);
        clone.setQuantification(this.quantification);
        clone.setType(this.type);
        clone.setMathType(getMathType());
        clone.setMathTypeValue(getMathTypeValue());
        return clone;
    }

    private PosSymbol createPosSymbol(String name) {
        PosSymbol posSym = new PosSymbol();
        posSym.setSymbol(Symbol.symbol(name));
        return posSym;
    }

    public List<Exp> getSubExpressions() {
        return new List<Exp>();
    }

    public void setSubExpression(int index, Exp e) {}

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof VarExp)) {
            return false;
        }
        if (qualifier != null && (((VarExp) e2).getQualifier() != null)) {
            if (!(qualifier.equals(((VarExp) e2).getQualifier().getName()))) {
                return false;
            }
        }
        if (!(name.equals(((VarExp) e2).getName().getName()))) {
            return false;
        }
        return true;
    }

    public Exp replace(Exp old, Exp replacement) {
        if (name != null) {
            if (old instanceof VarExp) {
                if (((VarExp) old).getName().toString().equals(name.toString())) {
                    return Exp.copy(replacement);
                }
                /*else {
                	return this;
                }*/
            }
        }

        return this;
    }

    public void prettyPrint() {
        if (qualifier != null)
            System.out.print(qualifier.getName() + ".");
        System.out.print(name.getName() + "(");
        if (quantification == NONE) {
            System.out.print("NONE");
        }
        else if (quantification == FORALL) {
            System.out.print("FORALL");
        }
        else if (quantification == EXISTS) {
            System.out.print("EXISTS");
        }
        else {
            System.out.print("UNIQUE");
        }
        System.out.print(")");
    }

    public Exp copy() {
        VarExp retval;
        PosSymbol newQualifier = null;
        if (qualifier != null)
            newQualifier = qualifier.copy();
        PosSymbol newName = name.copy();
        retval = new VarExp(location, newQualifier, newName, quantification);
        retval.setType(type);
        retval.setIsLocal(local);

        return retval;
    }

    public Exp simplify() {
        return this;
    }

    public boolean equals(Exp exp) {
        if (exp instanceof VarExp) {
            if (this.name.equals(((VarExp) exp).getName().getSymbol()))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsExistential() {
        return (quantification == EXISTS);
    }
}
