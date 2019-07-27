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

package com.ridderware.jfuzzy.ga.examples.flyga;

import com.ridderware.jevolve.Individual;
import com.ridderware.jevolve.Population;
import com.ridderware.jevolve.EvaluatorInterface;
import com.ridderware.jfuzzy.RuleBase;
import com.ridderware.jfuzzy.ga.FuzzyGAIndividual;

/**
 * A GA evaluator for the flyga example.
 *
 *  @author Jeff Ridder
 */
public class flygaEvaluator implements EvaluatorInterface
{
    double[] x = new double[60];
    double[] y = new double[60];
    
    /**
     *  Evaluates constraints.  Does nothing for this example.
     *
     * @param  ind individual to evaluate.
     */
    public void evaluateConstraints(Individual ind)
    {
    }
    
    
    /**
     * Preevaluates the population.  Creates a test set of angle-of-arrival
     * input values and desires roll-angle outputs.
     *
     * @param  pop population to preevaluate.
     */
    public void preevaluate(Population pop)
    {
        //	Create the test set
        for ( int i = 0; i < 60; i++ )
        {
            x[i] = -180. + i*360./59.;
            
            y[i] = -60. + i*120./59.;
        }
    }
    
    
    /**
     * Postevaluates the population.  Does nothing.
     *
     * @param  pop population to postevaluate.
     */
    public void postevaluate(Population pop)
    {
    }
    
    
    /**
     * Evaluates fitness.  For this example, fitness is determined to be the accumulated
     * difference between the desired and actual roll-angle outputs across a range
     * of angle-of-arrival input values.
     *
     * @param  ind individual to be evaluated.
     */
    public void evaluateFitness(Individual ind)
    {
        FuzzyGAIndividual ga_ind = (FuzzyGAIndividual) ind;
        
        if (ga_ind.getEvaluated())
        {
            return;
        }
        
        double fitness = 0.;
        
        ga_ind.assemble();
        
        RuleBase a = ga_ind.getRuleBase();
        for ( int i = 0; i < 60; i++ )
        {
            a.getInputVariable("AoA").fuzzify(x[i]);
            
            a.evaluateRules();
            
            fitness += Math.abs(a.getCrispOutput("Roll Angle")-y[i]);
        }
        
        ga_ind.setFitness(fitness);
        
    }
}

