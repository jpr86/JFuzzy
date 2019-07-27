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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class for fuzzy membership functions.
 * @author Jeff Ridder
 */
public class MembershipFunction
{
    private final ArrayList<DataPoint> points = new ArrayList<>();

    //  Name of the set.
    private String name;

    private double dom;

    private double[] discrete_y = new double[1];

    /**
     * Creates a new instance of MembershipFunction
     * @param name name of the membership function.
     */
    public MembershipFunction(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of the function.
     * @return name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Adds a data point to the membership function.
     * @param x x-value of the data point.
     * @param y y-value of the data point.
     */
    public void addDataPoint(double x, double y)
    {
        points.add(new DataPoint(x, y));

        Collections.sort(points, new DataPointsComparator());
    }

    /**
     * Returns the data point at the specified index.
     * @param index index of the data point.
     * @return the data point requested.
     */
    public DataPoint getDataPoint(int index)
    {
        return this.points.get(index);
    }

    /**
     * Returns the number of data points in the membership function.
     * @return number of data points.
     */
    public int getNumberOfDataPoints()
    {
        return this.points.size();
    }

    /**
     * Sets a discrete value for the membership function at the specified index.
     * @param index index at which to set the value.
     * @param y a discrete y value.
     */
    public void setDiscreteY(int index, double y)
    {
        discrete_y[index] = y;
    }

    /**
     * Sets the number of discrete values for the membership function.
     * @param discretes number of discrete values.
     */
    public void setNumberOfDiscreteValues(int discretes)
    {
        this.discrete_y = new double[discretes];
    }

    /**
     * Returns a discrete y-value at the index.
     * @param index index of the y-value requested.
     * @return discrete y-value.
     */
    public double getDiscreteY(int index)
    {
        return discrete_y[index];
    }

    /**
     * Returns the degree-of-membership last computed for this function.
     * @return fuzzy degree of membership.
     */
    public double getDOM()
    {
        return this.dom;
    }

    /**
     * Calculates the fuzzy degree of membership for the specified crisp input.
     * @param x input-value.
     * @return fuzzy degree of membership.
     */
    public double calculateDOM(double x)
    {
        if (x <= points.get(0).getX())
        {
            this.dom = points.get(0).getY();
        }
        else if (x >= points.get(points.size() - 1).getX())
        {
            this.dom = points.get(points.size() - 1).getY();
        }
        else
        {
            for (int i = 0; i < points.size() - 1; i++)
            {
                DataPoint p = points.get(i);
                DataPoint pp1 = points.get(i + 1);

                if (p.getX() < x && pp1.getX() >= x)
                {
                    this.dom = p.getY() + (pp1.getY() - p.getY()) * (x -
                        p.getX()) / (pp1.getX() - p.getX());
                    break;
                }
            }
        }

        return this.dom;
    }

    /**
     *  A comparator to support sorting of DataPoint objects
     */
    protected class DataPointsComparator implements Comparator<DataPoint>
    {
        /**
         *  Comparison function
         *
         * @param  o1 first data point for comparison.
         * @param  o2 second data point for comparison.
         * @return -1 if point 1 is < point 2, 0 if 1 = 2, +1 if 1 > 2
         */
        public int compare(DataPoint o1, DataPoint o2)
        {
            if (o1.getX() < o2.getX())
            {
                return -1;
            }
            else if (o1.getX() == o2.getX())
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
