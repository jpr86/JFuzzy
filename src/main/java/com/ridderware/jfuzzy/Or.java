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
 * Fuzzy Or condition.
 * @author Jeff Ridder
 */
public class Or implements ICondition
{
    /**
     * Enumeration of fuzzy Or operators.
     */
    public enum FuzzyOrOperator
    {
        /**
         * max(cond1, cond2)
         */
        MAX,
        /**
         * cond1+cond2-cond1*cond2
         */
        ASUM,
        /**
         * min(1, cond1+cond2)
         */
        BSUM

    }
    private ICondition condition1;

    private ICondition condition2;

    private static FuzzyOrOperator or_operator = FuzzyOrOperator.MAX;

    /** Creates a new instance of Or */
    public Or()
    {
        this.condition1 = null;
        this.condition2 = null;
    }

    /**
     * Creates a new instance of Or.
     * @param condition1 an ICondition object.
     * @param condition2 a second ICondition object.
     */
    public Or(ICondition condition1, ICondition condition2)
    {
        this.condition1 = condition1;
        this.condition2 = condition2;
    }

    /**
     * Returns a string containing a FCL statement.
     * @return FCL string.
     */
    public String writeFCL()
    {
        /*        String fcl = condition1.writeFCL();
        fcl += " OR ";
        fcl += condition2.writeFCL();
        return fcl;
         */
        return "";
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
        if (o instanceof Or)
        {
            Or obj = (Or) o;
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
            11 * hash +
            (this.condition1 != null ? this.condition1.hashCode() : 0);
        hash =
            11 * hash +
            (this.condition2 != null ? this.condition2.hashCode() : 0);
        return hash;
    }

    /**
     * Aggregates the conditions.
     * @return aggregated value.
     */
    public double aggregate()
    {
        double term1 = condition1.aggregate();
        double term2 = condition2.aggregate();

        switch (or_operator)
        {
            case MAX:
                return Math.max(term1, term2);
            case ASUM:
                return term1 + term2 - term1 * term2;
            case BSUM:
                return Math.min(1., term1 + term2);
            default:
                return 0.;
        }
    }

    /**
     * Sets the Or operator.
     * @param or_operator or operator.
     */
    public static void setOrOperator(FuzzyOrOperator or_operator)
    {
        Or.or_operator = or_operator;
    }

    /**
     * Returns the or operator.
     * @return or operator.
     */
    public static FuzzyOrOperator getOrOperator()
    {
        return Or.or_operator;
    }
}
