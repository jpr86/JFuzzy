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
 * Class to represent fuzzy linguistic variables.  Instantiate and
 * add membership functions by calling addTerm().  Fuzzy linguistic
 * variables are discretized prior to execution for rapid defuzzification.
 *
 * @author Jeff Ridder
 */
public class Variable
{
    private String name;

//    private ArrayList<MembershipFunction> terms = new ArrayList<MembershipFunction>();
    private MembershipFunction[] terms = new MembershipFunction[0];

    /**
     * Creates a new instance of Variable
     * @param name name of the variable.
     */
    public Variable(String name)
    {
        this.name = name;
    }

    /**
     * Adds a membership function to the variable.
     * @param term a MembershipFunction object.
     */
    public void addTerm(MembershipFunction term)
    {
//        terms.add(term);
        MembershipFunction[] newTerms = new MembershipFunction[terms.length + 1];

        for (int i = 0; i < terms.length; i++)
        {
            newTerms[i] = terms[i];
        }

        newTerms[newTerms.length - 1] = term;

        terms = newTerms;
    }

    /**
     * Returns all membership functions.
     * @return collection of terms.
     */
    public MembershipFunction[] getTerms()
    {
        return terms;
    }

    /**
     * Returns the specified MembershipFunction.
     * @param index index.
     * @return membership function.
     */
    public MembershipFunction getTerm(int index)
    {
        return terms[index];
    }

    /**
     * Returns the name of the variable.
     * @return name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Fuzzifies the variable for the specified crisp input value.
     * @param crisp_input crisp input value.
     */
    public void fuzzify(double crisp_input)
    {
        for (MembershipFunction term : terms)
        {
            term.calculateDOM(crisp_input);
        }
    }

    /**
     * Returns the index of the term of this name.
     * @param name name of requested term.
     * @return membership function.
     */
    public int getTermIndex(String name)
    {
        for (int i = 0; i < terms.length; i++)
        {
            MembershipFunction term = terms[i];
            if (term.getName().equals(name))
            {
                return i;
            }
        }

        return -1;
    }
}
