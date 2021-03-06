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
 * Verifier.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 2005-2010
 * Resolve Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.verification;

// import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.type.*;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.collections.Iterator; // import
// edu.clemson.cs.r2jt.errors.*;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.errors.ErrorHandler;

public class PrintAssertions extends ResolveConceptualVisitor {

    private CompileEnvironment myInstanceEnvironment;
    private ErrorHandler err;
    private String name;

    private String output = "";
    private int indent = 0;
    private boolean isabelle = false;
    private StringBuffer sb = new StringBuffer();
    private boolean single_line = false;

    /**
     * Construct a Verifier.
     */
    public PrintAssertions(CompileEnvironment env) {
        this.myInstanceEnvironment = env;
        name = env.getTargetFile().getName();
        name = name.substring(0, name.indexOf("."));
        isabelle = env.flags.isFlagSet(Verifier.FLAG_ISABELLE_VC);
        this.err = env.getErrorHandler();
    }

    public void setIsabelle() {
        isabelle = true;
    }

    public String visitAssertion(Exp exp) {
        if (exp == null) {
            return output;
        }
        exp.accept(this);
        return sb.toString();
    }

    public void visitDec(Dec dec) {
        if (dec == null)
            return;

        dec.accept(this);
    }

    public String clearAndVisitAssertion(Exp exp) {
        sb.setLength(0);
        String mystring = visitAssertion(exp);
        return mystring;
    }

    public void visitLambdaExp(LambdaExp exp) {
        printSpace(indent, sb);

        sb.append("lambda " + exp.getName().toString() + ": ");
        if (exp.getTy() != null)
            sb.append(exp.getTy().toString(0));
        sb.append(" (");
        visitAssertion(exp.getBody());
        sb.append(")");
    }

    public void visitAlternativeExp(AlternativeExp exp) {
        printSpace(indent, sb);

        sb.append("{{");
        Iterator<AltItemExp> it = exp.getAlternatives().iterator();
        while (it.hasNext()) {
            visitAssertion(it.next());
            sb.append("\n");
        }
        sb.append("}}");
    }

    public void visitAltItemExp(AltItemExp exp) {
        visitAssertion(exp.getAssignment());

        if (exp.getTest() != null) {
            sb.append(" if ");
            visitAssertion(exp.getTest());
        }
        else {
            sb.append(" otherwise");
        }
    }

    public void visitFunctionExp(FunctionExp exp) {
        if (!isabelle) {
            printSpace(indent, sb);

            if (exp.getQualifier() != null) {
                sb.append(exp.getQualifier() + ".");
            }
            if (exp.getName() != null) {
                String strName = exp.getName().toString();
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

            sb.append("(");

            boolean old_single_line = single_line;
            indent = 0;
            single_line = true;
            sb.append(paramListToString(exp.getParamList()));
            single_line = old_single_line;

            sb.append(")");
        }
        else {
            printSpace(indent, sb);
            sb.append("(");
            sb.append(exp.getName().toString() + " ");

            sb.append(isabParamListToString(exp.getParamList()));
            sb.append(")");
        }

    }

    public void visitPrefixExp(PrefixExp exp) {
        if (!isabelle) {
            printSpace(indent, sb);
            if (exp.getSymbol() != null)
                sb.append(exp.getSymbol().getName().toString());
            if (exp.getArgument() != null) {
                int oldIndent = indent;
                boolean old_single_line = single_line;
                indent = 0;
                single_line = true;
                sb.append("(");
                visitAssertion(exp.getArgument());
                sb.append(")");
                indent = oldIndent;
                single_line = old_single_line;
            }

        }
        else {
            printSpace(indent, sb);
            if (exp.getSymbol() != null)
                sb.append(exp.getSymbol().getName().toString());
            if (exp.getArgument() != null) {
                int oldIndent = indent;
                indent = 0;
                sb.append("(");
                visitAssertion(exp.getArgument());
                sb.append(")");
                indent = oldIndent;
            }

        }
    }

    public void visitIterativeExp(IterativeExp exp) {

        printSpace(indent, sb);

        if (exp.getOperator() == IterativeExp.SUM)
            sb.append("Sum ");
        else if (exp.getOperator() == IterativeExp.PRODUCT)
            sb.append("Product ");
        else if (exp.getOperator() == IterativeExp.CONCATENATION)
            sb.append("Concatenation ");
        else if (exp.getOperator() == IterativeExp.UNION)
            sb.append("Union ");
        else
            sb.append("Intersection ");
        sb.append(exp.getVar().toString(0) + " where ");

        sb.append(exp.getWhere().toString(0) + ", ");
        sb.append(exp.getBody().toString(0));

    }

    public void visitOutfixExp(OutfixExp exp) {
        int oldIndent;
        if (!isabelle) {
            switch (exp.getOperator()) {
            case 1:
                sb.append("<");
                oldIndent = indent;
                indent = 0;
                visitAssertion(exp.getArgument());
                indent = oldIndent;
                sb.append(">");
                break;
            case 2:
                sb.append("DBL_ANGLE");
                break;
            case 3:
                sb.append("SQUARE");
                break;
            case 4:
                sb.append("DBL_SQUARE");
                break;
            case 5:
                sb.append("|");
                oldIndent = indent;
                indent = 0;
                visitAssertion(exp.getArgument());
                indent = oldIndent;
                sb.append("|");
                break;
            case 6:
                sb.append("DBL_BAR");
                break;
            default:
                sb.append(exp.getOperator());
            }
        }
        else {
            switch (exp.getOperator()) {
            case 1:
                sb.append("<");
                visitAssertion(exp.getArgument());
                sb.append(">");
                break;
            case 2:
                sb.append("DBL_ANGLE");
                break;
            case 3:
                sb.append("SQUARE");
                break;
            case 4:
                sb.append("DBL_SQUARE");
                break;
            case 5:
                sb.append("length(");
                oldIndent = indent;
                indent = 0;
                visitAssertion(exp.getArgument());
                indent = oldIndent;
                sb.append(") ");
                break;
            case 6:
                sb.append("DBL_BAR");
                break;
            default:
                sb.append(exp.getOperator());
            }
        }

    }

    public void visitInfixExp(InfixExp exp) {

        if (myInstanceEnvironment.flags.isFlagSet(Verifier.FLAG_LISTVCS_VC)
                && !single_line) {
            getInfixAltString(exp, indent);
        }
        else if (isabelle) {
            getInfixIsabelleString(exp, indent);
        }
        else {

            if (exp.getLeft() != null) {
                int oldIndent = indent;
                indent = 0;
                if (exp.getOpName().toString().equals("implies")) {
                    printSpace(indent, sb);
                    sb.append("(");
                    visitAssertion(exp.getLeft());
                    sb.append(" ");
                }
                else {
                    sb.append("(");
                    visitAssertion(exp.getLeft());
                    sb.append(" ");
                }
                indent = oldIndent;
            }

            if (exp.getOpName() != null) {

                if (!AssertiveCode.isProvePart()
                        && exp.getOpName().toString().equals("and")
                        && !single_line) {
                    if (!single_line)
                        sb.append(exp.getOpName().toString() + "\n");
                }
                else if (AssertiveCode.isProvePart()
                        && exp.getOpName().toString().equals("and")) {
                    sb.append(" " + exp.getOpName().toString() + " ");
                }
                else {
                    sb.append(" " + exp.getOpName().toString() + " ");
                }
            }

            if (exp.getRight() != null) {
                if (exp.getOpName().toString().equals("implies")) {
                    /* This is an implication */
                    if (exp.getRight() instanceof InfixExp
                            && !((InfixExp) exp.getRight()).getOpName()
                                    .toString().equals("implies")) {
                        /* And the right Exp is NOT an implication */
                        sb.append("\n");
                        printSpace(indent + 4, sb);
                        int oldIndent = indent;
                        indent = indent + 4;
                        visitAssertion(exp.getRight());
                        sb.append(")");
                        indent = oldIndent;
                    }
                    else if (exp.getRight() instanceof InfixExp) {
                        /* And the right is an Implication, but could 
                         * contain an implication or is an and/or statement
                         */
                        sb.append("\n");
                        visitAssertion(exp.getRight());
                        sb.append(")");
                    }
                    else {
                        int oldIndent = indent;
                        indent = indent + 4;
                        sb.append("\n");
                        visitAssertion(exp.getRight());
                        sb.append(")");
                        indent = oldIndent;
                    }
                }
                else /* This is Not an Implication */
                if (exp.getRight() instanceof InfixExp
                        && !((InfixExp) exp.getRight()).getOpName().toString()
                                .equals("implies")) {
                    /* And the right Exp is NOT an implication */
                    visitAssertion(exp.getRight());
                    sb.append(")");

                }
                else if (exp.getRight() instanceof InfixExp) {
                    /* And the right is an Implication, but could 
                     * contain an implication or is an and/or statement
                     */
                    sb.append("\n");
                    visitAssertion(exp.getRight());
                    sb.append(")");
                }
                else {
                    int oldIndent = indent;
                    indent = 0;
                    visitAssertion(exp.getRight());
                    sb.append(")");
                    indent = oldIndent;
                }
            }

        }
    }

    /** Returns a formatted text string of this class. */
    public void getInfixAltString(InfixExp exp, int indent) {
        getInfixAltString(exp, indent, new AtomicInteger(0));

    }

    public void getInfixAltString(InfixExp exp, int indent,
            AtomicInteger mycount) {

        printSpace(indent, sb);
        int oldindent = indent;
        boolean old_single_line = single_line;

        if (exp.getOpName().toString().equals("or")) {
            indent = 0;
            single_line = true;

        }

        if (exp.getLeft() != null) {
            if (exp.getOpName().toString().equals("implies")) {
                int oldIndent = indent;
                indent = 0;
                visitAssertion(exp.getLeft());
                indent = oldIndent;
            }
            else {
                if (exp.getOpName().toString().equals("and")) {
                    if ((exp.getLeft() instanceof InfixExp && ((InfixExp) exp
                            .getLeft()).getOpName().toString().equals("and"))) {
                        getInfixAltString((InfixExp) exp.getLeft(), 0, mycount);
                    }
                    else if ((exp.getLeft() instanceof InfixExp && (((InfixExp) exp
                            .getLeft()).getOpName().toString().equals("or")))) {
                        int count = mycount.intValue();
                        count++;
                        mycount.set(count);
                        sb.append(count + ": ");
                        getInfixAltString((InfixExp) exp.getLeft(), 0, mycount);
                    }
                    else {
                        int count = mycount.intValue();
                        count++;
                        mycount.set(count);
                        sb.append(count + ": ");
                        visitAssertion(exp.getLeft());
                    }
                }
                else if (exp.getLeft() instanceof InfixExp) {
                    sb.append("(");
                    getInfixAltString(((InfixExp) exp.getLeft()), 0, mycount);
                }
                else {
                    int oldIndent = indent;
                    indent = 0;
                    sb.append("(");
                    visitAssertion(exp.getLeft());
                    sb.append(" ");
                    indent = oldIndent;
                }
            }
        }

        if (exp.getOpName() != null) {
            if (!AssertiveCode.isProvePart()
                    && exp.getOpName().toString().equals("and")) {
                sb.append("\n");
            }
            else
                sb.append(" " + exp.getOpName().toString() + " ");
        }

        if (exp.getRight() != null) {
            if (exp.getOpName().toString().equals("implies")) {
                /* This is an implication */
                if (exp.getRight() instanceof InfixExp
                        && !((InfixExp) exp.getRight()).getOpName().toString()
                                .equals("implies")) {
                    /* And the right Exp is NOT an implication */
                    sb.append("\n");
                    printSpace(indent, sb);
                    getInfixAltString(((InfixExp) exp.getRight()), indent,
                            mycount);
                }
                else if (exp.getRight() instanceof InfixExp) {
                    /* And the right is an Implication, but could 
                     * contain an implication or is an and/or statement
                     */
                    sb.append("\n");
                    getInfixAltString((InfixExp) exp.getRight(), indent,
                            mycount);
                }
                else {
                    sb.append("\n");
                    visitAssertion(exp.getRight());
                }
            }
            else /* This is Not an Implication */
            if (exp.getRight() instanceof InfixExp
                    && !((InfixExp) exp.getRight()).getOpName().toString()
                            .equals("implies")) {
                if (exp.getOpName().toString().equals("and")) {
                    if (((InfixExp) exp.getRight()).getOpName().toString()
                            .equals("and")) {
                        getInfixAltString((InfixExp) exp.getRight(), indent,
                                mycount);
                    }
                    else {

                        int count = mycount.intValue();
                        count++;
                        mycount.set(count);
                        sb.append(count + ": ");
                        visitAssertion(exp.getRight());
                    }
                }
                else {
                    /* And the right Exp is NOT an implication */
                    visitAssertion(exp.getRight());
                    sb.append(")");
                }

            }
            else if (exp.getRight() instanceof InfixExp) {
                /* And the right is an Implication, but could 
                 * contain an implication or is an and/or statement
                 */
                if (((InfixExp) exp.getRight()).getOpName().toString().equals(
                        "and")) {
                    getInfixAltString((InfixExp) exp.getRight(), indent,
                            mycount);
                }
                else {
                    int count = mycount.intValue();
                    count++;
                    mycount.set(count);
                    sb.append("" + count + ": ");
                    visitAssertion(exp.getRight());
                }
            }
            else {
                if (exp.getOpName().toString().equals("and")) {
                    if (AssertiveCode.isProvePart()) {
                        visitAssertion(exp.getRight());
                    }
                    else {
                        int count = mycount.intValue();
                        count++;
                        mycount.set(count);
                        sb.append("" + count + ": ");
                        int oldIndent = indent;
                        indent = 0;
                        visitAssertion(exp.getRight());
                        indent = oldIndent;
                    }
                }
                else {
                    int oldIndent = indent;
                    indent = 0;

                    visitAssertion(exp.getRight());
                    sb.append(")");
                    indent = oldIndent;
                }

            }
        }

        if (exp.getOpName().toString().equals("or")) {
            single_line = old_single_line;
            indent = oldindent;
        }

    }

    /** Returns a formatted text string of this class. */
    public String printLocation(InfixExp exp, final AtomicInteger mycount) {

        StringBuffer sb = new StringBuffer();

        if (exp.getLeft() != null) {
            if (exp.getOpName().toString().equals("implies")) {

            }
            else {
                if (exp.getOpName().toString().equals("and")) {
                    if (exp.getLeft() instanceof InfixExp
                            && ((InfixExp) exp.getLeft()).getOpName()
                                    .toString().equals("and")) {
                        sb.append(""
                                + ((InfixExp) exp.getLeft())
                                        .printLocation(mycount) + "");
                    }
                    else {
                        int count = mycount.intValue();
                        count++;
                        mycount.set(count);
                        if (exp.getLeft().getLocation() != null) {
                            sb.append("\n" + mycount + ": "
                                    + exp.getLeft().getLocation() + ": "
                                    + exp.getLeft().getLocation().getDetails());
                        }
                        else {
                            sb.append("" + mycount + ": ");
                        }
                    }
                }
            }
        }

        if (exp.getOpName() != null) {
            if (AssertiveCode.isProvePart()
                    || !exp.getOpName().toString().equals("and")) {
                int count = mycount.intValue();
                count++;
                mycount.set(count);
                if (exp.getLeft().getLocation() != null) {
                    sb.append("\n" + mycount + ": " + exp.getLocation() + ": "
                            + exp.getLocation().getDetails());
                }
                else {
                    sb.append("" + mycount + ": ");
                }
            }
        }

        if (exp.getRight() != null) {
            if (!exp.getOpName().toString().equals("implies")) {
                if (exp.getRight() instanceof InfixExp
                        && !((InfixExp) exp.getRight()).getOpName().toString()
                                .equals("implies")) {
                    if (exp.getOpName().toString().equals("and")) {
                        if (((InfixExp) exp.getRight()).getOpName().toString()
                                .equals("and")) {
                            sb.append(""
                                    + ((InfixExp) exp.getRight())
                                            .printLocation(mycount) + "");
                        }
                        else {
                            int count = mycount.intValue();
                            count++;
                            mycount.set(count);
                            if (exp.getRight().getLocation() != null) {
                                sb.append("\n"
                                        + mycount
                                        + ": "
                                        + exp.getRight().getLocation()
                                        + ": "
                                        + exp.getRight().getLocation()
                                                .getDetails());
                            }
                            else {
                                sb.append("\n" + mycount + ": ");
                            }
                        }
                    }
                }
                else {
                    int count = mycount.intValue();
                    count++;
                    mycount.set(count);
                    if (exp.getRight().getLocation() != null) {
                        sb.append("\n" + mycount + ": "
                                + exp.getRight().getLocation() + ": "
                                + exp.getRight().getLocation().getDetails());
                    }
                    else {
                        sb.append("\n" + mycount + ": ");
                    }
                }
            }
        }

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String getInfixIsabelleString(InfixExp exp, int indent) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);

        if (exp.getLeft() != null) {
            if (exp.getOpName().toString().equals("implies")) {
                sb.append("" + exp.getLeft().toString(0) + "");
            }
            else {
                if (exp.getOpName().toString().equals("and"))
                    sb.append("" + exp.getLeft().toString(0) + "");
                else
                    sb.append("(" + exp.getLeft().toString(0) + " ");
            }
        }

        if (exp.getOpName() != null) {
            if (!AssertiveCode.isProvePart()
                    && exp.getOpName().toString().equals("and")) {
                sb.append(";\n");
            }
            else if (AssertiveCode.isProvePart()
                    && exp.getOpName().toString().equals("and")) {
                sb.append(" & ");
            }
            else if (exp.getOpName().toString().equals("implies")) {
                sb.append(" --> ");
            }
            else if (exp.getOpName().toString().equals("o")) {
                sb.append(" * ");
            }
            else
                sb.append(exp.getOpName().toString() + " ");
        }

        if (exp.getRight() != null) {
            if (exp.getOpName().toString().equals("implies")) {
                /* This is an implication */
                if (exp.getRight() instanceof InfixExp
                        && !((InfixExp) exp.getRight()).getOpName().toString()
                                .equals("implies")) {
                    /* And the right Exp is NOT an implication */
                    sb.append("\n");
                    printSpace(indent, sb);
                    sb.append(exp.getRight().toString(indent) + "");
                }
                else if (exp.getRight() instanceof InfixExp) {
                    /* And the right is an Implication, but could 
                     * contain an implication or is an and/or statement
                     */
                    sb.append("\n");
                    sb.append(exp.getRight().toString(indent) + "");
                }
                else
                    sb.append("\n" + exp.getRight().toString(indent) + "");
            }
            else /* This is Not an Implication */
            if (exp.getRight() instanceof InfixExp
                    && !((InfixExp) exp.getRight()).getOpName().toString()
                            .equals("implies")) {
                if (exp.getOpName().toString().equals("and"))
                    sb.append(exp.getRight().toString(indent) + "");
                else
                    /* And the right Exp is NOT an implication */
                    sb.append(exp.getRight().toString(indent) + ")");

            }
            else if (exp.getRight() instanceof InfixExp) {
                /* And the right is an Implication, but could 
                 * contain an implication or is an and/or statement
                 */
                if (exp.getOpName().toString().equals("and"))
                    sb.append("\n" + exp.getRight().toString(indent) + "");
                else
                    sb.append("\n" + exp.getRight().toString(indent) + "");
            }
            else {
                if (exp.getOpName().toString().equals("and"))
                    sb.append(exp.getRight().toString(0) + "");
                else
                    sb.append(exp.getRight().toString(0) + ")");

            }
        }

        return sb.toString();
    }

    public void visitEqualsExp(EqualsExp exp) {
        if (!isabelle) {
            printSpace(indent, sb);

            if (exp.getLeft() != null) {
                visitAssertion(exp.getLeft());
            }

            if (exp.getOperator() == 1)
                sb.append(" = ");
            else {
                sb.append(" /= ");
            }

            if (exp.getRight() != null) {
                visitAssertion(exp.getRight());
            }
        }
        else {
            printSpace(indent, sb);

            if (exp.getLeft() != null) {
                visitAssertion(exp.getLeft());
            }

            if (exp.getOperator() == 1)
                sb.append(" = ");
            else
                sb.append(" ~= ");

            if (exp.getRight() != null) {
                visitAssertion(exp.getRight());
            }
        }

    }

    public void visitIsInExp(IsInExp exp) {
        if (!isabelle) {
            printSpace(indent, sb);

            if (exp.getLeft() != null) {
                visitAssertion(exp.getLeft());
            }

            if (exp.getOperator() == IsInExp.IS_IN) {
                sb.append(" is_in ");
            }
            else {
                sb.append(" is_not_in ");
            }

            if (exp.getRight() != null) {
                visitAssertion(exp.getRight());
            }
        }
        else {
            printSpace(indent, sb);

            if (exp.getLeft() != null) {
                visitAssertion(exp.getLeft());
            }

            if (exp.getOperator() == IsInExp.IS_IN) {
                sb.append(" is in ");
            }
            else {
                sb.append(" is not in ");
            }

            if (exp.getRight() != null) {
                visitAssertion(exp.getRight());
            }
        }

    }

    public void visitOldExp(OldExp exp) {

        printSpace(indent, sb);

        if (exp != null) {
            sb.append("#");
            visitAssertion(exp.getExp());
        }
    }

    public void visitVariableNameExp(VariableNameExp exp) {

        printSpace(indent, sb);

        if (exp.getQualifier() != null) {
            sb.append(exp.getQualifier().toString() + ".");
        }

        if (!isabelle) {
            printSpace(indent, sb);

            String strName = exp.getName().toString();
            if (exp.getName() != null) {
                int index = 0;
                int num = 0;
                while ((strName.charAt(index)) == '?') {
                    num++;
                    index++;
                }
                sb.append(strName.substring(index, strName.length()));
                for (int i = 0; i < num; i++) {
                    sb.append("'");
                }
            }

        }
        else {
            StringBuffer sb2 = new StringBuffer();

            printSpace(indent, sb2);
            String strName = exp.getName().toString();
            if (exp.getName() != null) {
                int index = 0;
                int num = 0;
                while ((strName.charAt(index)) == '?') {
                    num++;
                    index++;
                }
                sb2.append(strName.substring(index, strName.length()));
                if (num > 0)
                    sb2.append(num);
            }

            try {
                Type type = null;
                String typeStr = null;
                if (type == null) {
                    type = AssertiveCode.getCurrVar(exp.getName()).getType();
                    typeStr = type.asString();

                }
                if (type == null) {
                    //type = METR.getVarExpType(exp);

                    //	typeStr = type.toMath().toString().substring(0, (type.toMath()).toString().lastIndexOf("_Theory"));
                }
                if (typeStr == null) {
                    sb.append(sb2.toString());
                    return;
                }

                if (typeStr.equals("Integer") || typeStr.equals("*Z")) {
                    typeStr = "int";
                }
                else if (typeStr.equals("*String")
                        || typeStr.equals("*Str(*Entry)")) {
                    typeStr = "'obj string";
                }
                else if (typeStr.equals("*Entry") || typeStr.equals("*Entry")) {
                    typeStr = "'obj";
                }
                else {
                    sb.append(sb2.toString());
                    return;
                }

                sb2.append("::" + typeStr);
                sb2.append(")");
                sb2.insert(0, "(");
            }
            catch (Exception e) {

            }
            sb.append(sb2.toString());

        }

    }

    public void visitVarExp(VarExp exp) {
        if (!isabelle) {
            printSpace(indent, sb);
            if (exp.getName() == null) {
                exp = exp;
            }

            if (exp.getName() != null) {
                String strName = exp.getName().toString();
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

        }
        else {
            StringBuffer sb2 = new StringBuffer();

            if (exp.getName() != null) {
                printSpace(indent, sb2);
                String strName = exp.getName().toString();
                int index = 0;
                int num = 0;
                while ((strName.charAt(index)) == '?') {
                    num++;
                    index++;
                }
                sb2.append(strName.substring(index, strName.length()));
                if (num > 0)
                    sb2.append(num);
            }

            try {
                Type type = null;
                String typeStr = null;
                if (type == null) {
                    type = AssertiveCode.getCurrVar(exp.getName()).getType();
                    typeStr = type.asString();

                }
                if (type == null) {
                    typeStr = exp.getMathType().toString();
                }
                if (typeStr == null) {
                    sb.append(sb2.toString());
                    return;
                }

                if (typeStr.equals("Integer") || typeStr.equals("*Z")) {
                    typeStr = "int";
                }
                else if (typeStr.equals("*String")
                        || typeStr.equals("*Str(*Entry)")) {
                    typeStr = "'obj string";
                }
                else if (typeStr.equals("*Entry") || typeStr.equals("*Entry")) {
                    typeStr = "'obj";
                }
                else {
                    sb.append(sb2.toString());
                    return;
                }

                sb2.append("::" + typeStr);
                sb2.append(")");
                sb2.insert(0, "(");
            }
            catch (Exception e) {

            }
            sb.append(sb2.toString());

        }

    }

    public void visitQuantExp(QuantExp exp) {
        if (!isabelle) {
            printSpace(indent, sb);
            if (exp.getWhere() != null) {
                int oldindent = indent;
                indent = 1;
                visitAssertion(exp.getWhere());
                indent = oldindent;
            }
            sb.append(printConstant(exp.getOperator()));

            List<MathVarDec> list = exp.getVars();
            Iterator<MathVarDec> i = list.iterator();

            while (i.hasNext()) {
                MathVarDec tmp = i.next();
                sb.append(" ");
                sb.append(tmp.toString(0));
            }
            sb.append(", ");
            if (exp.getBody() != null) {
                int oldindent = indent;
                boolean old_single_line = single_line;
                indent = 0;
                single_line = true;
                visitAssertion(exp.getBody());
                single_line = old_single_line;
                indent = oldindent;
            }
        }
        else {
            printSpace(indent, sb);
            if (exp.getWhere() != null) {
                int oldindent = indent;
                indent = 1;
                visitAssertion(exp.getWhere());
                indent = oldindent;
            }
            sb.append(printIsabelleConstant(exp.getOperator()));

            List<MathVarDec> list = exp.getVars();
            Iterator<MathVarDec> i = list.iterator();

            while (i.hasNext()) {
                MathVarDec tmp = i.next();
                sb.append(" ");
                sb.append(tmp.toString(0));
            }
            sb.append(", ");
            if (exp.getBody() != null) {
                int oldindent = indent;
                indent = 0;
                visitAssertion(exp.getBody());
                indent = oldindent;
            }

        }

    }

    public void visitIntegerExp(IntegerExp exp) {

        if (exp.getQualifier() != null) {
            sb.append(exp.getQualifier() + ".");
        }

        sb.append(exp.getValue());

    }

    public void visitProgramIntegerExp(ProgramIntegerExp exp) {

        sb.append(exp.getValue());

    }

    public void visitDotExp(DotExp exp) {

        printSpace(indent, sb);

        segmentsToString(exp.getSegments());

    }

    public void visitVariableDotExp(VariableDotExp exp) {

        printSpace(indent, sb);

        varSegmentsToString(exp.getSegments());

    }

    public void visitIfExp(IfExp exp) {

        if (!isabelle) {
            printSpace(indent, sb);
            sb.append("If ");
            sb.append(exp.getTest().toString(0));

            sb.append(" then ");
            sb.append("(" + exp.getThenclause().toString(0) + ")");

            if (exp.getElseclause() != null) {
                sb.append("else ");
                sb.append("(" + exp.getElseclause().toString(0) + ")");
            }

        }
        else {

            printSpace(indent, sb);
            sb.append("If ");
            sb.append(exp.getTest().toString(0));

            sb.append(" then ");
            sb.append("(" + exp.getThenclause().toString(0) + ")");

            if (exp.getElseclause() != null) {
                sb.append("else ");
                sb.append("(" + exp.getElseclause().toString(0) + ")");
            }
        }

    }

    public void visitBetweenExp(BetweenExp exp) {

        if (!isabelle) {
            printSpace(indent, sb);
            List<Exp> list = exp.getLessExps();
            Iterator<Exp> i = list.iterator();
            while (i.hasNext()) {
                sb.append(i.next().toString(0));
                if (i.hasNext()) {
                    if (myInstanceEnvironment.flags
                            .isFlagSet(Verifier.FLAG_LISTVCS_VC))
                        sb.append(" and ");
                    else if (!AssertiveCode.isProvePart())
                        sb.append("and\n");
                    else
                        sb.append(" and ");
                }
            }
        }
        else {

            printSpace(indent, sb);
            List<Exp> list = exp.getLessExps();
            Iterator<Exp> i = list.iterator();
            while (i.hasNext()) {
                sb.append(i.next().toString(0));
                if (i.hasNext()) {
                    if (!AssertiveCode.isProvePart())
                        sb.append(";\n");
                    else
                        sb.append(" & ");
                }
            }
        }

    }

    public void visitMathVarDec(MathVarDec dec) {
        if (!isabelle) {

            String strName = dec.getName().toString();
            if (dec.getName() != null) {
                int index = 0;
                int num = 0;
                while ((strName.charAt(index)) == '?') {
                    num++;
                    index++;
                }
                strName =
                        strName.concat(strName.substring(index, strName
                                .length()));
                for (int i = 0; i < num; i++) {
                    strName = strName.concat("'");
                }
            }

            String str = strName.concat(":");
            if (dec.getTy() instanceof NameTy)
                str = str.concat(((NameTy) dec.getTy()).getName().toString());
            else
                str = str.concat(dec.getTy().toString(0));
            sb.append(str);
        }
        else {

            VarExp tmp =
                    new VarExp(dec.getName().getLocation(), null, dec.getName());
            PrintAssertions printer =
                    new PrintAssertions(myInstanceEnvironment);
            String str = printer.clearAndVisitAssertion(tmp);
            str = str.concat(":");
            if (dec.getTy() instanceof NameTy)
                str = str.concat(((NameTy) dec.getTy()).getName().toString());
            else
                str = str.concat(dec.getTy().toString(0));
            sb.append(str);
        }

    }

    /**
     * Builds a sequence of numSpaces spaces and returns that
     * sequence.
     */
    protected void printSpace(int numSpaces, StringBuffer buffer) {
        for (int i = 0; i < numSpaces; ++i) {
            buffer.append(" ");
        }
    }

    String paramListToString(List<FunctionArgList> paramList) {
        if (paramList != null) {
            String str = new String();
            Iterator<?> i = paramList.iterator();
            while (i.hasNext()) {
                str = functionArgListToString((FunctionArgList) i.next());
            }
            return str;
        }
        else
            return new String();
    }

    String functionArgListToString(FunctionArgList list) {
        List<Exp> expList = list.getArguments();
        return expListToString(expList);
    }

    String expListToString(List<Exp> list) {
        StringBuffer str = new StringBuffer();
        Iterator<?> i = list.iterator();
        if (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null)
                visitAssertion(exp);

        }
        while (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null) {

                sb.append(", ");
                visitAssertion(exp);
            }
        }
        return str.toString();
    }

    String isabParamListToString(List<FunctionArgList> paramList) {
        if (paramList != null) {
            String str = new String();
            Iterator<?> i = paramList.iterator();
            while (i.hasNext()) {
                str = isabFunctionArgListToString((FunctionArgList) i.next());
            }
            return str;
        }
        else
            return new String();
    }

    String isabFunctionArgListToString(FunctionArgList list) {
        List<Exp> expList = list.getArguments();
        return isabExpListToString(expList);
    }

    String isabExpListToString(List<Exp> list) {
        StringBuffer str = new StringBuffer();
        Iterator<?> i = list.iterator();
        if (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null)
                visitAssertion(exp);
        }
        while (i.hasNext()) {
            Exp exp = (Exp) i.next();
            if (exp != null) {
                str = str.append(" ");
                visitAssertion(exp);
            }
        }
        return str.toString();
    }

    private String printConstant(int k) {
        StringBuffer sb2 = new StringBuffer();
        switch (k) {
        case 1:
            sb2.append("for all");
            break;
        case 2:
            sb2.append("there exists");
            break;
        case 3:
            sb2.append("UNIQUE");
            break;
        default:
            sb2.append(k);
        }
        return sb2.toString();
    }

    private String printIsabelleConstant(int k) {
        StringBuffer sb2 = new StringBuffer();
        switch (k) {
        case 1:
            sb2.append("ALL");
            break;
        case 2:
            sb2.append("there exists");
            break;
        case 3:
            sb2.append("UNIQUE");
            break;
        default:
            sb2.append(k);
        }
        return sb2.toString();
    }

    private void segmentsToString(List<Exp> segments) {

        if (segments != null) {
            Iterator<Exp> i = segments.iterator();

            while (i.hasNext()) {
                int oldIndent = indent;
                indent = 0;
                visitAssertion(i.next());
                if (i.hasNext() && !isabelle)
                    sb.append(".");
                indent = oldIndent;
            }
        }

    }

    private void varSegmentsToString(List<VariableExp> segments) {

        if (segments != null) {
            Iterator<VariableExp> i = segments.iterator();

            while (i.hasNext()) {
                int oldIndent = indent;
                indent = 0;
                visitAssertion(i.next());
                if (i.hasNext() && !isabelle)
                    sb.append(".");
                indent = oldIndent;
            }
        }

    }

}
