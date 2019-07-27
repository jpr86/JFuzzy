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
 * Class to represent fuzzy linguistic output variables.
 *
 * @author Jeff Ridder
 */
public class OutputVariable extends Variable
{
    /**
     * Enumeration of defuzzification methods.
     */
    public enum DefuzzificationMethod
    {
        /**
         * Center of gravity.
         */
        COG,
        /**
         * Center of gravity for singletons.
         */
        COGS,
        /**
         * Center of area.
         */
        COA
    }
    
    private double crisp_value;

    private double default_value;

    private int num_discretes;

    private DefuzzificationMethod defuzzification_method;

    private DataPoint[] discrete_points = new DataPoint[0];

    /**
     * Creates a new instance of OutputVariable
     * @param name name of the variable.
     */
    public OutputVariable(String name)
    {
        super(name);
        this.defuzzification_method = DefuzzificationMethod.COG;
        this.default_value = 0.;
        this.num_discretes = 200;
    }

    /**
     * Sets the number of discrete values to use to support defuzzification.
     * @param num_discretes number of discrete values.
     */
    public void setNumDiscretes(int num_discretes)
    {
        this.num_discretes = num_discretes;
    }

    /**
     * Returns the number of discrete values for this variable.
     * @return number of discretes.
     */
    public int getNumDiscretes()
    {
        return this.num_discretes;
    }

    /**
     * Sets the defuzzification method for this variable.
     * @param defuzzification_method defuzzification method.
     */
    public void setDefuzzificationMethod(DefuzzificationMethod defuzzification_method)
    {
        this.defuzzification_method = defuzzification_method;
    }

    /**
     * Returns the defuzzification method for this variable.
     * @return defuzzification method.
     */
    public DefuzzificationMethod getDefuzzificationMethod()
    {
        return this.defuzzification_method;
    }

    /**
     * Returns the current crisp output value.
     *
     * @return current crisp value.
     */
    public double getCrispOutput()
    {
        return this.crisp_value;
    }

    /**
     * Returns the index of the nearest discrete point lower than the
     * input parameter.  Note that if you are searching for two indices that
     * cover a range of x, then you would do this as follows:
     * int imin = GetDiscreteIndex(xmin);
     * int imax = GetDiscreteIndex(xmax)+1;
     *
     * @param x crisp value corresponding to the index.
     *
     * @return nearest index corresponding to the input parameter.
     */
    public int getDiscreteIndex(double x)
    {
        double min_x = discrete_points[0].getX();
        double max_x = discrete_points[num_discretes - 1].getX();

        double deltaX = max_x - min_x;

        if (num_discretes >= 2)
        {
            deltaX /= (double) (num_discretes - 1);
        }

        return (int) ((x - min_x) / deltaX);
    }

    /**
     * Returns the value of the crisp variable at the specified index.
     *
     * @param index index of the crisp value.
     *
     * @return crisp value.
     */
    public double getDiscreteX(int index)
    {
        return this.discrete_points[index].getX();
    }

    /**
     * Returns the value of the fuzzy degree of membership at the specified index.
     *
     * @param index index of the fuzzy degree of membership.
     *
     * @return fuzzy dom.
     */
    public double getDiscreteY(int index)
    {
        return this.discrete_points[index].getY();
    }

    /**
     * Sets a discrete y-value for this variable.
     * @param index index of the crisp value.
     * @param value y-value.
     */
    public void setDiscreteY(int index, double value)
    {
        this.discrete_points[index].setY(value);
    }

    /**
     * Default value for this variable.
     * @param default_value default value.
     */
    public void setDefaultValue(double default_value)
    {
        this.default_value = default_value;
    }

    /**
     * Returns the default value.
     * @return default value.
     */
    public double getDefaultValue()
    {
        return this.default_value;
    }

    /**
     * Discretizes the crisp domain of the variable.  This is a one shot
     * deal for each output variable and helps to accelerate execution of
     * fuzzy rules.
     */
    public void discretize()
    {
        discrete_points = new DataPoint[num_discretes];

        //	Find min_x and max_x
        double min_x = Double.MAX_VALUE;
        double max_x = -Double.MAX_VALUE;

        for (MembershipFunction term : getTerms())
        {
            min_x = Math.min(min_x, term.getDataPoint(0).getX());
            max_x = Math.max(max_x, term.getDataPoint(term.getNumberOfDataPoints() - 1).
                getX());

            //	Reset them while we're at it.
            term.setNumberOfDiscreteValues(num_discretes);
        }

        //	Compute deltaX;
        double deltaX = max_x - min_x;

        if (num_discretes >= 2)
        {
            deltaX /= (double) (num_discretes - 1);
        }

        for (int i = 0; i < num_discretes; i++)
        {
            DataPoint p = new DataPoint(min_x + i * deltaX, 0.);
            discrete_points[i] = p;

            for (MembershipFunction term : getTerms())
            {
                term.setDiscreteY(i, term.calculateDOM(p.getX()));
            }
        }
    }

    /**
     * Reset the degree-of-membership values of the discrete set.
     */
    public void resetDiscretes()
    {
        for (DataPoint p : this.discrete_points)
        {
            p.setY(0.);
        }
    }

    /**
     * Defuzzifies the variable, returning a crisp result.
     *
     * @return crisp value.
     */
    public double defuzzify()
    {
        switch (this.defuzzification_method)
        {
            case COG:
            case COGS:
            {
                double sumMoments = 0.;
                double sumDOMS = 0.;
                double refX = discrete_points[0].getX();

                for (DataPoint p : this.discrete_points)
                {
                    sumMoments += p.getX() * p.getY();
                    sumDOMS += p.getY();
                }


                if (sumDOMS > 0.)
                {
                    this.crisp_value = sumMoments / sumDOMS;
                }
                else
                {
                    this.crisp_value = this.default_value;
                }

                break;
            }
            case COA:
            {
                double sumDOMS = 0.;

                for (DataPoint p : this.discrete_points)
                {
                    sumDOMS += p.getY();
                }

                if (sumDOMS <= 0.)
                {
                    this.crisp_value = this.default_value;
                    break;
                }

                //	Now go back and find the halfway point.
                double sumHalf = 0.;
                for (DataPoint p : this.discrete_points)
                {
                    sumHalf += p.getY();

                    if (sumHalf > 0.5 * sumDOMS)
                    {
                        this.crisp_value = p.getX();
                        break;
                    }
                }

                break;
            }
            default:
            {
                this.crisp_value = this.default_value;
            }
        }

        return this.crisp_value;
    }
}
