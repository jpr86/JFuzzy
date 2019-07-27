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

import com.ridderware.jevolve.*;
import com.ridderware.jfuzzy.*;
import com.ridderware.jfuzzy.ga.*;
import java.io.File;
import org.apache.logging.log4j.*;

/**
 * Implements and executes the fuzzy GA for this example.
 *
 * @author Jeff Ridder
 */
public class flygaMain
{
    private static final Logger logger = LogManager.getLogger(flygaMain.class);
            
    /**
     * The main function.
     *
     * @param args command line arguments.
     */
    public static void main(String args[])
    {
        RuleBase a = new RuleBase();
        
        File fcl = new File(args[0]);
        a.readFCL(fcl);
        //  Get the output variable and discretize
        OutputVariable ovar = a.getOutputVariable("Roll Angle");
        ovar.setNumDiscretes(361);
        ovar.discretize();
        
        FuzzyGAIndividual ind = new FuzzyGAIndividual(a);
        
        if (ind == null)
        {
            System.err.println("Holy Moly What's Going On?");
            System.err.println("--> My Individual is null");
        }
        
        ind.setMinMaxRules(7, 30);
        ind.setMaxInitialRules(15);
        
        ind.setProbMutation(0.1);
        ind.setProbRecombination(0.7);
        
        SimpleProblem prob = new SimpleProblem();
        
        prob.addBreeder(new SimpleGenerationalBreeder(new TournamentSelection(),
                new FuzzyCrossover(), new FuzzyMutator()));
        prob.addInitializer(new SimpleInitializer());
        prob.addEvaluator(new flygaEvaluator());
        prob.setStatistics(new Statistics("flygastats.txt"));
        
        Population pop = new Population(ind);
        
        pop.setMaxPopulationSize(100);
        
        prob.addPopulation(pop);
        pop.setElitist(true);
        pop.setMaxNumberOfElites(1);
        
        Stepper stepper = new Stepper();
        stepper.addProblem(prob);
        stepper.setMaxGenerations(500);
        
        stepper.setScreenOutput(true);
        stepper.evolve();
        
        FuzzyGAIndividual best = (FuzzyGAIndividual)prob.getStatistics().getBestEver();
        
        best.assemble();
        
        File fclout = new File("bestfly.fcl");
        
        best.getRuleBase().writeFCL(fclout);
    }
}


