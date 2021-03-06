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
 * ModuleRecord.java
 * 
 * The Resolve Software Composition Workbench Project
 * 
 * Copyright (c) 1999-2005
 * Reusable Software Research Group
 * Department of Computer Science
 * Clemson University
 */

package edu.clemson.cs.r2jt.init;

import java.io.File;
import java.util.Iterator;

import edu.clemson.cs.r2jt.absyn.ModuleDec;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.ModuleID;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.scope.OldSymbolTable;

/**
 * A record of a module's compilation history. It consists of two main
 * data structures: The module dec, and the symbol table. A module
 * record can be in any of three mutually exclusive states: begin,
 * final, or error. When a record is created it is in an error state.
 * When a non-null module dec is added the record is placed in a begin
 * state.  When a non-null symbol table is added the record is placed
 * in a final state. If an error occurs anytime after the module dec
 * is added, the record is put back into an error state and
 * compilation aborts.
 */
public class ModuleRecord {

    // ==========================================================
    // Variables 
    // ==========================================================

    private ModuleID id;

    private File file;

    private ModuleDec dec = null;

    private List<ModuleID> theories = new List<ModuleID>();

    private OldSymbolTable table = null;

    private boolean errors = false;

    // ==========================================================
    // Constructors
    // ==========================================================

    public ModuleRecord(ModuleID id, File file) {
        this.id = id;
        this.file = file;
    }

    // ==========================================================
    // Accessor Methods
    // ==========================================================

    // ----------------------------------------------------------
    // Get Methods
    // ----------------------------------------------------------

    public File getFile() {
        return file;
    }

    public ModuleID getModuleID() {
        return id;
    }

    public ModuleDec getModuleDec() {
        return dec;
    }

    public List<ModuleID> getTheories() {
        return theories;
    }

    public OldSymbolTable getSymbolTable() {
        return table;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    public void setErrorFlag() {
        errors = true;
    }

    public void setModuleDec(ModuleDec dec) {
        assert dec != null : "dec is null";
        this.dec = dec;
    }

    public void setTheories(List<ModuleID> theories) {
        assert dec != null : "dec is null";
        assert table == null : "table is not null";
        Iterator<ModuleID> it = theories.iterator();
        while (it.hasNext()) {
            ModuleID temp = it.next();
            if (!this.theories.contains(temp)) {
                this.theories.add(temp);
            }
        }
    }

    public void setSymbolTable(OldSymbolTable table) {
        assert dec != null : "dec is null";
        assert table != null : "table is null";
        this.table = table;
    }

    // -----------------------------------------------------------
    // Query Methods
    // -----------------------------------------------------------

    public boolean isComplete() {
        return (!errors && dec != null && table != null);
    }

    public boolean containsErrors() {
        return errors;
    }
}
