package edu.clemson.cs.r2jt.archiving;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import edu.clemson.cs.r2jt.ResolveCompiler;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.MetaFile;
import edu.clemson.cs.r2jt.init.CompileEnvironment;
import edu.clemson.cs.r2jt.translation.Translator;
import edu.clemson.cs.r2jt.utilities.Flag;
import edu.clemson.cs.r2jt.utilities.FlagDependencies;

public class Archiver {

    private static final String FLAG_SECTION_NAME = "Archiving";

    private static final String FLAG_DESC_ARCHIVE =
            "Create an executable jar from a RESOLVE Facility.";

    private static final String FLAG_DESC_VERBOSE_ARCHIVE =
            "Create an executable jar from a RESOLVE Facility, "
                    + "while printing the output from the Java compiler "
                    + "and jar utilities.";

    /**
     * <p>The main archiver flag.  Tells the compiler to attempt to
     * create an executable jar from a RESOLVE facility.</p>
     */
    public static final Flag FLAG_ARCHIVE =
            new Flag(FLAG_SECTION_NAME, "createJar", FLAG_DESC_ARCHIVE);

    /**
     * <p>The alternate archiver flag.  Can be used to print the
     * output from the javac and jar utilities when there is a 
     * problem creating the executable jar.</p>
     */
    public static final Flag FLAG_VERBOSE_ARCHIVE =
            new Flag(FLAG_SECTION_NAME, "verboseJar", FLAG_DESC_VERBOSE_ARCHIVE);

    private final CompileEnvironment myInstanceEnvironment;
    private boolean webOutput;

    // List of files that are temporarily created (manifest, .class)
    private List<File> createdFiles = new List<File>();

    // Lists for .java and .class files needed by the Archiver
    private List<String> sourceFiles = new List<String>();
    private List<String> archiveFiles = new List<String>();

    // Operating System name (necessary to know this
    // for launching javac and jar)
    //private String os = System.getProperty("os.name");

    // Variables to hold OS-specific information determined at runtime
    //private String slash = "";
    //private String split = "";

    // Variable to hold the shell command for the appropriate OS
    //private String[] cmds = new String[2];

    // Strings to hold the custom manifest information needed to execute the jar properly
    //private String manifest;
    //private String manifestFile;
    private String entryClass;

    // String to hold the filename of the output jar
    private String targetJarName;

    private File outputJarFile = null;

    private String workspaceDir;

    private MetaFile inputFile;

    private String[] stdResolve =
            { "RESOLVE_BASE.java", "RESOLVE_BASE_EXT.java",
                    "RESOLVE_INTERFACE.java", "RType.java",
                    "RTypeWrapper.java", "TextIO.java" };

    private String[] stdImports;
    public static int BUFFER_SIZE = 10240;

    public Archiver(CompileEnvironment e, File file, MetaFile inputFile) {
        myInstanceEnvironment = e;
        webOutput =
                myInstanceEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_WEB);
        this.inputFile = inputFile;
        String fileName = file.getAbsolutePath();
        int dot = fileName.lastIndexOf(".");
        targetJarName = fileName.substring(0, dot) + ".jar";

        // Determine what OS we're running on and set the appropriate
        // command line commands
        //setOsSpecific();

        // Add the standard imports to the list of files to archive
        createStandardImports();

        // Figure out the name of the workspace directory
        setWorkspacePath();

        // Generate the GUI wrapper
        createGuiWrapper(file);

        // the RESOLVE base Java files and standard imports
        addStandardImports();
    }

    public boolean createJar() {
        boolean ret = false;
        if (compileFiles()) {
            createArchive(true);
            ret = true;
        }
        else {
            createArchive(false);
        }
        return ret;
    }

    /**
     * Method to add the Java file to the list of files to send to javac
     * 
     * @param inputFile File to add to the archive
     */
    public void addFileToArchive(File inputFile) {
        String inFile = inputFile.toString();
        String[] temp = inFile.split("\\.");
        String ext = temp[temp.length - 1];
        String javaFile;
        if (ext.equals("co") || ext.equals("rb") || ext.equals("en")
                || ext.equals("fa") || ext.equals("java")) {
            /*if(onNoCompileList(inFile)){
            	return;
            }*/
            javaFile = modifyString(inFile, "\\." + ext, ".java");
            if (!sourceFiles.contains(javaFile)) {
                sourceFiles.add(javaFile);
                if (myInstanceEnvironment.flags
                        .isFlagSet(ResolveCompiler.FLAG_WEB)) {
                    String fileName = inputFile.getName();
                    //System.out.println(javaFile);
                    for (int i = 0; i < stdImports.length; i++) {
                        if (containsString(stdImports[i], fileName)) {
                            return;
                        }
                    }
                    createdFiles.add(new File(javaFile));
                }
            }
        }
    }

    /**
     * Method that invokes the javac
     * 
     * @return 0 if successful
     */
    public boolean compileFiles() {
        int ret = -1;
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler != null) {
                String[] compileOptions = new String[] {};
                Iterable<String> compilationOptions =
                        Arrays.asList(compileOptions);
                StandardJavaFileManager fileManager =
                        compiler.getStandardFileManager(null, Locale
                                .getDefault(), null);
                Iterable<? extends JavaFileObject> compilationUnits =
                        fileManager.getJavaFileObjectsFromStrings(sourceFiles);
                DiagnosticCollector<JavaFileObject> diagnosticListener =
                        new DiagnosticCollector<JavaFileObject>();
                CompilationTask compilerTask =
                        compiler.getTask(null, fileManager, diagnosticListener,
                                compilationOptions, null, compilationUnits);
                boolean status = compilerTask.call();
                if (status) {
                    ret = 0;
                }
                else {
                    printDiagnostics(diagnosticListener);
                }
                String msg = null;
                if (ret == 0) {
                    msg = "Java files successfully compiled with javac";
                }
                else {
                    msg = "Java files not compiled successfully";
                }
                System.out.println(msg);
            }
            else {
                if (!webOutput) {
                    System.out.println("Archiver error: Java compiler is null");
                }
                else {
                    myInstanceEnvironment.getCompileReport().addBugReport(
                            "Archiver error: Java compiler is null");
                }
            }
        }
        catch (Exception ex) {
            ret = -1;
            if (!webOutput) {
                System.out.println("Compiler Error: " + ex);
            }
            else {
                myInstanceEnvironment.getCompileReport().addBugReport(
                        ex.toString());
            }
        }
        if (ret == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setOutputJar(String jarFileString) {
        int dot = jarFileString.lastIndexOf(".");
        outputJarFile = new File(jarFileString.substring(0, dot) + ".jar");
    }

    /**
     * <p>Method to invoke the jar utility and create the jar file.</p>
     * 
     * @return 0 if successful
     */
    public int createArchive(boolean compileSuccess) {
        int ret = -1;
        FileOutputStream stream = null;
        JarOutputStream out = null;
        try {
            /*String regex = "(?<=\\.).*$"; //the file extension
            String extRegex = "(?<=\\.)class$"; //match .class files
            Pattern extPattern = Pattern.compile(extRegex);
            Matcher extMatcher = null;*/
            byte buffer[] = new byte[BUFFER_SIZE];
            if (compileSuccess) {
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(
                        Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
                        entryClass);
                if (outputJarFile != null) {
                    stream = new FileOutputStream(outputJarFile);
                }
                else {
                    stream = new FileOutputStream(targetJarName);
                }

                out = new JarOutputStream(stream, manifest);
            }

            for (String sf : sourceFiles) {
                int length = sf.length();
                int slashIndex = sf.lastIndexOf(File.separator, length - 1);
                int dotIndex = sf.lastIndexOf(".", length - 1);
                String modulePath = sf.substring(0, slashIndex);
                String moduleName = sf.substring(slashIndex + 1, dotIndex);
                String nameRegex = moduleName + "(\\.|(\\$[^\\.]+\\.))class$";
                Pattern namePattern = Pattern.compile(nameRegex);
                Matcher nameMatcher = null;
                File modulePathDir = new File(modulePath);
                if (modulePathDir.isDirectory()) {
                    File[] files = modulePathDir.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        String fileName = files[i].getAbsolutePath();
                        nameMatcher = namePattern.matcher(fileName);
                        if (nameMatcher.find()) {
                            if (compileSuccess) {
                                fileName = files[i].getAbsolutePath();
                                fileName =
                                        fileName
                                                .replaceAll(
                                                        "\\"
                                                                + System
                                                                        .getProperty("file.separator"),
                                                        "/");
                                // now we strip off the first few directories so we have an archive starting at RESOLVE
                                fileName =
                                        fileName.substring(fileName
                                                .indexOf("RESOLVE"));
                                //System.out.println(fileName);
                                //fileName = fileName.substring(workspaceDir.length());
                                JarEntry jarAdd = new JarEntry(fileName);
                                jarAdd.setTime(files[i].lastModified());
                                out.putNextEntry(jarAdd);
                                FileInputStream in =
                                        new FileInputStream(files[i]);
                                while (true) {
                                    int nRead =
                                            in.read(buffer, 0, buffer.length);
                                    if (nRead <= 0)
                                        break;
                                    out.write(buffer, 0, nRead);
                                }
                                in.close();
                            }
                            // add to the list of created files
                            createdFiles.add(new File(files[i]
                                    .getAbsolutePath()));
                        }
                    }
                }
            }
            if (compileSuccess) {
                ret = 0;
                out.close();
                stream.close();
            }
            String msg = "";
            if (ret == 0) {
                msg = "Jar archive successfully created";
            }
            else {
                //msg = "Jar archive unsuccessful";
            }
            System.out.println(msg);

        }
        catch (Exception ex) {
            ret = -1;
            if (!webOutput) {
                System.out.println("Archiver Error: " + ex);
            }
            else {
                myInstanceEnvironment.getCompileReport().addBugReport(
                        ex.toString());
            }
        }
        return ret;
    }

    /**
     * <p>Method to delete all the files that were created by the archiver.</p>
     */
    public void cleanupFiles() {
        File file;
        Iterator<File> it = createdFiles.iterator();
        while (it.hasNext()) {
            file = it.next();
            if (file.exists()) {
                //System.out.println(file.getAbsolutePath());
                file.delete();
            }
        }
    }

    /**
     * <p>Method used to print everything on the list to be translated (used for testing).</p>
     */
    public void printArchiveList() {
        Iterator<String> it = archiveFiles.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        it = sourceFiles.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    /**
     * <p>Method that uses a regex to modify a String.</p>
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

    private boolean containsString(String src, String tar) {
        Pattern p = Pattern.compile(tar);
        return p.matcher(src).find();
    }

    /**
     * <p>Method to generate the names of the standard imports to add.
     * (need to check the OS to find out what kind of slash to use first).</p>
     */
    private void createStandardImports() {
        stdImports = new String[14];
        stdImports[0] =
                "Boolean_Template" + File.separator + "Boolean_Template.java";
        stdImports[1] =
                "Boolean_Template" + File.separator + "Std_Boolean_Realiz.java";
        stdImports[2] =
                "Char_Str_Template" + File.separator + "Char_Str_Template.java";
        stdImports[3] =
                "Char_Str_Template" + File.separator
                        + "Std_Char_Str_Realiz.java";
        stdImports[4] =
                "Character_Template" + File.separator
                        + "Character_Template.java";
        stdImports[5] =
                "Character_Template" + File.separator
                        + "Std_Character_Realiz.java";
        stdImports[6] =
                "Integer_Template" + File.separator + "Integer_Template.java";
        stdImports[7] =
                "Integer_Template" + File.separator + "Std_Integer_Realiz.java";
        stdImports[8] = "io" + File.separator + "Seq_Input_Template.java";
        stdImports[9] = "io" + File.separator + "Std_Seq_Input_Realiz.java";
        stdImports[10] = "io" + File.separator + "Seq_Output_Template.java";
        stdImports[11] = "io" + File.separator + "Std_Seq_Output_Realiz.java";
        //stdImports[12] = "Print" + File.separator + "Print.java";
        //stdImports[13] = "Print" + File.separator + "Std_Print_Realiz.java";
        stdImports[12] =
                "Static_Array_Template" + File.separator
                        + "Std_Array_Realiz.java";
        stdImports[13] =
                "Location_Linking_Template_1" + File.separator
                        + "Std_Location_Linking_Realiz.java";
    }

    private void addStandardImports() {
        // Add RESOLVE.* imports
        for (String s : stdResolve) {
            sourceFiles.add(workspaceDir + "RESOLVE" + File.separator + s);
        }

        String mainDir = workspaceDir + "RESOLVE" + File.separator + "Main";
        // Add standard Imports
        for (String s : stdImports) {
            //System.out.println(s);
            sourceFiles.add(mainDir + File.separator + "Concepts"
                    + File.separator + "Standard" + File.separator + s);
        }

    }

    /**
     *
     */
    private void setWorkspacePath() {
        String mainDir = myInstanceEnvironment.getMainDir().toString();
        String[] temp = mainDir.split(Pattern.quote(File.separator));
        String resolveDir = "";
        for (int i = 0; i < temp.length - 1; i++) {
            resolveDir += temp[i];
            if (i != temp.length - 2) {
                resolveDir += File.separator;
            }
            if (i == temp.length - 3) {
                workspaceDir = resolveDir;
            }
        }
    }

    private void createGuiWrapper(File file) {
        GuiWrapper gui =
                new GuiWrapper("gui", file.getPath(), workspaceDir,
                        myInstanceEnvironment);
        if (inputFile != null) {
            gui.setJavaLocation(inputFile.getMyCustomFile().getAbsolutePath(),
                    inputFile.getMyFileName()
                            + inputFile.getMyKind().getExtension());
        }
        if (gui.generateCode() && gui.createJavaFile()) {
            sourceFiles.add(gui.getJavaPath());
            createdFiles.add(new File(gui.getJavaPath()));
            entryClass = gui.getEntryClass();
        }

    }

    public String[] getStandardImports() {
        return stdImports;
    }

    private void printDiagnostics(
            DiagnosticCollector<JavaFileObject> diagnosticListener) {
        for (Diagnostic<?> diagnostic : diagnosticListener.getDiagnostics()) {
            if (diagnostic.getKind().equals(Diagnostic.Kind.ERROR)) {
                String errMsg =
                        diagnostic.getMessage(null).replaceAll(
                                Pattern.quote(workspaceDir), "");
                if (!webOutput) {
                    System.out.println("Javac Error: " + errMsg);
                }
                else {
                    myInstanceEnvironment.getCompileReport().addBugReport(
                            "Javac Error: " + errMsg);
                }
            }
        }
    }

    private File[] getFiles() {
        int i = 0;
        File[] files = new File[sourceFiles.size()];
        for (String sf : sourceFiles) {
            files[i++] = new File(sf);
        }
        return files;
    }

    public static final void setUpFlags() {
        FlagDependencies.addRequires(FLAG_VERBOSE_ARCHIVE, FLAG_ARCHIVE);
        FlagDependencies.addImplies(FLAG_ARCHIVE, Translator.FLAG_TRANSLATE);
        FlagDependencies.addImplies(FLAG_ARCHIVE,
                Translator.FLAG_TRANSLATE_CLEAN);
    }
}