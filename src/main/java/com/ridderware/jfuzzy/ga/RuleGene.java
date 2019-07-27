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
import com.ridderware.jevolve.DiscreteDoubleGAGene;
import com.ridderware.jevolve.DoubleGAGene;
import com.ridderware.jfuzzy.*;
import com.ridderware.jevolve.GAGene;
import com.ridderware.jrandom.MersenneTwisterFast;
import java.util.ArrayList;
import org.apache.logging.log4j.*;

/**
 * Class to represent a fuzzy rule as a genetic algorithm gene.
 * The rule-base will be created from a collection of rule genes.
 * Every rule can select up to N conditions and 1 conclusion, where N is the
 * number of input variables in the rule-base.  In addition, each rule may 
 * specify a firing weight.
 *
 * Mutation may change the term of each input variable (or remove the variable
 * from the conditions) -- 2 discrete genes per variable to capture the term and
 * NOT condition.
 *
 * Mutation may also change the output variable, the term, and the firing weight.
 * 1 discrete gene for the output variable.  1 discrete gene for the terms of
 * each possible output variable (i.e., if M output variables, then M genes to
 * capture the number of terms per output variable).  And 1 continuous gene for the weight.
 *
 * Each RuleGene is a sub-section of the rule-base genome, but is represented
 * on the rule-base genome as a single gene.  This prevents destructive crossover
 * within a rule, relying solely on mutation to refine rules and crossover to 
 * determine which rules belong to which rule-bases (individuals).
 *
 * The rules, conditions, and conclusions will be assembled into the
 * rule-base upon decoding the genotype.
 *
 * @author Jeff Ridder
 */
public class RuleGene implements Cloneable
{
    //  The pool of input/output variables.
    private Variable[] input_variables;
    private OutputVariable[] output_variables;
    
    //  This is the subgenotype.  Should it be in the gene or in the individual?
    //  I think it goes here, but it is not really a GAGene object.
    private ArrayList<GAGene<Double>> subgenome = new ArrayList<GAGene<Double>>();
    
    private ArrayList<Double> subgenotype = new ArrayList<Double>();
    
    private static final Logger logger = LogManager.getLogger(RuleGene.class);
    
    /**
     * Creates a new instance of RuleGene
     * @param input_variables a Java array of input variables.
     * @param output_variables a Java array of output variables. 
     */
    public RuleGene(Variable[] input_variables, OutputVariable[] output_variables)
    {
        this.input_variables = input_variables;
        this.output_variables = output_variables;
    }
    
    /**
     * Clones the gene.
     * @return a clone of this gene.
     */
    @Override
    public RuleGene clone()
    {
        try
        {
            RuleGene obj = (RuleGene) super.clone();
            
            obj.input_variables = this.input_variables;
            
            obj.output_variables = this.output_variables;
            
            obj.encodeRule();
            
            obj.subgenotype = (ArrayList<Double>) this.subgenotype.clone();
            
            return obj;
        }
        catch(CloneNotSupportedException e)
        {
            logger.error("Clone not supported exception.");
            throw new InternalError(e.toString());
        }
    }
    
    /**
     * Deep copies the gene.
     * @param obj object from which to deep copy.
     */
    public void deepCopy(RuleGene obj)
    {
        this.input_variables = obj.input_variables;
        this.output_variables = obj.output_variables;
        this.encodeRule();
        this.subgenotype = (ArrayList<Double>)obj.subgenotype.clone();
    }
    
    /**
     * Randomly initialize the rule.
     */
    public void initialize()
    {
        subgenotype.clear();
        
        this.encodeRule();
        
        for ( GAGene<Double> g : subgenome )
        {
            subgenotype.add(g.randomUniformValue());
        }
    }
    
    /**
     * Mutate the rule.  Don't bother with probability of mutation here.
     * If this is called by FuzzyMutator, then the rule was selected for mutation.  
     * Randomly pick a single gene value and mutate it.
     */
    public void mutate()
    {
        int index = MersenneTwisterFast.getInstance().nextInt(subgenome.size());
        
        subgenotype.set(index,subgenome.get(index).randomGaussianValue(subgenotype.get(index)));
    }
    
    /**
     * Encodes the subgenome based on the input and output variables.
     */
    protected void encodeRule()
    {
        subgenome.clear();
        DiscreteDoubleGAGene gene;
        
        //  Loop over the input variables and add 2 discrete genes for each.
        for ( Variable ivar : this.input_variables )
        {
            gene = new DiscreteDoubleGAGene();
            subgenome.add(gene);
            //  -1 will be interpreted as turning the variable off for this rule-condition
            gene.addAllele(-1.);
            for ( int i = 0; i < ivar.getTerms().length; i++ )
            {
                gene.addAllele((double)i);
            }
            
            //  Not condition
            gene = new DiscreteDoubleGAGene();
            subgenome.add(gene);
            gene.addAllele(0.);
            gene.addAllele(1.);
        }
        
        //  Add a discrete gene for selection of the output variable.
        gene = new DiscreteDoubleGAGene();
        subgenome.add(gene);
        for ( int i = 0; i < output_variables.length; i++ )
        {
            gene.addAllele((double)i);
        }
        
        //  Now loop over the output variables, and add a discrete gene for the terms
        //  of each possible output variable.
        for ( OutputVariable ovar : this.output_variables )
        {
            gene = new DiscreteDoubleGAGene();
            subgenome.add(gene);
            for ( int i = 0; i < ovar.getTerms().length; i++ )
            {
                gene.addAllele((double)i);
            }
        }
        
        //  Add a gene for firing weight.
        DoubleGAGene dgene = new DoubleGAGene(0., 1., 0.1);
        subgenome.add(dgene);
    }
    
    /**
     * Decodes and returns the rule for the current genotype.
     * This rule will be added to the rule-base.
     *
     * @return the decoded rule.
     */
    public Rule decodeRule()
    {
        Rule rule = null;
        
        //  Decode the conditions first
        ArrayList<SubCondition> subs = new ArrayList<SubCondition>();
        
        int index = 0;
        for ( Variable ivar : input_variables )
        {
            int term = subgenotype.get(index++).intValue();
            boolean bnot = false;
            if ( subgenotype.get(index++).intValue() > 0 )
                bnot = true;
            
            if ( term >= 0 )
            {
                SubCondition sub = new SubCondition(ivar, term, bnot);
                subs.add(sub);
            }
        }
        
        ICondition root = null;
        if ( subs.size() == 1)
        {
            root = subs.get(0);
        }
        else if ( subs.size() > 1 )
        {
            And leaf = new And(subs.get(subs.size()-1), subs.get(subs.size()-2));
            And and = leaf;
            
            //  # of ands always # simples-1
            for ( int i = subs.size()-3; i >= 0; i-- )
            {
                and = new And(subs.get(i), and);
            }
            
            root = and;
        }
        
        //  Now get the conclusion variable.
        int conc_var = subgenotype.get(index++).intValue();
        OutputVariable ovar = output_variables[conc_var];
        
        //  Now get the term value corresponding to this variable.
        
        int term_index = subgenotype.get(index+conc_var).intValue();
        
        //  Now get firing weight.
        int weight_index = index + output_variables.length;
        if ( weight_index != subgenotype.size()-1 )
            logger.error("Decoding indices not right!");
        
        double weight = subgenotype.get(weight_index);
        
        if ( weight < 0. || weight > 1. )
            logger.error("Firing weight exceeds bounds: "+weight);
        
        Conclusion conc = new Conclusion(ovar, term_index, weight);
        
        if ( root == null )
            rule = null;
        else
            rule = new Rule(root, conc);
        
        return rule;
    }
}
