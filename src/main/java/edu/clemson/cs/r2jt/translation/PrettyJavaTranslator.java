/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.translation;

import edu.clemson.cs.r2jt.ResolveCompiler;
import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.analysis.ProgramExpTypeResolver;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.archiving.Archiver;
import edu.clemson.cs.r2jt.collections.Iterator;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.collections.Map;
import edu.clemson.cs.r2jt.compilereport.CompileReport;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.entry.OperationEntry;
import edu.clemson.cs.r2jt.entry.TypeEntry;
import edu.clemson.cs.r2jt.entry.VarEntry;
import edu.clemson.cs.r2jt.errors.ErrorHandler;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.location.SymbolSearchException;
import edu.clemson.cs.r2jt.location.TypeLocator;
import edu.clemson.cs.r2jt.location.VariableLocator;
import edu.clemson.cs.r2jt.scope.*;
import edu.clemson.cs.r2jt.type.*;
import edu.clemson.cs.r2jt.utilities.Flag;
import edu.clemson.cs.r2jt.utilities.FlagDependencies;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Chuck
 */
public class PrettyJavaTranslator extends ResolveConceptualVisitor {

    private static final String FLAG_SECTION_NAME = "Pretty JAVA Translation";

    private static final String FLAG_DESC_TRANSLATE =
            "Translate RESOLVE file to human readable Java source file.";

    //private static final String FLAG_DESC_TRANSLATE_CLEAN = 
    //"Regenerates Java code for all supporting RESOLVE files.";

    /**
     * <p>The main translator flag.  Tells the compiler convert
     * RESOLVE source code to Java source code.</p>
     */
    public static final Flag FLAG_TRANSLATE =
            new Flag(FLAG_SECTION_NAME, "translatePrettyJava",
                    FLAG_DESC_TRANSLATE);

    /**
     * <p>Tells the compiler to regenerate Java code for all 
     * supporting RESOLVE source files.</p>
     */
    //public static final Flag FLAG_TRANSLATE_CLEAN = 
    //new Flag(FLAG_SECTION_NAME, "translateClean", FLAG_DESC_TRANSLATE_CLEAN);

    private static String IMPORT_ARRAY =
            "import RESOLVE.Main.Concepts.Standard.Static_Array.*;\n";

    private final CompileEnvironment myInstanceEnvironment;

    private StringBuilder myJavaBuffer = new StringBuilder();
    private int myCurrentLineNumber = 1;
    private int myCurrentIndent = 0;
    /*
     * This boolean was added to provide a method for the compiler/web interface
     * to be able to check and make sure the RESOLVE file was translated before
     * attempting to create a jar file.
     */
    public static boolean translated = false;

    //private Environment         env                = Environment.getInstance();
    private ErrorHandler err;
    private OldSymbolTable table;
    private String targetFileName = new String();
    private StringBuffer facilityConstructorBuf = new StringBuffer();
    private StringBuffer operBuf = new StringBuffer();
    private StringBuffer headerBuf = new StringBuffer();
    private StringBuffer tailBuf = new StringBuffer();
    private StringBuffer typeBuf = new StringBuffer(); //restricted its use to a handful of places
    private StringBuffer typeFuncBuf = new StringBuffer();
    private StringBuffer usesItemBuf = new StringBuffer();
    //    private StringBuffer        cbHeadBuf          = new StringBuffer(); //apparently has no use; never set anywhere
    //    private StringBuffer        parmOpBuf          = new StringBuffer(); // removed all its uses
    private List<String> exportedTypeNames = new List<String>();
    private Map<String, String> castLookUpMap = new Map<String, String>();
    private List<String> importList;
    private List<String> parmOpList;
    //  removed the uses of the following 3 globals
    /*  
     *  private String              curRealizName;
     private String              curConceptName;
     private String              curFacilityName;
     */
    private PosSymbol curFacilityPosSym = null; // global bad but absyn has no parent ptr
    private PosSymbol curConceptPosSym = null; // added this global; removed curConceptName
    private PosSymbol curConceptBodyPosSym = null; // added this global; removed curRealizName
    private StringBuffer stmtBuf = new StringBuffer();
    private StringBuffer initBuf = new StringBuffer();
    private PosSymbol containsArrayName;
    private ProgramExp containsArraySubscript;
    private String containsArrayType;
    private StringBuffer consInitBuf = new StringBuffer();
    private Binding concBinding;
    private List<String> typeParms;
    private List<String> concParms;
    private boolean isInInterface = false;
    private boolean isLocalVar = false;
    /*    private boolean             vfdFirstParm;	
     *  	successfully removed all its uses -- one global down, 25 more to go
     */

    // Files not to compile (would overwrite hand-written stuff Java stuff)
    private String[] noTranslate =
            { "Std_Boolean_Fac.fa", "Std_Char_Str_Fac.fa",
                    "Std_Character_Fac.fa", "Std_Integer_Fac.fa",
                    "Std_Boolean_Realiz", "Integer_Template.co",
                    "Character_Template.co", "Char_Str_Template.co",
                    "Seq_Input_Template.co", "Seq_Output_Template.co",
                    "Print.co", "Std_Location_Linking_Realiz.rb" };

    // Freebie imports
    private String[] autoImports =
            { "RESOLVE.Main.Concepts.Standard.Integer_Template.*",
                    "RESOLVE.Main.Concepts.Standard.Boolean_Template.*",
                    "RESOLVE.Main.Concepts.Standard.Character_Template.*",
                    "RESOLVE.Main.Concepts.Standard.Char_Str_Template.*" };

    /**
     * Construct a Translator.
     */
    public PrettyJavaTranslator(CompileEnvironment e, OldSymbolTable table,
            ModuleDec dec, ErrorHandler err) {
        myInstanceEnvironment = e;
        targetFileName = dec.getName().getFile().toString();
        this.table = table;
        this.err = err;
    }

    public String getMainBuffer() {
        return myJavaBuffer.toString();
    }

    private void appendJava(String java, boolean indent) {
        if (indent) {
            for (int i = 0; i < myCurrentIndent; i++) {
                myJavaBuffer.append("\t");
            }
        }
        myJavaBuffer.append(java);
    }

    private void bumpLine() {
        appendJava("\n", false);
        myCurrentLineNumber++;
    }

    private boolean bumpLine(int targetLine) {
        boolean bumped = false;
        while (targetLine > myCurrentLineNumber) {
            bumped = true;
            appendJava("\n", false);
            myCurrentLineNumber++;
        }
        return bumped;
    }

    private void indent() {
        myCurrentIndent++;
    }

    private void unIndent() {
        myCurrentIndent--;
    }

    /*public String getMainBuffer() { 
        return buildHeaderComment() + usesItemBuf.toString() + 
               headerBuf.toString() + typeBuf.toString()     + 
               operBuf.toString()   + typeFuncBuf.toString() +
               tailBuf.toString(); 
    }*/

    public String getMainFileName() {
        return getMainFile();
    }

    /**
     * Builds a comment header to identify Java source files generated
     * by the compiler and from which Resolve source file the generated
     * file is derived.
     */
    /*private String buildHeaderComment() {
    	String[] temp = targetFileName.split("\\\\");
    	String fileName = temp[temp.length - 1];
        return
        "//\n" +
        "// Generated by the Resolve to Pretty Java Translator"  + "\n" +
        //"// from file:  " + env.getTargetFile().getName() + "\n" +
        "// from file:  " + fileName + "\n" +
        "// on:         " + new Date()                    + "\n" +
        "//\n";
    }*/

    // ===========================================================
    // Public Methods - Abstract Visit Methods
    // ===========================================================

    public void visitModuleDec(ModuleDec dec) {
        dec.accept(this);
    }

    public void visitDec(Dec dec) {
        dec.accept(this);
    }

    public void visitExp(Exp exp) {
        if (exp == null) {
            return;
        }
        exp.accept(this);
    }

    // ===========================================================
    // Public Methods - Declarations
    // ===========================================================

    // -----------------------------------------------------------
    // Module Declarations
    // -----------------------------------------------------------

    //public void visitMathModuleDec(MathModuleDec dec) {
    //}

    /**
     * <p>Constructs the package into which to place this module from the file
     * name.</p>
     * 
     * Currently, the compiler expects all resolve files to be housed in a
     * directory structure rooted in some directory called RESOLVE.
     * I've improved this to be a little more robust by initiating the directory
     * search from the right rather than the left (so that it doesn't confuse
     * the system if somewhere up the directory tree there's a directory called
     * RESOLVE.)  Only the first RESOLVE directory from the right counts.  I've
     * also made changes elsewhere so that this works correctly if you compile
     * files from the command line that are not in the present working
     * directory.  Finally, I've made this method generate an error if the file
     * is not housed in an appropriate directory, rather than just generating
     * bad code. -HwS
     * 
     * @param forFile The file for which we are constructing a java package.
     * @return The fully qualified package name into which that file should be
     *         compiled.
     */
    /*private String formPkgPath(File file) {
        StringBuffer    pkgPath  = new StringBuffer();
        String          filePath;
        if(file.exists()){
        	filePath = file.getAbsolutePath();
        }
        else{
        	filePath = file.getParentFile().getAbsolutePath();
        }
        StringTokenizer stTok    = new StringTokenizer(filePath,
                                                       File.separator);
        Deque<String> tokenStack = new LinkedList<String>();
        
        String curToken;
        while (stTok.hasMoreTokens()) {
        	curToken = stTok.nextToken();
        	tokenStack.push(curToken);
        }
        
        //Get rid of the actual file--we only care about the path to it
        if (file.isFile()) {
        	tokenStack.pop();
        }
        
        curToken = "";
        boolean foundRootDirectory = false;
        while (!tokenStack.isEmpty() && !foundRootDirectory) {
        	curToken = tokenStack.pop();
        	
        	if (pkgPath.length() != 0) {
        		pkgPath.insert(0, '.');
        	}

        	pkgPath.insert(0, curToken);
        	
        	foundRootDirectory = curToken.equalsIgnoreCase("RESOLVE");
        }
        
        if (!foundRootDirectory) {
        	err.error("Translation expects all compiled files to have a " +
        			"directory named 'RESOLVE' somewhere in their path, but " +
        			"the file:\n\t" + filePath + "\ndoes not.  Keep in mind " +
        			"that directories are case sensitive.");
        }
        
        return pkgPath.toString();
    }*/

    /**
     * This is a hack-y little method.  Basically, having a directory called
     * RESOLVE is problematic on some filesystems (namely, Fat32) and it doesn't
     * follow Java's package naming convention anyway.  It would be convenient
     * to call it resolve, but I don't want to break existing material.  This
     * will derive what the current set up is using as the root directory from
     * the file name of the source file.  -HwS
     * 
     * @param file The source file.
     * @return The name of the root pacakge.  Some variation on RESOLVE,
     *         Resolve, resolve, ReSoLvE, etc, etc.
     */
    /*private String getRootPackage(File file) {
    	String filePath = file.getAbsolutePath();
    	StringTokenizer stTok = new StringTokenizer(filePath, File.separator);
    	
        Deque<String> tokenStack = new LinkedList<String>();
        String curToken;
        while (stTok.hasMoreTokens()) {
        	curToken = stTok.nextToken();
        	tokenStack.push(curToken);
        }
        
        curToken = "";
        while (!(tokenStack.isEmpty() || curToken.equalsIgnoreCase("resolve"))) {
        	curToken = tokenStack.pop();
        }
        
        return curToken;
    }*/

    /*public void visitConceptModuleDec(ConceptModuleDec dec) {
    	ModuleID conceptID = ModuleID.createConceptID(dec.getName());
    	File sourceFile = myInstanceEnvironment.getFile(conceptID);
    	
    	table.beginModuleScope();
        isInInterface = true;
        if (dec.getFacilityInit() != null) {
            visitInitItem(dec.getFacilityInit());
        }
        if (dec.getFacilityFinal() != null) {
            visitFinalItem(dec.getFacilityFinal());
        }

        importList = new List<String>();
        ModuleID cid = ModuleID.createConceptID(dec.getName());
        typeParms = getTypeParms(cid);
        concParms = getConcParms(cid);

        usesItemBuf.append("package ");
        usesItemBuf.append(formPkgPath(sourceFile));
        usesItemBuf.append("; \n \n");
        usesItemBuf.append("import RESOLVE.*;\n");
        //addStdImports();
        visitUsesItemList(dec.getUsesItems());

        headerBuf.append("public interface ");
        headerBuf.append(dec.getName().toString());
        headerBuf.append(" extends RESOLVE_INTERFACE {\n\n");

        visitConceptDecList(dec.getDecs());
        formGetTypeMethods(typeParms);
        formGetModuleParmInterface(dec.getParameters());

        usesItemBuf.append("\n");

        if (headerBuf.length() > 0) {
            tailBuf.append("}\n");
        }

        isInInterface = false;
        table.endModuleScope();
    }*/
    /*
     * This method adds the standard imports to the java output file.
     */
    /*private void addStdImports(){
        for(String s : autoImports){
        	String importStr = "import " + s + ";\n";
        	//System.out.println(importStr);
        	if (!checkImportDup(importStr)) {
        		usesItemBuf.append(importStr);
        		importList.addUnique(importStr);
        		//System.out.println(importStr);
        	}
        }
    }*/

    /*private String declFacNames(List<Dec> facDecList) {
      	StringBuffer thisBuf = new StringBuffer();
        Dec facDec;
        Iterator<Dec> facDecIt = facDecList.iterator();
        while (facDecIt.hasNext()) {
            facDec = facDecIt.next();
            if (facDec instanceof FacilityDec) {
                thisBuf.append("\t");
                thisBuf.append(concNameforFac((FacilityDec)facDec));
                thisBuf.append(" ");
                thisBuf.append(facDec.getName().toString());
                thisBuf.append(";\n");
            } 
        }
        return thisBuf.toString();
    }*/

    /*public void visitConceptBodyModuleDec(ConceptBodyModuleDec dec) {
    	ModuleID conceptID = ModuleID.createConceptID(dec.getConceptName());
    	File packageParent = myInstanceEnvironment.getFile(conceptID);
    	
    	table.beginModuleScope();

        if (dec.getFacilityInit() != null) {
            visitInitItem(dec.getFacilityInit());
        }
        if (dec.getFacilityFinal() != null) {
            visitFinalItem(dec.getFacilityFinal());
        }

        curConceptBodyPosSym = dec.getName();
        importList = new List<String>();
        parmOpList = new List<String>();

        ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
        typeParms = getTypeParms(cid);
        concParms = getConcParms(cid);
        ConceptModuleDec cDec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(cid);
        curConceptPosSym = dec.getConceptName();
        
        usesItemBuf.append("package ");
        usesItemBuf.append(formPkgPath(packageParent));
        usesItemBuf.append("; \n \n");
        usesItemBuf.append("import ");
        usesItemBuf.append(getRootPackage(packageParent));
        usesItemBuf.append(".*;\n");

        //addStdImports();
        visitUsesItemList(cDec.getUsesItems());
        visitUsesItemList(dec.getUsesItems());
        
        //  Import files needed by the facilities 
        Iterator<Dec> it = dec.getDecs().iterator();
        
        while (it.hasNext()) {
        	//  Create a temporary storage for the next value in the iterator 
        	Dec temp = it.next();
        	
        	//  Check if that is an instance of FacilityDec 
        	if (temp instanceof FacilityDec) {
        		// Obtain the Concept file referred to by the FacilityDec 
        		ModuleID fid = ModuleID.createConceptID(((FacilityDec) temp).getConceptName());
                ConceptModuleDec fCDec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(fid);
                
                // Create a java import statement using the Concept file 
                String importStr = formJavaImport(fCDec.getName().getFile());
                
                // Check for duplicate imports 
                if (!checkImportDup(importStr)) {
                    usesItemBuf.append(importStr);
                    importList.addUnique(importStr);
                }
        	}
        }
        
        headerBuf.append("public class ");
        headerBuf.append(curConceptBodyPosSym.toString());
        headerBuf.append(" extends RESOLVE_BASE implements ");
        headerBuf.append(curConceptPosSym.toString());
        headerBuf.append(" {\n");
    //       headerBuf.append(cbHeadBuf.toString());

        headerBuf.append(visitModuleParameterList(cDec.getParameters(),";\n",";\n"));
        headerBuf.append(visitModuleParameterList(dec.getParameters(),";\n",";\n"));
        headerBuf.append(generateOPParamInterfaces(dec.getParameters()));
        
    //      This statement may work only for unenhanced and singly-enhanced facilities
        headerBuf.append(declFacNames(dec.getDecs()));
        headerBuf.append(formCommunalVarDecs(dec.getDecs())); //this is new addition
        //due to unknown reasons, it doesn't work if the next two statements are swapped
        visitTypes(dec.getDecs()); //this resets stmtBuf; affects constrInitBuf
    //       visitOpProcDecs(dec.getDecs()); //this resets stmtBuf
        //due to global stmtBuf, formJavaConstructors has to be done at the end
        operBuf.append(formJavaConstructors(curConceptBodyPosSym.toString(), cDec.getParameters(),dec.getParameters(),dec.getDecs()));
    
        visitProcedures(dec.getDecs());
        operBuf.append(visitCBTypeProcedures(dec.getDecs()));
        
        List<String> typeParms = getTypeParms(cid);
        String parmName = null;
        Iterator<String> typeIt = typeParms.iterator();
        while (typeIt.hasNext()) {
            parmName = typeIt.next();
            operBuf.append("\tpublic RType getType");
            operBuf.append(parmName);
            operBuf.append("() {\n");
            operBuf.append("\t\treturn ");
            operBuf.append(parmName);
            operBuf.append(";\n");
            operBuf.append("\t}\n");
        }

        operBuf.append(formGetModuleParm(cDec.getParameters()));
        operBuf.append(formGetModuleParm(dec.getParameters()));

        usesItemBuf.append("\n");

        if (headerBuf.length() > 0) {
            tailBuf.append("}\n");
        }
        table.endModuleScope();
    }*/

    /*public void visitEnhancementModuleDec(EnhancementModuleDec dec) {
        ModuleID enhancementID = ModuleID.createEnhancementID(
        		dec.getName(), dec.getConceptName());
        File sourceFile = myInstanceEnvironment.getFile(enhancementID);
    	
    	table.beginModuleScope();
        isInInterface = true;
        importList = new List<String>();
        ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
        typeParms = getTypeParms(cid);
        concParms = getConcParms(cid);
        ConceptModuleDec cDec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(cid);

        usesItemBuf.append("package ");
        usesItemBuf.append(formPkgPath(sourceFile));
        usesItemBuf.append("; \n \n");
        usesItemBuf.append("import ");
        usesItemBuf.append(getRootPackage(sourceFile));
        usesItemBuf.append(".*;\n");

        //addStdImports();
        visitUsesItemList(cDec.getUsesItems());
        visitUsesItemList(dec.getUsesItems());

        headerBuf.append("public interface ");
        headerBuf.append(dec.getName().toString());
        headerBuf.append(" extends ");
        headerBuf.append(dec.getConceptName().toString());
        headerBuf.append(" {\n\n");

        visitEnhancementDecList(dec.getDecs());

        usesItemBuf.append("\n");

        if (headerBuf.length() > 0) {
            tailBuf.append("}\n");
        }

        isInInterface = false;
        table.endModuleScope();
    }*/

    /*public void visitEnhancementBodyModuleDec(EnhancementBodyModuleDec dec) {
    	ModuleID enhancementID = ModuleID.createEnhancementID(
        		dec.getEnhancementName(), dec.getConceptName());
        File packageParent = myInstanceEnvironment.getFile(enhancementID);
    	
    	table.beginModuleScope();
        importList = new List<String>();
        parmOpList = new List<String>();
        
        ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
        typeParms = getTypeParms(cid);
        concParms = getConcParms(cid);
        ConceptModuleDec cDec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(cid);

        //ModuleID eid = ModuleID.createEnhancementID(dec.getEnhancementName(), dec.getConceptName());
        //EnhancementModuleDec eDec = (EnhancementModuleDec)env.getModuleDec(cid);
        
        curConceptPosSym = dec.getConceptName(); //added the next two statements; check if this causes trouble
        curConceptBodyPosSym = dec.getName();
    
        usesItemBuf.append("package ");
        usesItemBuf.append(formPkgPath(packageParent));
        usesItemBuf.append(";\n\n");
    // allow for proxy
        usesItemBuf.append("import java.lang.reflect.Proxy;\n");
        usesItemBuf.append("import java.lang.reflect.InvocationHandler;\n");
        usesItemBuf.append("import java.lang.reflect.Method;\n");
        usesItemBuf.append("\n");

        usesItemBuf.append("import ");
        usesItemBuf.append(getRootPackage(packageParent));
        usesItemBuf.append(".*;\n");
        concBinding = myInstanceEnvironment.getModuleScope(cid).getBinding();

        //addStdImports();
        visitUsesItemList(cDec.getUsesItems());
        visitUsesItemList(dec.getUsesItems());
        
        // Import files needed by the facilities 
        Iterator<Dec> it = dec.getDecs().iterator();
        
        while (it.hasNext()) {
        	// Create a temporary storage for the next value in the iterator 
        	Dec temp = it.next();
        	
        	// Check if that is an instance of FacilityDec 
        	if (temp instanceof FacilityDec) {
        		// Obtain the Concept file referred to by the FacilityDec 
        		ModuleID fid = ModuleID.createConceptID(((FacilityDec) temp).getConceptName());
                ConceptModuleDec fCDec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(fid);
                
                // Create a java import statement using the Concept file 
                String importStr = formJavaImport(fCDec.getName().getFile());
                
                // Check for duplicate imports 
                if (!checkImportDup(importStr)) {
                    usesItemBuf.append(importStr);
                    importList.addUnique(importStr);
                }
        	}
        }

        headerBuf.append("public class ");
        headerBuf.append(curConceptBodyPosSym.toString());
        headerBuf.append(" implements ");
        headerBuf.append(dec.getEnhancementName().toString());
        headerBuf.append(", ");
        headerBuf.append(curConceptPosSym.toString());
        headerBuf.append(", InvocationHandler");
        headerBuf.append(" {\n");
    //       headerBuf.append(cbHeadBuf.toString());

        //the following code is the same as in visitConceptBodyModuleDec
        headerBuf.append(visitModuleParameterList(cDec.getParameters(),";\n",";\n"));
        headerBuf.append(visitModuleParameterList(dec.getParameters(),";\n",";\n"));
        headerBuf.append(generateOPParamInterfaces(dec.getParameters()));
        
        //copied the following three stetements from visitConceptBodyModule
        headerBuf.append(declFacNames(dec.getDecs()));
        headerBuf.append(formCommunalVarDecs(dec.getDecs())); //this is new addition
        //due to unknown reasons, it doesn't work if the next two statements are swapped
        visitTypes(dec.getDecs()); //this resets stmtBuf
        createExportedTypeNames(cDec);
    //        visitOpProcDecs(dec.getDecs()); //this resets stmtBuf
        visitProcedures(dec.getDecs());
        operBuf.append(formJavaEnhancementConstructors(curConceptBodyPosSym.toString(), curConceptPosSym.toString(), cDec.getParameters(), dec.getParameters(), dec.getDecs()));
    
        operBuf.append(formConceptProcWrappers(cDec));
        operBuf.append(formConceptTypesWrappers(cDec));
        operBuf.append(formProxyProcedures(cDec, dec));


        usesItemBuf.append("\n");

        if (headerBuf.length() > 0) {
            tailBuf.append("}\n");
        }
        table.endModuleScope();
    }*/

    public void visitFacilityModuleDec(FacilityModuleDec dec) {
        ModuleID facilityID = ModuleID.createFacilityID(dec.getName());
        File sourceFile = myInstanceEnvironment.getFile(facilityID);

        table.beginModuleScope();
        importList = new List<String>();

        curFacilityPosSym = dec.getName();
        //usesItemBuf.append("package ");
        //usesItemBuf.append(formPkgPath(sourceFile));
        //usesItemBuf.append("; \n \n");
        //usesItemBuf.append("import ");
        //usesItemBuf.append(getRootPackage(sourceFile));
        //usesItemBuf.append(".*;\n");

        // @todo deal with uses statements
        //visitUsesItemList(dec.getUsesItems());

        //headerBuf.append("public class ");
        //headerBuf.append(curFacilityPosSym.toString());
        //headerBuf.append(" {\n");
        bumpLine(curFacilityPosSym.getPos().getLine());
        appendJava("public class ", true);
        appendJava(curFacilityPosSym.toString(), false);
        appendJava(" {", false);
        indent();
        List<Dec> facDecList = dec.getDecs();
        Dec facDec;
        Iterator<Dec> facDecIt = facDecList.iterator();
        while (facDecIt.hasNext()) {
            facDec = facDecIt.next();
            if (facDec instanceof FacilityDec) {
                visitFacilityDec((FacilityDec) facDec);
            }
            else if (facDec instanceof FacilityOperationDec) {
                visitFacilityOperationDec((FacilityOperationDec) facDec);
            }
            else if (facDec instanceof FacilityTypeDec) {
                visitFacilityTypeDec((FacilityTypeDec) facDec);
            }
            else {
                //System.out.println("facDec: " + facDec.asString(1,1));
            }
        }
        unIndent();
        appendJava("\n}", true);
        //       headerBuf.append(cbHeadBuf.toString());

        //facilityConstructorBuf.setLength(0);
        //facilityConstructorBuf.append("\tpublic ");
        //facilityConstructorBuf.append(curFacilityPosSym.toString());
        //facilityConstructorBuf.append("() {\n");

        // Murali is duplicating this code for temporary fix in CB -redo
        /*List<Dec> facDecList = dec.getDecs();
        Dec facDec;
        Iterator<Dec> facDecIt = facDecList.iterator();
        while (facDecIt.hasNext()) {
            facDec = facDecIt.next();
            if (facDec instanceof FacilityDec) {
                visitFacilityDec((FacilityDec)facDec);
            } else if (facDec instanceof FacilityOperationDec) {
                visitFacilityOperationDec((FacilityOperationDec)facDec);
            } else if (facDec instanceof FacilityTypeDec) {
                visitFacilityTypeDec((FacilityTypeDec)facDec);
            } else {
        //System.out.println("facDec: " + facDec.asString(1,1));
            }
        }*/
        //resetting the global so it doesn't get misused
        /*curFacilityPosSym = null;
        usesItemBuf.append("\n");

        if (headerBuf.length() > 0) {
            tailBuf.append("}\n");
        }

        facilityConstructorBuf.append("\t}\n");
        operBuf.append(facilityConstructorBuf.toString());*/

        table.endModuleScope();
    }

    /*public void visitShortFacilityModuleDec(ShortFacilityModuleDec dec) {
        table.beginModuleScope();
        importList = new List<String>();
        table.endModuleScope();
    }*/

    // factored this out from formOpParmsto FacDec to be able to handle multiple Op parameters
    // uses globals
    // changed it so now it works also for facility declarations with parameters 
    // inside realization bodies
    // revised this to work for the case when a realization parameter operation is passed
    // as an argument to a facility

    // this code needs much improvement; the logic is so complicated because a 
    // facility operation argument may come from one of several places

    // there are still bugs in generated code and there are places where this doesn't search yet.
    // temporary fix at this point.

    /*private String formActualOpParmToFacDec (ModuleArgumentItem modArgItem) {
        StringBuffer thisBuf = new StringBuffer();
        Iterator<ParameterVarDec> pvIt = null;
        int parmCnt=0;
        boolean firstParm = true;
        boolean found = false;        
        PosSymbol passedProcedure = modArgItem.getName();
    
        Iterator<Dec> decFromFMIt = null;
        // search facilities in uses list
        PosSymbol qual = modArgItem.getQualifier();
    //      // Below, if qual is true then found must have been false; 
        // So no check is done for if !found
        if (qual != null) {			
        	thisBuf.append(qual.toString());
        	thisBuf.append(".");
            ModuleID id = ModuleID.createFacilityID(qual);
            if (myInstanceEnvironment.contains(id)) {
                ModuleDec dec = myInstanceEnvironment.getModuleDec(id);
                FacilityDec fdec = null;
                if (dec instanceof ShortFacilityModuleDec) {
                    fdec = ((ShortFacilityModuleDec)(dec)).getDec();
                    PosSymbol cname = fdec.getConceptName();
                    ModuleID cid = ModuleID.createConceptID(cname);
                    ConceptModuleDec cdec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(cid);
        	        decFromFMIt = cdec.getDecs().iterator();
        	        while (!found && decFromFMIt.hasNext()) {
        	            Dec decFromFM = decFromFMIt.next();
        		        	if (decFromFM instanceof OperationDec) {
        		                if (passedProcedure.equals(((OperationDec)decFromFM).getName().getSymbol())) {
        		                	pvIt = ((OperationDec)decFromFM).getParameters().iterator();
        		                	found = true;
        	  	        	}
        	            }
        	        }
                }
            }
        }
        
        // search local facility or realization operations
        // curFacilityPosSym will be null for facility declarations within generic realization bodies
        if (!found) {
            if (curFacilityPosSym != null) {
    	        ModuleID fmid = ModuleID.createFacilityID(curFacilityPosSym);
    	        FacilityModuleDec md = (FacilityModuleDec)myInstanceEnvironment.getModuleDec(fmid);
    	        decFromFMIt = md.getDecs().iterator();
                while (!found && decFromFMIt.hasNext()) {
                    Dec decFromFM = decFromFMIt.next();
      	        	if (decFromFM instanceof FacilityOperationDec) {
      	                if (passedProcedure.equals(((FacilityOperationDec)decFromFM).getName().getSymbol())) {
      	                	pvIt = ((FacilityOperationDec)decFromFM).getParameters().iterator();
      	                	found = true;
          	        	}
                    }
                }
    	        decFromFMIt = md.getDecs().iterator();
            } else if (curConceptBodyPosSym != null) {
    	        ModuleID fmid = ModuleID.createConceptBodyID(curConceptBodyPosSym, curConceptPosSym);
    	        ConceptBodyModuleDec md = (ConceptBodyModuleDec)myInstanceEnvironment.getModuleDec(fmid);
    	        Iterator<ModuleParameter> params = md.getParameters().iterator();
    	        while (!found && params.hasNext()) {
    	            ModuleParameter mp = params.next();
    	            if (mp instanceof OperationDec) {
    	              OperationDec paramOpDec = (OperationDec)mp;
    	              if (passedProcedure.equals(paramOpDec.getName().getSymbol())) {
    	              	pvIt = paramOpDec.getParameters().iterator();
    	                parmCnt=0;
    	                found = true;
    	                thisBuf.append(curConceptBodyPosSym.toString());
    	                thisBuf.append(".this.");
    	                thisBuf.append(passedProcedure.toString());
    	                thisBuf.append("Parm.");*/
    /*
    thisBuf.append(passedProcedure.toString());
    thisBuf.append("(");
    while (pvIt.hasNext()) {
      if (!firstParm) {
        thisBuf.append(", ");
      }
      firstParm = false;
     ParameterVarDec pvDec = pvIt.next();
      thisBuf.append("(");
      thisBuf.append(formParameterVarDecType(pvDec));
      thisBuf.append(")");
      //have this name match name made up
      thisBuf.append("parm"); 
      thisBuf.append(++parmCnt);
    }
     */
    /*}
    }
    }
    decFromFMIt = md.getDecs().iterator();
    while (!found && decFromFMIt.hasNext()) {
    Dec decFromFM = decFromFMIt.next();
    if (decFromFM instanceof FacilityOperationDec) {
          if (passedProcedure.equals(((FacilityOperationDec)decFromFM).getName().getSymbol())) {
          	pvIt = ((FacilityOperationDec)decFromFM).getParameters().iterator();
          	found = true;
    	}
    }
    }
    decFromFMIt = md.getDecs().iterator();
    }
    }
    
    // search facility declarations and their enhancements
    while (!found && decFromFMIt.hasNext()) {
    Dec decFromFM = decFromFMIt.next();
    if (decFromFM instanceof FacilityDec) {
    FacilityDec fdec = (FacilityDec)decFromFM;
    PosSymbol cname = fdec.getConceptName();
    ModuleID cid = ModuleID.createConceptID(cname);
    ConceptModuleDec cdec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(cid);
    decFromFMIt = cdec.getDecs().iterator();
    while (!found && decFromFMIt.hasNext()) {
      Dec decFromConc = decFromFMIt.next();
      	if (decFromConc instanceof OperationDec) {
              if (passedProcedure.equals(((OperationDec)decFromConc).getName().getSymbol())) {
              	pvIt = ((OperationDec)decFromConc).getParameters().iterator();
              	found = true;
        	}
      }
    }
    List<EnhancementBodyItem> enhItemList = fdec.getEnhancementBodies();
    Iterator<EnhancementBodyItem> enhItemIt = enhItemList.iterator(); 
    EnhancementBodyItem enhItem;
    while (!found && enhItemIt.hasNext()) {
      enhItem = enhItemIt.next();
      PosSymbol ename = enhItem.getName();
      ModuleID eid = ModuleID.createEnhancementID(ename, cname);
      EnhancementModuleDec edec = (EnhancementModuleDec)myInstanceEnvironment.getModuleDec(eid);
      Iterator<Dec> decFromEnhIt = edec.getDecs().iterator();
      while (!found && decFromEnhIt.hasNext()) {
          Dec decFromEnh = decFromEnhIt.next();
          	if (decFromEnh instanceof OperationDec) {
                  if (passedProcedure.equals(((OperationDec)decFromEnh).getName().getSymbol())) {
                  	pvIt = ((OperationDec)decFromEnh).getParameters().iterator();
                  	found = true;
            	}
          }
      }
    }
    //	Check the concept for operation matching here, only enhancements have been checked
    //                   ConceptModuleDec cdec = (ConceptModuleDec)env.getModuleDec(cid);
    //       	        decFromFMIt = cdec.getDecs().iterator();
    }
    }
    
    if (found) {
    thisBuf.append(passedProcedure.toString());
    thisBuf.append("(");
    if (pvIt != null) {
    parmCnt=0;
    found = true;
    while (pvIt.hasNext()) {
    if (!firstParm) {
    thisBuf.append(", ");
    }
    firstParm = false;
    ParameterVarDec pvDec = pvIt.next();
    thisBuf.append("(");
    thisBuf.append(formParameterVarDecType(pvDec));
    thisBuf.append(")");
    //have this name match name made up
    thisBuf.append("parm"); 
    thisBuf.append(++parmCnt);
    }
    }
    thisBuf.append(");");
    }
    else {
    err.error("Error passed operation not found: " + modArgItem.asString(1,1));
    }
    
    //      Need to find where prodedure being passed as a parameter is declared to
    //      get the signature since only the name and not the signature is passed
    //      note this is not quite right as here we have to match on name and everyplace
    //      else we use name+signature.  Murali thinks we can get the signature by 
    //      looking at the realization declaration but thats wrong because there it
    //      will have generic parameters like type and here it has real parameters like
    //      Integer.  We could do a little better by counting the number of args, etc
    //      but for practical purposes the client can live with picking unique names IMHO
    
    return thisBuf.toString();
    }*/

    /* forms return type if function and void otherwise */
    /*private String retTypeString (Ty retTy) {
        StringBuffer thisBuf = new StringBuffer();
        if (retTy != null) { //this is a function operation
        	PosSymbol unused = null;
        	thisBuf.append(formVarDecType(unused, retTy));
        	thisBuf.append(" ");
        } else thisBuf.append("void ");
        return thisBuf.toString();
    }*/

    // factors out the large common code between formModuleParm and visitBodyBparm,
    // so now the bugs have to be fixed one less time;;
    /*private String formOpParmToFacDec(OperationDec opDec, 
    		ModuleArgumentItem modArgItem,
    		String decName) {
        StringBuffer thisBuf = new StringBuffer();
        int parmNum = 0;
        
        //removed the use of vfdFirstParm from this function
        thisBuf.append("new ");
        thisBuf.append(decName);
        thisBuf.append(".");
        thisBuf.append(opDec.getName().toString());
        parmNum++;
        thisBuf.append("() {");
        thisBuf.append("public ");

        String rtnStr = "";
        Ty retTy = opDec.getReturnTy();	// following code revised to cover all return types
        if (retTy != null) { //this is a function operation
        	PosSymbol unused = null;
        	thisBuf.append(formVarDecType(unused, retTy));
        	thisBuf.append(" ");
            rtnStr = "return ";
        } else 
        	thisBuf.append("void "); // bug fixed now?

        thisBuf.append(opDec.getName().toString());
        thisBuf.append("(");
        Iterator<ParameterVarDec> pvIt = opDec.getParameters().iterator();

        boolean firstParm = true;
        int parmCnt=0;
        while (pvIt.hasNext()) {
          if (!firstParm) {
            thisBuf.append(", ");
          }
          firstParm = false;
          ParameterVarDec pvDec = pvIt.next();
          thisBuf.append(formParameterVarDecType(pvDec));
          thisBuf.append(" ");
          thisBuf.append("parm"); //just make up a unique name
          thisBuf.append(++parmCnt);
        }

        thisBuf.append(")\n");
        thisBuf.append("\t\t{");
        thisBuf.append(rtnStr);  

        thisBuf.append(formActualOpParmToFacDec(modArgItem));

        thisBuf.append("}\n");
        thisBuf.append("}");
        return thisBuf.toString();
    }*/

    // replaces formModuleParm
    // this does not output parentheses unlike handleSimpleFacPramaters
    /*private void handleEnhancedFacParameters(FacilityDec dec,
            EnhancementBodyItem ebItem) {

        ModuleID ebid = ModuleID.createEnhancementBodyID(
                ebItem.getBodyName(), ebItem.getName(),
                dec.getConceptName());
        EnhancementBodyModuleDec ebDec = 
        	(EnhancementBodyModuleDec) myInstanceEnvironment.getModuleDec(ebid);
        handleFacParameters(dec.getConceptParams(), ebItem.getBodyParams(), ebDec.getParameters(), ebItem.getBodyName().toString());
    }*/

    // changed this handle realization parameters other than operation parameters
    // changed this to directly change stmtBuf, because that's how visitProgramExp is written
    /*private void visitBodyParm(ModuleArgumentItem modArgItem, ModuleParameter mp,
    		String decName) {
        
        //if (!env.compileBodies() && !myInstanceEnvironment.flags.isFlagSet(FLAG_BODIES)) {
            //System.out.println("Can not compile.  Try +bodies compile option.");
            //throw new RuntimeException();
        //}

    //       int parmNum = 0;
     */
    //most of the following has been moved to the calling point
    /*
    ModuleID bid = ModuleID.createConceptBodyID(facDec.getBodyName(),
                                                facDec.getConceptName());
    ConceptBodyModuleDec bDec = (ConceptBodyModuleDec)env.getModuleDec(bid);
    Iterator<ModuleParameter> mpIt = bDec.getParameters().iterator();
    OperationDec opDec = null;
    while (mpIt.hasNext()) {
    	if (!firstParm) {
            thisBuf.append(", ");
        }
    	ModuleParameter mp = mpIt.next();
    	if (mp instanceof OperationDec ) {
            firstParm = false;
            opDec = (OperationDec)mp;
            thisBuf.append(formOpParmsToFacDec(opDec, modArgItem, bDec.getName().toString()));
        }
    }
     */
    /*if (modArgItem.getName() != null) {
        OperationDec opDec = null;
    	if (mp instanceof OperationDec ) {
            opDec = (OperationDec)mp;
            stmtBuf.append(formOpParmToFacDec(opDec, modArgItem, decName));
        }  else if (modArgItem.getEvalExp() != null) {
        	visitProgramExp(modArgItem.getEvalExp()); // side effects stmtBuf
        }  else {
        	  //added the following to handle passing of Max_Length type arguments
       	  stmtBuf.append(modArgItem.getName().toString());
        } 
     } 
    
    // the folloiwng code has been factored out in the aboce call for reuse
    }*/

    //facility declaration done in 2 steps for reuse in Concept bodies
    /*public String concNameforFac(FacilityDec dec) {
        StringBuffer temp = new StringBuffer();
        
        List<EnhancementBodyItem> ebItemList = dec.getEnhancementBodies();
        if (ebItemList.size() == 0) {
            temp.append(dec.getConceptName().toString());
        } else if (ebItemList.size() == 1) {
    //        	 treat one as a special case because we can use a simple decorator pattern
    //        	 this is much more effichent the the alternative
        	            Iterator<EnhancementBodyItem> ebItemIt = ebItemList.iterator(); 
        	            EnhancementBodyItem ebItem = ebItemIt.next();
        	            temp.append(ebItem.getName().toString());
        } else {
            temp.append(dec.getConceptName().toString());
        }
        return temp.toString();
    }*/

    // can't avoid dependence on stmtBuf because visitFacilityParm side effects it
    //returns true iff there are conceptual parameters
    /*private boolean handleFacConcParameters(List<ModuleArgumentItem> parmList) {
        boolean isFirstParam = true;
        
        Iterator<ModuleArgumentItem> parmIter = parmList.iterator();
        while (parmIter.hasNext()) {    
        	ModuleArgumentItem arg = parmIter.next();
        	if (!isDefArg(arg)) {
            	if (isFirstParam) {
            		isFirstParam = false;
            	} else {
            		stmtBuf.append(", ");
            	}          
                visitConcParm(arg);
        	}
        }
        return !isFirstParam;
    }*/

    // factored out this code for reuse in realization and enhancement bodies
    /*private void handleFacParameters(List<ModuleArgumentItem> concArgs, 
    		List<ModuleArgumentItem> bodyArgs, 
    		List<ModuleParameter> bodyFormals,
    		String decName) {
        boolean hasConcParams = handleFacConcParameters(concArgs);
        Iterator<ModuleArgumentItem>  parmIter = bodyArgs.iterator();
        if (parmIter.hasNext()) {     
            Iterator<ModuleParameter> mpIt = bodyFormals.iterator();
            ModuleArgumentItem arg;
            //mpIt and parmIter are of the same length if it's syntactically correct
            while (parmIter.hasNext()) {      
            	arg = parmIter.next();
            	if (!isDefArg(arg)) {
            		if (!hasConcParams) 
            			hasConcParams = true;
            		else 
    	        		stmtBuf.append(", ");
    		            visitBodyParm(arg, mpIt.next(), decName); // side effects stmtBuf
            	}
            }
        }
    }*/

    /* factored out duplication of code for different cases in declNewFacility */
    // side effects stmtBuf
    // can't avoid dependence on stmtBuf because handleFacilityConcParameters side effects it
    /*private void handleSimpleFacParameters(FacilityDec dec) {
        stmtBuf.append("(");
    	// added the if condition below so it works for degenerate cases, such as, Std_Integer_Fac
    	if (!dec.getConceptParams().isEmpty()) {
            //if (!env.compileBodies() && !myInstanceEnvironment.flags.isFlagSet(FLAG_BODIES)) {
                //System.out.println("Can not compile.  Try +bodies compile option.");
                //throw new RuntimeException();
            //} else {*/

    /* Note: We already handled this in the Controller. All files that are not imported have a dummy ConceptBodyModuleDec 
     * to get around this problem. -YS
     */
    //if (!dec.getBodyName().equals("Std_Array_Realiz")) {  			
    /*
    ModuleID bid = ModuleID.createConceptBodyID(dec.getBodyName(),    
    		dec.getConceptName());   
    	    
    ConceptBodyModuleDec bDec = (ConceptBodyModuleDec)myInstanceEnvironment.getModuleDec(bid);
    handleFacParameters(dec.getConceptParams(), dec.getBodyParams(), bDec.getParameters(), bDec.getName().toString());    
    //}
    //}
    }
    stmtBuf.append(")");
    // the folloiwng code has been factored out in the aboce call for reuse
    }*/

    /* Tried replacing the use of global stmtBuf with a parameter, but
     * that version doesn't work because stmtBuf is passed as a parameter and is also modified directly;
     * In the long term, stmtBuf has to be removed across the board carefully
     */
    /*public void declNewFacility(FacilityDec dec) {
    	
        List<EnhancementBodyItem> ebItemList = dec.getEnhancementBodies();
        if (ebItemList.size() == 0) {
            stmtBuf.append(dec.getName().toString());
            stmtBuf.append(" = \n"); 
            stmtBuf.append("\t\t");
            stmtBuf.append(" new ");
            stmtBuf.append(dec.getBodyName().toString());
    // Why aren't the operation parameters handled here like below for enhancements?
            handleSimpleFacParameters(dec);
            stmtBuf.append(";");
            stmtBuf.append("\n");
        } else if (ebItemList.size() == 1) {
    // treat one as a special case because we can use a simple decorator pattern
    // this is much more effichent the the alternative
            Iterator<EnhancementBodyItem> ebItemIt = ebItemList.iterator(); 
            EnhancementBodyItem ebItem = ebItemIt.next();*/
    /*         stmtBuf.setLength(0);
               stmtBuf.append(ebItem.getName().toString());
               stmtBuf.append(" "); */
    /*stmtBuf.append(dec.getName().toString());
    stmtBuf.append(" = \n");
    stmtBuf.append("\t\t");
    stmtBuf.append(" new ");
    stmtBuf.append(ebItem.getBodyName().toString());
    stmtBuf.append("(\n");

    // operation parameters
    handleEnhancedFacParameters(dec, ebItem);
    stmtBuf.append(", ");
    stmtBuf.append(" new ");
    stmtBuf.append(dec.getBodyName().toString());
    handleSimpleFacParameters(dec);
    // the following two statements had to be swapped; does this hose up anything else?
    stmtBuf.append(")");
    stmtBuf.append(";\n");
    } else {*/
    // no choice use the expensive dynamic decorator pattern
    /*           stmtBuf.setLength(0);
               stmtBuf.append("\t");
               stmtBuf.append(dec.getConceptName().toString());
               stmtBuf.append(" "); */
    /*stmtBuf.append(dec.getName().toString());
    stmtBuf.append(" = \n");
    StringBuffer endBuf = new StringBuffer();
    EnhancementBodyItem ebItem;
    Iterator<EnhancementBodyItem> ebItemIt = ebItemList.iterator(); 
    boolean isFirstEnh = true;
    while (ebItemIt.hasNext()) {
    	if (isFirstEnh)
    		isFirstEnh = false;
    	else 
    		stmtBuf.append(", ");
        stmtBuf.append("\t\t");
        ebItem = ebItemIt.next();
        stmtBuf.append(ebItem.getBodyName().toString());
        stmtBuf.append(".createProxy(\n");
        handleEnhancedFacParameters(dec, ebItem);
        endBuf.append("\t\t)\n");
    }
    stmtBuf.append(", ");
    stmtBuf.append(" new ");
    stmtBuf.append(dec.getBodyName().toString());
    handleSimpleFacParameters(dec);
    stmtBuf.append("\n");
    stmtBuf.append(endBuf.toString());
    stmtBuf.append("\t\t;\n");
    }
    // The following code has been moved to visitFacilityDec.
    // The following code appears to handle the case of imports not declared in the uses clause; 
    // But appearances can be deceiving :-) the imports list gets formed only with the
    // inclusion of the code below. */
    /*
    operBuf.append(stmtBuf.toString());
    ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
    ConceptModuleDec cDec = (ConceptModuleDec)env.getModuleDec(cid);
    String importStr = formJavaImport(cDec.getName().getFile());
    if (!checkImportDup(importStr)) {
        usesItemBuf.append(importStr);
        importList.addUnique(importStr);
    }
     */
    //}

    /*public void visitFacilityDec(FacilityDec dec) {
        stmtBuf.setLength(0);
    	stmtBuf.append(concNameforFac(dec));
        stmtBuf.append(" ");
        declNewFacility(dec);
        operBuf.append(stmtBuf.toString());
        ModuleID cid = ModuleID.createConceptID(dec.getConceptName());
        ConceptModuleDec cDec = (ConceptModuleDec)myInstanceEnvironment.getModuleDec(cid);
        String importStr = formJavaImport(cDec.getName().getFile());
        if (!checkImportDup(importStr)) {
            usesItemBuf.append(importStr);
            importList.addUnique(importStr);
        }
    }*/

    // checks if an argument is a mathematical definition to avoiud code generation
    /*private boolean isDefArg (ModuleArgumentItem arg) {
        ModuleScope ms = table.getModuleScope();
        
        if (arg.getName() != null) {
              Symbol s = arg.getName().getSymbol();
              if (ms.containsDefinition(s)) return true ;
        }
        return false;
    }*/

    /* No need to worry about commas after last parameter in visitFacilityParm in the buffer; 
    it's handled at the calling place, eliminating the use of global variable vfdFirstParm;
    reduced code size by about half!
     */
    // requires that the argument is not a definition
    /*private void visitConcParm(ModuleArgumentItem arg) {
        if (arg.getName() != null) {
          Binding binding = table.getModuleScope().getBinding();
          Type type = binding.getType(arg.getQualifier(),arg.getName());
          if ((type instanceof NameType) || (type instanceof FormalType) || 
        		  (type instanceof ArrayType) || (type instanceof RecordType)) {
              stmtBuf.append(typeParamStr(type, arg.getQualifier(), arg.getName()));
          } else {
        	  //added the following to handle passing of Max_Length type arguments
           	  stmtBuf.append(arg.getName().toString());
          }
        } else if (arg.getEvalExp() != null) {
            visitProgramExp(arg.getEvalExp());
        }
    }*/

    /* There should be just one way to handle the type of a variable, 
     * whether it's a parameter or a local declaration; right now two separate 
     * methods do this job and neither of them considers all cases; so this is an
     * attempt to combine and handle it all in one place.
     */
    // See if the side effect on castLookUPMap can be removed; 
    // that's the only reason why the variable name (not just the type) is a parameter here
    private String formVarDecType(PosSymbol name, Ty ty) {
        StringBuffer strBuf = new StringBuffer();

        if (ty instanceof ArrayTy) {
            /*if (!checkImportDup(IMPORT_ARRAY)) { // only do once
              usesItemBuf.append(IMPORT_ARRAY);
              importList.addUnique(IMPORT_ARRAY);
            }
            strBuf.append("Std_Array_Realiz.Array ");
            headerBuf.append("\tStd_Array_Realiz ");
            headerBuf.append(name.getName().toString());
            headerBuf.append("_Array_Fac;\n");
            //           stmtBuf.setLength(0); Is anything else dependent on this?  Apparently, yes.  See below.
            ArrayTy arrayDec = (ArrayTy)ty;
            consInitBuf.append(formArrayDeclBuf(name.getName().toString(), arrayDec));*/
        }
        else {
            String typeNameStr = ((NameTy) ty).getName().toString();
            if (isTypeParm(typeNameStr)) {
                //strBuf.append("RType");
            }
            else if (exportedTypeNames.contains(typeNameStr)) { //get rid of this side effecting code
                /*strBuf.append(curConceptPosSym.toString());
                strBuf.append(".");
                StringBuffer castExp = new StringBuffer();
                castExp.append("((");
                castExp.append(curConceptBodyPosSym.toString());
                castExp.append(".");
                castExp.append(typeNameStr);
                castExp.append(")");
                castExp.append(name);
                castExp.append(")");
                castLookUpMap.put(name.toString(), castExp.toString());
                strBuf.append(typeNameStr);*/
            }
            else if (ty instanceof NameTy) {
                NameTy nameTy = (NameTy) ty;
                typeNameStr = nameTy.getName().getName();
                if (typeNameStr.equals("Integer")) {
                    typeNameStr = "Integer";
                }
                else if (typeNameStr.equals("Char_Str")) {
                    typeNameStr = "String";
                }
                else if (typeNameStr.equals("Boolean")) {
                    typeNameStr = "Boolean";
                }
                else if (typeNameStr.equals("Char")) {
                    typeNameStr = "Character";
                }
                strBuf.append(typeNameStr);
                //	            FacilityDec fDec = getFacility(nameTy.getName().getSymbol());
                /*FacilityDec fDec = getFacility(nameTy.getQualifier(), nameTy.getName());
                if (fDec != null) {
                    strBuf.append(fDec.getConceptName().toString());
                    strBuf.append(".");
                    strBuf.append(((NameTy)ty).getName().toString());
                } else {
                    Binding binding = table.getModuleScope().getBinding();
                    Type retType = binding.getType(nameTy.getQualifier(),
                                                   nameTy.getName());
                    if (retType != null) {
                    	if (retType instanceof ArrayType) {
                    		strBuf.append("\t\tStd_Array_Realiz.Array ");
                    	} else if (retType instanceof RecordType) {
                    		strBuf.append(typeNameStr);
                    	} else if (retType instanceof IndirectType) {
                    		// Modified so it creates the right code for:
                    		// Ex: Type AliasedStack is represented by Stack_Fac.Stack;
                    		// - YS
                    		fDec = getFacility(((IndirectType) retType).getQualifier(), ((IndirectType) retType).getName());
                    		
                    		if (fDec != null) {	                			
                    			// Save the type that is stored inside the Static_Array_Template so that we can use it to type
                    			// cast later on.
                    			// - YS
                    			if (fDec.getConceptName().getName().equals("Static_Array_Template")) {
                    				StringBuffer castExp = new StringBuffer();
                    		        castExp.append("(");
                    				ModuleArgumentItem arrayType = fDec.getConceptParams().get(0);
                    				if (arrayType.getName().getName().equals("Integer")) {
                    					castExp.append("Integer_Template.Integer");
                    				} else if (arrayType.getName().getName().equals("Boolean")) {
                    					castExp.append("Boolean_Template.Boolean");
                    				} else if (arrayType.getName().getName().equals("Char_Str")) {
                    					castExp.append("Char_Str_Template.Char_Str");
                    				} else if (arrayType.getName().getName().equals("Character")) {
                    					castExp.append("Character_Template.Character");
                    				}
                    		        castExp.append(")");
                    		        castLookUpMap.put(name.toString(), castExp.toString());
                    			}
                    			
                    			strBuf.append(fDec.getConceptName().getName());
                                strBuf.append(".");
                                strBuf.append(((IndirectType) retType).getName().getName());
                    		}
                    	} else {	// don't know under what condition it gets to the following else statement	                		
                    		strBuf.append(retType.getProgramName());	                		
                    	}
                    } else {
                      strBuf.append("RType");//a rather pragmatic choice 
                    }
                }*/
            }
            else {
                strBuf.append(typeNameStr);
            }
        }

        // The following code was in visitVarDec; now unused.

        //strBuf.append(" ");
        return strBuf.toString();
    }

    // formParameterVarDecType doesn't work for Array type parameters
    // Sometimes we need to play name games but still want the type
    // extracted in formatted
    /*private String formParameterVarDecType(ParameterVarDec dec) {
    	return (formVarDecType(dec.getName(), dec.getTy()));
    }*/

    /*private String formParameterVarDec(ParameterVarDec dec) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(formParameterVarDecType(dec));
        strBuf.append(" ");
        strBuf.append(dec.getName().toString());
        return strBuf.toString();
    }*/

    // can reuse the same operation as for concept realization bodies;
    // so visitEBModuleParameters is unused and has been removed.

    /*private String formModuleParameterOpDec(OperationDec opDec) {
        StringBuffer strBuf = new StringBuffer();
        
        table.beginOperationScope();
        parmOpList.addUnique(opDec.getName().toString());
        strBuf.append("\tpublic interface ");
        strBuf.append(opDec.getName().toString());
        strBuf.append(" {\n");
        strBuf.append("\t\t");

        strBuf.append(retTypeString(opDec.getReturnTy()));
        strBuf.append(opDec.getName().toString());
        strBuf.append("(");
        List<ParameterVarDec> varDecs = opDec.getParameters();
        Iterator<ParameterVarDec> varIter = varDecs.iterator();
        while (varIter.hasNext()) {           
            strBuf.append(formParameterVarDec(varIter.next()));
            if (varIter.hasNext()) {
                strBuf.append(", ");
            }
        }
        strBuf.append(");\n");
        strBuf.append("\t}\n");
        table.endOperationScope();
        
        return strBuf.toString();
    }*/

    // can reuse the same operation as for concept realization bodies;
    // so visitEBParameterVarDec is unused and has been removed.

    // -----------------------------------------------------------
    // Type Declarations
    // -----------------------------------------------------------

    //the following class has been improved to handle record type declarations
    /*public void visitFacilityTypeDec(FacilityTypeDec dec) {
        table.beginTypeScope();
        if (dec.getInitialization() != null) {
            visitInitItem(dec.getInitialization());
        }
        if (dec.getFinalization() != null) {
            visitFinalItem(dec.getFinalization());
        }
        Ty curTy = dec.getRepresentation();
        if (curTy instanceof ArrayTy) {
            if (!checkImportDup(IMPORT_ARRAY)) { // only do once
              usesItemBuf.append(IMPORT_ARRAY);
              importList.addUnique(IMPORT_ARRAY);
            }
            typeBuf.append("\tStd_Array_Realiz.Array ");
            typeBuf.append(dec.getName().toString());
            typeBuf.append(";\n");
            headerBuf.append("\tStd_Array_Realiz ");
            headerBuf.append(dec.getName().toString());
            headerBuf.append("_Array_Fac;\n");
            facilityConstructorBuf.append("\t\t");
            facilityConstructorBuf.append(formArrayDeclBuf(dec.getName().toString(), (ArrayTy)curTy));
        } else
        	if (curTy instanceof RecordTy) {
        		formImplClassDeclBuf(dec.getName().toString(), "RType", "RESOLVE_BASE", typeBuf);
        		formRepClassDeclBuf(dec.getName().toString(), (RecordTy)curTy, typeBuf);
        	}
        table.endTypeScope();
    }*/

    public void visitProcedureDec(ProcedureDec dec) {
        table.beginOperationScope();
        table.beginProcedureScope();
        int line = dec.getName().getPos().getLine();
        bumpLine(line);
        String operName = dec.getName().toString();
        Boolean function = false;
        PosSymbol retSymbol = null;
        if (operName.equals("Main")) {
            appendJava("public static void main(String[] args)", true);
            bumpLine();
            appendJava("{", true);

        }
        else {
            appendJava("public ", true);
            Ty returnTy = dec.getReturnTy();
            if (returnTy != null) {
                function = true;
                ;
            }
            if (function) {
                appendJava(returnTy.toString(0) + " ", false);
            }
            appendJava(operName, false);
            appendJava("()", false);
            bumpLine();
            appendJava("{", true);
            if (function) {
                retSymbol =
                        new PosSymbol(((NameTy) returnTy).getName()
                                .getLocation(), Symbol.symbol("_returnVar"));
                VarDec retVarDec = new VarDec(retSymbol, returnTy);
                visitVarDec(retVarDec);
                //appendJava(returnTy.toString(0) + " ", false);
            }
        }
        indent();
        VarDec curVar;
        Iterator<VarDec> varIt = dec.getVariables().iterator();
        while (varIt.hasNext()) {
            curVar = varIt.next();
            //stmtBuf.setLength(0);
            visitVarDec(curVar);
            //operBuf.append(stmtBuf.toString());
        }
        List<Statement> statements = dec.getStatements();
        Iterator<Statement> stmtIter = statements.iterator();
        while (stmtIter.hasNext()) {
            visitStatement(stmtIter.next());
        }
        appendJava("\n", false);
        unIndent();
        if (function) {
            appendJava("return " + retSymbol.getName() + "; }", true);
        }
        else {
            appendJava("}", true);
        }
        /*table.bindProcedureTypeNames();
        operBuf.append("\tpublic "); 
        Ty retTy = dec.getReturnTy();
        operBuf.append(retTypeString(retTy));
        operBuf.append(dec.getName().toString());
        operBuf.append("(");*/

        /*castLookUpMap = new Map<String, String>();
        List<ParameterVarDec> varDecs = dec.getParameters();
        Iterator<ParameterVarDec> varIter = varDecs.iterator();
        while (varIter.hasNext()) {           
            operBuf.append(formParameterVarDec(varIter.next()));
            if (varIter.hasNext()) {
                operBuf.append(", ");
            }
        }*/

        //operBuf.append(") {\n");

        /*if (retTy instanceof NameTy) {
            operBuf.append("\t\t");
        //           FacilityDec fDec = getFacility(((NameTy)retTy).getName().getSymbol());
            FacilityDec fDec = getFacility(((NameTy)retTy).getQualifier(), ((NameTy)retTy).getName());
            operBuf.append(fDec.getConceptName().toString());
            operBuf.append(".");
            String typeName = ((NameTy)retTy).getName().toString();
            operBuf.append(typeName);
            operBuf.append(" ");
            operBuf.append(dec.getName().toString());
            operBuf.append(" = ");
            Binding binding = table.getModuleScope().getBinding();
            NameType nType = 
                (NameType)binding.getType(((NameTy)retTy).getQualifier(),
                                          ((NameTy)retTy).getName());
            if (nType.getFacility() != null) {
                operBuf.append(nType.getFacility().toString());
            } else {
                TypeName tName = nType.getProgramName();
                operBuf.append(tName.getFacilityQualifier().toString());
            }
            operBuf.append(".create");
            operBuf.append(typeName);
            operBuf.append("();\n");
            operBuf.append("\n");
        }*/

        /*FacilityDec curFac;
        Iterator<FacilityDec> facIt = dec.getFacilities().iterator();
        while (facIt.hasNext()) {
            curFac = facIt.next();
            stmtBuf.setLength(0);
            visitFacilityDec(curFac);
        }*/

        /*VarDec curVar;
        Iterator<VarDec> varIt = dec.getVariables().iterator();
        while (varIt.hasNext()) {
            curVar = varIt.next();
            stmtBuf.setLength(0);
            visitVarDec(curVar);
            operBuf.append(stmtBuf.toString());
        }
        operBuf.append("\n");*/

        /*List<Statement> statements = dec.getStatements();
        Iterator<Statement> stmtIter = statements.iterator();
        while (stmtIter.hasNext()) {           
            stmtBuf.setLength(0);// clear 
            visitStatement(stmtIter.next());
            operBuf.append(stmtBuf.toString());
        }

        if (retTy instanceof NameTy) {
            operBuf.append("\t\treturn ");
            operBuf.append(dec.getName().toString());
            operBuf.append(";\n");
        }

        operBuf.append("\t}\n\n");*/

        table.endProcedureScope();
        table.endOperationScope();
    }

    // reusing identical visitProcedureDec instead; 
    // so visitEBProcedureDec is unused and removed.  But note the following:
    // only difference seems to be in one statement that side effects global castLookUp
    // don't know why that has to be a difference

    //factored this out from variable declaration for use in record field declarations
    // moved thisBuf.append(";\n): to the caller's side, 
    // so the separator can be either a comma or semicolon, depending on the context.
    /*public String formNewVarDecl(VarDec curVar) {
    	PosSymbol name = curVar.getName();
    	Ty ty = curVar.getTy();
    	StringBuffer thisBuf = new StringBuffer();
    	
        Ty curVarTy = curVar.getTy();
        if (curVarTy instanceof ArrayTy) {
          thisBuf.append(curVar.getName().toString());
          thisBuf.append("_Array_Fac.createArray()");
        } else if (curVarTy instanceof NameTy) {
            String typeNameStr = ((NameTy)ty).getName().toString();
            if (isTypeParm(typeNameStr)) {
                thisBuf.append("getType");
                thisBuf.append(typeNameStr);
                thisBuf.append("().initialValue()");
            } else if (exportedTypeNames.contains(typeNameStr)) { //get rid of this side effecting code
                thisBuf.append("con.create");
                thisBuf.append(typeNameStr);
                thisBuf.append("()");
            } else if (ty instanceof NameTy) {
                NameTy nameTy = (NameTy)ty;
    //	            FacilityDec fDec = getFacility(nameTy.getName().getSymbol());
                FacilityDec fDec = getFacility(nameTy.getQualifier(), nameTy.getName());
                if (fDec != null) {
    	            thisBuf.append(fDec.getName());
    	            thisBuf.append(".");
    	            thisBuf.append("create");
    	            thisBuf.append(typeNameStr);
    	            thisBuf.append("()");
                } else {
                    Binding binding = table.getModuleScope().getBinding();
                    Type retType = binding.getType(nameTy.getQualifier(),
                                                   nameTy.getName());
                    if (retType != null) {
                    	if (retType instanceof ArrayType) {
                            thisBuf.append(((ArrayType)retType).getName().toString());
                            thisBuf.append("_Array_Fac.createArray()");
                    	} else if (retType instanceof RecordType) {
                            thisBuf.append("new ");
                            thisBuf.append(((RecordType)retType).getName().getName());
                            thisBuf.append("()");
                    	} else if (retType instanceof IndirectType) {
                    		// Modified so it creates the right code for:
                    		// Ex: Type AliasedStack is represented by Stack_Fac.Stack;
                    		// - YS
                    		fDec = getFacility(((IndirectType) retType).getQualifier(), ((IndirectType) retType).getName());
                    		
                    		if (fDec != null) {
                    			thisBuf.append(fDec.getName());
            		            thisBuf.append(".");
            		            thisBuf.append("create");
            		            thisBuf.append(((IndirectType) retType).getName());
            		            thisBuf.append("()");
                    		}
                    	} else {
                    		thisBuf.append(retType.getProgramName());
                    	}
                    } else {
                      thisBuf.append("RType");//a rather pragmatic choice 
                    }
                }
            } else {
                thisBuf.append(typeNameStr);
            }
        }
        // code below has been factored out and removed.
    
        thisBuf.append(";\n");
    	return thisBuf.toString();
    }*/

    //improved this to handle Record type variable declarations
    public void visitVarDec(VarDec curVar) {
        PosSymbol name = curVar.getName();
        Ty ty = curVar.getTy();
        boolean bumped = bumpLine(name.getPos().getLine());
        appendJava((!bumped) ? " " : "", false);
        appendJava(formVarDecType(name, ty), bumped);
        appendJava(" ", false);
        appendJava(formVarInit(curVar), false);
        appendJava(";", false);
        //stmtBuf.append(formVarDecType(name, ty));
        //formVarInit(curVar, stmtBuf);
    }

    private void visitStatement(Statement stmt) {
        //stmtBuf.append("\t\t");
        if (stmt instanceof FuncAssignStmt) {
            bumpLine(((FuncAssignStmt) stmt).getLocation().getPos().getLine());
            appendJava("", true);
            visitFuncAssignStmt((FuncAssignStmt) stmt);
        }
        else if (stmt instanceof SwapStmt) {
            visitSwapStmt((SwapStmt) stmt);
        }
        else if (stmt instanceof CallStmt) {
            visitCallStmt((CallStmt) stmt);
        }
        else if (stmt instanceof IfStmt) {
            visitIfStmt((IfStmt) stmt);
        }
        else if (stmt instanceof IterateExitStmt) {}
        else if (stmt instanceof IterateStmt) {}
        else if (stmt instanceof MemoryStmt) {}
        else if (stmt instanceof SelectionStmt) {}
        else if (stmt instanceof WhileStmt) {
            visitWhileStmt((WhileStmt) stmt);
        }
        else if (stmt instanceof ConfirmStmt) {
            bumpLine(((ConfirmStmt) stmt).getLocation().getPos().getLine());
            appendJava("// Confirm ", true);
            Exp exp = ((ConfirmStmt) stmt).getAssertion();
            String assertion = exp.toString(0);
            appendJava(assertion, false);
        }
        else {
            assert false;
        }
        //stmtBuf.append("\n");
    }

    public void visitWhileStmt(WhileStmt stmt) {
        int line = stmt.getLocation().getPos().getLine();
        bumpLine(line);
        appendJava("while (", true);
        visitProgramExp(stmt.getTest());
        appendJava(")", false);
        indent();
        List<VariableExp> changings = stmt.getChanging();
        if (changings != null) {
            line = changings.get(0).getLocation().getPos().getLine();
            bumpLine(line);
            appendJava("// changing ", true);
            Iterator it = changings.iterator();
            while (it.hasNext()) {
                VariableExp exp = (VariableExp) it.next();
                line = exp.getLocation().getPos().getLine();
                bumpLine(line);
                appendJava(exp.toString(0), false);
                if (it.hasNext()) {
                    appendJava(", ", false);
                }
            }
            //appendJava()
        }
        Exp maintaining = stmt.getMaintaining();
        if (maintaining != null) {
            line = maintaining.getLocation().getPos().getLine();
            bumpLine(line);
            appendJava("// maintaining ", true);
            appendJava(maintaining.toString(0), false);
        }
        Exp decreasing = stmt.getDecreasing();
        if (decreasing != null) {
            line = decreasing.getLocation().getPos().getLine();
            bumpLine(line);
            appendJava("// decreasing ", true);
            appendJava(decreasing.toString(0), false);
        }
        unIndent();
        bumpLine();
        appendJava("{", true);
        bumpLine();
        indent();
        //stmtBuf.append("while (((Std_Boolean_Realiz.Boolean)(");
        //visitProgramExp(stmt.getTest());
        //stmtBuf.append(")).val) {\n");
        List<Statement> statements = stmt.getStatements();
        Iterator<Statement> stmtIter = statements.iterator();
        while (stmtIter.hasNext()) {
            //stmtBuf.append("\t");
            //appendJava("", true);
            Statement temp = stmtIter.next();
            visitStatement(temp);
            if (stmtIter.hasNext()) {

            }
            //stmtBuf.append("\n");
        }
        unIndent();
        bumpLine();
        appendJava("}", true);

        //stmtBuf.append("\t\t}\n");
    }

    public void visitIfStmt(IfStmt stmt) {
        // if part
        bumpLine(stmt.getTest().getLocation().getPos().getLine());
        appendJava("if (", true);
        visitProgramExp(stmt.getTest());
        appendJava(" ) {", false);
        bumpLine();
        List<Statement> statements = stmt.getThenclause();
        Iterator<Statement> stmtIt = statements.iterator();
        indent();
        while (stmtIt.hasNext()) {
            visitStatement(stmtIt.next());
        }
        unIndent();
        bumpLine();
        appendJava("}", true);
        bumpLine();

        // else if (s)
        List<ConditionItem> elseifList = stmt.getElseifpairs();
        if (elseifList != null) {
            ConditionItem condItem;
            Iterator<ConditionItem> elseifIt = elseifList.iterator();
            while (elseifIt.hasNext()) {
                condItem = elseifIt.next();
                appendJava("else if (", true);
                visitProgramExp(condItem.getTest());
                appendJava(" ) {", false);

                statements = condItem.getThenclause();
                stmtIt = statements.iterator();
                indent();
                while (stmtIt.hasNext()) {
                    visitStatement(stmtIt.next());
                }
                unIndent();
                bumpLine();
                appendJava("}", true);
                bumpLine();
            }
        }

        // else
        statements = stmt.getElseclause();
        if (statements != null && !statements.isEmpty()) {
            appendJava("else {", true);
            stmtIt = statements.iterator();
            while (stmtIt.hasNext()) {
                visitStatement(stmtIt.next());
            }
            unIndent();
            bumpLine();
            appendJava("}", true);
        }

        // if part
        /*stmtBuf.append("if (((Std_Boolean_Realiz.Boolean)(");
        visitProgramExp(stmt.getTest());
        stmtBuf.append(")).val) {\n");
        List<Statement> statements = stmt.getThenclause();
        Iterator<Statement> stmtIt = statements.iterator();
        while (stmtIt.hasNext()) {           
            stmtBuf.append("\t");
            visitStatement(stmtIt.next());
            stmtBuf.append("\n");
        }
        stmtBuf.append("\t\t}\n");*/

        // else if (s)
        /*List<ConditionItem> elseifList = stmt.getElseifpairs();
        if (elseifList != null) {
            ConditionItem condItem;
            Iterator<ConditionItem> elseifIt = elseifList.iterator();
            while (elseifIt.hasNext()) {
                condItem = elseifIt.next();
                stmtBuf.append("else if (((Std_Boolean_Realiz.Boolean)(");
                visitProgramExp(condItem.getTest());
                stmtBuf.append(")).val) {\n");

                statements = condItem.getThenclause();
                stmtIt = statements.iterator();
                while (stmtIt.hasNext()) {           
                    stmtBuf.append("\t");
                    visitStatement(stmtIt.next());
                    stmtBuf.append("\n");
                }
                stmtBuf.append("\t\t}\n");
            }            
        }

        // else
        statements = stmt.getElseclause();
        if (statements != null && !statements.isEmpty()) {
            stmtBuf.append("\t\telse {\n");
            stmtIt = statements.iterator();
            while (stmtIt.hasNext()) {
                stmtBuf.append("\t");
                visitStatement(stmtIt.next());
                stmtBuf.append("\n");
            }
            stmtBuf.append("\t\t}\n");
        }*/
    }

    public void visitCallStmt(CallStmt stmt) {
        bumpLine(stmt.getName().getLocation().getPos().getLine());
        if (stmt.getQualifier() != null) {
            stmtBuf.append(stmt.getQualifier().toString());
            stmtBuf.append(".");
        }
        else {
            ProgramExpTypeResolver resolver =
                    new ProgramExpTypeResolver(table, myInstanceEnvironment);
            Location loc =
                    (stmt.getQualifier() == null) ? stmt.getName()
                            .getLocation() : stmt.getQualifier().getLocation();
            ProgramFunctionExp exp =
                    new ProgramFunctionExp(loc, stmt.getQualifier(), stmt
                            .getName(), stmt.getArguments());
            try {
                OperationEntry oper = resolver.getOperationEntry(exp);
                ModuleScope modScope = (ModuleScope) oper.getScope();
                FacilityDec fDec = modScope.getFacilityDec();
                //buildCallQualifier(oper, fDec);
            }
            catch (TypeResolutionException trex) {
                System.out.println("TypeResolutionException");
                // do nothing - the  was already reported
            }
        }

        String callName = stmt.getName().toString();
        if (isParmOp(callName)) {
            stmtBuf.append(callName);
            stmtBuf.append("Parm.");
        }
        if (callName.equals("Write")) {
            appendJava("System.out.println(", true);
            List<ProgramExp> expList = stmt.getArguments();
            Iterator<ProgramExp> expIter = expList.iterator();
            while (expIter.hasNext()) {
                visitProgramExp(expIter.next());
                if (expIter.hasNext()) {
                    appendJava(", ", false);
                }
            }
            appendJava(");", false);
        }
        /*else if(callName.equals("Read")){
            List<ProgramExp> expList = stmt.getArguments();
            Iterator<ProgramExp> expIter = expList.iterator();
            String varName = "";
            while (expIter.hasNext()) {
               varName = expIter.next().toString(0);
            }
            appendJava(varName, true);
            appendJava(" = (new BufferedReader(new InputStreamReader(System.in))).readLine();", false);
        }*/
        else {
            appendJava(callName, true);
            appendJava("(", false);
            List<ProgramExp> expList = stmt.getArguments();
            Iterator<ProgramExp> expIter = expList.iterator();
            while (expIter.hasNext()) {
                visitProgramExp(expIter.next());
                if (expIter.hasNext()) {
                    appendJava(", ", false);
                }
            }

            appendJava(");", false);
        }
        /*stmtBuf.append(callName);
        stmtBuf.append("(");
        List<ProgramExp> expList = stmt.getArguments();
        Iterator<ProgramExp> expIter = expList.iterator();
        while (expIter.hasNext()) {           
            visitProgramExp(expIter.next());
            if (expIter.hasNext()) {
                stmtBuf.append(", ");
            }
        }

        stmtBuf.append(")");
        stmtBuf.append(";");*/
    }

    private void visitConfirmStmt(Statement stmt) {

    }

    /*private void buildCallQualifier(OperationEntry oper, FacilityDec fDec) {
        if (fDec != null) {
    // if multiple enhancements then dynamic decorators are used and casting may be needed
            if (fDec.getEnhancementBodies().size() > 1) {
                String fileName = oper.getLocation().getFilename();
                String fileNameExt = fileName.substring(fileName.length()-2); 
                if ("en".equals(fileNameExt)) {
                    String enName = fileName.substring(0, fileName.length()-3);
                    stmtBuf.append("((");
                    stmtBuf.append(enName);
                    stmtBuf.append(")");
                    stmtBuf.append(fDec.getName().toString());
                    stmtBuf.append(")");
                    stmtBuf.append(".");
                 } else {
                    stmtBuf.append(fDec.getName().toString());
                    stmtBuf.append(".");
                 };
            } else {
                stmtBuf.append(fDec.getName().toString());
                stmtBuf.append(".");
            }
        }
    }*/

    /*public void visitSwapStmt(SwapStmt stmt) {
    	VariableExp lhs = stmt.getLeft();
    	VariableExp rhs = stmt.getRight();
    	if(!(lhs instanceof VariableArrayExp) || !(rhs instanceof VariableArrayExp)) {
            if (ContainsArray(lhs,false)) {
                if (containsArrayType != null) {
                  stmtBuf.append(containsArrayType);
                } else {
                  stmtBuf.append(containsArrayName);
                }
                stmtBuf.append("_Array_Fac.Swap_Entry(");
                visitVariableExp(lhs);
                stmtBuf.append(", ");
                visitProgramExp(containsArraySubscript);
                stmtBuf.append(", ");
                visitVariableExp(rhs);
                stmtBuf.append(")");
              } else if (ContainsArray(rhs,false)) {
                if (containsArrayType != null) {
                  stmtBuf.append(containsArrayType);
                } else {
                  stmtBuf.append(containsArrayName);
                }
                stmtBuf.append("_Array_Fac.Swap_Entry(");
                visitVariableExp(rhs);
                stmtBuf.append(", ");
                visitProgramExp(containsArraySubscript);
                stmtBuf.append(", ");
                visitVariableExp(lhs);
                stmtBuf.append(")");
              } else {
              	String facName = getFacName(lhs);
              	if (facName != null) { 
              		stmtBuf.append(facName);
              	} else { 	//temp. fix? special handling of arrays and records; won't work for nested fields yet
                      VariableExp varExp = lhs;
              		if (varExp instanceof VariableNameExp) {
                          PosSymbol varName = ((VariableNameExp)varExp).getName();
                          Binding binding = table.getModuleScope().getBinding();*/
    /*  Scope curScope = table.getCurrentScope();
     VarEntry tblVarEntry = curScope.getVariable(varName.getSymbol());
     IndirectType indType = (IndirectType)tblVarEntry.getType(); */
    /*Type type = binding.getType(null, varName);
    if (type instanceof ArrayType) {
        stmtBuf.append(((ArrayType)type).getName().toString());
        stmtBuf.append("_Array_Fac.");
    } else if (type instanceof RecordType) {
    	visitVariableExp(lhs);
        stmtBuf.append(".");
    }
    } else if (varExp instanceof VariableRecordExp) {
    stmtBuf.append(((VariableRecordExp)varExp).getName().toString());
    stmtBuf.append(".");
    }
    }
    stmtBuf.append("swap(");
    visitVariableExp(lhs);
    stmtBuf.append(", ");
    visitVariableExp(rhs);
    stmtBuf.append(")");
    }
    }
    else{
    // Case: A[1] :=: A[2]
    if (ContainsArray(lhs,false)) {
    if (containsArrayType != null) {
    stmtBuf.append(containsArrayType);
    } else {
    stmtBuf.append(containsArrayName);
    }
    stmtBuf.append("_Array_Fac.Swap_Two_Entries(");
    visitVariableExp(lhs);
    stmtBuf.append(", ");
    visitProgramExp(containsArraySubscript);
    stmtBuf.append(", ");
    if (ContainsArray(rhs,false)) {
    visitProgramExp(containsArraySubscript);
    }
    stmtBuf.append(")");
    }
    }
    stmtBuf.append(";");
    }*/

    // ok, the inRecord to track the context is gross but I'm short ideas - jmh
    /*private boolean ContainsArray(VariableExp varExp, boolean inRecord) {
    	boolean containsArray = false;
        if (varExp instanceof VariableNameExp) {
            containsArray = false;
        } else if (varExp instanceof VariableDotExp) {
            VariableRecordExp varRecExp = 
                (VariableRecordExp)((VariableDotExp)varExp).getSemanticExp();
            VariableExp field;
            
            Iterator<VariableExp> fieldsIter = varRecExp.getFields().iterator();
            while (fieldsIter.hasNext() && containsArray == false) {
                field = fieldsIter.next();  
                containsArray = ContainsArray(field, true); 
            }
        } else if (varExp instanceof VariableRecordExp) {
            VariableRecordExp varRecExp = (VariableRecordExp)varExp;
            VariableExp field;
            Iterator<VariableExp> fieldsIter = varRecExp.getFields().iterator();
            while (fieldsIter.hasNext() && containsArray == false) {
                field = fieldsIter.next();  
                containsArray = ContainsArray(field, true); 
            }
        } else if (varExp instanceof VariableArrayExp) {

            if (!inRecord) {
              VariableArrayExp vae = (VariableArrayExp)varExp;
              VariableLocator locator = new VariableLocator(table, err);
              try {
                  VarEntry entry = locator.locateProgramVariable(vae.getQualifier(),
                                                                 vae.getName());
                  Type type = entry.getType();
                  if (type instanceof IndirectType) {
                    IndirectType indType = (IndirectType)type;
                    containsArrayType = indType.getName().toString();
                  }
              } catch (SymbolSearchException ex) {
              }
            }
            containsArray = true;
            containsArrayName = ((VariableArrayExp)varExp).getName();
            containsArraySubscript = ((VariableArrayExp)varExp).getArgument();
        } else {
            assert false;
        }

        return containsArray;
    }*/

    public void visitFuncAssignStmt(FuncAssignStmt stmt) {
        bumpLine(stmt.getLocation().getPos().getLine());
        ProgramExp pgmExp = stmt.getAssign();
        if (pgmExp instanceof ProgramParamExp
                || pgmExp instanceof ProgramIntegerExp
                || pgmExp instanceof ProgramCharExp
                || pgmExp instanceof ProgramStringExp
                || pgmExp instanceof VariableExp) {
            visitVariableExp(stmt.getVar());
            appendJava(" = ", false);
            visitProgramExp(stmt.getAssign());
            /*stmtBuf.append(getFacName(stmt.getVar()));
            stmtBuf.append("assign(");
            visitVariableExp(stmt.getVar());
            stmtBuf.append(", ");
            visitProgramExp(stmt.getAssign());
            stmtBuf.append(")");*/
        }
        else {
            //I'm not sure we ever get here but when I refactor last summers
            //code this logically comes here 5/21/2004
            visitVariableExp(stmt.getVar());
            appendJava(" = ", false);
            visitProgramExp(stmt.getAssign());
            /*visitVariableExp(stmt.getVar());
            stmtBuf.append(" = ");
            visitProgramExp(stmt.getAssign());*/
        }
        appendJava(";", false);
        //stmtBuf.append(";");
    }

    // this seems quite buggy; don't know when/why it returns facName = "" or null 
    // removed several system.out statements
    /*private String getFacName(VariableExp varExp) {
        String facName = null;
        Type varType = null;
        
        ProgramExpTypeResolver resolver = new ProgramExpTypeResolver(table, myInstanceEnvironment);
        if (varExp instanceof VariableNameExp) {
            VariableNameExp curExp = (VariableNameExp)varExp;
            try {
               varType = resolver.getVariableNameExpType(curExp);
            } catch (TypeResolutionException trex) {
                System.out.println("TypeResolutionException");
                // do nothing - the  was already reported
            } 
        } else if (varExp instanceof VariableDotExp) {
            VariableRecordExp varRecExp = 
                (VariableRecordExp)((VariableDotExp)varExp).getSemanticExp();
            try {
               varType = resolver.getVariableRecordExpType(varRecExp);
            } catch (TypeResolutionException trex) {
                System.out.println("TypeResolutionException");
                // do nothing - the  was already reported
            }
        } else if (varExp instanceof VariableRecordExp) {
    System.out.println("VariableRecordExp found" );
            assert false;
        } else if (varExp instanceof VariableArrayExp) {
            VariableArrayExp curArrayExp = (VariableArrayExp)varExp;
            try {
               varType = resolver.getVariableArrayExpType(curArrayExp);
    //JMH
    System.out.println("varType: " + varType);
            } catch (TypeResolutionException trex) {
                System.out.println("TypeResolutionException");
                // do nothing - the  was already reported
            }
        } else {
            assert false;
        }

        if (varType instanceof IndirectType) {
            IndirectType indType = (IndirectType)varType;
            Type indTypeType = indType.getType();
            if (indTypeType instanceof NameType) {
                NameType nameType = (NameType)indTypeType;
                if (nameType.getFacility() != null) {
                    facName = nameType.getFacility().toString();
                } else {
                    TypeName typeName = nameType.getProgramName();
                    facName = typeName.getFacilityQualifier().toString();
                }
                facName += ".";
            } else {
                facName = ""; //does not yet have a facility
            }
        } 
        return facName;
    }*/

    private void visitVariableExp(VariableExp varExp) {
        bumpLine(varExp.getLocation().getPos().getLine());
        if (varExp instanceof VariableNameExp) {
            String name = ((VariableNameExp) varExp).getName().toString();
            appendJava(name, false);
            /*if (isConcParm(name)) {
                stmtBuf.append("get");
                stmtBuf.append(name);
                stmtBuf.append("()");
            } else {
                stmtBuf.append(name);
            }*/
        }
        else if (varExp instanceof VariableDotExp) {
            VariableRecordExp varRecExp =
                    (VariableRecordExp) ((VariableDotExp) varExp)
                            .getSemanticExp();
            if (!isLocalVar) { //inside record constructor doin't qualify
                stmtBuf.append(castLookUp(varRecExp.getName().toString()));
                stmtBuf.append(".rep.");
            }
            VariableExp field;
            Iterator<VariableExp> fieldsIter = varRecExp.getFields().iterator();
            while (fieldsIter.hasNext()) {
                field = fieldsIter.next();
                if (field instanceof VariableNameExp) {
                    stmtBuf.append(((VariableNameExp) field).getName()
                            .toString());
                }
                else if (field instanceof VariableExp) {
                    stmtBuf.append(((VariableArrayExp) field).getName()
                            .toString());
                }
                if (fieldsIter.hasNext()) {
                    stmtBuf.append(".rep.");
                }
            }
        }
        else if (varExp instanceof VariableRecordExp) {
            VariableRecordExp varRecExp = (VariableRecordExp) varExp;
            stmtBuf.append(castLookUp(varRecExp.getName().toString()));
            stmtBuf.append(".rep.");
            VariableExp field;
            Iterator<VariableExp> fieldsIter = varRecExp.getFields().iterator();
            while (fieldsIter.hasNext()) {
                field = fieldsIter.next();
                if /* (field instanceof VariableRecordExp) {
                   stmtBuf.append(((VariableRecordExp)field).getName().toString());
                    stmtBuf.append(".rep");
                   } else if */(field instanceof VariableNameExp) {
                    stmtBuf.append(((VariableNameExp) field).getName()
                            .toString());
                }
                else if (field instanceof VariableExp) {
                    stmtBuf.append(((VariableArrayExp) field).getName()
                            .toString());
                }
                if (fieldsIter.hasNext()) {
                    stmtBuf.append(".rep.");
                }
            }
        }
        else if (varExp instanceof VariableArrayExp) {
            stmtBuf.append(((VariableArrayExp) varExp).getName().toString());
        }
        else {
            assert false;
        }
    }

    /*private String getOpFacility(ProgramOpExp opExp) {
    // assume we are comparing two Integer until we find otherwise
        String facName = "Std_Integer_Fac.";

        ProgramExp first = opExp.getFirst();
        if (first instanceof VariableExp) {
          facName = getFacName((VariableExp)first);
        } else if (first instanceof ProgramParamExp) {
          ProgramParamExp pgmPE = (ProgramParamExp)first;
          ProgramExp se = pgmPE.getSemanticExp();
          if (se instanceof ProgramFunctionExp) {
            ProgramFunctionExp exp = (ProgramFunctionExp)se;
            ProgramExpTypeResolver resolver = new ProgramExpTypeResolver(table, myInstanceEnvironment);
            try {
              OperationEntry oper = resolver.getOperationEntry(exp);
              ModuleScope modScope = (ModuleScope)oper.getScope();
              FacilityDec fDec = modScope.getFacilityDec();
    // an enhancement can call ops on its concept for which a facility does not 
    // (yet) exist - jmh
    //              buildCallQualifier(oper, fDec);
              Type type = oper.getType();
              TypeName tname = type.getProgramName();
              facName = tname.getFacilityQualifier() + ".";
            } catch (TypeResolutionException trex) {
              System.out.println("TypeResolutionException");
              // do nothing - the  was already reported
            }
          } else {
            System.out.println("ProgramExp is not ProgramFunctionExp");
            assert false;
          }
        } else if (first instanceof ProgramOpExp) {
    System.out.println("ProgramOpExp");
        } else if (first instanceof ProgramIntegerExp) {
    System.out.println("ProgramIntegerExp");
        } else if (first instanceof ProgramFunctionExp) {
    System.out.println("ProgramFunctionExp");
        } else if (first instanceof ProgramDoubleExp) {
    System.out.println("ProgramDoubleExp");
        } else if (first instanceof ProgramDotExp) {
    //System.out.println("ProgramDotExp");
        }
        return facName;
    }*/

    private void visitProgramExp(ProgramExp pgmExp) {
        bumpLine(pgmExp.getLocation().getPos().getLine());
        if (pgmExp instanceof VariableExp) {
            visitVariableExp((VariableExp) pgmExp);
        }
        else if (pgmExp instanceof ProgramParamExp) {
            visitProgramExp(((ProgramParamExp) pgmExp).getSemanticExp());
        }
        else if (pgmExp instanceof ProgramDotExp) {
            visitProgramExp(((ProgramDotExp) pgmExp).getSemanticExp());
        }
        else if (pgmExp instanceof ProgramDoubleExp) {}
        else if (pgmExp instanceof ProgramFunctionExp) {
            // Since Entry_Replica operation returns something of RType, we need to cast it to the proper type
            // when using it as parameter to a function invokation.
            // - YS
            if (((ProgramFunctionExp) pgmExp).getName().getName().equals(
                    "Entry_Replica")) {
                VariableExp tempExp =
                        (VariableExp) ((ProgramFunctionExp) pgmExp)
                                .getArguments().get(0);
                if (tempExp instanceof VariableNameExp) {
                    VariableNameExp arrayName = (VariableNameExp) tempExp;
                    stmtBuf.append(castLookUp(arrayName.getName().getName()));
                }
                else if (tempExp instanceof VariableDotExp) {

                    // TODO: Check to see how this needs to work.
                    //VariableDotExp arrayName = (VariableDotExp) tempExp;
                    //stmtBuf.append(castLookUp(arrayName.toString()));
                }
            }
            visitProgramFunctionExp((ProgramFunctionExp) pgmExp);
        }
        else if (pgmExp instanceof ProgramIntegerExp) {
            appendJava(Integer
                    .toString(((ProgramIntegerExp) pgmExp).getValue()), false);
            //stmtBuf.append("Std_Integer_Fac.createInteger(");
            //stmtBuf.append(((ProgramIntegerExp)pgmExp).getValue());
            //stmtBuf.append(")");
        }
        else if (pgmExp instanceof ProgramCharExp) {
            appendJava(
                    Character.toString(((ProgramCharExp) pgmExp).getValue()),
                    false);
            //stmtBuf.append("Std_Character_Fac.createCharacter('"); //changed this from Char to Character
            //stmtBuf.append(((ProgramCharExp)pgmExp).getValue());
            //stmtBuf.append("')");
        }
        else if (pgmExp instanceof ProgramStringExp) {
            appendJava(((ProgramStringExp) pgmExp).getValue(), false);
            //stmtBuf.append("Std_Char_Str_Fac.createChar_Str(");
            //stmtBuf.append(((ProgramStringExp)pgmExp).getValue());
            //stmtBuf.append(")");
        }
        else if (pgmExp instanceof ProgramOpExp) {
            ProgramOpExp pgmOpExp = (ProgramOpExp) pgmExp;
            switch (pgmOpExp.getOperator()) {
            case ProgramOpExp.AND:
                //stmtBuf.append("Std_Boolean_Fac.And("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" && ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.OR:
                //stmtBuf.append("Std_Boolean_Fac.Or("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" || ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.EQUAL:
                //stmtBuf.append(getOpFacility(pgmOpExp)); 
                //stmtBuf.append("Are_Equal("); 
                visitProgramExp(pgmOpExp.getFirst());
                appendJava(" == ", false);
                //stmtBuf.append(", ");
                visitProgramExp(pgmOpExp.getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.NOT_EQUAL:
                //stmtBuf.append(getOpFacility(pgmOpExp)); 
                //stmtBuf.append("Are_Not_Equal("); 
                visitProgramExp(pgmOpExp.getFirst());
                appendJava(" != ", false);
                //stmtBuf.append(", ");
                visitProgramExp(pgmOpExp.getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.LT:
                //stmtBuf.append(getOpFacility(pgmOpExp)); 
                //stmtBuf.append("Less("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" < ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.LT_EQL:
                //stmtBuf.append(getOpFacility(pgmOpExp)); 
                //stmtBuf.append("Less_Or_Equal("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" <= ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.GT:
                //stmtBuf.append(getOpFacility(pgmOpExp)); 
                //stmtBuf.append("Greater("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" > ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.GT_EQL:
                //stmtBuf.append(getOpFacility(pgmOpExp)); 
                //stmtBuf.append("Greater_Or_Equal("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" >= ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.PLUS:
                //stmtBuf.append("Std_Integer_Fac.Sum("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" + ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.MINUS:
                //stmtBuf.append("Std_Integer_Fac.Difference("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" - ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.MULTIPLY:
                //stmtBuf.append("Std_Integer_Fac.Product("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" * ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.DIVIDE:
                //stmtBuf.append("Std_Integer_Fac.Quotient("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" / ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.REM:
                //stmtBuf.append("Std_Integer_Fac.Rem("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" % ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.MOD:
                //stmtBuf.append("Std_Integer_Fac.Mod("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" % ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.DIV:
                //stmtBuf.append("Std_Integer_Fac.Div("); 
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                appendJava(" / ", false);
                //stmtBuf.append(", ");
                visitProgramExp(((ProgramOpExp) pgmExp).getSecond());
                //stmtBuf.append(")");
                break;
            /*case ProgramOpExp.EXP:
              stmtBuf.append("Std_Integer_Fac.Power("); 
              visitProgramExp(((ProgramOpExp)pgmExp).getFirst());
              stmtBuf.append(", ");
              visitProgramExp(((ProgramOpExp)pgmExp).getSecond());
              stmtBuf.append(")");
              break;*/
            case ProgramOpExp.NOT:
                //stmtBuf.append("Std_Boolean_Fac.Not("); 
                appendJava("-", false);
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                //stmtBuf.append(")");
                break;
            case ProgramOpExp.UNARY_MINUS:
                //stmtBuf.append("Std_Integer_Fac.Negate("); 
                appendJava("-", false);
                visitProgramExp(((ProgramOpExp) pgmExp).getFirst());
                //stmtBuf.append(")");
                break;
            default:
                appendJava(Integer.toString(((ProgramOpExp) pgmExp)
                        .getOperator()), false);
                //stmtBuf.append(((ProgramOpExp)pgmExp).getOperator());
                break;
            }

        }
        else if (pgmExp instanceof ProgramParamExp) {}
        else {
            assert false;
        }
    }

    public void visitProgramFunctionExp(ProgramFunctionExp exp) {
        if (exp.getQualifier() != null) {
            stmtBuf.append(exp.getQualifier().toString());
            stmtBuf.append(".");
        }
        else {
            ProgramExpTypeResolver resolver =
                    new ProgramExpTypeResolver(table, myInstanceEnvironment);
            try {
                OperationEntry oper = resolver.getOperationEntry(exp);
                ModuleScope modScope = (ModuleScope) oper.getScope();
                FacilityDec fDec = modScope.getFacilityDec();
                // an enhancement can call ops on its concept for which a facility does not 
                // (yet) exist - jmh
                //buildCallQualifier(oper, fDec);
            }
            catch (TypeResolutionException trex) {
                System.out.println("TypeResolutionException");
                // do nothing - the  was already reported
            }
        }

        String callName = exp.getName().toString();
        if (isParmOp(callName)) {
            stmtBuf.append(callName);
            stmtBuf.append("Parm.");
        }
        /*if(callName.equals("Write")){
            appendJava("System.out.println(", false);
            List<ProgramExp> expList = exp.getArguments();
            Iterator<ProgramExp> expIter = expList.iterator();
            while (expIter.hasNext()) {           
                visitProgramExp(expIter.next());
                if (expIter.hasNext()) {
                    appendJava(", ", false);
                }
            }
            appendJava(")", false);
        }
        else if(callName.equals("Read")){
            appendJava("(new BufferedReader(new InputStreamReader(System.in))).readLine()", false);
        }*/
        //else{
        appendJava(callName, false);
        appendJava("(", false);
        List<ProgramExp> expList = exp.getArguments();
        Iterator<ProgramExp> expIter = expList.iterator();
        while (expIter.hasNext()) {
            visitProgramExp(expIter.next());
            if (expIter.hasNext()) {
                appendJava(", ", false);
            }
        }

        appendJava(")", false);
        //}
        /*stmtBuf.append(callName);
        stmtBuf.append("(");
        List<ProgramExp> expList = exp.getArguments();
        Iterator<ProgramExp> expIter = expList.iterator();
        while (expIter.hasNext()) {           
            visitProgramExp(expIter.next());
            if (expIter.hasNext()) {
                stmtBuf.append(", ");
            }
        }

        stmtBuf.append(")");*/
    }

    /*public void formImplClassDeclBuf(String typeName, String interfaceName, String extName, StringBuffer thisBuf) {
        thisBuf.append("\tclass ");
        thisBuf.append(typeName);
        if (extName != null) {
            thisBuf.append(" extends ");
        	thisBuf.append(extName);
        }
        thisBuf.append(" implements ");
        thisBuf.append(interfaceName);
        thisBuf.append(" {\n");

        thisBuf.append("\t\t");
        thisBuf.append(typeName);
        thisBuf.append("_Rep rep;\n\n");

        thisBuf.append("\t\t");
        thisBuf.append(typeName);
        thisBuf.append("() {\n");
        thisBuf.append("\t\t\trep = new ");
        thisBuf.append(typeName);
        thisBuf.append("_Rep();\n");
        thisBuf.append("\t\t}\n\n");

        thisBuf.append("\t\tpublic Object getRep() {\n");
        thisBuf.append("\t\t\treturn rep;\n");
        thisBuf.append("\t\t}\n\n");

        thisBuf.append("\t\tpublic void setRep(Object o) {\n");
        thisBuf.append("\t\t\trep = (");
        thisBuf.append(typeName);
        thisBuf.append("_Rep)o;\n");
        thisBuf.append("\t\t}\n\n");

        thisBuf.append("\t\tpublic RType initialValue() {\n");
        thisBuf.append("\t\t\treturn new ");
        thisBuf.append(typeName);
        thisBuf.append("();\n");
        thisBuf.append("\t\t}\n\n");

        thisBuf.append("\t\tpublic String toString() {\n");
        thisBuf.append("\t\t\treturn rep.toString();\n");
        thisBuf.append("\t\t}\n\n");

        thisBuf.append("\t}\n\n");
    }*/

    //side effects or depends on initBuf
    /*public void formRepClassDeclBuf(String typeName, RecordTy curTy, StringBuffer thisBuf) {
        List<VarDec> varList = ((RecordTy)curTy).getFields();
        
        thisBuf.append("\tclass ");
        thisBuf.append(typeName);
        thisBuf.append("_Rep {\n");
        
        recordVarDeclarations(varList, thisBuf);
        thisBuf.append("\n");

        thisBuf.append("\t\t");
        thisBuf.append(typeName);
        thisBuf.append("_Rep() {\n");
        recordVarInitialization(varList, thisBuf);
        if (initBuf.length() > 0) {
            thisBuf.append("\t\t\t{\n");
            thisBuf.append(initBuf.toString());
            thisBuf.append("\t\t\t}\n");
        }
        thisBuf.append("\t\t}\n");
        thisBuf.append("\n");

        thisBuf.append("\t\tpublic String toString() {\n");
        thisBuf.append("\t\t\tStringBuffer sb = new StringBuffer();\n");
        recordVartoString(varList, thisBuf);
        thisBuf.append("\t\t\treturn sb.toString();\n");
        thisBuf.append("\t\t}\n");
        thisBuf.append("\n");

        thisBuf.append("\t}\n\n");
    }*/

    /*public FacilityTypeDec facTypeDecforRepDec(RepresentationDec dec) {
    	FacilityTypeDec facDec = new FacilityTypeDec();
    	facDec.setName(dec.getName());
       	facDec.setRepresentation(dec.getRepresentation());
       	facDec.setConvention(dec.getConvention());
       	facDec.setInitialization(dec.getInitialization());
       	facDec.setFinalization(dec.getFinalization());
    	return facDec;
    }*/

    /*public boolean isLocalType (RepresentationDec dec) {
        //re-do;
        //essentially copied and added the following code from 
        //addTypeFamilyVariablesToScope from Populator.java
        boolean result = true;
        TypeEntry specentry = null;
        Symbol tsym = dec.getName().getSymbol();
        Iterator<ModuleID> i = table.getModuleScope().getSpecIterator();
        while (i.hasNext()) {
            ModuleScope scope = myInstanceEnvironment.getModuleScope(i.next());
            if (scope.containsLocalConcType(tsym)) {
                specentry = scope.getLocalType(tsym);
                result = false;
            }
        }
        return result;
    }*/

    /*public void visitRepresentationDec(RepresentationDec dec) {
    	StringBuffer localTypeBuf = new StringBuffer();
       	StringBuffer interfaceName = new StringBuffer();
       	
        if (isLocalType(dec)) {
        	visitFacilityTypeDec(facTypeDecforRepDec(dec));
        	// another global variable problem
        	// added the following to handle local array type declarations
        	typeBuf.append(facilityConstructorBuf.toString());
        } else {
            table.beginTypeScope();
            if (dec.getInitialization() != null) {
                visitInitItem(dec.getInitialization());
            }
            if (dec.getFinalization() != null) {
                visitFinalItem(dec.getFinalization());
            }
     
            String typeName = dec.getName().getSymbol().toString();
            exportedTypeNames.addUnique(typeName);
            interfaceName.append(curConceptPosSym.toString());
            interfaceName.append(".");
            interfaceName.append(typeName);
            formImplClassDeclBuf(typeName, interfaceName.toString(), null, localTypeBuf);
            Ty curTy = dec.getRepresentation();
            //Note that this only works if the type is a Record; there's no else clause
            if (curTy instanceof RecordTy) {
            	formRepClassDeclBuf(typeName, (RecordTy)curTy, localTypeBuf);
            } // end RecordTy
            typeBuf.append(localTypeBuf.toString());
            table.endTypeScope();
        }
    }*/

    //requires Type type = binding.getType(qualifier,name);
    //changed this to return a String instead of using a String parameter
    /*private String typeParamStr(Type type, PosSymbol qualifier, PosSymbol name) {
        StringBuffer thisBuf = new StringBuffer();
        
    	if (type instanceof ArrayType) {
        	ArrayType aType = (ArrayType)type;
            thisBuf.append(aType.getName().toString());
            thisBuf.append("_Array_Fac.createArray()");
        } else if (type instanceof RecordType) {
           	RecordType rType = (RecordType)type;
            thisBuf.append("new ");
            thisBuf.append((rType.getName()).toString());
            thisBuf.append("()");
          	} else if (type instanceof NameType) {
              NameType nType = (NameType)type;
              if (qualifier != null) {
                  thisBuf.append(qualifier.toString());
              } else {
                  if (nType == null) {
                    return thisBuf.toString();
                  }
                  if (nType.getFacility() != null) {
                      thisBuf.append(nType.getFacility().toString());
                  } else {
                      TypeName tName = nType.getProgramName();
                      thisBuf.append(tName.getFacilityQualifier().toString());
                  }
              }
              thisBuf.append(".create");
              thisBuf.append(name.toString());
              thisBuf.append("()");
         } else if (type instanceof FormalType) {
              FormalType fType = (FormalType)type;
              //Don't understand why getType is appended here; 
              // For one example, it seems to work without it.
              //thisBuf.append("getType");
              //thisBuf.append(fType.getName());
              //thisBuf.append("()");
    	            thisBuf.append(fType.getName().toString());
            } 
        return thisBuf.toString();
    }*/

    //requires the name is an array and type is an array type
    //resets stmtBuf
    /*private String formArrayDeclBuf(String VarName, ArrayTy arrayDec) {
     	StringBuffer thisBuf = new StringBuffer();
     	
    	thisBuf.append("\t\t");
        thisBuf.append(VarName);
        thisBuf.append("_Array_Fac = new Std_Array_Realiz(");
    
        NameTy arg = ((NameTy)arrayDec.getEntryType());
        Binding binding = table.getModuleScope().getBinding();
        Type type = binding.getType(arg.getQualifier(),arg.getName());
        thisBuf.append(typeParamStr(type, arg.getQualifier(), arg.getName()));
        thisBuf.append(", ");
        stmtBuf.setLength(0);
        visitProgramExp(arrayDec.getLo()); //side effects stmtBuf
        thisBuf.append(stmtBuf.toString());
        thisBuf.append(", ");
        stmtBuf.setLength(0);
        visitProgramExp(arrayDec.getHi());
        thisBuf.append(stmtBuf.toString());
        stmtBuf.setLength(0);
        thisBuf.append(");\n");
        
        return thisBuf.toString();
    }*/

    // this has been rewritten, reusing formVarDecType to handle all cases, in general
    // this used to be recordVarDeclarations
    /*private void recordVarDeclarations(List<VarDec> varList, StringBuffer thisTypeBuf) {
        Iterator<VarDec> i = varList.iterator();
        while (i.hasNext()) {
          thisTypeBuf.append("\t\t");
          formVarDecl(i.next(), thisTypeBuf);
        }
        // following code factored out and removed

    }*/

    // this part factored out for reuse; also used to handle communal variables
    /*private void formVarDecl(VarDec curVar, StringBuffer thisBuf)  {
        thisBuf.append(formVarDecType(curVar.getName(), curVar.getTy()));
        thisBuf.append(curVar.getName().toString());
        thisBuf.append(";\n");
    }*/

    /*private void recordVarInitialization(List<VarDec> varList, StringBuffer thisTypeBuf) {
        Iterator<VarDec> i = varList.iterator();
        while (i.hasNext()) {
          thisTypeBuf.append("\t\t\t");
          formVarInit(i.next(), thisTypeBuf);
          // following code has been factored out and removed
        }
    }*/

    // this part factored out for reuse; also used to handle communal variables
    private String formVarInit(VarDec curVar) {
        StringBuilder sb = new StringBuilder();
        sb.append(curVar.getName().toString());
        sb.append(" = ");
        sb.append(formNewVarDecl(curVar));
        return sb.toString();
    }

    public String formNewVarDecl(VarDec curVar) {
        Ty ty = curVar.getTy();
        String result = "null";
        if (ty instanceof NameTy) {
            String typeNameStr = ((NameTy) ty).getName().toString();
            if (typeNameStr.equals("Integer")) {
                result = "0";
            }
            else if (typeNameStr.equals("Boolean")) {
                result = "true";
            }
            else if (typeNameStr.equals("Char_Str")) {
                result = "\"\"";
            }
            else if (typeNameStr.equals("Character")) {
                result = "\"\"";
            }
        }
        return result;
    }

    /* private String getFacilityNameFromNameTy(NameTy nameTy) {
         String facName = null;
         Binding binding = table.getModuleScope().getBinding();
         Type type = binding.getType(nameTy.getQualifier(), nameTy.getName());
         if (type instanceof NameType) {
             NameType nType = (NameType)type;
             if (nType.getFacility() != null) {
                 facName = nType.getFacility().toString();
             } else {
                 TypeName tName = nType.getProgramName();
                 facName = tName.getFacilityQualifier().toString();
             }
         } else if (type instanceof ConcType) {
             facName = "nowwhat";
         }
         return facName;
     }*/

    /*private void recordVartoString(List<VarDec> varList, StringBuffer thisBuf) {
        Iterator<VarDec> i = varList.iterator();
        while (i.hasNext()) {
          thisBuf.append("\t\t\t");
          VarDec curVar = i.next();
          thisBuf.append("sb.append(");
          thisBuf.append(curVar.getName().toString());
          thisBuf.append(".toString());\n");
        }
    }*/

    // -----------------------------------------------------------
    // Operation Declarations
    // -----------------------------------------------------------

    // eliminated much of this code using visitProcedureDec instead
    public void visitFacilityOperationDec(FacilityOperationDec dec) {

        visitProcedureDec(procDecforFacOpDec(dec));

        String operName = dec.getName().toString();

        /*
        table.beginOperationScope();
        table.beginProcedureScope();
        table.bindProcedureTypeNames();

        stmtBuf.setLength(0);


         stmtBuf.append("\tpublic "); 
         Ty retTy = dec.getReturnTy();
         stmtBuf.append(retTypeString(retTy));
         stmtBuf.append(operName);
         stmtBuf.append("(");
         List<ParameterVarDec> varDecs = dec.getParameters();
         Iterator<ParameterVarDec> varIter = varDecs.iterator();
         while (varIter.hasNext()) {           
             stmtBuf.append(formParameterVarDec(varIter.next()));
             if (varIter.hasNext()) {
                 stmtBuf.append(", ");
             }
         }
         stmtBuf.append(") {\n");
         operBuf.append(stmtBuf.toString());

         if (retTy instanceof NameTy) {
             operBuf.append("\t\t");
        //           FacilityDec fDec = getFacility(((NameTy)retTy).getName().getSymbol());
             FacilityDec fDec = getFacility(((NameTy)retTy).getQualifier(), ((NameTy)retTy).getName());
             operBuf.append(fDec.getConceptName().toString());
             operBuf.append(".");
             String typeName = ((NameTy)retTy).getName().toString();
             operBuf.append(typeName);
             operBuf.append(" ");
             operBuf.append(dec.getName().toString());
             operBuf.append(" = ");
             Binding binding = table.getModuleScope().getBinding();
             NameType nType =
                 (NameType)binding.getType(((NameTy)retTy).getQualifier()
        ,
                                           ((NameTy)retTy).getName());
             if (nType.getFacility() != null) {
                 operBuf.append(nType.getFacility().toString());
             } else {
                 TypeName tName = nType.getProgramName();
                 operBuf.append(tName.getFacilityQualifier().toString());
             }
             operBuf.append(".create");
             operBuf.append(typeName);
             operBuf.append("();\n");
             operBuf.append("\n");
         }

         VarDec curVar;
         Iterator<VarDec> varIt = dec.getVariables().iterator();
         while (varIt.hasNext()) {
             curVar = varIt.next();
             stmtBuf.setLength(0);
             visitVarDec(curVar);
             operBuf.append(stmtBuf.toString());
         }
         operBuf.append("\n");

         List<Statement> statements = dec.getStatements();
         Iterator<Statement> stmtIter = statements.iterator();
         while (stmtIter.hasNext()) {           
             stmtBuf.setLength(0);// clear 
             visitStatement(stmtIter.next());
             operBuf.append(stmtBuf.toString());
         }

         if (retTy instanceof NameTy) {
             operBuf.append("\t\treturn ");
             operBuf.append(dec.getName().toString());
             operBuf.append(";\n");
         }

         operBuf.append("\t}\n\n");

         */
        if ("Main".equals(operName)) {
            operBuf.append("\tpublic static void main(String args[]) {\n");
            operBuf.append("\t\t");
            operBuf.append(curFacilityPosSym.toString());
            operBuf.append(" start = new ");
            operBuf.append(curFacilityPosSym.toString());
            operBuf.append("();\n");
            operBuf.append("\t\tstart.Main();\n");
            operBuf.append("\t}\n");
        }
        /*
         table.endProcedureScope();
         table.endOperationScope();
         */
    }

    /*public void visitConceptTypeDec(TypeDec dec) {
        table.beginTypeScope();

        if (dec.getInitialization() != null) {
            visitInitItem(dec.getInitialization());
        }
        if (dec.getFinalization() != null) {
            visitFinalItem(dec.getFinalization());
        }

        String typeName = dec.getName().getSymbol().toString();

        typeBuf.append("\tinterface ");
        typeBuf.append(typeName);
        typeBuf.append(" extends RType {\n");
        typeBuf.append("\t}\n\n");

        typeFuncBuf.append("\t");
        typeFuncBuf.append(typeName);
        typeFuncBuf.append(" create");
        typeFuncBuf.append(typeName);
        typeFuncBuf.append("();\n");
        table.endTypeScope();
    }*/

    public void visitOperationDec(OperationDec dec) {
        table.beginOperationScope();
        int line = dec.getName().getPos().getLine();
        boolean bumped = bumpLine(line);
        //operBuf.append("\t");

        //operBuf.append(retTypeString(dec.getReturnTy()));
        /*
        Ty retTy = dec.getReturnTy();
        if (retTy instanceof NameTy) {
            NameTy nameTy = (NameTy)retTy;
            FacilityDec fDec = getFacility(nameTy.getName().getSymbol());
            if (fDec != null) {
                operBuf.append(fDec.getConceptName().toString());
                operBuf.append(".");
                operBuf.append(((NameTy)retTy).getName().toString());
            } else {
                Binding binding = table.getModuleScope().getBinding();
                Type retType = binding.getType(nameTy.getQualifier(),
                                               nameTy.getName());
                operBuf.append(retType.getProgramName());
            }
            operBuf.append(" ");
        }
        else operBuf.append("void ");
         */
        String operName = dec.getName().toString();
        if (operName.equals("Main")) {
            indent();
            appendJava("public static void main(String[] args) {", bumped);
        }
        /*operBuf.append(dec.getName().toString());
        operBuf.append("(");

        List<ParameterVarDec> decs = dec.getParameters();
        Iterator<ParameterVarDec> i = decs.iterator();
        while (i.hasNext()) {           
            operBuf.append(formParameterVarDec(i.next()));
            if (i.hasNext()) {
                operBuf.append(", ");
            }
        }

        operBuf.append(");\n");*/
        table.endOperationScope();
    }

    /*public String formOperWrapper(OperationDec dec) {
        StringBuffer wrapBuf = new StringBuffer();
        wrapBuf.append("\tpublic ");
        wrapBuf.append(formOperTypeWrapper(dec.getReturnTy()));
        wrapBuf.append(" ");
        wrapBuf.append(dec.getName().toString());
        wrapBuf.append("(");

        String parm = null;
        StringBuffer parmBuf = new StringBuffer();
        List<ParameterVarDec> decs = dec.getParameters();
        Iterator<ParameterVarDec> i = decs.iterator();
        ParameterVarDec pVarDec = null;
        while (i.hasNext()) {           
            pVarDec = i.next();
            wrapBuf.append(formOperTypeWrapper(pVarDec.getTy()));
            wrapBuf.append(" ");
            wrapBuf.append(pVarDec.getName().toString());
            if (i.hasNext()) {
                wrapBuf.append(", ");
            }
            parmBuf.append(pVarDec.getName().toString());
            if (i.hasNext()) {
                parmBuf.append(", ");
            }
        }

        wrapBuf.append(") {\n");
        wrapBuf.append("\t\t");
        //	bug
    //       if (dec.getReturnTy() instanceof NameTy) { 
        if (dec.getReturnTy() != null) {
            wrapBuf.append("return ");
        }
        wrapBuf.append("con.");
        wrapBuf.append(dec.getName().toString());
        wrapBuf.append("(");
        wrapBuf.append(parmBuf.toString());
        wrapBuf.append(");\n");
        wrapBuf.append("\t}\n");

        return wrapBuf.toString(); 
    }

    String formOperTypeWrapper(Ty ty) {
        String typeStr = "RType";
        if (ty == null) {
            typeStr = "void";
        } else if (ty instanceof NameTy) {
            NameTy nameTy = (NameTy)ty;
            Type vnType = concBinding.getType(nameTy.getQualifier(),
                                              nameTy.getName());
            TypeName typeName = vnType.getProgramName();

            if (vnType instanceof ConcType) {
              typeStr = typeName.toString();
            } else if (vnType instanceof NameType) {
              FacilityDec fDec = getFacility((NameType)vnType);
              typeStr = fDec.getConceptName().toString();
              typeStr += ".";
              typeStr += ((NameType)vnType).getName().toString();
            }
        } else if (ty instanceof FunctionTy) {
    System.out.println("formOperTypeWrapper FunctionTy");
        } else if (ty instanceof ConstructedTy) {
    System.out.println("formOperTypeWrapper ConstructedTy");
        } else if (ty instanceof TupleTy) {
    System.out.println("formOperTypeWrapper TupleTy");
        } else if (ty instanceof RecordTy) {
    System.out.println("formOperTypeWrapper RecordTy");
        } else if (ty instanceof CartProdTy) {
    System.out.println("formOperTypeWrapper CartProdTy");
        } else if (ty instanceof ArrayTy) {
    System.out.println("formOperTypeWrapper ArrayTy");
        } else  {
    System.out.println("formOperTypeWrapper not found" + ty);
            typeStr = "formOperParmTypeWrapper no type found " + ty;
        }
        return typeStr;
    }*/

    /*public void visitEnhancementOperationDec(OperationDec dec) {
        table.beginOperationScope();
        operBuf.append("\t");
        
        operBuf.append(retTypeString(dec.getReturnTy()));

        operBuf.append(dec.getName().toString());
        operBuf.append("(");

        List<ParameterVarDec> decs = dec.getParameters();
        Iterator<ParameterVarDec> i = decs.iterator();
        while (i.hasNext()) {           
            operBuf.append(formParameterVarDec(i.next()));
            if (i.hasNext()) {
                operBuf.append(", ");
            }
        }

        operBuf.append(");\n");
        table.endOperationScope();
    }*/
    // ===========================================================
    // Public Methods - Non-declarative Constructs
    // ===========================================================

    /*public void visitFinalItem(FinalItem item) {
        table.beginOperationScope();
        table.beginProcedureScope();
        //FIX visitFacilityDecList(item.getFacilities());
        //FIX visitStatementList(item.getStatements());
        table.endProcedureScope();
        table.endOperationScope();
    }*/

    /*public void visitInitItem(InitItem item) {
        table.beginOperationScope();
        table.beginProcedureScope();
        isLocalVar = true;
        stmtBuf.setLength(0);
        initBuf.setLength(0);
        List<VarDec> vars = item.getVariables();
        recordVarDeclarations(vars, initBuf);
        recordVarInitialization(vars, initBuf);
        List<Statement> statements = item.getStatements();
        Iterator<Statement> stmtIt = statements.iterator();
        while (stmtIt.hasNext()) {
            stmtBuf.setLength(0);// clear 
            visitStatement(stmtIt.next());
            initBuf.append(stmtBuf.toString());
        }
        stmtBuf.setLength(0);
        isLocalVar = false;
        table.endProcedureScope();
        table.endOperationScope();
    }*/

    /* Murali's first attempt to make a change to the compiler and 
     * fix the problem with uses list that includes concept names. 
     *  Changed this so that fac. decl. is generated only for non-duplicate uses 
     *  clauses */
    public void visitUsesItem(UsesItem item) {

        //No changes here.
        ModuleID id = ModuleID.createFacilityID(item.getName());

        if (myInstanceEnvironment.contains(id)) {

            ModuleDec dec = myInstanceEnvironment.getModuleDec(id);
            if (dec instanceof ShortFacilityModuleDec) {
                ShortFacilityModuleDec sdec = (ShortFacilityModuleDec) (dec);
                FacilityDec fdec = sdec.getDec();
                PosSymbol cname = fdec.getConceptName();
                ModuleID cid = ModuleID.createConceptID(cname);
                // changed the code below so fac. decl. is generated only for non-duplicates
                String importStr = "";//formJavaImport(myInstanceEnvironment.getFile(cid));
                if (!checkImportDup(importStr)) {
                    if (!isInInterface) {
                        visitFacilityDec(sdec.getDec());
                    }
                    else {
                        usesItemBuf.append(importStr);
                        importList.addUnique(importStr);
                    }
                    //System.out.println(importStr);
                }
            }
        }
        //Added the part below.
        ModuleID cid = ModuleID.createConceptID(item.getName());
        if (myInstanceEnvironment.contains(cid)) {
            String importStr = "";//formJavaImport(myInstanceEnvironment.getFile(cid));
            if (!checkImportDup(importStr)) {
                usesItemBuf.append(importStr);
                importList.addUnique(importStr);
                //System.out.println(importStr);
            }
        }
    }

    //private String formJavaImport(File file) {

    //return "import " + formPkgPath(file) + ".*;\n";
    /*
     StringBuffer pkgPath = new StringBuffer();
     boolean pkgStart = false;

     StringToken stTok = 
     new StringTokenizer(file.getPath(), File.separator);
    
     curToken = "";
     while (!tokenStack.isEmpty() && curToken.get) {
     String curTok = stTok.nextToken();
     if (pkgStart) {
     if (stTok.hasMoreTokens()) { // stop at parent
     pkgPath.append(".");
     pkgPath.append(curTok);
     }
     }
     if ("RESOLVE".equals(curTok)) { 
     pkgPath.append("import RESOLVE");
     pkgStart = true; 
     }
     }
     if (pkgPath.length() > 0) {
     pkgPath.append(".*;\n");
     }
     return pkgPath.toString();*/
    //}

    /*private void visitConceptDecList(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof OperationDec) {
                visitOperationDec((OperationDec)dec);
            } else  if (dec instanceof TypeDec) {
                visitConceptTypeDec((TypeDec)dec);
            } else {
            }
        }
    }*/

    /*private void visitEnhancementDecList(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof OperationDec) {
                visitEnhancementOperationDec((OperationDec)dec);
            }
            if (dec instanceof TypeDec) {
                visitConceptTypeDec((TypeDec)dec);
            }
        }
    }*/

    /*public void visitTypeDec(TypeDec dec) {
        table.beginTypeScope();
        if (dec.getInitialization() != null) {
            visitInitItem(dec.getInitialization());
        }
        if (dec.getFinalization() != null) {
            visitFinalItem(dec.getFinalization());
        }
        table.endTypeScope();
    }*/

    /*private void formGetTypeMethods(List<String> typeParms) {
        String parmName = null;
        Iterator<String> typeIt = typeParms.iterator();
        while (typeIt.hasNext()) {
            parmName = typeIt.next();
            typeFuncBuf.append("\tpublic RType getType");
            typeFuncBuf.append(parmName);
            typeFuncBuf.append("();\n");
        }
    }*/

    /*private String formConceptTypesWrappers(ConceptModuleDec cDec) {
        List<Dec> decs = cDec.getDecs();
        StringBuffer wrappersBuf = new StringBuffer();
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof TypeDec) {
                String typeName = dec.getName().getSymbol().toString();
                wrappersBuf.append("\tpublic ");
                wrappersBuf.append(curConceptPosSym.toString());
                wrappersBuf.append(".");
                wrappersBuf.append(typeName);
                wrappersBuf.append(" create");
                wrappersBuf.append(typeName);
                wrappersBuf.append("() {\n");
                wrappersBuf.append("\t\treturn con.create");
                wrappersBuf.append(typeName);
                wrappersBuf.append("();\n");
                wrappersBuf.append("\t}\n\n");
            }
        }
        wrappersBuf.append("\tpublic void swap(RType r1, RType r2) {\n");
        wrappersBuf.append("\t\tcon.swap(r1, r2);\n");
        wrappersBuf.append("\t}\n\n");

        wrappersBuf.append("\tpublic void assign(RType r1, RType r2) {\n");
        wrappersBuf.append("\t\tcon.assign(r1, r2);\n");
        wrappersBuf.append("\t}\n\n");

        ModuleID cid = ModuleID.createConceptID(cDec.getName());
        List<String>typeParms = getTypeParms(cid);
        String parmName = null;
        Iterator<String> typeIt = typeParms.iterator();
        while (typeIt.hasNext()) {
            parmName = typeIt.next();
            wrappersBuf.append("\tpublic RType getType");
            wrappersBuf.append(parmName);
            wrappersBuf.append("() {\n");
            wrappersBuf.append("\t\treturn con.getType");
            wrappersBuf.append(parmName);
            wrappersBuf.append("();\n");
            wrappersBuf.append("\t}\n");
        }

        List<ModuleParameter> parms = cDec.getParameters();
        if (parms != null) {
            Iterator<ModuleParameter> mpIt = parms.iterator();
            ModuleParameter mp = null;
            while (mpIt.hasNext()) {
                mp = mpIt.next();
                if (mp instanceof ConstantParamDec) {
                    ConstantParamDec cp = (ConstantParamDec)mp;
                    NameTy nameTy = (NameTy)cp.getTy();
                    FacilityDec fDec = 
                        getFacility(nameTy.getQualifier(), nameTy.getName());
                    wrappersBuf.append("\n");
                    wrappersBuf.append("\tpublic ");
                    wrappersBuf.append(fDec.getConceptName());
                    wrappersBuf.append(".");
                    wrappersBuf.append(nameTy.getName());
                    wrappersBuf.append(" get");
                    wrappersBuf.append(cp.getName().toString());
                    wrappersBuf.append("() {\n");
                    wrappersBuf.append("\t\treturn con.get");
                    wrappersBuf.append(cp.getName().toString());
                    wrappersBuf.append("();\n");
                    wrappersBuf.append("\t}\n");
                }
            }
        }

        return wrappersBuf.toString();
    }*/

    // changed this so that the proxy procedures now have the extra concept paramaters
    // need for enhancements
    // though this reuses some code, the later part of it contains duplication; re-code
    /*private String formProxyProcedures(ConceptModuleDec cDec, EnhancementBodyModuleDec ebDec) {
        StringBuffer pBuf = new StringBuffer();
        pBuf.append("\n\tpublic Object invoke(Object proxy, Method method, Object[] args) throws Throwable {\n");
        boolean isFirst = true;
        Iterator<Dec> i = ebDec.getDecs().iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof ProcedureDec) {
                if (isFirst) {
                    pBuf.append("\t\tif (\"");
                    isFirst = false;
                } else {
                    pBuf.append("\t\telse if (\"");
                } 
                pBuf.append(((ProcedureDec)dec).getName());
                pBuf.append("\".equals(method.getName())) {\n");
                pBuf.append("\t\t\treturn method.invoke(this, args);\n");
                pBuf.append("\t\t}\n");
            }
        } 

        if (!isFirst) { // if no procedures don't write else clause
            pBuf.append("\t\telse {\n");
            pBuf.append("\t\t\treturn method.invoke(con, args);\n");
            pBuf.append("\t\t}\n");
        }            

        pBuf.append("\t}\n");

        pBuf.append("\n\tpublic static ");
        pBuf.append(ebDec.getConceptName().toString());
        pBuf.append(" createProxy(");
        
        pBuf.append(visitModuleParameterList(cDec.getParameters(), ",", ","));
        if (!ebDec.getParameters().isEmpty()) {
        	pBuf.append(visitModuleParameterList(ebDec.getParameters(), ",", ","));
        };
        pBuf.append(" ");
        // following code factored out and removed
    
        pBuf.append(ebDec.getConceptName().toString());
        pBuf.append(" toWrap) {\n");
        pBuf.append("\t\t");
        pBuf.append(ebDec.getName().toString());
        pBuf.append(" eObj = new ");
        pBuf.append(ebDec.getName().toString());
        pBuf.append("(");
        
        // added this loop to print out parameters for the concept that corresponds to the enhancement
        Iterator<ModuleParameter> cpIt = cDec.getParameters().iterator();
        ModuleParameter modParm;
        while (cpIt.hasNext()) {
            modParm = cpIt.next();
            pBuf.append(((Dec)modParm).getName().toString());
            pBuf.append(", ");
         }
        
        Iterator<ModuleParameter> bpIt = ebDec.getParameters().iterator();
        while (bpIt.hasNext()) {
           modParm = bpIt.next();
           if (modParm instanceof OperationDec) {
             OperationDec opDec = (OperationDec) modParm;
             pBuf.append(opDec.getName().toString());
             pBuf.append("Parm, ");
           } else {
             System.out.println("Error expecting OperationDec found: " + modParm.asString(1,1));
           }
        }
        
        pBuf.append("toWrap);\n");
        pBuf.append("\t\tClass[] toWrapInterfaces = toWrap.getClass().getInterfaces();\n");
        pBuf.append("\t\tClass[] thisInterfaces = new Class[toWrapInterfaces.length+1];\n");
        pBuf.append("\t\tClass[] tmpInterfaces = eObj.getClass().getInterfaces();\n");
        pBuf.append("\t\tthisInterfaces[0] = tmpInterfaces[0];\n");
        pBuf.append("\t\tSystem.arraycopy(toWrapInterfaces, 0, thisInterfaces, 1, toWrapInterfaces.length);\n");
        pBuf.append("\t\treturn (");
        pBuf.append(ebDec.getConceptName().toString());
        pBuf.append(")(Proxy.newProxyInstance(");
        pBuf.append(ebDec.getConceptName().toString());
        pBuf.append(".class.getClassLoader(),thisInterfaces, eObj));\n");
        pBuf.append("\t}\n");

        return pBuf.toString();
    }*/

    /*private String formCommunalVarDecs(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        StringBuffer thisBuf = new StringBuffer();
        
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof VarDec) {
            	formVarDecl((VarDec)dec, thisBuf);
            }
        }
        return thisBuf.toString();
    }*/

    /*private String formCommunalVarInits(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        StringBuffer thisBuf = new StringBuffer();
        
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof VarDec) {
            	formVarInit((VarDec)dec, thisBuf);
            }
        }
        return thisBuf.toString();
    }*/

    /*private void visitTypes(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof RepresentationDec) {
                visitRepresentationDec((RepresentationDec)dec);
            }
        }
    }*/

    // created this for use in compiling enhancement bodies that declare variables 
    // of the type provided by the concept
    // not straightforward to get rid of global exportedTypeNames
    /*private void createExportedTypeNames(ConceptModuleDec cDec) {
    	List<Dec> decs = cDec.getDecs();
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof TypeDec) 
                	exportedTypeNames.addUnique(((TypeDec)dec).getName().getSymbol().toString());
        }
    }*/

    // Removed the need for using instead visitProcedures
    /*
    private void visitOpProcDecs(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof FacilityOperationDec) {
                visitFacilityOperationDec((FacilityOperationDec)dec);
            }
        }
    }
     */

    //changed this to output a public interface operation for only exported types
    /*private String visitCBTypeProcedures(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        StringBuffer thisBuf = new StringBuffer();
        
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof RepresentationDec) {
            	if (!isLocalType((RepresentationDec)dec))
                	thisBuf.append(visitTypeProc((RepresentationDec)dec));
            }
        }
        return thisBuf.toString();
    }*/

    /*private String visitTypeProc(RepresentationDec dec) {
        StringBuffer thisBuf = new StringBuffer();
        
        thisBuf.append("\tpublic ");
        thisBuf.append(curConceptPosSym.toString());
        thisBuf.append(".");
        thisBuf.append(dec.getName().toString());
        thisBuf.append(" create");
        thisBuf.append(dec.getName().toString());
        thisBuf.append("() {\n");
        thisBuf.append("\t\treturn new ");
        thisBuf.append(dec.getName().toString());
        thisBuf.append("();\n");
        thisBuf.append("\t}\n\n");
        
        return thisBuf.toString();
    }*/

    public ProcedureDec procDecforFacOpDec(FacilityOperationDec dec) {
        ProcedureDec procDec = new ProcedureDec();

        procDec.setName(dec.getName());
        procDec.setParameters(dec.getParameters());
        procDec.setAuxVariables(dec.getAuxVariables());
        procDec.setDecreasing(dec.getDecreasing());
        procDec.setFacilities(dec.getFacilities());
        procDec.setReturnTy(dec.getReturnTy());
        procDec.setStatements(dec.getStatements());
        procDec.setStateVars(dec.getStateVars());
        procDec.setVariables(dec.getVariables());

        return procDec;
    }

    // unifying procedure handling in concept and enhancement bodies
    private void visitProcedures(List<Dec> decs) {
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof ProcedureDec) {
                visitProcedureDec((ProcedureDec) dec);
            }
            else if (dec instanceof FacilityOperationDec) {
                visitProcedureDec(procDecforFacOpDec((FacilityOperationDec) dec));
            }
        }
    }

    //reusing visitProcedures instead; visitEBProcedures removed.

    //formJavaConstructorsParms operation is no longer used and has been removed.

    /*private String formJavaConstructorsAssign(List<ModuleParameter> parameters) {
        StringBuffer caBuf = new StringBuffer();
        Iterator<ModuleParameter> parmIt = parameters.iterator();

        while (parmIt.hasNext()) {
            Dec dec = (Dec)parmIt.next();
            if (dec instanceof ConceptTypeParamDec) {
                caBuf.append("\t\tthis.");
                caBuf.append(dec.getName().toString());
                caBuf.append(" = ");
                caBuf.append(dec.getName().toString());
                caBuf.append(";\n");
            } else if (dec instanceof ConstantParamDec) {
                ConstantParamDec vdec = (ConstantParamDec)dec;
                Ty ty = vdec.getTy();
                if (ty instanceof NameTy) {
                    caBuf.append("\t\tthis.");
                    caBuf.append(vdec.getName().toString());
                    caBuf.append(" = ");
                    caBuf.append(vdec.getName().toString());
                    caBuf.append(";\n");
                } else{}
            } else if (dec instanceof OperationDec) {
                OperationDec odec = (OperationDec)dec;
                caBuf.append("\t\tthis.");
                caBuf.append(odec.getName());
                caBuf.append("Parm");
                caBuf.append(" = ");
                caBuf.append(odec.getName());
                caBuf.append("Parm;\n");
            } else {
            // math types don't affect executable output - skip them
            }
        }
        return caBuf.toString();
    }*/

    //factored this our to reuse in enhancement body and concept body constructors
    //side effects and resets stmtBuf
    /*private String declNewFacilities (List<Dec> facDecList) {
        StringBuffer thisBuf = new StringBuffer();
        stmtBuf.setLength(0);
        
        Dec facDec;
        Iterator<Dec> facDecIt = facDecList.iterator();
        while (facDecIt.hasNext()) {
            facDec = facDecIt.next();
            if (facDec instanceof FacilityDec) {
            	thisBuf.append("\t");
            	declNewFacility((FacilityDec)facDec);
            }
        }
        thisBuf.append(stmtBuf.toString());
        stmtBuf.setLength(0); //this is new addition; does it hose up anything?
        return thisBuf.toString();
    }*/

    //simplified the code; remove aliasing by removing two parameter list arguments 
    //removed parmOpList as an argument
    //reuses visitModuleParameterList eliminating the need for formJavaConstructorsParms
    /*private String formJavaConstructors(String cbName, 
                               List<ModuleParameter> conceptParameters,
                               List<ModuleParameter> cBodyParameters,
                               List<Dec> decList) {


        StringBuffer consBuf = new StringBuffer();

        consBuf.append("\tpublic ");
        consBuf.append(cbName);
        consBuf.append("(");

        consBuf.append(visitModuleParameterList(conceptParameters, ",", ""));
        if (!cBodyParameters.isEmpty()) {
        	consBuf.append(",");
        	consBuf.append(visitModuleParameterList(cBodyParameters, ",", ""));
        };
    
        consBuf.append(") {\n");
        consBuf.append(formJavaConstructorBody(conceptParameters, cBodyParameters, decList));
        consBuf.append("\t}\n\n");
        return consBuf.toString();
    }*/

    // factored this out for reuse in realization and enhancement body constructors
    // uses, but does not change global consInitBuf
    /*private String formJavaConstructorBody(List<ModuleParameter> conceptParameters,
            List<ModuleParameter> cBodyParameters, List<Dec> decList) {
        StringBuffer consBodyBuf = new StringBuffer();
        
        consBodyBuf.append(formJavaConstructorsAssign(conceptParameters));
        consBodyBuf.append(formJavaConstructorsAssign(cBodyParameters));
        
        //the following code adds new facility creations to the constructor
        //may work for only unenhanced or singly-enhanced facilities
        //Note: stmtBuf has a value here, so it has to be reset here to avoid appending to consBuf the same info. twice; 
        //Since stmtBuf is global, hopefully, resetting it below won't mess up something else.
        //Though stmtBuf has been used all over the place, it'll be nice to get rid of these globals.
        // More global problems; stmtBuf is appended to consInitBuf, another global, elsewhere.
        
        consBodyBuf.append(consInitBuf.toString());
        consBodyBuf.append(declNewFacilities(decList));
        consBodyBuf.append(formCommunalVarInits(decList));
        return consBodyBuf.toString();
    }*/

    // formJavaParmOpVar operation is no longer used and has been removed.

    // formJavaParmOpParm operation is no longer used and has been removed.

    // formJavaParmOpAssign operation is no longer used and has been removed.

    // improved it to handle facilities in enhancement bodies
    //this is similar, but unfortunately not identical to the ones for realization bodies
    //unified with realization body as far as possible, but there's some duplication
    /*private String formJavaEnhancementConstructors(String enhancementName, 
                                                   String conceptName,
                                                   List<ModuleParameter> conceptParameters,
                                                   List<ModuleParameter> cBodyParameters,
                                                   List<Dec> decList) {
        StringBuffer consBuf = new StringBuffer();
        consBuf.append("\n");

        // the following statement made unnecessary due to its handling elsewhere
    //       consBuf.append(formJavaParmOpVar(parmOpList));

        consBuf.append("\t");
        consBuf.append(conceptName);
        consBuf.append(" con;\n");
        consBuf.append("\tpublic ");
        consBuf.append(enhancementName);
        consBuf.append("(");

        consBuf.append(visitModuleParameterList(conceptParameters, ",", ","));
        if (!cBodyParameters.isEmpty()) {
        	consBuf.append(visitModuleParameterList(cBodyParameters, ",", ","));
        };
        consBuf.append(" ");
        consBuf.append(conceptName);
        consBuf.append(" con");
        consBuf.append(") {\n");

        consBuf.append(formJavaConstructorBody(conceptParameters, cBodyParameters, decList));
        consBuf.append("this.con = con;");	//Don't know when this got lost?
        consBuf.append("\t}\n\n");
        return consBuf.toString();
    }*/

    /*private String formConceptProcWrappers(ConceptModuleDec cDec) {
        StringBuffer wrappersBuf = new StringBuffer();
        List<Dec> decs = cDec.getDecs();
        Iterator<Dec> i = decs.iterator();
        while (i.hasNext()) {
            Dec dec = i.next();
            if (dec instanceof OperationDec) {
                wrappersBuf.append(formOperWrapper((OperationDec)dec));
                wrappersBuf.append("\n");
            }
        }
        return wrappersBuf.toString();
    }*/

    //factored this out for reuse in visitModuleParameterList and in constructor parameter generation
    /*private String ModuleParameterStr(Dec dec) {
      	StringBuffer thisBuf = new StringBuffer();
      	
        if (dec instanceof ConceptTypeParamDec) {
            thisBuf.append(" RType ");
            thisBuf.append(dec.getName().toString());
        } else if (dec instanceof ConstantParamDec) {
            ConstantParamDec vdec = (ConstantParamDec)dec;
            String str = vdec.getName().toString();
            Ty ty = vdec.getTy();
            if (ty instanceof NameTy) {
                thisBuf.append(" ");
    //               FacilityDec fDec = getFacility(((NameTy)ty).getName().getSymbol());
                FacilityDec fDec = getFacility(((NameTy)ty).getQualifier(), ((NameTy)ty).getName());
                thisBuf.append(fDec.getConceptName().toString());
                thisBuf.append(".");
                String typeName = ((NameTy)ty).getName().toString();
                thisBuf.append(typeName);
                thisBuf.append(" ");
                thisBuf.append(vdec.getName().toString());
            }
        } else if (dec instanceof OperationDec) {
                thisBuf.append(" ");
                thisBuf.append(dec.getName());
                thisBuf.append(" ");
                thisBuf.append(dec.getName());
                thisBuf.append("Parm");
        } 
        return thisBuf.toString();
    }*/

    // modified to return the generated string instead of directly adding it to global headerBuf
    /*private String visitModuleParameterList(List<ModuleParameter> parameters, String separator, String lastSeparator) {
        Iterator<ModuleParameter> i = parameters.iterator();
       	StringBuffer thisBuf = new StringBuffer();
       	boolean isFirstParam = true;
       	Dec dec;
       	
        while (i.hasNext()) {
            dec = (Dec)i.next();
            if (!(dec instanceof DefinitionDec)) {
            	if (isFirstParam)
            		isFirstParam = false;
            	else 
            		thisBuf.append(separator);
                thisBuf.append(ModuleParameterStr(dec)); 
            }
        }
    	if (!isFirstParam)
    		thisBuf.append(lastSeparator);
        return thisBuf.toString();
    }*/

    // unfortunately this side effects paramOpBuf through a call to visitModuleParameterOpDec
    /*private String generateOPParamInterfaces(List<ModuleParameter> parameters) {
        Iterator<ModuleParameter> i = parameters.iterator();
       	StringBuffer thisBuf = new StringBuffer();
       	Dec dec;
       	
        while (i.hasNext()) {
            dec = (Dec)i.next();
            if (dec instanceof OperationDec) 
            	thisBuf.append(formModuleParameterOpDec((OperationDec)dec));
        }
        return thisBuf.toString();
    }*/

    private void visitUsesItemList(List<UsesItem> items) {

        Iterator<UsesItem> i = items.iterator();
        while (i.hasNext()) {
            visitUsesItem(i.next());
        }
    }

    private String getMainFile() {
        File file = myInstanceEnvironment.getTargetFile();
        ModuleID cid = myInstanceEnvironment.getModuleID(file);
        file = myInstanceEnvironment.getFile(cid);
        String filename = file.toString();
        int temp = filename.indexOf(".");
        String tempfile = filename.substring(0, temp);
        String mainFileName = tempfile + ".java";
        return mainFileName;
    }

    private boolean checkImportDup(String importStr) {
        return importList.contains(importStr);
    }

    private String castLookUp(String varName) {
        if (castLookUpMap.containsKey(varName)) {
            return castLookUpMap.get(varName);
        }
        else {
            return varName;
        }
    }

    public void outputJavaCode(File outputFile) {
        //Assume files have already been translated
        if (!myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB)
                || myInstanceEnvironment.flags.isFlagSet(Archiver.FLAG_ARCHIVE)) {
            //outputAsFile(targetFileName, getMainBuffer());
            //outputAsFile(outputFile.getAbsolutePath(), getMainBuffer());
            System.out.println(getMainBuffer());
        }
        else {
            outputToReport(getMainBuffer());
        }
        //outputAsFile(getMainFileName(), getMainBuffer());
    }

    private void outputAsFile(String fileName, String fileContents) {
        String[] temp = fileName.split("\\.");
        fileName = temp[0] + ".java";
        if (fileContents != null && fileContents.length() > 0) {
            try {
                File outputJavaFile = new File(fileName);
                if (!outputJavaFile.exists()) {
                    outputJavaFile.createNewFile();
                }
                byte buf[] = fileContents.getBytes();
                OutputStream outFile = new FileOutputStream(outputJavaFile);
                outFile.write(buf);
                outFile.close();
                //System.out.println(fileContents);
                //System.out.println("Writing file: "+fileName);
            }
            catch (IOException ex) {
                //FIX: Something should be done with this exception
                //System.out.println(fileName+" : "+ex);
                ;
            }
        }
        else {
            System.out.println("No translation available for " + fileName);
        }
    }

    private void outputToReport(String fileContents) {
        CompileReport report = myInstanceEnvironment.getCompileReport();
        report.setTranslateSuccess();
        report.setOutput(fileContents);
    }

    private List<String> getTypeParms(ModuleID cid) {
        List<String> typeParms = new List<String>();
        ConceptModuleDec cDec =
                (ConceptModuleDec) myInstanceEnvironment.getModuleDec(cid);
        List<ModuleParameterDec> mpList = cDec.getParameters();
        Iterator<ModuleParameterDec> mpIt = mpList.iterator();
        ModuleParameterDec md = null;
        Dec mp = null;
        while (mpIt.hasNext()) {
            md = mpIt.next();
            mp = md.getWrappedDec();
            if (mp instanceof ConceptTypeParamDec) {
                typeParms.addUnique(((ConceptTypeParamDec) mp).getName()
                        .toString());
            }
        }
        return typeParms;
    }

    private List<String> getConcParms(ModuleID cid) {
        List<String> concParms = new List<String>();
        ConceptModuleDec cDec =
                (ConceptModuleDec) myInstanceEnvironment.getModuleDec(cid);
        List<ModuleParameterDec> mpList = cDec.getParameters();
        Iterator<ModuleParameterDec> mpIt = mpList.iterator();
        ModuleParameterDec md = null;
        Dec mp = null;
        while (mpIt.hasNext()) {
            md = mpIt.next();
            mp = md.getWrappedDec();
            if (mp instanceof ConstantParamDec) {
                concParms.addUnique(((ConstantParamDec) mp).getName()
                        .toString());
            }
        }
        return concParms;
    }

    private boolean isTypeParm(String name) {
        if (typeParms == null) {
            return false;
        }
        return typeParms.contains(name);
    }

    private boolean isParmOp(String name) {
        if (parmOpList == null) {
            return false;
        }
        return parmOpList.contains(name);
    }

    private boolean isConcParm(String name) {
        if (concParms == null) {
            return false;
        }
        return concParms.contains(name);
    }

    private FacilityDec getFacility(NameType nameType) {
        return getFacility(null, nameType.getName().getSymbol());
    }

    //removed all uses of the following version of getFacility with the next one 
    // that seems to take into account types qualified with facilities properly
    /*
    private FacilityDec getFacility(Symbol sym) {
        FacilityDec fDec = null;
        ModuleScope modScope = getTypeModuleScope(null, sym);
        if (modScope != null) {
            fDec = modScope.getFacilityDec();
        }
        return fDec;
    }
     */
    private FacilityDec getFacility(PosSymbol qual, PosSymbol name) {
        Symbol qualSym = null;
        if (qual != null) {
            qualSym = qual.getSymbol();
        }
        return getFacility(qualSym, name.getSymbol());
    }

    private FacilityDec getFacility(Symbol qual, Symbol name) {
        FacilityDec fDec = null;
        ModuleScope modScope = getTypeModuleScope(qual, name);
        if (modScope != null) {
            fDec = modScope.getFacilityDec();
        }
        return fDec;
    }

    // the following version is apparently unused
    /*
    private FacilityDec getFacility(VarDec curVar) {
        FacilityDec fDec = null;
        NameTy nameTy = (NameTy)curVar.getTy();
        Symbol qual = null;
        if (nameTy.getQualifier() != null)  {
            qual = nameTy.getQualifier().getSymbol();
        }
        ModuleScope modScope = getTypeModuleScope(qual,
                                        nameTy.getName().getSymbol());
        if (modScope != null) {
            fDec = modScope.getFacilityDec();
        }
        return fDec;
    }
     */

    private ModuleScope getTypeModuleScope(Symbol qual, Symbol name) {
        ModuleScope modScope = null;
        TypeEntry typeEntry = null;
        TypeLocator typeLoc = null;
        TypeID typeId = new TypeID(qual, name, 0);
        if (table.getCurrentScope() instanceof OperationScope) {
            try {
                typeLoc =
                        new TypeLocator(table.getModuleScope(),
                                myInstanceEnvironment);
                typeEntry = typeLoc.locateProgramType(typeId);
            }
            catch (SymbolSearchException ex) {
                //System.out.println("have SymbolSearchException");
            }
            ;
        }
        else if (table.getCurrentScope() instanceof TypeScope) {
            try {
                typeLoc =
                        new TypeLocator(table.getModuleScope(),
                                myInstanceEnvironment);
                typeEntry = typeLoc.locateProgramType(typeId);
            }
            catch (SymbolSearchException ex) {
                //System.out.println("have SymbolSearchException");
            }
            ;
        }
        else {
            try {
                typeLoc =
                        new TypeLocator(table.getCurrentScope(),
                                myInstanceEnvironment);
                typeEntry = typeLoc.locateProgramType(typeId);
            }
            catch (SymbolSearchException ex) {
                //System.out.println("have SymbolSearchException");
            }
            ;
        }
        if (typeEntry != null) {
            modScope = (ModuleScope) typeEntry.getScope();
        }
        return modScope;
    }

    private TypeEntry getTypeEntry(PosSymbol qual, PosSymbol name) {
        Symbol qualSym = null;
        if (qual != null) {
            qualSym = qual.getSymbol();
        }
        return getTypeEntry(qualSym, name.getSymbol());
    }

    private TypeEntry getTypeEntry(Symbol qual, Symbol name) {
        ModuleScope modScope = null;
        TypeEntry typeEntry = null;
        TypeLocator typeLoc = null;
        TypeID typeId = new TypeID(qual, name, 0);
        if (table.getCurrentScope() instanceof OperationScope) {
            try {
                typeLoc =
                        new TypeLocator(table.getModuleScope(),
                                myInstanceEnvironment);
                typeEntry = typeLoc.locateProgramType(typeId);
            }
            catch (SymbolSearchException ex) {
                //System.out.println("have SymbolSearchException");
            }
            ;
        }
        else if (table.getCurrentScope() instanceof TypeScope) {
            try {
                typeLoc =
                        new TypeLocator(table.getModuleScope(),
                                myInstanceEnvironment);
                typeEntry = typeLoc.locateProgramType(typeId);
            }
            catch (SymbolSearchException ex) {
                //System.out.println("have SymbolSearchException");
            }
            ;
        }
        else {
            try {
                typeLoc =
                        new TypeLocator(table.getCurrentScope(),
                                myInstanceEnvironment);
                typeEntry = typeLoc.locateProgramType(typeId);
            }
            catch (SymbolSearchException ex) {
                //System.out.println("have SymbolSearchException");
            }
            ;
        }
        return typeEntry;
    }

    private void formGetModuleParmInterface(List<ModuleParameter> parms) {
        Iterator<ModuleParameter> mpIt = parms.iterator();
        ModuleParameter mp = null;
        while (mpIt.hasNext()) {
            mp = mpIt.next();
            if (mp instanceof ConstantParamDec) {
                ConstantParamDec cp = (ConstantParamDec) mp;
                NameTy nameTy = (NameTy) cp.getTy();
                FacilityDec fDec =
                        getFacility(nameTy.getQualifier(), nameTy.getName());
                typeFuncBuf.append("\tpublic ");
                typeFuncBuf.append(fDec.getConceptName());
                typeFuncBuf.append(".");
                typeFuncBuf.append(nameTy.getName());
                typeFuncBuf.append(" get");
                typeFuncBuf.append(cp.getName().toString());
                typeFuncBuf.append("();\n");
            }
        }
    }

    // removed the use of global operBuf
    private String formGetModuleParm(List<ModuleParameter> parms) {
        Iterator<ModuleParameter> mpIt = parms.iterator();
        ModuleParameter mp = null;
        StringBuffer thisBuf = new StringBuffer();

        while (mpIt.hasNext()) {
            mp = mpIt.next();
            if (mp instanceof ConstantParamDec) {
                ConstantParamDec cp = (ConstantParamDec) mp;
                NameTy nameTy = (NameTy) cp.getTy();
                FacilityDec fDec =
                        getFacility(nameTy.getQualifier(), nameTy.getName());
                thisBuf.append("\n");
                thisBuf.append("\tpublic ");
                thisBuf.append(fDec.getConceptName());
                thisBuf.append(".");
                thisBuf.append(nameTy.getName());
                thisBuf.append(" get");
                thisBuf.append(cp.getName().toString());
                thisBuf.append("() {\n");
                thisBuf.append("\t\treturn ");
                thisBuf.append(cp.getName().toString());
                thisBuf.append(";\n");
                thisBuf.append("\t}\n");
            }
        }
        return thisBuf.toString();
    }

    private Type getVarType(VarDec varDec) {
        NameTy nameTy = (NameTy) varDec.getTy();
        Binding binding = table.getModuleScope().getBinding();
        Type type = binding.getType(nameTy.getQualifier(), nameTy.getName());
        return type;
    }

    private void idType(Type type) {
        if (type instanceof ConcType) {
            System.out.println("have ConcType ");
        }
        else if (type instanceof FormalType) {
            System.out.println("have FormalType ");
        }
        else if (type instanceof IndirectType) {
            System.out.println("have IndirectType ");
        }
        else if (type instanceof NameType) {
            System.out.println("have NameType ");
        }
        else if (type instanceof MathFormalType) {
            System.out.println("have MathFormalType ");
        }
        else if (type instanceof ArrayType) {
            System.out.println("have ArrayType ");
        }
        else if (type instanceof ConstructedType) {
            System.out.println("have ConstructedType ");
        }
        else if (type instanceof FieldItem) {
            System.out.println("have FieldItem ");
        }
        else if (type instanceof FunctionType) {
            System.out.println("have FunctionType ");
        }
        else if (type instanceof PrimitiveType) {
            System.out.println("have PrimativeType ");
        }
        else if (type instanceof RecordType) {
            System.out.println("have RecordType ");
        }
        else if (type instanceof TupleType) {
            System.out.println("have TupleType ");
        }
        else if (type instanceof VoidType) {
            System.out.println("have VoidType ");
        }
        else {
            System.out.println("unknown");
        }
    }

    /**
     * Method to check if the file needs to be compiled by javac
     * (rules out math files, etc that the compiler needs)
     * 
     * @param file the File that the compiler is currently attempting to translate
     * @return true if the File needs to be translated
     */
    public boolean needToTranslate(File file) {
        boolean translate = false;
        String inFile = file.toString();
        String[] temp = inFile.split("\\.");
        String ext = temp[temp.length - 1];
        if (!onNoCompileList(file)) {
            if (ext.equals("co") || ext.equals("rb") || ext.equals("en")
                    || ext.equals("fa")) {
                String javaName = modifyString(inFile, "\\." + ext, ".java");
                File javaFile = new File(javaName);
                //addFileToArchive(javaFile);
                if (!javaFile.exists() || sourceNewerThan(file, javaFile)) {
                    translate = true;
                }
                //else if(myInstanceEnvironment.flags.isFlagSet(FLAG_TRANSLATE_CLEAN)){
                //translate = true;
                //}
            }
            /*if(myInstanceEnvironment.flags.isFlagSet(FLAG_TRANSLATE_CLEAN)){
            	translate = true;
            }*/
        }
        return translate;
    }

    /**
     * Method to check the to see if the File is on the list of files not to translate
     * 
     * @param file the File to check
     * @return true if the file should not be translated
     */
    public boolean onNoCompileList(File file) {
        Pattern p = null;
        String fileName = file.toString();
        for (String s : noTranslate) {
            p = Pattern.compile(s);
            if (p.matcher(fileName).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that compares when a RESOLVE source file and the Java output file of the
     * same name were last modified to determine if the RESOLVE file needs to be
     * translated again
     * 
     * @param a the RESOLVE file that wants to be translated
     * @param b the existing Java file associated with the RESOLVE file
     * @return true if the RESOLVE file has been modified more recently than the Java file
     */
    private boolean sourceNewerThan(File a, File b) {
        if (a.lastModified() > b.lastModified()) {
            return true;
        }
        return false;
    }

    /**
     * Method that uses a regex to modify a String
     * 
     * @param src source String to check
     * @param find target that needs to be replaced
     * @param replace String to add in place of the target
     * @return
     */
    private String modifyString(String src, String find, String replace) {
        Pattern pattern = Pattern.compile(find);
        Matcher matcher = pattern.matcher(src);
        return matcher.replaceAll(replace);
    }

    public static final void setUpFlags() {
    //FlagDependencies.addRequires(FLAG_TRANSLATE_CLEAN, FLAG_TRANSLATE);
    }
}
