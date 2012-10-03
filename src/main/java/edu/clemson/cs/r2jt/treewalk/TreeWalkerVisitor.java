package edu.clemson.cs.r2jt.treewalk;

import edu.clemson.cs.r2jt.absyn.*;

public class TreeWalkerVisitor {

    public void preAny(ResolveConceptualElement data) {}

    public void postAny(ResolveConceptualElement data) {}

    public void preFinalItem(FinalItem data) {}

    public void midFinalItem(FinalItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFinalItem(FinalItem data) {}

    public void preFacilityModuleDec(FacilityModuleDec data) {}

    public void midFacilityModuleDec(FacilityModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFacilityModuleDec(FacilityModuleDec data) {}

    public void preCategoricalDefinitionDec(CategoricalDefinitionDec data) {}

    public void midCategoricalDefinitionDec(CategoricalDefinitionDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postCategoricalDefinitionDec(CategoricalDefinitionDec data) {}

    public void preLambdaExp(LambdaExp data) {}

    public void midLambdaExp(LambdaExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postLambdaExp(LambdaExp data) {}

    public void preParameterVarDec(ParameterVarDec data) {}

    public void midParameterVarDec(ParameterVarDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postParameterVarDec(ParameterVarDec data) {}

    public void preConstructedTy(ConstructedTy data) {}

    public void midConstructedTy(ConstructedTy node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConstructedTy(ConstructedTy data) {}

    public void preQuantExp(QuantExp data) {}

    public void midQuantExp(QuantExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postQuantExp(QuantExp data) {}

    public void preCrossTypeExpression(CrossTypeExpression data) {}

    public void midCrossTypeExpression(CrossTypeExpression node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postCrossTypeExpression(CrossTypeExpression data) {}

    public void preRepresentationDec(RepresentationDec data) {}

    public void midRepresentationDec(RepresentationDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postRepresentationDec(RepresentationDec data) {}

    public void preCartProdTy(CartProdTy data) {}

    public void midCartProdTy(CartProdTy node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postCartProdTy(CartProdTy data) {}

    public void preNameTy(NameTy data) {}

    public void midNameTy(NameTy node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postNameTy(NameTy data) {}

    public void preUsesItem(UsesItem data) {}

    public void midUsesItem(UsesItem node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postUsesItem(UsesItem data) {}

    public void preTypeDec(TypeDec data) {}

    public void midTypeDec(TypeDec node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postTypeDec(TypeDec data) {}

    public void preModuleArgumentItem(ModuleArgumentItem data) {}

    public void midModuleArgumentItem(ModuleArgumentItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postModuleArgumentItem(ModuleArgumentItem data) {}

    public void preMathAssertionDec(MathAssertionDec data) {}

    public void midMathAssertionDec(MathAssertionDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMathAssertionDec(MathAssertionDec data) {}

    public void preDeductionExp(DeductionExp data) {}

    public void midDeductionExp(DeductionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postDeductionExp(DeductionExp data) {}

    public void preCharExp(CharExp data) {}

    public void midCharExp(CharExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postCharExp(CharExp data) {}

    public void preVarExp(VarExp data) {}

    public void midVarExp(VarExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVarExp(VarExp data) {}

    public void preSubtypeDec(SubtypeDec data) {}

    public void midSubtypeDec(SubtypeDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postSubtypeDec(SubtypeDec data) {}

    public void preFieldExp(FieldExp data) {}

    public void midFieldExp(FieldExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFieldExp(FieldExp data) {}

    public void preOutfixExp(OutfixExp data) {}

    public void midOutfixExp(OutfixExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postOutfixExp(OutfixExp data) {}

    public void preSetExp(SetExp data) {}

    public void midSetExp(SetExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postSetExp(SetExp data) {}

    public void preSuppositionExp(SuppositionExp data) {}

    public void midSuppositionExp(SuppositionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postSuppositionExp(SuppositionExp data) {}

    public void preEnhancementBodyModuleDec(EnhancementBodyModuleDec data) {}

    public void midEnhancementBodyModuleDec(EnhancementBodyModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postEnhancementBodyModuleDec(EnhancementBodyModuleDec data) {}

    public void preVariableRecordExp(VariableRecordExp data) {}

    public void midVariableRecordExp(VariableRecordExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVariableRecordExp(VariableRecordExp data) {}

    public void preFacilityOperationDec(FacilityOperationDec data) {}

    public void midFacilityOperationDec(FacilityOperationDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFacilityOperationDec(FacilityOperationDec data) {}

    public void preBetweenExp(BetweenExp data) {}

    public void midBetweenExp(BetweenExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postBetweenExp(BetweenExp data) {}

    public void preTypeTheoremDec(TypeTheoremDec data) {}

    public void midTypeTheoremDec(TypeTheoremDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postTypeTheoremDec(TypeTheoremDec data) {}

    public void preEnhancementItem(EnhancementItem data) {}

    public void midEnhancementItem(EnhancementItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postEnhancementItem(EnhancementItem data) {}

    public void preProofDec(ProofDec data) {}

    public void midProofDec(ProofDec node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProofDec(ProofDec data) {}

    public void preBooleanTy(BooleanTy data) {}

    public void midBooleanTy(BooleanTy node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postBooleanTy(BooleanTy data) {}

    public void preGoalExp(GoalExp data) {}

    public void midGoalExp(GoalExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postGoalExp(GoalExp data) {}

    public void preIterativeExp(IterativeExp data) {}

    public void midIterativeExp(IterativeExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIterativeExp(IterativeExp data) {}

    public void preSuppositionDeductionExp(SuppositionDeductionExp data) {}

    public void midSuppositionDeductionExp(SuppositionDeductionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postSuppositionDeductionExp(SuppositionDeductionExp data) {}

    public void prePerformanceOperationDec(PerformanceOperationDec data) {}

    public void midPerformanceOperationDec(PerformanceOperationDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postPerformanceOperationDec(PerformanceOperationDec data) {}

    public void preIntegerExp(IntegerExp data) {}

    public void midIntegerExp(IntegerExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIntegerExp(IntegerExp data) {}

    public void preProgramExp(ProgramExp data) {}

    public void midProgramExp(ProgramExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramExp(ProgramExp data) {}

    public void preConceptBodyModuleDec(ConceptBodyModuleDec data) {}

    public void midConceptBodyModuleDec(ConceptBodyModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConceptBodyModuleDec(ConceptBodyModuleDec data) {}

    public void preDec(Dec data) {}

    public void midDec(Dec node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postDec(Dec data) {}

    public void preFunctionTy(FunctionTy data) {}

    public void midFunctionTy(FunctionTy node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFunctionTy(FunctionTy data) {}

    public void preModuleParameterDec(ModuleParameterDec data) {}

    public void midModuleParameterDec(ModuleParameterDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postModuleParameterDec(ModuleParameterDec data) {}

    public void preStringExp(StringExp data) {}

    public void midStringExp(StringExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postStringExp(StringExp data) {}

    public void preAssumeStmt(AssumeStmt data) {}

    public void midAssumeStmt(AssumeStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAssumeStmt(AssumeStmt data) {}

    public void preAlternativeExp(AlternativeExp data) {}

    public void midAlternativeExp(AlternativeExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAlternativeExp(AlternativeExp data) {}

    public void preFunctionExp(FunctionExp data) {}

    public void midFunctionExp(FunctionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFunctionExp(FunctionExp data) {}

    public void preStatement(Statement data) {}

    public void midStatement(Statement node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postStatement(Statement data) {}

    public void preArrayTy(ArrayTy data) {}

    public void midArrayTy(ArrayTy node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postArrayTy(ArrayTy data) {}

    public void preUnaryMinusExp(UnaryMinusExp data) {}

    public void midUnaryMinusExp(UnaryMinusExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postUnaryMinusExp(UnaryMinusExp data) {}

    public void preProgramDotExp(ProgramDotExp data) {}

    public void midProgramDotExp(ProgramDotExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramDotExp(ProgramDotExp data) {}

    public void preAbstractParameterizedModuleDec(
            AbstractParameterizedModuleDec data) {}

    public void midAbstractParameterizedModuleDec(
            AbstractParameterizedModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAbstractParameterizedModuleDec(
            AbstractParameterizedModuleDec data) {}

    public void preVariableNameExp(VariableNameExp data) {}

    public void midVariableNameExp(VariableNameExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVariableNameExp(VariableNameExp data) {}

    public void preStructureExp(StructureExp data) {}

    public void midStructureExp(StructureExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postStructureExp(StructureExp data) {}

    public void preVariableExp(VariableExp data) {}

    public void midVariableExp(VariableExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVariableExp(VariableExp data) {}

    public void preSwapStmt(SwapStmt data) {}

    public void midSwapStmt(SwapStmt node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postSwapStmt(SwapStmt data) {}

    public void preDoubleExp(DoubleExp data) {}

    public void midDoubleExp(DoubleExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postDoubleExp(DoubleExp data) {}

    public void preMathTypeFormalDec(MathTypeFormalDec data) {}

    public void midMathTypeFormalDec(MathTypeFormalDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMathTypeFormalDec(MathTypeFormalDec data) {}

    public void prePerformanceFinalItem(PerformanceFinalItem data) {}

    public void midPerformanceFinalItem(PerformanceFinalItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postPerformanceFinalItem(PerformanceFinalItem data) {}

    public void preDefinitionDec(DefinitionDec data) {}

    public void midDefinitionDec(DefinitionDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postDefinitionDec(DefinitionDec data) {}

    public void preMathModuleDec(MathModuleDec data) {}

    public void midMathModuleDec(MathModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMathModuleDec(MathModuleDec data) {}

    public void preProgramFunctionExp(ProgramFunctionExp data) {}

    public void midProgramFunctionExp(ProgramFunctionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramFunctionExp(ProgramFunctionExp data) {}

    public void preInitItem(InitItem data) {}

    public void midInitItem(InitItem node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postInitItem(InitItem data) {}

    public void preProgramIntegerExp(ProgramIntegerExp data) {}

    public void midProgramIntegerExp(ProgramIntegerExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramIntegerExp(ProgramIntegerExp data) {}

    public void preEnhancementModuleDec(EnhancementModuleDec data) {}

    public void midEnhancementModuleDec(EnhancementModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postEnhancementModuleDec(EnhancementModuleDec data) {}

    public void preEnhancementBodyItem(EnhancementBodyItem data) {}

    public void midEnhancementBodyItem(EnhancementBodyItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postEnhancementBodyItem(EnhancementBodyItem data) {}

    public void preAuxVarDec(AuxVarDec data) {}

    public void midAuxVarDec(AuxVarDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAuxVarDec(AuxVarDec data) {}

    public void preConfirmStmt(ConfirmStmt data) {}

    public void midConfirmStmt(ConfirmStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConfirmStmt(ConfirmStmt data) {}

    public void preExp(Exp data) {}

    public void midExp(Exp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postExp(Exp data) {}

    public void preProgramCharExp(ProgramCharExp data) {}

    public void midProgramCharExp(ProgramCharExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramCharExp(ProgramCharExp data) {}

    public void prePerformanceInitItem(PerformanceInitItem data) {}

    public void midPerformanceInitItem(PerformanceInitItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postPerformanceInitItem(PerformanceInitItem data) {}

    public void preEqualsExp(EqualsExp data) {}

    public void midEqualsExp(EqualsExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postEqualsExp(EqualsExp data) {}

    public void preIfStmt(IfStmt data) {}

    public void midIfStmt(IfStmt node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIfStmt(IfStmt data) {}

    public void prePerformanceTypeDec(PerformanceTypeDec data) {}

    public void midPerformanceTypeDec(PerformanceTypeDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postPerformanceTypeDec(PerformanceTypeDec data) {}

    public void preAffectsItem(AffectsItem data) {}

    public void midAffectsItem(AffectsItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAffectsItem(AffectsItem data) {}

    public void preMathTypeDec(MathTypeDec data) {}

    public void midMathTypeDec(MathTypeDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMathTypeDec(MathTypeDec data) {}

    public void preSelectionStmt(SelectionStmt data) {}

    public void midSelectionStmt(SelectionStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postSelectionStmt(SelectionStmt data) {}

    public void preDotExp(DotExp data) {}

    public void midDotExp(DotExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postDotExp(DotExp data) {}

    public void preShortFacilityModuleDec(ShortFacilityModuleDec data) {}

    public void midShortFacilityModuleDec(ShortFacilityModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postShortFacilityModuleDec(ShortFacilityModuleDec data) {}

    public void preJustifiedExp(JustifiedExp data) {}

    public void midJustifiedExp(JustifiedExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postJustifiedExp(JustifiedExp data) {}

    public void preRealizationParamDec(RealizationParamDec data) {}

    public void midRealizationParamDec(RealizationParamDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postRealizationParamDec(RealizationParamDec data) {}

    public void preFunctionValueExp(FunctionValueExp data) {}

    public void midFunctionValueExp(FunctionValueExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFunctionValueExp(FunctionValueExp data) {}

    public void prePerformanceModuleDec(PerformanceModuleDec data) {}

    public void midPerformanceModuleDec(PerformanceModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postPerformanceModuleDec(PerformanceModuleDec data) {}

    public void preConceptTypeParamDec(ConceptTypeParamDec data) {}

    public void midConceptTypeParamDec(ConceptTypeParamDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConceptTypeParamDec(ConceptTypeParamDec data) {}

    public void preFacilityDec(FacilityDec data) {}

    public void midFacilityDec(FacilityDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFacilityDec(FacilityDec data) {}

    public void preRenamingItem(RenamingItem data) {}

    public void midRenamingItem(RenamingItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postRenamingItem(RenamingItem data) {}

    public void preMemoryStmt(MemoryStmt data) {}

    public void midMemoryStmt(MemoryStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMemoryStmt(MemoryStmt data) {}

    public void preTupleExp(TupleExp data) {}

    public void midTupleExp(TupleExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postTupleExp(TupleExp data) {}

    public void preJustificationExp(JustificationExp data) {}

    public void midJustificationExp(JustificationExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postJustificationExp(JustificationExp data) {}

    public void preOperationDec(OperationDec data) {}

    public void midOperationDec(OperationDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postOperationDec(OperationDec data) {}

    public void preIfExp(IfExp data) {}

    public void midIfExp(IfExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIfExp(IfExp data) {}

    public void preAbstractFunctionExp(AbstractFunctionExp data) {}

    public void midAbstractFunctionExp(AbstractFunctionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAbstractFunctionExp(AbstractFunctionExp data) {}

    public void preProcedureDec(ProcedureDec data) {}

    public void midProcedureDec(ProcedureDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProcedureDec(ProcedureDec data) {}

    public void preConceptModuleDec(ConceptModuleDec data) {}

    public void midConceptModuleDec(ConceptModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConceptModuleDec(ConceptModuleDec data) {}

    public void preAuxCodeStmt(AuxCodeStmt data) {}

    public void midAuxCodeStmt(AuxCodeStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAuxCodeStmt(AuxCodeStmt data) {}

    public void preArbitraryExpTy(ArbitraryExpTy data) {}

    public void midArbitraryExpTy(ArbitraryExpTy node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postArbitraryExpTy(ArbitraryExpTy data) {}

    public void preProgramDoubleExp(ProgramDoubleExp data) {}

    public void midProgramDoubleExp(ProgramDoubleExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramDoubleExp(ProgramDoubleExp data) {}

    public void preRecordTy(RecordTy data) {}

    public void midRecordTy(RecordTy node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postRecordTy(RecordTy data) {}

    public void preTypeFunctionExp(TypeFunctionExp data) {}

    public void midTypeFunctionExp(TypeFunctionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postTypeFunctionExp(TypeFunctionExp data) {}

    public void preTupleTy(TupleTy data) {}

    public void midTupleTy(TupleTy node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postTupleTy(TupleTy data) {}

    public void preProgramOpExp(ProgramOpExp data) {}

    public void midProgramOpExp(ProgramOpExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramOpExp(ProgramOpExp data) {}

    public void preMathVarDec(MathVarDec data) {}

    public void midMathVarDec(MathVarDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMathVarDec(MathVarDec data) {}

    public void preProofDefinitionExp(ProofDefinitionExp data) {}

    public void midProofDefinitionExp(ProofDefinitionExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProofDefinitionExp(ProofDefinitionExp data) {}

    public void preVariableDotExp(VariableDotExp data) {}

    public void midVariableDotExp(VariableDotExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVariableDotExp(VariableDotExp data) {}

    public void preVarDec(VarDec data) {}

    public void midVarDec(VarDec node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVarDec(VarDec data) {}

    public void preIterateStmt(IterateStmt data) {}

    public void midIterateStmt(IterateStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIterateStmt(IterateStmt data) {}

    public void preProgramStringExp(ProgramStringExp data) {}

    public void midProgramStringExp(ProgramStringExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramStringExp(ProgramStringExp data) {}

    public void preIterateExitStmt(IterateExitStmt data) {}

    public void midIterateExitStmt(IterateExitStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIterateExitStmt(IterateExitStmt data) {}

    public void preProgramParamExp(ProgramParamExp data) {}

    public void midProgramParamExp(ProgramParamExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProgramParamExp(ProgramParamExp data) {}

    public void preIsInExp(IsInExp data) {}

    public void midIsInExp(IsInExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postIsInExp(IsInExp data) {}

    public void preModuleDec(ModuleDec data) {}

    public void midModuleDec(ModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postModuleDec(ModuleDec data) {}

    public void preVariableArrayExp(VariableArrayExp data) {}

    public void midVariableArrayExp(VariableArrayExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postVariableArrayExp(VariableArrayExp data) {}

    public void preOldExp(OldExp data) {}

    public void midOldExp(OldExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postOldExp(OldExp data) {}

    public void preFunctionArgList(FunctionArgList data) {}

    public void midFunctionArgList(FunctionArgList node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFunctionArgList(FunctionArgList data) {}

    public void preProofModuleDec(ProofModuleDec data) {}

    public void midProofModuleDec(ProofModuleDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postProofModuleDec(ProofModuleDec data) {}

    public void preFuncAssignStmt(FuncAssignStmt data) {}

    public void midFuncAssignStmt(FuncAssignStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFuncAssignStmt(FuncAssignStmt data) {}

    public void preResolveConceptualElement(ResolveConceptualElement data) {}

    public void midResolveConceptualElement(ResolveConceptualElement node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postResolveConceptualElement(ResolveConceptualElement data) {}

    public void preLineNumberedExp(LineNumberedExp data) {}

    public void midLineNumberedExp(LineNumberedExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postLineNumberedExp(LineNumberedExp data) {}

    public void preCallStmt(CallStmt data) {}

    public void midCallStmt(CallStmt node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postCallStmt(CallStmt data) {}

    public void preDefinitionBody(DefinitionBody data) {}

    public void midDefinitionBody(DefinitionBody node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postDefinitionBody(DefinitionBody data) {}

    public void prePrefixExp(PrefixExp data) {}

    public void midPrefixExp(PrefixExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postPrefixExp(PrefixExp data) {}

    public void preConditionItem(ConditionItem data) {}

    public void midConditionItem(ConditionItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConditionItem(ConditionItem data) {}

    public void preTy(Ty data) {}

    public void midTy(Ty node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postTy(Ty data) {}

    public void preChoiceItem(ChoiceItem data) {}

    public void midChoiceItem(ChoiceItem node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postChoiceItem(ChoiceItem data) {}

    public void preHypDesigExp(HypDesigExp data) {}

    public void midHypDesigExp(HypDesigExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postHypDesigExp(HypDesigExp data) {}

    public void preAltItemExp(AltItemExp data) {}

    public void midAltItemExp(AltItemExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postAltItemExp(AltItemExp data) {}

    public void preInfixExp(InfixExp data) {}

    public void midInfixExp(InfixExp node, ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postInfixExp(InfixExp data) {}

    public void preMathRefExp(MathRefExp data) {}

    public void midMathRefExp(MathRefExp node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postMathRefExp(MathRefExp data) {}

    public void preFacilityTypeDec(FacilityTypeDec data) {}

    public void midFacilityTypeDec(FacilityTypeDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postFacilityTypeDec(FacilityTypeDec data) {}

    public void preWhileStmt(WhileStmt data) {}

    public void midWhileStmt(WhileStmt node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postWhileStmt(WhileStmt data) {}

    public void preConstantParamDec(ConstantParamDec data) {}

    public void midConstantParamDec(ConstantParamDec node,
            ResolveConceptualElement prevChild,
            ResolveConceptualElement nextChild) {}

    public void postConstantParamDec(ConstantParamDec data) {}
}
