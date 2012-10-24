package edu.clemson.cs.r2jt.population;

import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.mathtype.*;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTable.FacilityStrategy;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTable.ImportStrategy;
import edu.clemson.cs.r2jt.mathtype.PTPrimitive.PrimitiveTypeName;
import edu.clemson.cs.r2jt.mathtype.ProgramParameterEntry.ParameterMode;
import edu.clemson.cs.r2jt.treewalk.*;
import edu.clemson.cs.r2jt.typereasoning.*;
import edu.clemson.cs.r2jt.utilities.SourceErrorException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MathPopulator extends TreeWalkerVisitor {

    private static final boolean PRINT_DEBUG = true;

    private static final TypeComparison<AbstractFunctionExp, MTFunction> EXACT_DOMAIN_MATCH =
            new ExactDomainMatch();

    private static final Comparator<MTType> EXACT_PARAMETER_MATCH =
            new ExactParameterMatch();

    private final TypeComparison<AbstractFunctionExp, MTFunction> INEXACT_DOMAIN_MATCH =
            new InexactDomainMatch();

    private final TypeComparison<Exp, MTType> INEXACT_PARAMETER_MATCH =
            new InexactParameterMatch();

    private MathSymbolTableBuilder myBuilder;
    private ModuleScopeBuilder myCurModuleScope;

    private int myTypeValueDepth = 0;

    /**
     * <p>Any quantification-introducing syntactic node (like, e.g., a 
     * QuantExp), introduces a level to this stack to reflect the quantification 
     * that should be applied to named variables as they are encountered.  Note 
     * that this may change as the children of the node are processed--for 
     * example, MathVarDecs found in the declaration portion of a QuantExp 
     * should have quantification (universal or existential) applied, while 
     * those found in the body of the QuantExp should have no quantification 
     * (unless there is an embedded QuantExp).  In this case, QuantExp should 
     * <em>not</em> remove its layer, but rather change it to 
     * MathSymbolTableEntry.None.</p>
     * 
     * <p>This stack is never empty, but rather the bottom layer is always
     * MathSymbolTableEntry.None.</p>
     */
    private Deque<SymbolTableEntry.Quantification> myActiveQuantifications =
            new LinkedList<SymbolTableEntry.Quantification>();

    private final TypeGraph myTypeGraph;
    private TreeWalker myWalker;

    /**
     * <p>While we walk the children of a direct definition, this will be set
     * with a pointer to the definition declaration we are walking, otherwise
     * it will be null.  Note that definitions cannot be nested, so there's
     * no need for a stack.</p>
     */
    private DefinitionDec myCurrentDirectDefinition;

    /**
     * <p>While walking a procedure, this is set to the entry for the operation 
     * or FacilityOperation that the procedure is attempting to implement.</p>
     * 
     * <p><strong>INVARIANT:</strong> 
     * <code>myCorrespondingOperation != null</code> <em>implies</em> 
     * <code>myCurrentParameters != null</code>.</p>
     */
    private OperationEntry myCorrespondingOperation;
    
    /**
     * <p>While we walk the children of an operation, FacilityOperation, or
     * procedure, this list will contain all formal parameters encountered so 
     * far, otherwise it will be null.  Since none of these structures can be
     * be nested, there's no need for a stack.</p>
     * 
     * <p>If you need to distinguish if you're in the middle of an 
     * operation/FacilityOperation or a procedure, check 
     * myCorrespondingOperation.</p>
     */
    private List<ProgramParameterEntry> myCurrentParameters;

    /**
     * <p>A mapping from generic types that appear in the module to the math
     * types that bound their possible values.</p>
     */
    private Map<String, MTType> myGenericTypes = new HashMap<String, MTType>();

    public MathPopulator(MathSymbolTableBuilder builder) {
        myActiveQuantifications.push(SymbolTableEntry.Quantification.NONE);

        myTypeGraph = builder.getTypeGraph();
        myBuilder = builder;
    }

    public void setTreeWalker(TreeWalker w) {
        //TODO : This is required by an annoying circular dependency.  Ideally,
        //       the methods of TreeWalker should just be static so that an
        //       instance is not required
        myWalker = w;
    }

    //-------------------------------------------------------------------
    //   Visitor methods
    //-------------------------------------------------------------------

    @Override
    public void preModuleDec(ModuleDec node) {
        MathPopulator.emitDebug("----------------------\nBEGIN MATH POPULATOR");

        myCurModuleScope = myBuilder.startModuleScope(node);
    }

    @Override
    public void preEnhancementModuleDec(EnhancementModuleDec enhancement) {

        //Enhancements implicitly import the concepts they enhance
        myCurModuleScope.addImport(new ModuleIdentifier(enhancement
                .getConceptName().getName()));
    }

    @Override
    public void preEnhancementBodyModuleDec(
            EnhancementBodyModuleDec enhancementRealization) {

        //Enhancement realizations implicitly import the concepts they enhance
        //and the enhancements they realize
        myCurModuleScope.addImport(new ModuleIdentifier(enhancementRealization
                .getConceptName().getName()));
        myCurModuleScope.addImport(new ModuleIdentifier(enhancementRealization
                .getEnhancementName().getName()));
    }

    @Override
    public void postUsesItem(UsesItem uses) {
        myCurModuleScope.addImport(new ModuleIdentifier(uses));
    }

    @Override
    public void postConstantParamDec(ConstantParamDec param) {
        try {
            String paramName = param.getName().getName();

            myBuilder.getInnermostActiveScope().addFormalParameter(paramName,
                    param, ParameterMode.EVALUATES,
                    param.getTy().getProgramTypeValue());
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(param.getName().getName(), param.getName()
                    .getLocation());
        }
    }

    @Override
    public void postConceptTypeParamDec(ConceptTypeParamDec param) {
        try {
            String paramName = param.getName().getName();

            myBuilder.getInnermostActiveScope().addFormalParameter(paramName,
                    param, ParameterMode.TYPE, new PTElement(myTypeGraph));

            myGenericTypes.put(paramName, myTypeGraph.ENTITY);
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(param.getName().getName(), param.getName()
                    .getLocation());
        }
    }

    @Override
    public void postStructureExp(StructureExp structure) {
        //TODO: Remove the StructureExps from where they appear--they're no 
        //      longer used

        //Type it so we don't get an error
        structure.setMathType(myTypeGraph.ENTITY);
    }

    @Override
    public void preFacilityOperationDec(FacilityOperationDec dec) {
        myBuilder.startScope(dec);
    }
    
    @Override
    public void preProcedureDec(ProcedureDec dec) {
        
        try {
            //Figure out what Operation we correspond to (we don't use 
            //OperationQuery because we want to check parameter types 
            //separately in postProcedureDec)
            myCorrespondingOperation = 
                    myBuilder.getInnermostActiveScope().queryForOne(
                        new NameAndEntryTypeQuery(null, dec.getName(), 
                            OperationEntry.class, ImportStrategy.IMPORT_NAMED, 
                            FacilityStrategy.FACILITY_IGNORE, false))
                        .toOperationEntry(dec.getLocation());
            
            myBuilder.startScope(dec);
        
            myCurrentParameters = new LinkedList<ProgramParameterEntry>();
        }
        catch (NoSuchSymbolException nsse) {
            throw new SourceErrorException("Procedure " + 
                    dec.getName().getName() + " does not implement any known " +
                    "Operation.", dec.getName().getLocation());
        }
        catch (DuplicateSymbolException dse) {
            //We should have caught this before now, like when we defined the
            //duplicate Operation
            throw new RuntimeException("Duplicate Operations for " + 
                    dec.getName().getName() + "?");
        }
    }
    
    @Override
    public void postProcedureDec(ProcedureDec dec) {
        myBuilder.endScope();
        
        //We're about to throw away all information about procedure parameters,
        //since they're redundant anyway.  So we sanity-check them first.
        Ty returnTy = dec.getReturnTy();
        PTType returnType;
        if (returnTy == null) {
            returnType = PTVoid.getInstance(myTypeGraph);
        }
        else {
            returnType = returnTy.getProgramTypeValue();
        }

        if (!returnType.equals(myCorrespondingOperation.getReturnType())) {
            throw new SourceErrorException("Procedure return type does " +
                    "not correspond to the return type of the operation " +
                    "it implements.  \n\nExpected type: " + 
                    myCorrespondingOperation.getReturnType() + " (" + 
                    myCorrespondingOperation.getSourceModuleIdentifier() +
                    "." + myCorrespondingOperation.getName() + ")\n\n" +
                    "Found type: " + returnType, dec.getLocation());
        }

        if (myCorrespondingOperation.getParameters().size() !=
                myCurrentParameters.size()) {
            throw new SourceErrorException("Procedure parameter count " +
                    "does not correspond to the parameter count of the " +
                    "operation it implements. \n\nExpected count: " +
                    myCorrespondingOperation.getParameters().size() + " (" +
                    myCorrespondingOperation.getSourceModuleIdentifier() +
                    "." + myCorrespondingOperation.getName() + ")\n\n" +
                    "Found count: " + myCurrentParameters.size(), 
                    dec.getLocation());
        }

        Iterator<ProgramParameterEntry> opParams = 
                myCorrespondingOperation.getParameters().iterator();
        Iterator<ProgramParameterEntry> procParams =
                myCurrentParameters.iterator();
        ProgramParameterEntry curOpParam, curProcParam;
        while (opParams.hasNext()) {
            curOpParam = opParams.next();
            curProcParam = procParams.next();

            if (!curOpParam.getParameterMode().canBeImplementedWith(
                    curProcParam.getParameterMode())) {
                throw new SourceErrorException(
                        curOpParam.getParameterMode() + "-mode parameter " +
                        "cannot be implemented with " + 
                        curProcParam.getParameterMode() + " mode.  " +
                        "Select one of these valid modes instead: " + 
                        Arrays.toString(curOpParam.getParameterMode().getValidImplementationModes()),
                        curProcParam.getDefiningElement().getLocation());
            }

            if (!curOpParam.getDeclaredType().equals(
                    curProcParam.getDeclaredType())) {
                throw new SourceErrorException("Parameter type does not " +
                        "match corresponding operation parameter type." +
                        "\n\nExpected: " + curOpParam.getDeclaredType() +
                        " (" + curOpParam.getSourceModuleIdentifier() + 
                        "." + myCorrespondingOperation.getName() + ")\n\n" +
                        "Found: " + curProcParam.getDeclaredType(),
                        curProcParam.getDefiningElement().getLocation());
            }

            if (!curOpParam.getName().equals(curProcParam.getName())) {
                throw new SourceErrorException("Parmeter name does not " +
                        "match corresponding operation parameter name." +
                        "\n\nExpected name: " + curOpParam.getName() + 
                        " (" + curOpParam.getSourceModuleIdentifier() +
                        "." + myCorrespondingOperation.getName() + ")\n\n" +
                        "Found name: " + curProcParam.getName(),
                        curProcParam.getDefiningElement().getLocation());
            }
        }
        
        try {
            myBuilder.getInnermostActiveScope().addProcedure(
                    dec.getName().getName(), dec, myCorrespondingOperation);
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(dec.getName().getName(), dec.getName()
                    .getLocation());
        }
        
        myCurrentParameters = null;
    }
    
    @Override
    public void preOperationDec(OperationDec dec) {
        myBuilder.startScope(dec);

        myCurrentParameters = new LinkedList<ProgramParameterEntry>();
    }

    @Override
    public void midOperationDec(OperationDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {

        if (prevChild == node.getReturnTy() && node.getReturnTy() != null) {
            try {
                //Inside the operation's assertions, the name of the operation
                //refers to its return value
                myBuilder.getInnermostActiveScope().addBinding(
                        node.getName().getName(), node,
                        node.getReturnTy().getMathTypeValue());
            }
            catch (DuplicateSymbolException dse) {
                //This shouldn't be possible--the operation declaration has a 
                //scope all its own and we're the first ones to get to
                //introduce anything
                throw new RuntimeException(dse);
            }
        }
    }

    @Override
    public void postOperationDec(OperationDec dec) {
        myBuilder.endScope();

        try {
            Ty returnTy = dec.getReturnTy();
            PTType returnType;
            if (returnTy == null) {
                returnType = PTVoid.getInstance(myTypeGraph);
            }
            else {
                returnType = returnTy.getProgramTypeValue();
            }

            myBuilder.getInnermostActiveScope().addOperation(
                    dec.getName().getName(), dec, myCurrentParameters,
                    returnType);
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(dec.getName().getName(), dec.getName()
                    .getLocation());
        }

        myCurrentParameters = null;
    }

    @Override
    public void postFacilityOperationDec(FacilityOperationDec dec) {
        myBuilder.endScope();
    }

    @Override
    public void postParameterVarDec(ParameterVarDec dec) {

        ParameterMode mode =
                ProgramParameterEntry.OLD_TO_NEW_MODE.get(dec.getMode());

        if (mode == null) {
            throw new RuntimeException("Unexpected parameter mode: "
                    + dec.getMode());
        }

        try {
            ProgramParameterEntry paramEntry =
                    myBuilder.getInnermostActiveScope().addFormalParameter(
                            dec.getName().getName(), dec, mode,
                            dec.getTy().getProgramTypeValue());
            myCurrentParameters.add(paramEntry);
        }
        catch (DuplicateSymbolException e) {
            duplicateSymbol(dec.getName().getName(), dec.getName()
                    .getLocation());
        }
    }

    @Override
    public void preTypeDec(TypeDec dec) {
        myBuilder.startScope(dec);
    }

    @Override
    public void midTypeDec(TypeDec dec, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {

        if (prevChild == dec.getModel()) {
            //We've parsed the model, but nothing else, so we can add our 
            //exemplar to scope if we've got one
            PosSymbol exemplar = dec.getExemplar();

            if (exemplar != null) {
                try {
                    myBuilder.getInnermostActiveScope().addBinding(
                            exemplar.getName(), dec,
                            dec.getModel().getMathTypeValue());
                }
                catch (DuplicateSymbolException dse) {
                    //This shouldn't be possible--the type declaration has a 
                    //scope all its own and we're the first ones to get to
                    //introduce anything
                    throw new RuntimeException(dse);
                }
            }
        }
    }

    @Override
    public void postTypeDec(TypeDec dec) {
        myBuilder.endScope();

        try {
            myBuilder.getInnermostActiveScope().addProgramType(
                    dec.getName().getName(), dec,
                    dec.getModel().getMathTypeValue());
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(dec.getName().getName(), dec.getName()
                    .getLocation());
        }
    }

    @Override
    public void postNameTy(NameTy ty) {
        //Note that all mathematical types are ArbitraryExpTys, so this must
        //be in a program-type syntactic slot.

        PosSymbol tySymbol = ty.getName();
        PosSymbol tyQualifier = ty.getQualifier();
        Location tyLocation = tySymbol.getLocation();
        String tyName = tySymbol.getName();

        try {
            ProgramTypeEntry type =
                    myBuilder.getInnermostActiveScope().queryForOne(
                            new NameQuery(tyQualifier, tySymbol,
                                    ImportStrategy.IMPORT_NAMED,
                                    FacilityStrategy.FACILITY_INSTANTIATE,
                                    false)).toProgramTypeEntry(tyLocation);

            ty.setProgramTypeValue(type.getProgramType());
            ty.setMathType(myTypeGraph.MTYPE);
            ty.setMathTypeValue(type.getModelType());
        }
        catch (NoSuchSymbolException nsse) {
            noSuchSymbol(tyQualifier, tyName, tyLocation);
        }
        catch (DuplicateSymbolException dse) {
            //Shouldn't be possible--NameQuery can't throw this
            throw new RuntimeException(dse);
        }
    }

    @Override
    public void postFacilityDec(FacilityDec facility) {
        try {
            myBuilder.getInnermostActiveScope().addFacility(facility);
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(facility.getName().getName(), facility.getName()
                    .getLocation());
        }
    }

    @Override
    public void postMathAssertionDec(MathAssertionDec node) {
        if (node.getAssertion() != null) {
            expectType(node.getAssertion(), myTypeGraph.BOOLEAN);
        }

        String name = node.getName().getName();
        addBinding(name, node.getName().getLocation(), node,
                myTypeGraph.BOOLEAN);

        MathPopulator.emitDebug("New theorem: " + name);
    }

    @Override
    public void postMathVarDec(MathVarDec node) {

        MTType mathTypeValue = node.getTy().getMathTypeValue();
        String varName = node.getName().getName();

        node.setMathType(mathTypeValue);

        SymbolTableEntry.Quantification q =
                SymbolTableEntry.Quantification.UNIVERSAL;
        //TODO : the for-alls in a type theorem should be normal QuantExps and
        //       the quantification here should depend on the innermost QuantExp
        addBinding(varName, node.getName().getLocation(), q, node,
                mathTypeValue);

        MathPopulator.emitDebug("  New variable: " + varName + " of type "
                + mathTypeValue.toString() + " with quantification " + q);
    }

    @Override
    public void postVarDec(VarDec programVar) {

        MTType mathTypeValue = programVar.getTy().getMathTypeValue();
        String varName = programVar.getName().getName();

        programVar.setMathType(mathTypeValue);

        try {
            myBuilder.getInnermostActiveScope().addProgramVariable(varName,
                    programVar, programVar.getTy().getProgramTypeValue());
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(varName, programVar.getLocation());
        }

        MathPopulator.emitDebug("  New program variable: " + varName
                + " of type " + mathTypeValue.toString()
                + " with quantification NONE");
    }

    @Override
    public void postVariableNameExp(VariableNameExp node) {
        try {
            ProgramVariableEntry entry = 
                    myBuilder.getInnermostActiveScope().queryForOne(
                        new ProgramVariableQuery(node.getQualifier(), 
                            node.getName()));
            
            node.setProgramType(entry.getProgramType());
            
            //Handle math typing stuff
            postSymbolExp(node.getQualifier(), node.getName().getName(), node);
        }
        catch (NoSuchSymbolException nsse) {
            noSuchSymbol(node.getQualifier(), node.getName().getName(), 
                    node.getLocation());
        }
        catch (DuplicateSymbolException dse) {
            throw new RuntimeException("ToDo"); //TODO
        }
    }

    @Override
    public void postProgramParamExp(ProgramParamExp node) {
        List<ProgramExp> args = node.getArguments();
        
        List<PTType> argTypes = new LinkedList<PTType>();
        for (ProgramExp arg : args) {
            argTypes.add(arg.getProgramType());
        }
        
        try {
            OperationEntry op = myBuilder.getInnermostActiveScope().queryForOne(
                    new OperationQuery(null, node.getName(), argTypes));
            
            node.setProgramType(op.getReturnType());
            node.setMathType(op.getReturnType().toMath());
        }
        catch (NoSuchSymbolException nsse) {
            noSuchSymbol(null, node.getName().getName(), node.getLocation());
        }
        catch (DuplicateSymbolException dse) {
            //This should be caught earlier, when the duplicate operation is
            //created
            throw new RuntimeException();
        }
    }

    @Override
    public void postOldExp(OldExp exp) {
        exp.setMathType(exp.getExp().getMathType());
        exp.setMathTypeValue(exp.getExp().getMathTypeValue());
    }

    @Override
    public void preDefinitionDec(DefinitionDec node) {
        myBuilder.startScope(node);

        if (!node.isInductive()) {
            myCurrentDirectDefinition = node;
        }
    }

    @Override
    public void postDefinitionDec(DefinitionDec node) {
        myBuilder.endScope();

        MTType declaredType = node.getReturnTy().getMathTypeValue();

        if (node.getDefinition() != null) {
            expectType(node.getDefinition(), declaredType);
        }
        else if (node.isInductive()) {
            expectType(node.getBase(), myTypeGraph.BOOLEAN);
            expectType(node.getHypothesis(), myTypeGraph.BOOLEAN);
        }

        List<MathVarDec> listVarDec = node.getParameters();
        if (listVarDec != null) {
            declaredType = new MTFunction(myTypeGraph, node);
        }
        String definitionSymbol = node.getName().getName();

        MTType typeValue = null;
        if (node.getDefinition() != null) {
            typeValue = node.getDefinition().getMathTypeValue();
        }

        //Note that, even if typeValue is null at this point, if declaredType
        //returns true from knownToContainOnlyMTypes(), a new type value will
        //still be created by the symbol table
        addBinding(definitionSymbol, node.getName().getLocation(), node,
                declaredType, typeValue);

        MathPopulator.emitDebug("New definition: " + definitionSymbol
                + " of type " + declaredType
                + ((typeValue != null) ? " with type value " + typeValue : ""));

        myCurrentDirectDefinition = null;
    }

    @Override
    public void preQuantExp(QuantExp node) {
        MathPopulator.emitDebug("Entering preQuantExp...");
        myBuilder.startScope(node);
    }

    public void walkQuantExp(QuantExp node) {
        MathPopulator.emitDebug("Entering walkQuantExp...");
        List<MathVarDec> vars = node.getVars();

        SymbolTableEntry.Quantification quantification;
        switch (node.getOperator()) {
        case QuantExp.EXISTS:
            quantification = SymbolTableEntry.Quantification.EXISTENTIAL;
            break;
        case QuantExp.FORALL:
            quantification = SymbolTableEntry.Quantification.UNIVERSAL;
            break;
        default:
            throw new RuntimeException("Unrecognized quantification type: "
                    + node.getOperator());
        }

        myActiveQuantifications.push(quantification);
        for (MathVarDec v : vars) {
            myWalker.visit(v);
        }
        myActiveQuantifications.pop();

        myActiveQuantifications.push(SymbolTableEntry.Quantification.NONE);
        myWalker.visit(node.getBody());
        myActiveQuantifications.pop();

        MathPopulator.emitDebug("Exiting walkQuantExp.");
    }

    @Override
    public void postQuantExp(QuantExp node) {
        myBuilder.endScope();

        expectType(node.getBody(), myTypeGraph.BOOLEAN);
        node.setMathType(myTypeGraph.BOOLEAN);
    }

    @Override
    public void postIfExp(IfExp exp) {
        //An "if expression" is a functional condition, as in the following 
        //example:
        //   x = (if (y > 0) then y else -y)
        //Its condition had better be a boolean.  Its type resolves to the 
        //shared type of its branches.

        //TODO : Currently, the parser permits the else clause to be optional.
        //       That is nonsense in a functional context and should be fixed.
        if (exp.getElseclause() == null) {
            throw new RuntimeException("IfExp has no else clause.  The "
                    + "parser should be changed to disallow this and this "
                    + "error should be removed.");
        }

        expectType(exp.getTest(), myTypeGraph.BOOLEAN);

        Exp ifClause = exp.getThenclause();
        Exp elseClause = exp.getElseclause();

        MTType ifType = ifClause.getMathType();
        MTType elseType = elseClause.getMathType();

        boolean ifIsSuperType = myTypeGraph.isSubtype(elseType, ifType);

        //One of these had better be a (non-strict) subtype of the other
        if (!ifIsSuperType && !myTypeGraph.isSubtype(ifType, elseType)) {
            throw new SourceErrorException("Branches must share a type.\n"
                    + "If branch:   " + ifType + "\n" + "Else branch: "
                    + elseType, exp.getLocation());
        }

        MTType finalType, finalTypeValue;
        if (ifIsSuperType) {
            finalType = ifType;
            finalTypeValue = ifClause.getMathTypeValue();
        }
        else {
            finalType = elseType;
            finalTypeValue = elseClause.getMathTypeValue();
        }

        exp.setMathType(finalType);
        exp.setMathTypeValue(finalTypeValue);
    }

    @Override
    public void preArbitraryExpTy(ArbitraryExpTy node) {
        enteringTypeValueNode();
    }

    @Override
    public void postSetExp(SetExp e) {
        MathVarDec varDec = e.getVar();
        MTType varType = varDec.getMathType();

        Exp body = e.getBody();

        expectType(body, myTypeGraph.BOOLEAN);

        if (e.getWhere() != null) {
            body = myTypeGraph.formConjunct(e.getWhere(), body);
        }

        e.setMathType(new MTSetRestriction(myTypeGraph, varType, varDec
                .getName().getName(), body));
        e.setMathTypeValue(new MTPowertypeApplication(myTypeGraph, varType));
    }

    @Override
    public void postIntegerExp(IntegerExp e) {
        postSymbolExp(e.getQualifier(), "" + e.getValue(), e);
    }
    
    @Override
    public void postProgramIntegerExp(ProgramIntegerExp e) {
        e.setProgramType(PTPrimitive.getInstance(myTypeGraph, 
                PrimitiveTypeName.INTEGER));
        
        //Do math type stuff
        postSymbolExp(null, "" + e.getValue(), e);
    }
    
    @Override
    public void postProgramOpExp(ProgramOpExp e) {
        e.setProgramType(e.getProgramType(myTypeGraph));
        e.setMathType(e.getProgramType().toMath());
    }

    @Override
    public void postVarExp(VarExp e) {
        MathSymbolEntry intendedEntry =
                postSymbolExp(e.getQualifier(), e.getName().getName(), e);

        e.setQuantification(intendedEntry.getQuantification()
                .toVarExpQuantificationCode());
    }

    @Override
    public void postAbstractFunctionExp(AbstractFunctionExp foundExp) {

        MTFunction foundExpType;
        foundExpType = foundExp.getConservativePreApplicationType(myTypeGraph);

        MathPopulator.emitDebug("Expression: " + foundExp.toString() + "(" + foundExp.getLocation() + ") "  
                + " of type " + foundExpType.toString());

        MathSymbolEntry intendedEntry = getIntendedFunction(foundExp);
        MTFunction expectedType = (MTFunction) intendedEntry.getType();

        //We know we match expectedType--otherwise the above would have thrown
        //an exception.

        //TODO : The range of our type could be some function of the concrete
        //       binding of the domain.  E.g., foo(x : (X : MType)) : X.  Right
        //       now we ignore this complexity.

        foundExp.setMathType(expectedType.getRange());
        foundExp.setQuantification(intendedEntry.getQuantification());

        if (myTypeValueDepth > 0) {
            //I had better identify a type
            MTFunction entryType = (MTFunction) intendedEntry.getType();

            List<MTType> arguments = new LinkedList<MTType>();
            MTType argTypeValue;
            for (Exp arg : foundExp.getParameters()) {
                argTypeValue = arg.getMathTypeValue();

                if (argTypeValue == null) {
                    notAType(arg);
                }

                arguments.add(argTypeValue);
            }

            foundExp.setMathTypeValue(entryType.getApplicationType(
                    intendedEntry.getName(), arguments));
        }
    }

    @Override
    public void postTupleExp(TupleExp node) {
        //See the note in TupleExp on why TupleExp isn't an AbstractFunctionExp

        //This looks weird, but we're converting from the ridiculous 
        //RESOLVE-internal List into an ordinary java.util.List because we don't
        //live in bizarro-world
        List<Exp> fields = new LinkedList<Exp>(node.getFields());

        if (fields.size() < 2) {
            //We assert that this can't happen, but who knows?
            throw new RuntimeException("Unanticipated tuple size.");
        }

        List<MTCartesian.Element> fieldTypes =
                new LinkedList<MTCartesian.Element>();
        for (Exp field : fields) {
            fieldTypes.add(new MTCartesian.Element(field.getMathType()));
        }

        node.setMathType(new MTCartesian(myTypeGraph, fieldTypes));
    }

    @Override
    public void postArbitraryExpTy(ArbitraryExpTy node) {
        leavingTypeValueNode();

        Exp typeExp = node.getArbitraryExp();
        MTType mathType = typeExp.getMathType();
        MTType mathTypeValue = typeExp.getMathTypeValue();
        if (!mathType.isKnownToContainOnlyMTypes()) {
            notAType(typeExp);
        }

        node.setMathType(mathType);
        node.setMathTypeValue(mathTypeValue);
    }

    @Override
    public void postExp(Exp node) {
        //myMathModeFlag && 
        if (node.getMathType() == null) {
            throw new RuntimeException("Exp " + node + " (" + node.getClass()
                    + ", " + node.getLocation()
                    + ") got through the populator " + "with no math type.");
        }
        
        if (node instanceof ProgramExp && 
                ((ProgramExp) node).getProgramType() == null) {
            throw new RuntimeException("Exp " + node + " (" + node.getClass()
                    + ", " + node.getLocation()
                    + ") got through the populator " + "with no program type.");
        }
    }

    @Override
    public void preTypeTheoremDec(TypeTheoremDec node) {
        myBuilder.startScope(node);
    }

    @Override
    public void postTypeTheoremDec(TypeTheoremDec node) {
        node.setMathType(myTypeGraph.BOOLEAN);

        try {
            myTypeGraph
                    .addRelationship(node.getBindingExpression(), node
                            .getAssertedType().getMathTypeValue(), node
                            .getBindingCondition(), myBuilder
                            .getInnermostActiveScope());
        }
        catch (IllegalArgumentException iae) {
            throw new SourceErrorException(iae.getMessage(), node.getLocation());
        }

        myBuilder.endScope();
    }

    @Override
    public void postModuleDec(ModuleDec node) {
        myBuilder.endScope();

        MathPopulator.emitDebug("END MATH POPULATOR\n----------------------\n");
    }

    //-------------------------------------------------------------------
    //   Error handling
    //-------------------------------------------------------------------

    public void noSuchModule(PosSymbol qualifier) {
        throw new SourceErrorException(
                "Module does not exist or is not in scope.", qualifier);
    }

    public void noSuchSymbol(PosSymbol qualifier, String symbolName, Location l) {

        String message;

        if (qualifier == null) {
            message = "No such symbol: " + symbolName;
        }
        else {
            message =
                    "No such symbol in module: " + qualifier.getName() + "."
                            + symbolName;
        }

        throw new SourceErrorException(message, l);
    }

    public <T extends SymbolTableEntry> void ambiguousSymbol(String symbolName,
            Location l, List<T> candidates) {

        String message = "Ambiguous symbol.  Candidates: ";

        boolean first = true;
        for (SymbolTableEntry candidate : candidates) {
            if (first) {
                first = false;
            }
            else {
                message += ", ";
            }

            message +=
                    candidate.getSourceModuleIdentifier()
                            .fullyQualifiedRepresentation(symbolName);
        }

        message += ".  Consider qualifying.";

        throw new SourceErrorException(message, l);
    }

    public void notAType(SymbolTableEntry entry, Location l) {
        throw new SourceErrorException(entry.getSourceModuleIdentifier()
                .fullyQualifiedRepresentation(entry.getName())
                + " is not known to be a type.", l);
    }

    public void notAType(Exp e) {
        throw new SourceErrorException("Not known to be a type.", e
                .getLocation());
    }

    public void expected(Exp e, MTType expectedType) {
        throw new SourceErrorException("Expected: " + expectedType
                + "\nFound: " + e.getMathType(), e.getLocation());
    }

    public void duplicateSymbol(String symbol, Location l) {
        throw new SourceErrorException("Duplicate symbol: " + symbol, l);
    }

    public void expectType(Exp e, MTType expectedType) {
        if (!myTypeGraph.isKnownToBeIn(e, expectedType)) {
            expected(e, expectedType);
        }
    }

    //-------------------------------------------------------------------
    //   Helper functions
    //-------------------------------------------------------------------

    private SymbolTableEntry addBinding(String name, Location l,
            SymbolTableEntry.Quantification q,
            ResolveConceptualElement definingElement, MTType type,
            MTType typeValue) {
        if (type != null) {
            try {
                return myBuilder.getInnermostActiveScope().addBinding(name, q,
                        definingElement, type, typeValue);
            }
            catch (DuplicateSymbolException dse) {
                duplicateSymbol(name, l);
            }
        }
        return null;
    }

    private SymbolTableEntry addBinding(String name, Location l,
            ResolveConceptualElement definingElement, MTType type,
            MTType typeValue) {
        return addBinding(name, l, SymbolTableEntry.Quantification.NONE,
                definingElement, type, typeValue);
    }

    private SymbolTableEntry addBinding(String name, Location l,
            SymbolTableEntry.Quantification q,
            ResolveConceptualElement definingElement, MTType type) {
        return addBinding(name, l, q, definingElement, type, null);
    }

    private SymbolTableEntry addBinding(String name, Location l,
            ResolveConceptualElement definingElement, MTType type) {
        return addBinding(name, l, SymbolTableEntry.Quantification.NONE,
                definingElement, type, null);
    }

    private void enteringTypeValueNode() {
        myTypeValueDepth++;
    }

    private void leavingTypeValueNode() {
        myTypeValueDepth--;
    }

    private static MTFunction deschematize(MTFunction original,
            Exp soleParameter) {

        Map<String, MTType> bindings = new HashMap<String, MTType>();

        MTType domain = original.getDomain();
        if (domain instanceof MTCartesian) {
            if (!(soleParameter instanceof TupleExp)) {
                deschematize((MTCartesian) domain, (TupleExp) soleParameter,
                        bindings);
            }
        }
        else {
            MTType parameterTypeValue = soleParameter.getMathTypeValue();
            if (parameterTypeValue != null) {
                String tag = original.getSingleParameterName();

                if (tag != null) {
                    bindings.put(tag, parameterTypeValue);
                }
            }

            deschematize(original, soleParameter.getMathType(), bindings);
        }

        VariableReplacingVisitor replacer =
                new VariableReplacingVisitor(bindings);

        original.accept(replacer);

        return (MTFunction) replacer.getFinalExpression();
    }

    private static void deschematize(MTCartesian original, TupleExp value,
            Map<String, MTType> bindings) {

        int originalSize = original.size();
        if (originalSize != value.getSize()) {
            throw new IllegalArgumentException();
        }

        String tag;
        Exp field;
        MTType fieldTypeValue, factor;
        for (int i = 0; i < originalSize; i++) {
            factor = original.getFactor(i);
            field = value.getField(i);
            fieldTypeValue = field.getMathTypeValue();

            if (fieldTypeValue != null) {
                tag = original.getTag(i);

                if (tag != null) {
                    bindings.put(tag, fieldTypeValue);
                }
            }

            deschematize(factor, field.getMathType(), bindings);
        }
    }

    private static void deschematize(MTType original, MTType value,
            Map<String, MTType> bindings) {

        ParameterGenericApplyingVisitor genericApplier =
                new ParameterGenericApplyingVisitor(bindings);

        genericApplier.visit(original, value);

        if (genericApplier.encounteredError()) {
            throw new IllegalArgumentException();
        }
    }

    public static void emitDebug(String msg) {
        if (PRINT_DEBUG) {
            System.out.println(msg);
        }
    }

    private MathSymbolEntry getIntendedEntry(PosSymbol qualifier,
            String symbolName, Exp node) {

        MathSymbolEntry result;

        try {
            result =
                    myBuilder.getInnermostActiveScope().queryForOne(
                            new MathSymbolQuery(qualifier, symbolName, node
                                    .getLocation()));
        }
        catch (DuplicateSymbolException dse) {
            duplicateSymbol(symbolName, node.getLocation());
            throw new RuntimeException(); //This will never fire
        }
        catch (NoSuchSymbolException nsse) {
            noSuchSymbol(qualifier, symbolName, node.getLocation());
            throw new RuntimeException(); //This will never fire
        }

        return result;
    }

    private MathSymbolEntry postSymbolExp(PosSymbol qualifier,
            String symbolName, Exp node) {

        MathSymbolEntry intendedEntry =
                getIntendedEntry(qualifier, symbolName, node);
        node.setMathType(intendedEntry.getType());

        try {
            if (intendedEntry.getQuantification() == SymbolTableEntry.Quantification.NONE) {

                node.setMathTypeValue(intendedEntry.getTypeValue());
            }
            else {
                if (intendedEntry.getType().isKnownToContainOnlyMTypes()) {
                    node.setMathTypeValue(new MTNamed(myTypeGraph, symbolName));
                }
            }
        }
        catch (SymbolNotOfKindTypeException snokte) {
            if (myTypeValueDepth > 0) {
                //I had better identify a type
                notAType(intendedEntry, node.getLocation());
            }
        }

        MathPopulator.emitDebug("Processed symbol " + symbolName
                + " with type " + node.getMathType()
                + ", referencing math type " + node.getMathTypeValue());

        return intendedEntry;
    }

    /**
     * <p>For a given <code>AbstractFunctionExp</code>, finds the entry in the
     * symbol table to which it refers.  For a complete discussion of the
     * algorithm used, see <a href="http://sourceforge.net/apps/mediawiki/resolve/index.php?title=Package_Search_Algorithm">
     * Package Search Algorithm</a>.</p>
     */
    private MathSymbolEntry getIntendedFunction(AbstractFunctionExp e) {

        //TODO : All this logic should be encapsulated into a SymbolQuery called
        //       MathFunctionQuery.

        MTFunction eType = e.getConservativePreApplicationType(myTypeGraph);

        PosSymbol eOperator =
                ((AbstractFunctionExp) e).getOperatorAsPosSymbol();
        String eOperatorString = eOperator.getSymbol().getName();

        List<MathSymbolEntry> sameNameFunctions =
                myBuilder.getInnermostActiveScope().query(
                        new MathFunctionNamedQuery(e.getQualifier(), e
                                .getOperatorAsPosSymbol()));

        if (sameNameFunctions.isEmpty()) {
            throw new SourceErrorException("No such function.", e.getLocation());
        }

        MathSymbolEntry intendedEntry;
        try {
            intendedEntry = getExactDomainTypeMatch(e, sameNameFunctions);
        }
        catch (NoSolutionException nse) {
            try {
                intendedEntry = getInexactDomainTypeMatch(e, sameNameFunctions);
            }
            catch (NoSolutionException nsee2) {
                boolean foundOne = false;
                String errorMessage =
                        "No function applicable for " + "domain: "
                                + eType.getDomain() + "\n\nCandidates:\n";

                for (SymbolTableEntry entry : sameNameFunctions) {

                    if (entry instanceof MathSymbolEntry
                            && ((MathSymbolEntry) entry).getType() instanceof MTFunction) {
                        errorMessage +=
                                "\t" + entry.getName() + " : "
                                        + ((MathSymbolEntry) entry).getType()
                                        + "\n";

                        foundOne = true;
                    }
                }

                if (!foundOne) {
                    throw new SourceErrorException("No such function.", e
                            .getLocation());
                }

                throw new SourceErrorException(errorMessage, e.getLocation());
            }
        }

        if (intendedEntry.getDefiningElement() == myCurrentDirectDefinition) {
            throw new SourceErrorException("Direct definition cannot "
                    + "contain recursive call.", e.getLocation());
        }

        MTFunction intendedEntryType = (MTFunction) intendedEntry.getType();

        MathPopulator.emitDebug("Matching " + eOperatorString + " : " + eType
                + " to " + intendedEntry.getName() + " : " + intendedEntryType
                + ".");

        return intendedEntry;
    }

    private MathSymbolEntry getExactDomainTypeMatch(AbstractFunctionExp e,
            List<MathSymbolEntry> candidates) throws NoSolutionException {

        return getDomainTypeMatch(e, candidates, EXACT_DOMAIN_MATCH);
    }

    private MathSymbolEntry getInexactDomainTypeMatch(AbstractFunctionExp e,
            List<MathSymbolEntry> candidates) throws NoSolutionException {

        return getDomainTypeMatch(e, candidates, INEXACT_DOMAIN_MATCH);
    }

    //TODO : This is a mess, clean it up!
    private MathSymbolEntry getDomainTypeMatch(AbstractFunctionExp e,
            List<MathSymbolEntry> candidates,
            TypeComparison<AbstractFunctionExp, MTFunction> comparison)
            throws NoSolutionException {

        //Exp soleParameter = e.getSoleParameter(myTypeGraph);

        MTFunction eType = e.getConservativePreApplicationType(myTypeGraph);

        /*
        eType =
                (MTFunction) eType
                        .getCopyWithVariablesSubstituted(myGenericTypes);
        e = TypeGraph.getCopyWithVariablesSubstituted(e, myGenericTypes);
        */
        
        MathSymbolEntry match = null;

        //MTFunction candidateType;
        for (MathSymbolEntry candidate : candidates) {
            if (candidate.getType() instanceof MTFunction) {

                try {
                    //candidateType = (MTFunction) candidate.getType();
                    //candidateType = deschematize(candidateType, soleParameter);

                    if (comparison.compare(e, eType,
                            (MTFunction) candidate.getType())) {

                        if (match != null) {
                            throw new SourceErrorException("Multiple "
                                    + comparison.description() + " domain "
                                    + "matches.  For example, "
                                    + match.getName() + " : "
                                    + match.getType() + " and "
                                    + candidate.getName() + " : "
                                    + candidate.getType()
                                    + ".  Consider explicitly qualifying.",
                                    e.getLocation());
                        }

                        match = candidate;
                    }
                }
                catch (IllegalArgumentException iae) {
                    //couldn't deschematize--try the next one
                }
            }
        }

        if (match == null) {
            throw new NoSolutionException();
        }

        return match;
    }

    //-------------------------------------------------------------------
    //   Helper classes
    //-------------------------------------------------------------------

    private static class ExactDomainMatch
            implements
                TypeComparison<AbstractFunctionExp, MTFunction> {

        @Override
        public boolean compare(AbstractFunctionExp foundValue,
                MTFunction foundType, MTFunction expectedType) {

            return foundType.parameterTypesMatch(expectedType,
                    EXACT_PARAMETER_MATCH);
        }

        @Override
        public String description() {
            return "exact";
        }
    }

    private class InexactDomainMatch
            implements
                TypeComparison<AbstractFunctionExp, MTFunction> {

        @Override
        public boolean compare(AbstractFunctionExp foundValue,
                MTFunction foundType, MTFunction expectedType) {

            return expectedType.parametersMatch(foundValue.getParameters(),
                    INEXACT_PARAMETER_MATCH);
        }

        @Override
        public String description() {
            return "inexact";
        }
    }

    private static class ExactParameterMatch implements Comparator<MTType> {

        @Override
        public int compare(MTType o1, MTType o2) {
            int result;

            if (o1.equals(o2)) {
                result = 0;
            }
            else {
                result = 1;
            }

            return result;
        }

    }

    private class InexactParameterMatch implements TypeComparison<Exp, MTType> {

        @Override
        public boolean compare(Exp foundValue, MTType foundType,
                MTType expectedType) {

            return myTypeGraph.isKnownToBeIn(foundValue, expectedType);
        }

        @Override
        public String description() {
            return "inexact";
        }
    }
}