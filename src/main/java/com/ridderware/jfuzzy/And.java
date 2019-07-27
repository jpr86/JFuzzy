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
 * And represents the fuzzy logic "And" operator between two conditions.
 * Instantiate an And object, specifying the conditions to the constructor.
 *
 * @author Jeff Ridder
 */
public class And implements ICondition
{
    /**
     * Enumeration of fuzzy And operators.
     */
    public enum FuzzyAndOperator
    {
        /**
         * min(cond1, cond2)
         */
        MIN,
        /**
         * cond1 * cond2
         */
        PROD,
        /**
         * max(0, cond1+cond2-1)
         */
        BDIF

    }
    private ICondition condition1;

    private ICondition condition2;

    private static FuzzyAndOperator and_operator = FuzzyAndOperator.MIN;

    /** Creates a new instance of And */
    public And()
    {
        super();
        this.condition1 = null;
        this.condition2 = null;
    }

    /**
     * Creates a new instance of And.
     * @param condition1 an ICondition object.
     * @param condition2 a second ICondition object.
     */
    public And(ICondition condition1, ICondition condition2)
    {
        super();
        this.condition1 = condition1;
        this.condition2 = condition2;
    }

    /**
     * Writes the condition to a string in Fuzzy Control Language.
     * @return String containing the FCL condition.
     */
    public String writeFCL()
    {
        String fcl = condition1.writeFCL();

        fcl += " AND ";

        fcl += condition2.writeFCL();

        return fcl;
    }

    /**
     * Determines equivalence between this object and the one being tested.
     * If the conditions are the same for both, then this function returns true.
     * @param o object to be tested for equivalence to this.
     * @return true if the object is equivalent, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof And)
        {
            And obj = (And) o;
            if ((obj.condition1.equals(condition1) &&
                obj.condition2.equals(condition2)) ||
                (obj.condition1.equals(condition2) &&
                obj.condition2.equals(condition1)))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash =
            79 * hash +
            (this.condition1 != null ? this.condition1.hashCode() : 0);
        hash =
            79 * hash +
            (this.condition2 != null ? this.condition2.hashCode() : 0);
        return hash;
    }

    /**
     * Aggregates subconditions and returns the result.
     *@return result of aggregation.
     */
    public double aggregate()
    {
        double term1 = condition1.aggregate();
        double term2 = condition2.aggregate();

        switch (and_operator)
        {
            case MIN:
                return Math.min(term1, term2);
            case PROD:
                return term1 * term2;
            case BDIF:
                return Math.max(0., term1 + term2 - 1.);
            default:
                return 0.;
        }
    }

    /**
     * Sets the And operator.
     * @param and_operator And operator to be set.
     */
    public static void setAndOperator(FuzzyAndOperator and_operator)
    {
        And.and_operator = and_operator;
    }

    /**
     * Returns the And operator.
     * @return And operator.
     */
    public static FuzzyAndOperator getAndOperator()
    {
        return And.and_operator;
    }
}
