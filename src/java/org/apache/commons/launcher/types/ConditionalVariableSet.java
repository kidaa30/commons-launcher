/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *             Copyright (c) 2002 The Apache Software Foundation.            *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The Jakarta  Project",  and  "Apache  Software Foundation" *
 *    must not  be used  to endorse  or promote  products derived  from this *
 *    software without  prior written  permission.  For written  permission, *
 *    please contact <apache@apache.org>.                                    *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

package org.apache.commons.launcher.types;

import java.util.ArrayList;
import java.util.Stack;
import org.apache.commons.launcher.Launcher;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * A class that represents a set of nested elements of
 * {@link ConditionalVariable} objects.
 *
 * @author Patrick Luby
 */
public class ConditionalVariableSet extends DataType {

    //------------------------------------------------------------------ Fields

    /**
     * Cached variables and nested ConditionalVariableSet objects
     */
    private ArrayList list = new ArrayList();

    //----------------------------------------------------------------- Methods

    /**
     * Add a {@link ConditionalVariable}.
     *
     * @param variable the {@link ConditionalVariable} to be
     *  added
     */
    protected void addConditionalvariable(ConditionalVariable variable) {

        if (isReference())
            throw noChildrenAllowed();
        list.add(variable);

    }

    /**
     * Add a {@link ConditionalVariableSet}.
     *
     * @param set the {@link ConditionalVariableSet} to be added
     */
    protected void addConditionalvariableset(ConditionalVariableSet set) {

        if (isReference())
            throw noChildrenAllowed();
        list.add(set);

    }

    /**
     * Get {@link ConditionalVariable} instances.
     *
     * @return the {@link ConditionalVariable} instances
     */
    public ArrayList getList() {

        // Make sure we don't have a circular reference to this instance
        if (!checked) {
            Stack stk = new Stack();
            stk.push(this);
            dieOnCircularReference(stk, project);
        }

        // Recursively work through the tree of ConditionalVariableSet objects
        // and accumulate the list of ConditionalVariable objects.
        ArrayList mergedList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            ConditionalVariableSet nestedSet = null;
            if (o instanceof Reference) {
                o = ((Reference)o).getReferencedObject(project);
                // Only references to this class are allowed
                if (!o.getClass().isInstance(this))
                    throw new BuildException(Launcher.getLocalizedString("cannot.reference", this.getClass().getName()));
                nestedSet = (ConditionalVariableSet)o;
            } else if (o.getClass().isInstance(this)) {
                nestedSet = (ConditionalVariableSet)o;
            } else if (o instanceof ConditionalVariable) {
                mergedList.add(o);
            } else {
                throw new BuildException(Launcher.getLocalizedString("cannot.nest", this.getClass().getName()));
            }
            if (nestedSet != null)
                mergedList.addAll(nestedSet.getList());
        }

        return mergedList;

    }

    /**
     * Makes this instance a reference to another instance. You must not
     * set another attribute or nest elements inside this element if you
     * make it a reference.
     *
     * @param r the reference to another {@link ConditionalVariableSet}
     *  instance
     */
    public void setRefid(Reference r) throws BuildException {

        if (!list.isEmpty())
            throw tooManyAttributes();
        list.add(r);
        super.setRefid(r);

    }

}