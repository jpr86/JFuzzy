/* %%
 * 
 * JFuzzy
 *
 * Copyright 2006 Jeff Ridder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ridderware.jfuzzy;

/**
 * A class to represent simple fuzzy logic conditions of the form:
 * variable is [NOT] term
 *
 * @author Jeff Ridder
 */
public class SubCondition implements ICondition
{
    private Variable variable;

    private int term_index;

    private boolean not;

    /**
     * Creates a new instance of SubCondition.
     * 
     * @param variable Variable object.
     * @param term_index index of membership function relating to this condition.
     */
    public SubCondition(Variable variable, int term_index)
    {
        this.variable = variable;
        this.term_index = term_index;
        this.not = false;
    }

    /**
     * Creates a new instance of SubCondition
     * 
     * @param variable Variable object.
     * @param term_index index of membership function relating to this condition.
     * @param not true if this is a NOT statement (i.e., variable is NOT term).
     */
    public SubCondition(Variable variable, int term_index, boolean not)
    {
        this.variable = variable;
        this.term_index = term_index;
        this.not = not;
    }

    /**
     * Determines equivalence between this object and the one being tested.
     * If the attributes are the same for both, then this function returns true.
     * @param o object to be tested for equivalence to this.
     * @return true if the object is equivalent, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof SubCondition)
        {
            SubCondition obj = (SubCondition) o;

            if (obj.variable == this.variable &&
                obj.term_index == this.term_index &&
                obj.not == this.not)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public
    int hashCode()
    {
        int hash = 7;
        hash =
            29 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        hash = 29 * hash + this.term_index;
        hash = 29 * hash + (this.not ? 1 : 0);
        return hash;
    }

    /**
     * Returns a string containing the FCL statement for this condition.
     * @return FCL string.
     */
    public String writeFCL()
    {
        if (this.not)
        {
            return variable.getName() + " IS NOT " + variable.getTerm(term_index).
                getName();
        }
        else
        {
            return variable.getName() + " IS " + variable.getTerm(term_index).
                getName();
        }
    }

    /**
     * Aggregates the condition.
     * @return aggregated value.
     */
    public double aggregate()
    {
        if (this.not)
        {
            return 1. - variable.getTerm(term_index).getDOM();
        }
        else
        {
            return variable.getTerm(term_index).getDOM();
        }
    }
}
