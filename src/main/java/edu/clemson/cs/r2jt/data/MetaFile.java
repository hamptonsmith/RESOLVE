package edu.clemson.cs.r2jt.data;

import java.io.File;

import edu.clemson.cs.r2jt.init.CompileEnvironment;

/**
 * @author Chuck
 * This class is used in conjunction with the web interface as a way
 * to store all the information for user created components that is
 * needed by the compiler (since they will not be written to disk) 
 */

public class MetaFile {

    private String myFileName;
    private String myAssocConcept;
    private String myPkg;
    private String myFileSource;
    private ModuleKind myKind;
    private boolean myCustom;
    private String myCustomPath;
    private String jarTempDir;

    public MetaFile(String fileName, String assocConcept, String pkg,
            String fileSource, ModuleKind kind) {
        myFileName = fileName;
        myAssocConcept = assocConcept;
        myPkg = pkg;
        myFileSource = fileSource;
        myKind = kind;
        myCustom = false;
    }

    public void setMyFileName(String myFileName) {
        this.myFileName = myFileName;
    }

    public void setMyAssocConcept(String myAssocConcept) {
        this.myAssocConcept = myAssocConcept;
    }

    public void setMyPkg(String pkg) {
        this.myPkg = pkg;
    }

    public void setMyFileSource(String myFileSource) {
        this.myFileSource = myFileSource;
    }

    public void setIsCustomLoc() {
        myCustom = true;
    }

    public void setMyCustomPath(String customPath) {
        myCustomPath = customPath;
    }

    /*public void setMyKind(ModuleKind myKind) {
    	this.myKind = myKind;
    }*/

    public String getMyFileName() {
        return myFileName;
    }

    public String getMyAssocConcept() {
        return myAssocConcept;
    }

    public String getMyPkg() {
        return myPkg;
    }

    public String getMyFileSource() {
        return myFileSource;
    }

    public ModuleKind getMyKind() {
        return myKind;
    }

    public boolean getIsCustomLoc() {
        return myCustom;
    }

    public void setJarTempDir(String dir) {
        jarTempDir = dir;
    }

    public String getJarTempDir() {
        return jarTempDir;
    }

    public File getMyCustomFile() {
        String filePath = myCustomPath;
        if (myKind.equals(ModuleKind.FACILITY)) {
            filePath += File.separator + "Facilities" + File.separator;
        }
        else {
            filePath += File.separator + "Concepts" + File.separator;
        }
        filePath += myPkg + File.separator + myFileName + myKind.getExtension();
        return new File(filePath);
    }

    public File getMyFile(File mainDir) {
        if (false) {
            String filePath = myCustomPath;
            if (myKind.equals(ModuleKind.FACILITY)) {
                filePath += File.separator + "Facilities" + File.separator;
            }
            else {
                filePath += File.separator + "Concepts" + File.separator;
            }
            filePath +=
                    myPkg + File.separator + myFileName + myKind.getExtension();
            return new File(filePath);
        }
        else {
            String filePath = mainDir.getAbsolutePath();
            if (myKind.equals(ModuleKind.FACILITY)) {
                filePath += File.separator + "Facilities" + File.separator;
            }
            else {
                filePath += File.separator + "Concepts" + File.separator;
            }
            if (myPkg.equals("Static_Array_Template")) {
                myPkg = "Standard" + File.separator + "Static_Array_Template";
            }
            else if (myPkg.equals("Location_Linking_Template_1")) {
                myPkg =
                        "Standard" + File.separator
                                + "Location_Linking_Template_1";
            }
            filePath +=
                    myPkg + File.separator + myFileName + myKind.getExtension();
            return new File(filePath);
        }
    }
}
