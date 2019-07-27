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
 *  Conclusion is a class to represent fuzzy logic conclusion
 * statements -- the "Then" part of If [Condition] Then [Conclusion] rules.
 * Instantiate a Conclusion object with a variable, membership function, and weight.
 * @author Jeff Ridder
 */
public class Conclusion
{
    private OutputVariable output_variable;

    //  Index of the term of the variable.
    private int term_index;

    //  Weight of activation.
    private double weight;

    //  Computed activation level.
    private double activation_level;

    /**
     * Creates an instance of Conclusion.
     * @param output_variable output variable for this conclusion.
     * @param term_index index of membership function relating to this conclusion.
     */
    public Conclusion(OutputVariable output_variable, int term_index)
    {
        this.output_variable = output_variable;
        this.term_index = term_index;
        this.weight = 1.;
    }

    /**
     * Creates a new instance of Conclusion
     * @param output_variable output variable for this conclusion.
     * @param term_index index of membership function relating to this conclusion.
     * @param weight activation weight for this conclusion.
     */
    public Conclusion(OutputVariable output_variable, int term_index,
        double weight)
    {
        this.output_variable = output_variable;
        this.term_index = term_index;
        this.weight = weight;
    }

    /**
     * Returns a string with an FCL statement for this conclusion.
     * @return string containing FCL statement.
     */
    public String writeFCL()
    {
        MembershipFunction term = output_variable.getTerm(term_index);

        String fcl = output_variable.getName() + " IS " + term.getName();

        if (this.weight < 1.0)
        {
            fcl += " WITH " + weight;
        }

        return fcl;
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
        if (o instanceof Conclusion)
        {
            Conclusion obj = (Conclusion) o;

            if (obj.output_variable == this.output_variable &&
                obj.term_index == this.term_index &&
                obj.weight == this.weight)
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
        int hash = 3;
        hash =
            61 * hash +
            (this.output_variable != null ? this.output_variable.hashCode() : 0);
        hash = 61 * hash + this.term_index;
        hash =
            61 * hash +
            (int) (Double.doubleToLongBits(this.weight) ^
            (Double.doubleToLongBits(this.weight) >>> 32));
        hash =
            61 * hash +
            (int) (Double.doubleToLongBits(this.activation_level) ^
            (Double.doubleToLongBits(this.activation_level) >>> 32));
        return hash;
    }

    /**
     * Returns the start of the crisp domain concerning this conclusion.
     *
     * @return index of the start of the crisp domain.
     */
    public int getStartX()
    {
        return output_variable.getDiscreteIndex(output_variable.getTerm(term_index).
            getDataPoint(0).getX());
    }

    /**
     * Returns the end of the crisp domain concerning this conclusion.
     *
     * @return index of the end of the crisp domain.
     */
    public int getEndX()
    {
        MembershipFunction term = output_variable.getTerm(term_index);

        return output_variable.getDiscreteIndex(term.getDataPoint(term.getNumberOfDataPoints() -
            1).getX());
    }

    /**
     * Returns the crisp value corresponding to the index.
     *
     * @param index index of the crisp variable.
     *
     * @return crisp value.
     */
    public double getX(int index)
    {
        return output_variable.getDiscreteX(index);
    }

    /**
     * Returns the fuzzy degree of membership corresponding to the index.
     *
     * @param index index of the fuzzy dom.
     *
     * @return fuzzy dom.
     */
    public double getY(int index)
    {
        return output_variable.getDiscreteY(index);
    }

    /**
     * Sets the fuzzy degree of membership at the index.
     *
     * @param index index of the fuzzy dom.
     * @param value fuzzy dom.
     */
    public void setY(int index, double value)
    {
        this.output_variable.setDiscreteY(index, value);
    }

    /**
     * Sets the activation level for the output set.  This
     * should be the result of aggregation of conditions.
     * Note that this is multiplied internally by weight to
     * determine the final activation level.
     *
     * @param level activation level from aggregation.
     */
    public void setActivationLevel(double level)
    {
        activation_level = level * weight;
    }

    /**
     * Sets the activation weight.
     * @param weight activation weight.
     */
    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    /**
     * Returns the activated value at point x.
     * This is determined by multiplying the activation level
     * times the value of the term (membership function) at x.
     *
     * @param activation_method  activation method.
     * @param index  point in the crisp domain at which to compute
     * 	the activated value.
     *
     * @return activated value at index.
     */
    public double getActivatedValue(RuleBase.ActivationMethod activation_method,
        int index)
    {
        if (this.output_variable == null)
        {
            return 0.;
        }

        //	This forces the membership function to do an interpolation.
        //	Would be better to precompute all DOMs on discrete points
        //	and store them for fast reference.
        double dom =
            output_variable.getTerm(this.term_index).getDiscreteY(index);

        switch (activation_method)
        {
            case PROD:
            {
                return this.activation_level * dom;
            }
            case MIN:
            {
                return Math.min(this.activation_level, dom);
            }
            default:
            {
                return 0.;
            }
        }
    }
}
