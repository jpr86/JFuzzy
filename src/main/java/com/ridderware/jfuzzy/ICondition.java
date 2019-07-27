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
 * Interface for a fuzzy logic condition.  The condition is 
 * the "if" part of a fuzzy rule:  if [condition] then [conclusion].
 *
 * @author Jeff Ridder
 */
public interface ICondition
{
    /**
     * Writes the condition to a string in Fuzzy Control Language.
     * @return String containing the FCL condition.
     */
    public String writeFCL();

    /**
     * Aggregates subconditions and returns the result.
     *@return result of aggregation.
     */
    public double aggregate();
}
