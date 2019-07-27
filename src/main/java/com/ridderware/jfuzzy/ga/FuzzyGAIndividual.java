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
import com.ridderware.jevolve.GAGene;
import com.ridderware.jevolve.Individual;
import com.ridderware.jfuzzy.And;
import com.ridderware.jfuzzy.MembershipFunction;
import com.ridderware.jfuzzy.OutputVariable;
import com.ridderware.jfuzzy.Rule;
import com.ridderware.jfuzzy.RuleBase;
import com.ridderware. jfuzzy.Variable;
import com.ridderware.jrandom.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.apache.logging.log4j.*;

/**
 * Class for an individual describing the search space for evolving a fuzzy
 * rule base.  This includes selection of conditions, conclusions, firing weights,
 * membership functions, and operators.  The fuzzy ga individual has fixed and
 * variable length segments (making it variable length overall).  The fixed
 * length segment contains membership function values (continuous genes), and operator
 * selection (discrete).  The variable length segment represents each rule as
 * a separate gene.  Crossover between individuals allows for rules to be
 * swapped.  Mutation of a rule gene causes that rule to be internally mutated.
 *
 * To use this class, create an individual, passing the rule-base to be generated
 * as an argument.  Initializing automatically encodes the individual and randomizes
 * the initial values.  To evaluate an individual, call assemble to assemble the rules for
 * the rule base.  You can then evaluate the rules of the rule-base to evaluate and
 * assign fitness.
 *
 * @author Jeff Ridder
 */
public class FuzzyGAIndividual extends Individual
{
    private RuleBase rule_base;
    
    //  Fixed length part of the genome.
    private ArrayList<GAGene<Double>> fixed_genome = new ArrayList<GAGene<Double>>();
    
    //  Fixed length part of the genotype.
    private ArrayList<Double> fixed_genotype = new ArrayList<Double>();
    
    private ArrayList<RuleGene> rule_genes = new ArrayList<RuleGene>();
    
    private int max_rules;
    
    private int min_rules;
    
    private int max_initial_rules;
    
    private static final Logger logger = LogManager.getLogger(FuzzyGAIndividual.class);
    
    /**
     * Creates a new instance of FuzzyGAIndividual
     * @param rule_base the rule base that this individual represents.
     */
    public FuzzyGAIndividual(RuleBase rule_base)
    {
        super();
        this.rule_base = rule_base;
        this.max_rules = 10;
        this.min_rules = 3;
        this.max_initial_rules = 10;
    }
    
    /**
     * Creates a new instance of FuzzyGAIndividual for multi-objective GA (MOGA).
     * @param numObjectives number of objectives for this individual.
     * @param rule_base the rule base that this individual represents.
     */
    public FuzzyGAIndividual(int numObjectives, RuleBase rule_base)
    {
        super(numObjectives);
        
        this.rule_base = rule_base;
        this.max_rules = 10;
        this.min_rules = 3;
        this.max_initial_rules = 10;
    }
    
    /**
     * Returns the rule base represented by this individual.
     * @return the rule base.
     */
    public RuleBase getRuleBase()
    {
        return this.rule_base;
    }
    
    /**
     * Sets the minimum and maximum number of rules to be contained by the rule base.
     * @param min_rules minimum number of rules.
     * @param max_rules maximum number of rules.
     */
    public void setMinMaxRules(int min_rules, int max_rules)
    {
        this.min_rules = min_rules;
        this.max_rules = max_rules;
    }
    
    /**
     * Returns the maximum number of rules to be contained by the rule base.
     * @return maximum number of rules.
     */
    public int getMaxRules()
    {
        return this.max_rules;
    }
    
    /**
     * Returns the minimum number of rules to be contained by the rule base.
     * @return minimum number of rules.
     */
    public int getMinRules()
    {
        return this.min_rules;
    }
    
    /**
     * Sets the maximum number of rules to be created during initialization.
     * @param max_initial_rules the maximum number of initial rules.
     */
    public void setMaxInitialRules(int max_initial_rules)
    {
        this.max_initial_rules = max_initial_rules;
    }
    
    /**
     * Returns the size of the fixed-length section of the genotype.
     * @return size of fixed-length portion of genotype.
     */
    public int getFixedGenotypeSize()
    {
        return this.fixed_genotype.size();
    }
    
    /**
     * Returns the size of the rules section (variable length) of the genotype.
     * @return size of the rules section of the genotype.
     */
    public int getRulesGenotypeSize()
    {
        return this.rule_genes.size();
    }
    
    /**
     * Returns the size of the full genotype (fixed and variable length sections).
     * @return size of the genotype.
     */
    public int getGenotypeSize()
    {
        return fixed_genotype.size()+this.rule_genes.size();
    }
    
    /**
     * Returns the fixed-length section of the genotype.
     * @return an ArrayList of double values.
     */
    public ArrayList<Double> getFixedGenotype()
    {
        return this.fixed_genotype;
    }
    
    /**
     * Returns the fixed-length portion of the genome.
     * @return an ArrayList of genes.
     */
    public ArrayList<GAGene<Double>> getFixedGenome()
    {
        return this.fixed_genome;
    }
    
    /**
     * Returns the rules section (variable length) of the genotype.
     * @return an ArrayList of rule genes
     */
    public ArrayList<RuleGene> getRulesGenotype()
    {
        return this.rule_genes;
    }
    
    /**
     * Called by jevolve to initialize the individual.
     */
    public void initialize()
    {
        this.encodeFixed();
        
        //  Loop over the genome for fixed length part.
        //  Then randomly generate and initialize rules for the variable length part.
        fixed_genotype.clear();
        
        for (GAGene<Double> g : fixed_genome)
        {
            fixed_genotype.add(g.randomUniformValue());
        }
        
        this.rule_genes.clear();
        int num_rules = this.min_rules + MersenneTwisterFast.getInstance().nextInt(this.max_initial_rules-this.min_rules+1);
        for ( int i = 0; i < num_rules; i++ )
        {
            RuleGene rg = new RuleGene(rule_base.getInputVariables(), rule_base.getOutputVariables());
            rg.initialize();
            this.rule_genes.add(rg);
        }
    }
    
    /**
     * Encodes the fixed length part of the genome, including membership functions.
     */
    protected void encodeFixed()
    {
        this.fixed_genome.clear();
        
        //  Encode input variables
        for ( Variable ivar : rule_base.getInputVariables() )
        {
            this.encodeVariable(ivar);
        }
        
        //  Encode output variables, including defuzzification method
        for ( OutputVariable ovar : rule_base.getOutputVariables() )
        {
            this.encodeVariable(ovar);
            
            //	Defuzzification method
            DiscreteDoubleGAGene g = new DiscreteDoubleGAGene();
            g.addAllele(0.);
            g.addAllele(1.);
            g.addAllele(2.);
            
            this.fixed_genome.add(g);
        }
        
        //  Add genes for And, activation, and accumulation
        
        //	Activation:
        DiscreteDoubleGAGene g = new DiscreteDoubleGAGene();
        g.addAllele(0.);
        g.addAllele(1.);
        this.fixed_genome.add(g);
        
        //	Accumulation:
        g = new DiscreteDoubleGAGene();
        g.addAllele(0.);
        g.addAllele(1.);
        this.fixed_genome.add(g);
        
        //	And
        g = new DiscreteDoubleGAGene();
        g.addAllele(0.);
        g.addAllele(1.);
        g.addAllele(2.);
        this.fixed_genome.add(g);
    }
    
    /**
     * Decodes the fixed length part of the genome, including membership functions.
     */
    protected void decodeFixed()
    {
        int index = 0;
        
        //  Loop over input variables and decode them.
        for ( Variable ivar : this.rule_base.getInputVariables() )
        {
            index = decodeVariable(ivar, index);
        }
        
        //  Decode output variables
        for ( OutputVariable ovar : this.rule_base.getOutputVariables() )
        {
            index = decodeVariable(ovar, index);
            
            ovar.discretize();
            
            int defuzz = this.fixed_genotype.get(index++).intValue();
            ovar.setDefuzzificationMethod(OutputVariable.DefuzzificationMethod.values()[defuzz]);
        }
        
        //  Activation method
        int act = this.fixed_genotype.get(index++).intValue();
        this.rule_base.setActivationMethod(RuleBase.ActivationMethod.values()[act]);
        
        //  Accumulation
        int accu = this.fixed_genotype.get(index++).intValue();
        this.rule_base.setAccumulationMethod(RuleBase.AccumulationMethod.values()[accu]);
        
        //  And
        int and = this.fixed_genotype.get(index++).intValue();
        this.rule_base.setAndOperator(And.FuzzyAndOperator.values()[and]);
    }
    
    /**
     * Encodes the membership functions of the variable.  Many different ways
     * to go about doing this:
     * 1) Require membership function endpoints to be anchored to each other.
     * 2) Require only triangles.
     * 3) Let triangles float freely (not anchored to each other) -- this
     *    could result in uncovered patches.
     *
     * In this implementation we assume that membership functions are independent
     * (not anchored to each other -- option 3 above) and are described by triangles.
     * In this case, the number of interior data points to be determined is: (#terms - 2)*3 + 2
     *
     * @param var the fuzzy variable to be encoded.
     */
    protected void encodeVariable(Variable var)
    {
        //  First, calculate the number of interior points.
        int ninterior = var.getTerms().length-2;
        int npoints = ninterior*3+2;
        
        //  Now, find min_x and max_x to set the bounds
        double min_x = Double.MAX_VALUE;
        double max_x = -Double.MAX_VALUE;
        
        for ( MembershipFunction term : var.getTerms() )
        {
            min_x = Math.min(min_x, term.getDataPoint(0).getX());
            max_x = Math.max(max_x, term.getDataPoint(term.getNumberOfDataPoints()-1).getX());
        }
        
        //  First, create genes for the single points associated with the end functions
        DoubleGAGene g = new DoubleGAGene(min_x, max_x, (max_x-min_x)/20.);
        this.fixed_genome.add(g);
        
        g = new DoubleGAGene(min_x, max_x, (max_x-min_x)/20.);
        this.fixed_genome.add(g);
        
        //  Now create genes for the interior membership functions
        for ( int i = 0; i < ninterior; i++ )
        {
            //  First, the peak point
            g = new DoubleGAGene(min_x, max_x, (max_x-min_x)/20.);
            this.fixed_genome.add(g);
            
            //  Now, two points relative to that.  First is the point to the left.
            //  Second is point to the right.
            g = new DoubleGAGene(0., max_x-min_x, (max_x-min_x)/20.);
            this.fixed_genome.add(g);
            g = new DoubleGAGene(0., max_x-min_x, (max_x-min_x)/20.);
            this.fixed_genome.add(g);
        }
    }
    
    /**
     * Decodes the membership functions for the variable, starting at the specified
     * index, then returns the index of the next location in the genotype.
     * This decoder assumes that the number of interior points
     * for the variable = (#terms-2)*3+2, and that all membership functions
     * are triangles.  The decoder pulls the points for each membership function
     * from the genome, sorts them to be increasing, then sets the x-values
     * of each data point in the membership function.
     * @param var the fuzzy variable to be decoded.
     * @param index the genotype index at which to find this variable.
     * @return the next index in the genotype.
     */
    protected int decodeVariable(Variable var, int index)
    {
        //  First, calculate the number of interior points.
        int ninterior = var.getTerms().length-2;
        int npoints = ninterior*3+2;
        
        //  Get the two end points
        double p = this.fixed_genotype.get(index);
        var.getTerm(0).getDataPoint(1).setX(p);
        p = this.fixed_genotype.get(index+1);
        var.getTerm(var.getTerms().length-1).getDataPoint(0).setX(p);
        
        //  Now read in the interior points
        ArrayList<Triplet> mfuncs = new ArrayList<Triplet>();
        for ( int i = 0; i < ninterior; i++ )
        {
            p = this.fixed_genotype.get(index+2+i*3);
            double left = this.fixed_genotype.get(index+3+i*3);
            double right = this.fixed_genotype.get(index+4+i*3);
            
            mfuncs.add(new Triplet(p, left, right));
        }
        
        Collections.sort(mfuncs, new TripletsComparator());
        
        double min_x = var.getTerm(0).getDataPoint(0).getX();
        double max_x = var.getTerm(var.getTerms().length-1).getDataPoint(1).getX();
        //  Now loop over the sorted mfuncs
        for ( int i = 0; i < ninterior; i++ )
        {
            Triplet t = mfuncs.get(i);
            
            var.getTerm(i+1).getDataPoint(1).setX(t.p);
            
            double left = Math.max(t.p-t.left, min_x);
            double right = Math.min(t.p+t.right, max_x);
            
            var.getTerm(i+1).getDataPoint(0).setX(left);
            var.getTerm(i+1).getDataPoint(2).setX(right);
        }
        
        index += npoints;
        
        return index;
    }
    
    /**
     * Class to define a triplet.  This is used as a helper for decoding membership
     * functions and sorting them.
     * @author Jeff Ridder
     */
    protected class Triplet
    {
        /** p */
        public double p;
        /** left */
        public double left;
        /** right */
        public double right;
        
        /**
         * Creates a new instance of Triplet.
         * @param p p.
         * @param left left.
         * @param right right.
         */
        public Triplet(double p, double left, double right)
        {
            this.p = p;
            this.left = left;
            this.right = right;
        }
    }
    
    /**
     *  A comparator to support sorting of Triplet objects
     */
    protected class TripletsComparator implements Comparator<Triplet>
    {
        /**
         *  Comparison function
         *
         * @param  o1 first triplet for comparison.
         * @param  o2 second triplet for comparison.
         * @return -1 if point 1 is < point 2, 0 if 1 = 2, +1 if 1 > 2
         */
        public int compare(Triplet o1, Triplet o2)
        {
            if (o1.p < o2.p)
            {
                return -1;
            }
            else if (o1.p == o2.p)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
    
    /**
     * Assembles the rule base by decoding the genotype.
     */
    public void assemble()
    {
        this.decodeFixed();
        
        this.rule_base.clearRules();
        
        //  Loop over all rule genes
        for ( RuleGene g : this.rule_genes )
        {
            Rule r = g.decodeRule();
            
            if ( r != null )
            {
                this.rule_base.addRule(r);
            }
        }
    }
    
    /**
     * A function requiring implementation from the base class.  This always returns
     * 0 (i.e., it is not used in the fuzzy GA).
     * @return 0.
     * @param ind individual to compute the distance to.
     */
    public double genotypeDistance(Individual ind)
    {
        return 0.;
    }
    
    /**
     * Clones the individual.
     * @return clone of this individual.
     */
    @Override
    public FuzzyGAIndividual clone()
    {
        FuzzyGAIndividual obj = (FuzzyGAIndividual) super.clone();
        
        obj.fixed_genome = this.fixed_genome;
        
        obj.fixed_genotype = (ArrayList<Double>) this.fixed_genotype.clone();
        
        obj.rule_base = this.rule_base;
        
        obj.rule_genes = new ArrayList<RuleGene>();
        
        obj.rule_genes.clear();
        
        for ( RuleGene g : this.rule_genes )
        {
            obj.rule_genes.add(g.clone());
        }
        
        return obj;
    }
    
    /**
     * Sets the attributes of this via a deep copy of the specified object.
     * @param obj object to be copied.
     */
    @Override
    public void deepCopy(Individual obj)
    {
        super.deepCopy(obj);
        
        FuzzyGAIndividual ga_obj = (FuzzyGAIndividual) obj;
        
        this.fixed_genome = ga_obj.fixed_genome;
        
        this.fixed_genotype = (ArrayList<Double>) ga_obj.fixed_genotype.clone();
        
        this.rule_base = ga_obj.rule_base;
        
        this.rule_genes.clear();
        
        for ( RuleGene g : ga_obj.rule_genes )
        {
            this.rule_genes.add(g.clone());
        }
    }
}
