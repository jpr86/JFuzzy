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
 * A simple class to represent a 2-dimensional data point (x, y).
 *
 * @author Jeff Ridder
 */
public class DataPoint
{
    private double x;

    private double y;

    /**
     * Creates a new instance of DataPoint.
     * @param x x-value.
     * @param y y-value.
     */
    public DataPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-value of this data point.
     * @return x-value.
     */
    public double getX()
    {
        return this.x;
    }

    /**
     * Returns the y-value of this data point.
     * @return y-value.
     */
    public double getY()
    {
        return this.y;
    }

    /**
     * Sets the y-value for the point.
     * @param y y-value.
     */
    public void setY(double y)
    {
        this.y = y;
    }

    /**
     * Sets the x-value for the point.
     * @param x x-value.
     */
    public void setX(double x)
    {
        this.x = x;
    }
}
