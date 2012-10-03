package edu.clemson.cs.r2jt.postprocessing;

/* Libraries */
import java.util.Iterator;

import edu.clemson.cs.r2jt.absyn.CallStmt;
import edu.clemson.cs.r2jt.absyn.ConceptBodyModuleDec;
import edu.clemson.cs.r2jt.absyn.Dec;
import edu.clemson.cs.r2jt.absyn.EnhancementBodyModuleDec;
import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.FacilityOperationDec;
import edu.clemson.cs.r2jt.absyn.FacilityModuleDec;
import edu.clemson.cs.r2jt.absyn.FacilityTypeDec;
import edu.clemson.cs.r2jt.absyn.FuncAssignStmt;
import edu.clemson.cs.r2jt.absyn.NameTy;
import edu.clemson.cs.r2jt.absyn.ProcedureDec;
import edu.clemson.cs.r2jt.absyn.ProgramExp;
import edu.clemson.cs.r2jt.absyn.ProgramParamExp;
import edu.clemson.cs.r2jt.absyn.RecordTy;
import edu.clemson.cs.r2jt.absyn.RepresentationDec;
import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;
import edu.clemson.cs.r2jt.absyn.Statement;
import edu.clemson.cs.r2jt.absyn.SwapStmt;
import edu.clemson.cs.r2jt.absyn.VarDec;
import edu.clemson.cs.r2jt.absyn.VariableArrayExp;
import edu.clemson.cs.r2jt.absyn.VariableExp;
import edu.clemson.cs.r2jt.absyn.VariableDotExp;
import edu.clemson.cs.r2jt.absyn.VariableNameExp;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.treewalk.TreeWalkerStackVisitor;

/**
 * 
 */
public class PostProcessor extends TreeWalkerStackVisitor {

    // ===========================================================
    // Global Variables 
    // ===========================================================

    /* A counter used to keep track the number of variables created */
    private int myCounter;

    /* Keeps track if the symbol table has changed or not */
    private Boolean myCondition;

    /* List of SwapStmts to be added */
    private edu.clemson.cs.r2jt.collections.List<SwapStmt> mySwapList;

    // ===========================================================
    // Constructors
    // ===========================================================

    public PostProcessor() {
        myCounter = 1;
        mySwapList = new edu.clemson.cs.r2jt.collections.List<SwapStmt>();

        // Set myCondition to false since we haven't changed anything
        setFalse();
    }

    // -----------------------------------------------------------
    // Methods - Changes to the AST
    // -----------------------------------------------------------

    // Function to check if we have changed the AST or not
    public Boolean haveChanged() {
        return myCondition;
    }

    // Set boolean myCondition to true
    // Note: Only used when we modify the AST
    private void setTrue() {
        myCondition = true;
    }

    // Set boolean myCondition to false
    // Note: Only used in RepArguments constructor
    private void setFalse() {
        myCondition = false;
    }

    // -----------------------------------------------------------
    // Call Statement
    // -----------------------------------------------------------

    @Override
    public void postCallStmt(CallStmt stmt) {
        /* Variables */
        edu.clemson.cs.r2jt.collections.List<VarDec> explicitArgList;

        /* List of variables within Scope */
        FacilityOperationDec currentFacOpDec = null;
        ProcedureDec currentProcDec = null;
        edu.clemson.cs.r2jt.collections.List<VarDec> localVarList =
                new edu.clemson.cs.r2jt.collections.List<VarDec>();
        edu.clemson.cs.r2jt.collections.List<VarDec> globalVarList =
                new edu.clemson.cs.r2jt.collections.List<VarDec>();
        edu.clemson.cs.r2jt.collections.List<Dec> recordList =
                new edu.clemson.cs.r2jt.collections.List<Dec>();

        /* Iterate through AST */
        Iterator<ResolveConceptualElement> it = this.getAncestorInterator();
        while (it.hasNext()) {
            /* Obtain a temp from it */
            ResolveConceptualElement temp = it.next();

            /* Look for FacilityOperationDec */
            if (temp instanceof FacilityOperationDec) {
                /* Store a link to our current FacilityOperationDec */
                if (currentFacOpDec == null) {
                    currentFacOpDec = (FacilityOperationDec) temp;
                }

                /* Get List of Local Variables */
                localVarList = ((FacilityOperationDec) temp).getVariables();
            }

            /* Look for ProcedureDecs */
            if (temp instanceof ProcedureDec) {
                /* Store a link to our current ProcedureDec */
                if (currentProcDec == null) {
                    currentProcDec = (ProcedureDec) temp;
                }

                /* Get List of Local Variables */
                localVarList = ((ProcedureDec) temp).getVariables();
            }

            /* Look for FacilityModuleDec */
            if (temp instanceof FacilityModuleDec) {
                /* Create a iterator for the list of Decs */
                Iterator<Dec> it2 =
                        ((FacilityModuleDec) temp).getDecs().iterator();

                /* Loop through the list of Decs */
                while (it2.hasNext()) {
                    /* Obtain nextDec from the iterator */
                    Dec nextDec = it2.next();

                    /* Check if it is a global variable */
                    if (nextDec instanceof VarDec) {
                        /* Add it to our global variable list */
                        globalVarList.add((VarDec) nextDec);
                    } /* Check to see if it is a FacilityTypeDec */
                    else if (nextDec instanceof FacilityTypeDec) {
                        if (((FacilityTypeDec) nextDec).getRepresentation() instanceof RecordTy) {
                            recordList.add(nextDec);
                        }
                    }
                }
            }

            /* Look for ConceptBodyModuleDec */
            if (temp instanceof ConceptBodyModuleDec) {
                /* Create a iterator for the list of Decs */
                Iterator<Dec> it2 =
                        ((ConceptBodyModuleDec) temp).getDecs().iterator();

                /* Loop through the list of Decs */
                while (it2.hasNext()) {
                    /* Obtain nextDec from the iterator */
                    Dec nextDec = it2.next();

                    /* Check if it is a global variable */
                    if (nextDec instanceof VarDec) {
                        /* Add it to our global variable list */
                        globalVarList.add((VarDec) nextDec);
                    } /* Check to see if it is a FacilityTypeDec */
                    else if (nextDec instanceof RepresentationDec) {
                        if (((RepresentationDec) nextDec).getRepresentation() instanceof RecordTy) {
                            recordList.add(nextDec);
                        }
                    }
                }
            }

            /* Look for EnhancementBodyModuleDec */
            if (temp instanceof EnhancementBodyModuleDec) {
                /* Create a iterator for the list of Decs */
                Iterator<Dec> it2 =
                        ((EnhancementBodyModuleDec) temp).getDecs().iterator();

                /* Loop through the list of Decs */
                while (it2.hasNext()) {
                    /* Obtain nextDec from the iterator */
                    Dec nextDec = it2.next();

                    /* Check if it is a global variable */
                    if (nextDec instanceof VarDec) {
                        /* Add it to our global variable list */
                        globalVarList.add((VarDec) nextDec);
                    } /* Check to see if it is a FacilityTypeDec */
                    else if (nextDec instanceof RepresentationDec) {
                        if (((RepresentationDec) nextDec).getRepresentation() instanceof RecordTy) {
                            recordList.add(nextDec);
                        }
                    }
                }
            }
        }

        /* Check for Explicit Repeated Arguments */
        explicitArgList =
                explicitRepArgCheck(stmt.getArguments(), localVarList,
                        globalVarList, recordList);

        /* If we have detected explicit repeated arguments */
        if (explicitArgList.size() != 0) {
            /* We have modified the ModuleDec */
            setTrue();

            /* Add all newly created variables to localVarList */
            for (int i = 0; i < explicitArgList.size(); i++) {
                localVarList.add(explicitArgList.get(i));
            }

            /* Add localVarList back to the FacilityOperationDec/ProcedureDec it corresponds to */
            if (currentFacOpDec != null) {
                currentFacOpDec.setVariables(localVarList);
            }
            else if (currentProcDec != null) {
                currentProcDec.setVariables(localVarList);
            }
        }

        /* Check if we have any swap statements we need to add */
        if (mySwapList.size() != 0) {
            it = this.getAncestorInterator();
            while (it.hasNext()) {
                /* Obtain a temp from it */
                ResolveConceptualElement temp = it.next();

                /* Look for FacilityOperationDec */
                if (temp instanceof FacilityOperationDec) {
                    edu.clemson.cs.r2jt.collections.List<Statement> stmtList =
                            ((FacilityOperationDec) temp).getStatements();

                    /* Loop through the list */
                    for (int i = 0; i < stmtList.size(); i++) {
                        if (stmtList.get(i) instanceof CallStmt
                                && ((CallStmt) stmtList.get(i)).getName()
                                        .getLocation() == stmt.getName()
                                        .getLocation()) {
                            /* Add the swap statements after the call statement */
                            for (int j = 0; j < mySwapList.size(); j++) {
                                SwapStmt newSwapStmt = mySwapList.get(j);
                                newSwapStmt.setLocation(stmt.getName()
                                        .getLocation());
                                stmtList.add(i + 1, newSwapStmt);
                            }

                            /* Add the swap statements before the call statement */
                            for (int j = mySwapList.size() - 1; j >= 0; j--) {
                                SwapStmt newSwapStmt = mySwapList.get(j);
                                newSwapStmt.setLocation(stmt.getName()
                                        .getLocation());
                                stmtList.add(i, newSwapStmt);
                            }

                            break;
                        }
                    }
                }
            }

            /* Clear the list */
            mySwapList.clear();
        }
    }

    // -----------------------------------------------------------
    // Function Assignment Statement
    // -----------------------------------------------------------

    @Override
    public void postFuncAssignStmt(FuncAssignStmt stmt) {
        /* Check if we have a Function Assignment */
        if (stmt.getAssign() instanceof ProgramParamExp) {
            /* Variables */
            edu.clemson.cs.r2jt.collections.List<VarDec> explicitArgList;

            /* List of variables within Scope */
            FacilityOperationDec currentFacOpDec = null;
            ProcedureDec currentProcDec = null;
            edu.clemson.cs.r2jt.collections.List<VarDec> localVarList =
                    new edu.clemson.cs.r2jt.collections.List<VarDec>();
            edu.clemson.cs.r2jt.collections.List<VarDec> globalVarList =
                    new edu.clemson.cs.r2jt.collections.List<VarDec>();
            edu.clemson.cs.r2jt.collections.List<Dec> recordList =
                    new edu.clemson.cs.r2jt.collections.List<Dec>();

            /* Iterate through AST */
            Iterator<ResolveConceptualElement> it = this.getAncestorInterator();
            while (it.hasNext()) {
                /* Obtain a temp from it */
                ResolveConceptualElement temp = it.next();

                /* Look for FacilityOperationDec */
                if (temp instanceof FacilityOperationDec) {
                    /* Store a link to our current FacilityOperationDec */
                    if (currentFacOpDec == null) {
                        currentFacOpDec = (FacilityOperationDec) temp;
                    }

                    /* Get List of Local Variables */
                    localVarList = ((FacilityOperationDec) temp).getVariables();
                }

                /* Look for ProcedureDecs */
                if (temp instanceof ProcedureDec) {
                    /* Store a link to our current ProcedureDec */
                    if (currentProcDec == null) {
                        currentProcDec = (ProcedureDec) temp;
                    }

                    /* Get List of Local Variables */
                    localVarList = ((ProcedureDec) temp).getVariables();
                }

                /* Look for FacilityModuleDec */
                if (temp instanceof FacilityModuleDec) {
                    /* Create a iterator for the list of Decs */
                    Iterator<Dec> it2 =
                            ((FacilityModuleDec) temp).getDecs().iterator();

                    /* Loop through the list of Decs */
                    while (it2.hasNext()) {
                        /* Obtain nextDec from the iterator */
                        Dec nextDec = it2.next();

                        /* Check if it is a global variable */
                        if (nextDec instanceof VarDec) {
                            /* Add it to our global variable list */
                            globalVarList.add((VarDec) nextDec);
                        } /* Check to see if it is a FacilityTypeDec */
                        else if (nextDec instanceof FacilityTypeDec) {
                            if (((FacilityTypeDec) nextDec).getRepresentation() instanceof RecordTy) {
                                recordList.add(nextDec);
                            }
                        }
                    }
                }

                /* Look for ConceptBodyModuleDec */
                if (temp instanceof ConceptBodyModuleDec) {
                    /* Create a iterator for the list of Decs */
                    Iterator<Dec> it2 =
                            ((ConceptBodyModuleDec) temp).getDecs().iterator();

                    /* Loop through the list of Decs */
                    while (it2.hasNext()) {
                        /* Obtain nextDec from the iterator */
                        Dec nextDec = it2.next();

                        /* Check if it is a global variable */
                        if (nextDec instanceof VarDec) {
                            /* Add it to our global variable list */
                            globalVarList.add((VarDec) nextDec);
                        } /* Check to see if it is a FacilityTypeDec */
                        else if (nextDec instanceof RepresentationDec) {
                            if (((RepresentationDec) nextDec)
                                    .getRepresentation() instanceof RecordTy) {
                                recordList.add(nextDec);
                            }
                        }
                    }
                }

                /* Look for EnhancementBodyModuleDec */
                if (temp instanceof EnhancementBodyModuleDec) {
                    /* Create a iterator for the list of Decs */
                    Iterator<Dec> it2 =
                            ((EnhancementBodyModuleDec) temp).getDecs()
                                    .iterator();

                    /* Loop through the list of Decs */
                    while (it2.hasNext()) {
                        /* Obtain nextDec from the iterator */
                        Dec nextDec = it2.next();

                        /* Check if it is a global variable */
                        if (nextDec instanceof VarDec) {
                            /* Add it to our global variable list */
                            globalVarList.add((VarDec) nextDec);
                        } /* Check to see if it is a FacilityTypeDec */
                        else if (nextDec instanceof RepresentationDec) {
                            if (((RepresentationDec) nextDec)
                                    .getRepresentation() instanceof RecordTy) {
                                recordList.add(nextDec);
                            }
                        }
                    }
                }
            }

            /* Check for Explicit Repeated Arguments */
            explicitArgList =
                    explicitRepArgCheck(((ProgramParamExp) stmt.getAssign())
                            .getArguments(), localVarList, globalVarList,
                            recordList);

            /* If we have detected explicit repeated arguments */
            if (explicitArgList.size() != 0) {
                /* We have modified the ModuleDec */
                setTrue();

                /* Add all newly created variables to localVarList */
                for (int i = 0; i < explicitArgList.size(); i++) {
                    localVarList.add(explicitArgList.get(i));
                }

                /* Add localVarList back to the FacilityOperationDec/ProcedureDec it corresponds to */
                if (currentFacOpDec != null) {
                    currentFacOpDec.setVariables(localVarList);
                }
                else if (currentProcDec != null) {
                    currentProcDec.setVariables(localVarList);
                }
            }

            /* Check if we have any swap statements we need to add */
            if (mySwapList.size() != 0) {
                it = this.getAncestorInterator();
                while (it.hasNext()) {
                    /* Obtain a temp from it */
                    ResolveConceptualElement temp = it.next();

                    /* Look for FacilityOperationDec */
                    if (temp instanceof FacilityOperationDec) {
                        edu.clemson.cs.r2jt.collections.List<Statement> stmtList =
                                ((FacilityOperationDec) temp).getStatements();

                        /* Loop through the list */
                        for (int i = 0; i < stmtList.size(); i++) {
                            if (stmtList.get(i) instanceof FuncAssignStmt
                                    && ((FuncAssignStmt) stmtList.get(i))
                                            .getLocation() == stmt
                                            .getLocation()) {
                                /* Add the swap statements after the call statement */
                                for (int j = 0; j < mySwapList.size(); j++) {
                                    SwapStmt newSwapStmt = mySwapList.get(j);
                                    newSwapStmt.setLocation(stmt.getLocation());
                                    stmtList.add(i + 1, newSwapStmt);
                                }

                                /* Add the swap statements before the call statement */
                                for (int j = mySwapList.size() - 1; j >= 0; j--) {
                                    SwapStmt newSwapStmt = mySwapList.get(j);
                                    newSwapStmt.setLocation(stmt.getLocation());
                                    stmtList.add(i, newSwapStmt);
                                }

                                break;
                            }
                        }
                    }
                    else if (temp instanceof ProcedureDec) {
                        edu.clemson.cs.r2jt.collections.List<Statement> stmtList =
                                ((ProcedureDec) temp).getStatements();

                        /* Loop through the list */
                        for (int i = 0; i < stmtList.size(); i++) {
                            if (stmtList.get(i) instanceof FuncAssignStmt
                                    && ((FuncAssignStmt) stmtList.get(i))
                                            .getLocation() == stmt
                                            .getLocation()) {
                                /* Add the swap statements after the call statement */
                                for (int j = 0; j < mySwapList.size(); j++) {
                                    SwapStmt newSwapStmt = mySwapList.get(j);
                                    newSwapStmt.setLocation(stmt.getLocation());
                                    stmtList.add(i + 1, newSwapStmt);
                                }

                                /* Add the swap statements before the call statement */
                                for (int j = mySwapList.size() - 1; j >= 0; j--) {
                                    SwapStmt newSwapStmt = mySwapList.get(j);
                                    newSwapStmt.setLocation(stmt.getLocation());
                                    stmtList.add(i, newSwapStmt);
                                }

                                break;
                            }
                        }
                    }
                }

                /* Clear the list */
                mySwapList.clear();
            }
        }
    }

    // -----------------------------------------------------------
    // Methods - Explicit/Implicit Repeated Argument Checks
    // -----------------------------------------------------------

    private List<VarDec> explicitRepArgCheck(List<ProgramExp> args,
            edu.clemson.cs.r2jt.collections.List<VarDec> localVarList,
            edu.clemson.cs.r2jt.collections.List<VarDec> globalVarList,
            edu.clemson.cs.r2jt.collections.List<Dec> recordList) {
        /* Variables */
        edu.clemson.cs.r2jt.collections.List<VarDec> createdVariablesList =
                new edu.clemson.cs.r2jt.collections.List<VarDec>();
        VariableNameExp retVariableNameExp;
        VarDec retVarDec;

        /*
         * Check for repeated arguments of the following type Type 1 repeated
         * arguments {F(U, U)} Type 3 repeated arguments {F(A, A[i])} Type 4
         * repeated arguments {F(R, R.x)} Type 5 repeated arguments {F(U), where
         * U is a global variable}
         */
        for (int i = 0; i < args.size() && args.size() > 1; i++) {
            /* Get the current element in the arg list */
            ProgramExp current = args.get(i);

            /* Check to see if it is of type VariableExp */
            if (current instanceof VariableExp
                    && !(current instanceof VariableArrayExp)) {
                /* Check to see if it is a repeated argument of type 5 */
                if (current instanceof VariableNameExp) {
                    //retVarDec = type5Check((VariableNameExp)current, globalVarList);
                    retVarDec = null;
                }
                else {
                    retVarDec = null;
                }

                if (retVarDec != null) {
                    retVariableNameExp =
                            createVariableNameExp((VariableNameExp) current);

                    /* Set j to modified */
                    args.set(i, retVariableNameExp);

                    /* Change name of retVarDec */
                    retVarDec.setName(retVariableNameExp.getName());

                    /* Add it to our list of return variables */
                    createdVariablesList.add(retVarDec);
                }
                else {
                    for (int j = 0; j < args.size(); j++) {
                        /* Get the temp element pointed by j */
                        ProgramExp temp = args.get(j);

                        /* i != j and check to see if it is of type VariableExp  */
                        if (i != j && temp instanceof VariableExp) {
                            /* Check to see if it is a repeated argument of type 1 (VariableNameExp) */
                            if (current instanceof VariableNameExp
                                    && temp instanceof VariableNameExp) {
                                retVariableNameExp =
                                        type1Check((VariableNameExp) current,
                                                (VariableNameExp) temp);

                                /* We found a type 1 repeated argument */
                                if (retVariableNameExp != null) {
                                    /* Set j to modified */
                                    args.set(j, retVariableNameExp);

                                    /* Create a VarDec */
                                    retVarDec =
                                            createVarDec(
                                                    ((VariableNameExp) temp)
                                                            .getName(),
                                                    localVarList);

                                    /* Change name of retVarDec */
                                    retVarDec.setName(retVariableNameExp
                                            .getName());

                                    /* Add it to our list of return variables */
                                    createdVariablesList.add(retVarDec);
                                }
                            }
                            /* Check to see if it is a repeated argument of type 1 (VariableDotExp) */
                            else if (current instanceof VariableDotExp
                                    && temp instanceof VariableDotExp) {
                                retVariableNameExp =
                                        type1Check((VariableDotExp) current,
                                                (VariableDotExp) temp);

                                /* We found a type 1 repeated argument */
                                if (retVariableNameExp != null) {
                                    /* Set j to modified */
                                    args.set(j, retVariableNameExp);

                                    /* Find Variable Name */
                                    edu.clemson.cs.r2jt.collections.List<VariableExp> tempSegList =
                                            ((VariableDotExp) temp)
                                                    .getSegments();
                                    String variableName = "";
                                    variableName +=
                                            ((VariableNameExp) tempSegList
                                                    .get(tempSegList.size() - 1))
                                                    .getName().getName();

                                    /* Create a VarDec */
                                    VarDec modifiedVarDec = new VarDec();
                                    modifiedVarDec.setName(retVariableNameExp
                                            .getName());

                                    /* Iterate through list of Local Variables */
                                    Iterator<VarDec> it =
                                            localVarList.iterator();
                                    while (it.hasNext()) {
                                        /* Obtain nextDec from the iterator */
                                        VarDec nextVarDec = it.next();

                                        /* Get the ty from nextVarDec */
                                        if (nextVarDec
                                                .getName()
                                                .getName()
                                                .compareTo(
                                                        ((VariableNameExp) tempSegList
                                                                .get(0))
                                                                .getName()
                                                                .getName()) == 0) {
                                            PosSymbol recordTyName =
                                                    ((NameTy) nextVarDec
                                                            .getTy()).getName();

                                            /* Iterate through list of records */
                                            Iterator<Dec> it2 =
                                                    recordList.iterator();
                                            while (it2.hasNext()) {
                                                /* Obtain nextRecordDec from the iterator */
                                                Dec tempNextDec = it2.next();
                                                if (tempNextDec instanceof FacilityTypeDec) {
                                                    FacilityTypeDec nextRecordDec =
                                                            (FacilityTypeDec) tempNextDec;
                                                    /* Compare to see if nextRecordDec = recordTyName */
                                                    if (nextRecordDec
                                                            .getName()
                                                            .getName()
                                                            .compareTo(
                                                                    recordTyName
                                                                            .getName()) == 0) {
                                                        edu.clemson.cs.r2jt.collections.List<VarDec> varDecsInRecord =
                                                                ((RecordTy) nextRecordDec
                                                                        .getRepresentation())
                                                                        .getFields();

                                                        /* Loop through to find the ty we need */
                                                        Iterator<VarDec> it3 =
                                                                varDecsInRecord
                                                                        .iterator();
                                                        while (it3.hasNext()) {
                                                            /* Obtain varInDec from the iterator */
                                                            VarDec varInDec =
                                                                    it3.next();

                                                            /* Get the ty from temp */
                                                            if (varInDec
                                                                    .getName()
                                                                    .getName()
                                                                    .compareTo(
                                                                            variableName) == 0) {
                                                                modifiedVarDec
                                                                        .setTy(varInDec
                                                                                .getTy());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                                else {
                                                    RepresentationDec nextRecordDec =
                                                            (RepresentationDec) tempNextDec;

                                                    /* Compare to see if nextRecordDec = recordTyName */
                                                    if (nextRecordDec
                                                            .getName()
                                                            .getName()
                                                            .compareTo(
                                                                    recordTyName
                                                                            .getName()) == 0) {
                                                        edu.clemson.cs.r2jt.collections.List<VarDec> varDecsInRecord =
                                                                ((RecordTy) nextRecordDec
                                                                        .getRepresentation())
                                                                        .getFields();

                                                        /* Loop through to find the ty we need */
                                                        Iterator<VarDec> it3 =
                                                                varDecsInRecord
                                                                        .iterator();
                                                        while (it3.hasNext()) {
                                                            /* Obtain varInDec from the iterator */
                                                            VarDec varInDec =
                                                                    it3.next();

                                                            /* Get the ty from temp */
                                                            if (varInDec
                                                                    .getName()
                                                                    .getName()
                                                                    .compareTo(
                                                                            variableName) == 0) {
                                                                modifiedVarDec
                                                                        .setTy(varInDec
                                                                                .getTy());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }

                                    /* Add it to our list of return variables */
                                    createdVariablesList.add(modifiedVarDec);
                                }
                            }
                            /* Check to see if it is a repeated argument of type 4 P(R, R.x) */
                            else if (current instanceof VariableNameExp
                                    && temp instanceof VariableDotExp) {
                                retVariableNameExp =
                                        type4Check((VariableNameExp) current,
                                                (VariableDotExp) temp);

                                /* We found a type 4 repeated argument */
                                if (retVariableNameExp != null) {
                                    /* Set j to modified */
                                    args.set(j, retVariableNameExp);

                                    /* Get Variable Name */
                                    String variableName = "";
                                    edu.clemson.cs.r2jt.collections.List<VariableExp> dotList =
                                            ((VariableDotExp) temp)
                                                    .getSegments();
                                    variableName +=
                                            ((VariableNameExp) (dotList
                                                    .get(dotList.size() - 1)))
                                                    .getName().getName();

                                    /* Create a VarDec */
                                    VarDec modifiedVarDec = new VarDec();
                                    modifiedVarDec.setName(retVariableNameExp
                                            .getName());

                                    /* Iterate through list of Local Variables */
                                    Iterator<VarDec> it =
                                            localVarList.iterator();
                                    while (it.hasNext()) {
                                        /* Obtain nextDec from the iterator */
                                        VarDec nextVarDec = it.next();

                                        /* Get the ty from nextVarDec */
                                        if (nextVarDec
                                                .getName()
                                                .getName()
                                                .compareTo(
                                                        ((VariableNameExp) current)
                                                                .getName()
                                                                .getName()) == 0) {
                                            PosSymbol recordTyName =
                                                    ((NameTy) nextVarDec
                                                            .getTy()).getName();

                                            /* Iterate through list of records */
                                            Iterator<Dec> it2 =
                                                    recordList.iterator();
                                            while (it2.hasNext()) {
                                                /* Obtain nextRecordDec from the iterator */
                                                Dec tempNextDec = it2.next();
                                                if (tempNextDec instanceof FacilityTypeDec) {
                                                    FacilityTypeDec nextRecordDec =
                                                            (FacilityTypeDec) tempNextDec;
                                                    /* Compare to see if nextRecordDec = recordTyName */
                                                    if (nextRecordDec
                                                            .getName()
                                                            .getName()
                                                            .compareTo(
                                                                    recordTyName
                                                                            .getName()) == 0) {
                                                        edu.clemson.cs.r2jt.collections.List<VarDec> varDecsInRecord =
                                                                ((RecordTy) nextRecordDec
                                                                        .getRepresentation())
                                                                        .getFields();

                                                        /* Loop through to find the ty we need */
                                                        Iterator<VarDec> it3 =
                                                                varDecsInRecord
                                                                        .iterator();
                                                        while (it3.hasNext()) {
                                                            /* Obtain varInDec from the iterator */
                                                            VarDec varInDec =
                                                                    it3.next();

                                                            /* Get the ty from temp */
                                                            if (varInDec
                                                                    .getName()
                                                                    .getName()
                                                                    .compareTo(
                                                                            variableName) == 0) {
                                                                modifiedVarDec
                                                                        .setTy(varInDec
                                                                                .getTy());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                                else {
                                                    RepresentationDec nextRecordDec =
                                                            (RepresentationDec) tempNextDec;

                                                    /* Compare to see if nextRecordDec = recordTyName */
                                                    if (nextRecordDec
                                                            .getName()
                                                            .getName()
                                                            .compareTo(
                                                                    recordTyName
                                                                            .getName()) == 0) {
                                                        edu.clemson.cs.r2jt.collections.List<VarDec> varDecsInRecord =
                                                                ((RecordTy) nextRecordDec
                                                                        .getRepresentation())
                                                                        .getFields();

                                                        /* Loop through to find the ty we need */
                                                        Iterator<VarDec> it3 =
                                                                varDecsInRecord
                                                                        .iterator();
                                                        while (it3.hasNext()) {
                                                            /* Obtain varInDec from the iterator */
                                                            VarDec varInDec =
                                                                    it3.next();

                                                            /* Get the ty from temp */
                                                            if (varInDec
                                                                    .getName()
                                                                    .getName()
                                                                    .compareTo(
                                                                            variableName) == 0) {
                                                                modifiedVarDec
                                                                        .setTy(varInDec
                                                                                .getTy());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }

                                    /* Add it to our list of return variables */
                                    createdVariablesList.add(modifiedVarDec);
                                }
                            }
                            /* Check to see if it is a repeated argument of type 4 P(R.x, R) */
                            else if (current instanceof VariableDotExp
                                    && temp instanceof VariableNameExp) {
                                retVariableNameExp =
                                        type4Check((VariableNameExp) temp,
                                                (VariableDotExp) current);

                                /* We found a type 4 repeated argument */
                                if (retVariableNameExp != null) {
                                    /* Set j to modified */
                                    args.set(i, retVariableNameExp);

                                    /* Get Variable Name */
                                    String variableName = "";
                                    edu.clemson.cs.r2jt.collections.List<VariableExp> dotList =
                                            ((VariableDotExp) current)
                                                    .getSegments();
                                    variableName +=
                                            ((VariableNameExp) dotList
                                                    .get(dotList.size() - 1))
                                                    .getName().getName();

                                    /* Create a VarDec */
                                    VarDec modifiedVarDec = new VarDec();
                                    modifiedVarDec.setName(retVariableNameExp
                                            .getName());

                                    /* Iterate through list of Local Variables */
                                    Iterator<VarDec> it =
                                            localVarList.iterator();
                                    while (it.hasNext()) {
                                        /* Obtain nextDec from the iterator */
                                        VarDec nextVarDec = it.next();

                                        /* Get the ty from nextVarDec */
                                        if (nextVarDec
                                                .getName()
                                                .getName()
                                                .compareTo(
                                                        ((VariableNameExp) temp)
                                                                .getName()
                                                                .getName()) == 0) {
                                            PosSymbol recordTyName =
                                                    ((NameTy) nextVarDec
                                                            .getTy()).getName();

                                            /* Iterate through list of records */
                                            Iterator<Dec> it2 =
                                                    recordList.iterator();
                                            while (it2.hasNext()) {
                                                /* Obtain nextRecordDec from the iterator */
                                                Dec tempNextDec = it2.next();

                                                if (tempNextDec instanceof FacilityTypeDec) {
                                                    FacilityTypeDec nextRecordDec =
                                                            (FacilityTypeDec) tempNextDec;

                                                    /* Compare to see if nextRecordDec = recordTyName */
                                                    if (nextRecordDec
                                                            .getName()
                                                            .getName()
                                                            .compareTo(
                                                                    recordTyName
                                                                            .getName()) == 0) {
                                                        edu.clemson.cs.r2jt.collections.List<VarDec> varDecsInRecord =
                                                                ((RecordTy) nextRecordDec
                                                                        .getRepresentation())
                                                                        .getFields();

                                                        /* Loop through to find the ty we need */
                                                        Iterator<VarDec> it3 =
                                                                varDecsInRecord
                                                                        .iterator();
                                                        while (it3.hasNext()) {
                                                            /* Obtain varInDec from the iterator */
                                                            VarDec varInDec =
                                                                    it3.next();

                                                            /* Get the ty from temp */
                                                            if (varInDec
                                                                    .getName()
                                                                    .getName()
                                                                    .compareTo(
                                                                            variableName) == 0) {
                                                                modifiedVarDec
                                                                        .setTy(varInDec
                                                                                .getTy());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                                else {
                                                    RepresentationDec nextRecordDec =
                                                            (RepresentationDec) tempNextDec;

                                                    /* Compare to see if nextRecordDec = recordTyName */
                                                    if (nextRecordDec
                                                            .getName()
                                                            .getName()
                                                            .compareTo(
                                                                    recordTyName
                                                                            .getName()) == 0) {
                                                        edu.clemson.cs.r2jt.collections.List<VarDec> varDecsInRecord =
                                                                ((RecordTy) nextRecordDec
                                                                        .getRepresentation())
                                                                        .getFields();

                                                        /* Loop through to find the ty we need */
                                                        Iterator<VarDec> it3 =
                                                                varDecsInRecord
                                                                        .iterator();
                                                        while (it3.hasNext()) {
                                                            /* Obtain varInDec from the iterator */
                                                            VarDec varInDec =
                                                                    it3.next();

                                                            /* Get the ty from temp */
                                                            if (varInDec
                                                                    .getName()
                                                                    .getName()
                                                                    .compareTo(
                                                                            variableName) == 0) {
                                                                modifiedVarDec
                                                                        .setTy(varInDec
                                                                                .getTy());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }

                                    /* Add it to our list of return variables */
                                    createdVariablesList.add(modifiedVarDec);

                                    /* Add a new swap statement into the list */
                                    mySwapList.add(new SwapStmt(null,
                                            (VariableExp) current,
                                            retVariableNameExp));
                                }
                            }
                        }
                    }
                }
            }
        }

        return createdVariablesList;
    }

    private VariableNameExp type1Check(VariableNameExp current,
            VariableNameExp temp) {
        /* Variables */
        VariableNameExp modified = null;

        /* Check if the names are the same */
        if (current.getName().getName().compareTo(temp.getName().getName()) == 0) {
            /* Create a new VariableNameExp */
            modified = createVariableNameExp((VariableNameExp) temp);
        }

        return modified;
    }

    private VariableNameExp type1Check(VariableDotExp current,
            VariableDotExp temp) {
        /* Variables */
        VariableNameExp modified = null;

        /* Get the part before the dot */
        VariableNameExp currentName =
                (VariableNameExp) ((VariableDotExp) current).getSegments().get(
                        0);
        VariableNameExp tempName =
                (VariableNameExp) ((VariableDotExp) temp).getSegments().get(0);

        /* Check if they are equal */
        if (currentName.getName().getName().compareTo(
                tempName.getName().getName()) == 0) {
            /* Get the segments for each variable dot expression */
            edu.clemson.cs.r2jt.collections.List<VariableExp> currentSegList =
                    ((VariableDotExp) current).getSegments();
            edu.clemson.cs.r2jt.collections.List<VariableExp> tempSegList =
                    ((VariableDotExp) temp).getSegments();

            /* Proceed if the size of the two list is the same */
            if (currentSegList.size() == tempSegList.size()) {
                /* Iterator */
                Iterator<VariableExp> currentIt = currentSegList.iterator();
                Iterator<VariableExp> tempIt = tempSegList.iterator();

                /* Boolean */
                boolean equal = true;

                /* While Loop */
                while (currentIt.hasNext()) {
                    if (currentIt.next().toString().compareTo(
                            tempIt.next().toString()) != 0) {
                        equal = false;
                        break;
                    }
                }

                /* Check if the two are the same */
                if (equal == true) {
                    modified =
                            createVariableNameExp((VariableNameExp) tempName);
                }
            }
        }

        return modified;
    }

    private VariableNameExp type4Check(VariableNameExp name,
            VariableDotExp record) {
        /* Variables */
        String recordName = "";
        String entireVariableName = "";
        String variableName = "";
        edu.clemson.cs.r2jt.collections.List<VariableExp> dotList =
                ((VariableDotExp) record).getSegments();
        VariableNameExp modified = null;

        /* Loop through dotList */
        for (int k = 0; k < dotList.size(); k++) {
            /* Construct recordName */
            if (k < dotList.size() - 1) {
                recordName +=
                        ((VariableNameExp) dotList.get(k)).getName().getName();
            }

            /* Construct variableName */
            if (k == dotList.size() - 1) {
                variableName +=
                        ((VariableNameExp) dotList.get(k)).getName().getName();
            }
        }

        entireVariableName += (recordName + variableName);

        /* Check to see if it is a repeated argument of type 4 */
        if (((VariableNameExp) name).getName().getName().compareTo(recordName) == 0) {
            /* Create a new modified VariableNameExp */
            PosSymbol newName =
                    new PosSymbol(record.getLocation(), Symbol
                            .symbol("_RepArg_" + entireVariableName + "_"
                                    + myCounter++));
            modified = new VariableNameExp(record.getLocation(), null, newName);
        }

        return modified;
    }

    private VarDec type5Check(VariableNameExp current,
            edu.clemson.cs.r2jt.collections.List<VarDec> globalVarList) {
        /* Variable */
        VarDec retVarDec = null;

        /* Loop through to see if that variable is in the global variable list */
        Iterator<VarDec> it = globalVarList.iterator();
        while (it.hasNext()) {
            VarDec temp = it.next();

            if (temp.getName().getName().equals(current.getName().getName())) {
                retVarDec = createVarDec(current.getName(), globalVarList);
                break;
            }
        }

        return retVarDec;
    }

    private VariableNameExp createVariableNameExp(VariableNameExp old) {
        /* Create a copy of temp and modify it's name */
        VariableNameExp modified = (VariableNameExp) Exp.clone(old);
        PosSymbol oldName = modified.getName();
        PosSymbol newName =
                new PosSymbol(modified.getLocation(), Symbol.symbol("_RepArg_"
                        + oldName.getName() + "_" + myCounter++));
        modified.setName(newName);

        return modified;
    }

    private VarDec createVarDec(PosSymbol newName,
            edu.clemson.cs.r2jt.collections.List<VarDec> list) {
        /* Create a VarDec */
        VarDec modifiedVarDec = new VarDec();
        modifiedVarDec.setName(newName);

        /* Iterate through list to find the variable we need */
        VarDec retVarDec = iterateFindVarDec(newName, list);

        /* Set the Ty of the new VarDec and return it*/
        if (retVarDec != null) {
            modifiedVarDec.setTy(retVarDec.getTy());
            return modifiedVarDec;
        }
        else {
            return retVarDec;
        }
    }

    private VarDec iterateFindVarDec(PosSymbol name,
            edu.clemson.cs.r2jt.collections.List<VarDec> list) {
        /* Variables */
        VarDec nextVarDec = null;

        /* Iterate through list of Local Variables */
        Iterator<VarDec> it = list.iterator();
        while (it.hasNext()) {
            /* Obtain nextDec from the iterator */
            nextVarDec = it.next();

            /* We found it */
            if (nextVarDec.getName().getName().compareTo(name.getName()) == 0) {
                break;
            }
        }

        return nextVarDec;
    }
}
/**
 * <p>The purpose of the <code>Repeated Arguments</code> is to take the abstract syntax tree, 
 * generated by ANTLR and the Symbol Table generated by the <code>Populator</code>
 * and make necessary changes to the AST if repeated arguments exist during a call
 * statement.
 */
