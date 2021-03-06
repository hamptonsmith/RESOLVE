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
 * EnhancementBodyItem.java
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

public class EnhancementBodyItem extends ResolveConceptualElement {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The name member. */
    private PosSymbol name;

    /** The performance profile name member. */
    private PosSymbol profileName;

    /** The params member. */
    private List<ModuleArgumentItem> params;

    /** The bodyName member. */
    private PosSymbol bodyName;

    /** The bodyParams member. */
    private List<ModuleArgumentItem> bodyParams;

    // ===========================================================
    // Constructors
    // ===========================================================

    public EnhancementBodyItem() {};

    public EnhancementBodyItem(PosSymbol name, List<ModuleArgumentItem> params,
            PosSymbol bodyName, PosSymbol profileName,
            List<ModuleArgumentItem> bodyParams) {
        this.name = name;
        this.params = params;
        this.bodyName = bodyName;
        this.profileName = profileName;
        this.bodyParams = bodyParams;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    public Location getLocation() {
        return name.getLocation();
    }

    /** Returns the value of the name variable. */
    public PosSymbol getName() {
        return name;
    }

    /** Returns the value of the profileName variable. */
    public PosSymbol getProfileName() {
        return profileName;
    }

    /** Returns the value of the params variable. */
    public List<ModuleArgumentItem> getParams() {
        return params;
    }

    /** Returns the value of the bodyName variable. */
    public PosSymbol getBodyName() {
        return bodyName;
    }

    /** Returns the value of the bodyParams variable. */
    public List<ModuleArgumentItem> getBodyParams() {
        return bodyParams;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the name variable to the specified value. */
    public void setName(PosSymbol name) {
        this.name = name;
    }

    /** Sets the profileName variable to the specified value. */
    public void setProfileName(PosSymbol name) {
        this.profileName = name;
    }

    /** Sets the params variable to the specified value. */
    public void setParams(List<ModuleArgumentItem> params) {
        this.params = params;
    }

    /** Sets the bodyName variable to the specified value. */
    public void setBodyName(PosSymbol bodyName) {
        this.bodyName = bodyName;
    }

    /** Sets the bodyParams variable to the specified value. */
    public void setBodyParams(List<ModuleArgumentItem> bodyParams) {
        this.bodyParams = bodyParams;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitEnhancementBodyItem(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("EnhancementBodyItem\n");

        if (name != null) {
            sb.append(name.asString(indent + increment, increment));
        }

        if (params != null) {
            sb.append(params.asString(indent + increment, increment));
        }

        if (bodyName != null) {
            sb.append(bodyName.asString(indent + increment, increment));
        }

        if (bodyParams != null) {
            sb.append(bodyParams.asString(indent + increment, increment));
        }

        return sb.toString();
    }
}
