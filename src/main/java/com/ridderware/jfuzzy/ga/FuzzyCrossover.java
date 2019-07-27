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
import com.ridderware.jevolve.Recombinator;
import com.ridderware.jrandom.MersenneTwisterFast;
import org.apache.logging.log4j.*;

/**
 * Crossover operator for the fuzzy GA.  Fuzzy GA individuals are variable
 * length, with a fixed length segment and a variable length segment.  We
 * need to take care to preserve this structure.
 *
 * @author Jeff Ridder
 */
public class FuzzyCrossover extends Recombinator
{
    private static final Logger logger = LogManager.getLogger(FuzzyCrossover.class);
    
    /** Creates a new instance of FuzzyCrossover */
    public FuzzyCrossover()
    {
    }
    
    /**
     * Performs recombination of parents to form two children.
     * @param parent1 dad
     * @param parent2 mom
     * @param child1 boy
     * @param child2 girl
     */
    public void recombine(Individual parent1, Individual parent2, Individual child1, Individual child2)
    {
        if ( MersenneTwisterFast.getInstance().nextDouble() > parent2.getProbRecombination() )
        {
            //  Then do a straight copy
            if ( child1 != null )
            {
                child1.deepCopy(parent1);
            }
            if ( child2 != null )
            {
                child2.deepCopy(parent2);
            }
        }
        else
        {
            FuzzyGAIndividual dad = (FuzzyGAIndividual)parent1;
            FuzzyGAIndividual mom = (FuzzyGAIndividual)parent2;
            FuzzyGAIndividual boy = (FuzzyGAIndividual)child1;
            FuzzyGAIndividual girl = (FuzzyGAIndividual)child2;
            
            boolean fixed = true;
            int xoverPoint1, xoverPoint2;
            
            int boyrules, girlrules;
            
            //  Need to ensure that we don't exceed the min/max rules bounds.
            do
            {
                xoverPoint1 = MersenneTwisterFast.getInstance().nextInt(dad.getGenotypeSize()-1);
                
                xoverPoint2 = xoverPoint1;
                
                fixed = true;
                if ( xoverPoint1 > dad.getFixedGenotypeSize() )
                {
                    xoverPoint2 = mom.getFixedGenotypeSize()+MersenneTwisterFast.getInstance().nextInt(mom.getRulesGenotypeSize()-1);
                    fixed = false;
                }
                
                //  Calculate number of rules children will have
                boyrules = xoverPoint1-dad.getFixedGenotypeSize() + mom.getGenotypeSize() - xoverPoint2;
                girlrules = xoverPoint2-mom.getFixedGenotypeSize() + dad.getGenotypeSize()-xoverPoint1;
            }
            while(boyrules > mom.getMaxRules() || boyrules < mom.getMinRules() ||
                    girlrules > mom.getMaxRules() || girlrules < mom.getMinRules() );
            
            if ( boy != null )
            {
                boy.getFixedGenotype().clear();
                boy.getRulesGenotype().clear();
            }
            
            if ( girl != null )
            {
                girl.getFixedGenotype().clear();
                girl.getRulesGenotype().clear();
            }
            
            
            if ( fixed )
            {
                //  Crossover will happen in the fixed length part
                for ( int i = 0; i < mom.getFixedGenotypeSize(); i++ )
                {
                    if ( i <= xoverPoint1 )
                    {
                        boy.getFixedGenotype().add(dad.getFixedGenotype().get(i));
                        girl.getFixedGenotype().add(mom.getFixedGenotype().get(i));
                    }
                    else
                    {
                        boy.getFixedGenotype().add(mom.getFixedGenotype().get(i));
                        girl.getFixedGenotype().add(dad.getFixedGenotype().get(i));
                    }
                }
                
                //  Rules completely crossed over
                for ( RuleGene rule : mom.getRulesGenotype() )
                {
                    boy.getRulesGenotype().add(rule.clone());
                }
                for ( RuleGene rule : dad.getRulesGenotype() )
                {
                    girl.getRulesGenotype().add(rule.clone());
                }
                
                if ( boy.getRulesGenotypeSize() != mom.getRulesGenotypeSize() ||
                        girl.getRulesGenotypeSize() != dad.getRulesGenotypeSize() )
                    logger.error("Fixed segment crossover failed doing rules copy");
            }
            else
            {
                //  Crossover is in the rules segment
                //  First, copy the fixed segments to the right places
                boy.getFixedGenotype().addAll(mom.getFixedGenotype());
                girl.getFixedGenotype().addAll(dad.getFixedGenotype());
                
                //  Next, deal with crossover of dad's rules
                for ( int i = 0; i < dad.getRulesGenotypeSize(); i++ )
                {
                    RuleGene g = dad.getRulesGenotype().get(i).clone();
                    
                    if ( i < xoverPoint1-dad.getFixedGenotypeSize() )
                    {
                        boy.getRulesGenotype().add(g);
                    }
                    else
                    {
                        girl.getRulesGenotype().add(g);
                    }
                }
                
                //  Now crossover mom's rules
                for ( int i = 0; i < mom.getRulesGenotypeSize(); i++ )
                {
                    RuleGene g = mom.getRulesGenotype().get(i).clone();
                    
                    if ( i < xoverPoint2 - mom.getFixedGenotypeSize() )
                    {
                        girl.getRulesGenotype().add(g);
                    }
                    else
                    {
                        boy.getRulesGenotype().add(g);
                    }
                }
                
                if ( boy.getRulesGenotypeSize() > boy.getMaxRules() )
                    logger.error("Crossover exceeded max rules for child: "+boy.getRulesGenotypeSize());
                if ( girl.getRulesGenotypeSize() > girl.getMaxRules())
                    logger.error("Crossover exceeded max rules for child: "+girl.getRulesGenotypeSize());
                if ( boy.getRulesGenotypeSize() < boy.getMinRules() )
                    logger.error("Crossover generated too few rules for child: "+boy.getRulesGenotypeSize());
                if ( girl.getRulesGenotypeSize() < girl.getMinRules() )
                    logger.error("Crossover generated too few rules for child: "+girl.getRulesGenotypeSize());
                
                if ( boy.getRulesGenotypeSize()+girl.getRulesGenotypeSize() != mom.getRulesGenotypeSize()+dad.getRulesGenotypeSize() )
                    logger.error("Total rules not conserved in crossover.");
            }
            
            boy.setEvaluated(false);
            girl.setEvaluated(false);
        }
    }
}
