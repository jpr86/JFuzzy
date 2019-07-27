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
 * Class to represent a fuzzy rule.  Each rule is of the form:
 * If [Condition] Then [Conclusion]
 * @author Jeff Ridder
 */
public class Rule
{
    private ICondition condition;

    private Conclusion conclusion;

    /**
     * Creates a new instance of Rule
     * @param condition a condition object.
     * @param conclusion a conclusion object.
     */
    public Rule(ICondition condition, Conclusion conclusion)
    {
        this.condition = condition;
        this.conclusion = conclusion;
    }

    /**
     * Fires the inference for the rule.  Note that this results in:
     * -# Aggregation: evaluating conditions.
     * -# Activation: assigning activation level.
     * -# Accumulation : assigning results of this rule to the
     * 	output variable.
     * 
     * @param activation_method activation method.
     * @param accumulation_method accumulation method.
     */
    public void infer(RuleBase.ActivationMethod activation_method,
        RuleBase.AccumulationMethod accumulation_method)
    {
        //	Aggregate
        conclusion.setActivationLevel(condition.aggregate());

        int xstart = conclusion.getStartX();
        int xend = conclusion.getEndX();

        //	Accumulate
        switch (accumulation_method)
        {
            case MAX:
            {
                for (int i = xstart; i <= xend; i++)
                {
                    double value =
                        conclusion.getActivatedValue(activation_method, i);
                    conclusion.setY(i, Math.max(value, conclusion.getY(i)));
                }
                break;
            }
            case BSUM:
            {
                for (int i = xstart; i <= xend; i++)
                {
                    double value =
                        conclusion.getActivatedValue(activation_method, i);
                    conclusion.setY(i, Math.min(1., value + conclusion.getY(i)));
                }
                break;
            }
            default:
            {
            }
        }
    }

    /**
     * Writes the rule to a string in Fuzzy Control Language.
     * @param rule_number the index of this rule to be included in the string.
     * @return String containing the FCL rule.
     */
    public String writeFCL(int rule_number)
    {
        String str = "\tRULE " + rule_number + " : IF ";
        str += this.getCondition().writeFCL();
        str += " THEN ";
        str += this.getConclusion().writeFCL();
        str += ";";

        return str;
    }

    /**
     * Returns the condition of the rule.
     *
     * @return the condition.
     */
    public ICondition getCondition()
    {
        return condition;
    }

    /**
     * Returns the conclusion of the rule.
     *
     * @return the conclusion.
     */
    public Conclusion getConclusion()
    {
        return conclusion;
    }
}
