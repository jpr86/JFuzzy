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

package com.ridderware.jfuzzy.ga;

import com.ridderware.jevolve.Individual;
import com.ridderware.jevolve.Mutator;
import com.ridderware.jrandom.MersenneTwisterFast;

/**
 * Mutation operator for the fuzzy GA.  If mutation occurs in the fixed-length
 * section of the individual, then Gaussian mutation is applied.  If mutation occurs
 * on a rule gene, then the we call the mutate method of the RuleGene class.
 * @author Jeff Ridder
 */
public class FuzzyMutator extends Mutator
{
    
    /** Creates a new instance of FuzzyMutator */
    public FuzzyMutator()
    {
    }

    /**
     * Mutates the individual.
     * @param ind individual to be mutated.
     */
    public void mutate(Individual ind)
    {
        FuzzyGAIndividual ga = (FuzzyGAIndividual)ind;
        
        for ( int i = 0; i < ga.getGenotypeSize(); i++ )
        {
            if ( MersenneTwisterFast.getInstance().nextDouble() <= ind.getProbMutation() )
            {
                ga.setEvaluated(false);
                
                //  See if we're in the fixed or variable length segment
                if ( i < ga.getFixedGenotypeSize() )
                {
                    ga.getFixedGenotype().set(i, ga.getFixedGenome().get(i).randomGaussianValue(ga.getFixedGenotype().get(i)));
                }
                else
                {
                    int index = i-ga.getFixedGenotypeSize();
                    ga.getRulesGenotype().get(index).mutate();
                }
            }
        }
    }    
}
