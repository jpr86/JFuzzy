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

package com.ridderware.jfuzzy.examples.fly;

import com.ridderware.jfuzzy.*;
import java.io.File;

/**
 * A simple example of a fuzzy logic controller for an airplane that homes in on
 * a radar by adjusting its roll angle based on the angle-of-arrival of a signal.
 *
 * @author Jeff Ridder
 */
public class flyMain
{
    
    /** Creates a new instance of flyMain */
    public flyMain()
    {
    }

    /**
     * The main function.
     * @param args command line arguments.
     */
    public static void main(String[] args)
    {
        RuleBase a = new RuleBase();
        
	//	Add input variables
	Variable aoa = new Variable("AoA");
        a.addInputVariable(aoa);
        
        Variable amp_slope = new Variable("Amplitude Slope");
        a.addInputVariable(amp_slope);      

	//	Create terms for the Ao
        MembershipFunction fn = new MembershipFunction("Rear Left");
        fn.addDataPoint(-180., 1.);
        fn.addDataPoint(-90.,0.);
        aoa.addTerm(fn);
        
        fn = new MembershipFunction("Left");
        fn.addDataPoint(-135., 0.);
        fn.addDataPoint(-90., 1.);
        fn.addDataPoint(-45., 0.);
        aoa.addTerm(fn);
        
        fn = new MembershipFunction("Front Left");
        fn.addDataPoint(-90., 0.);
        fn.addDataPoint(-45., 1.);
        fn.addDataPoint(0., 0.);
        aoa.addTerm(fn);
        
        fn = new MembershipFunction("Front");
        fn.addDataPoint(-20.,0.);
        fn.addDataPoint(0.,1.);
        fn.addDataPoint(20.,0.);
        aoa.addTerm(fn);

        fn = new MembershipFunction("Front Right");
        fn.addDataPoint(0., 0.);
        fn.addDataPoint(45., 1.);
        fn.addDataPoint(90., 0.);
        aoa.addTerm(fn);
        
        fn = new MembershipFunction("Right");
        fn.addDataPoint(45., 0.);
        fn.addDataPoint(90., 1.);
        fn.addDataPoint(135., 0.);
        aoa.addTerm(fn);
        
        fn = new MembershipFunction("Rear Right");
        fn.addDataPoint(90.,0.);
        fn.addDataPoint(180.,1.);
        aoa.addTerm(fn);

	//	Create terms for the Amplitude Slope
        fn = new MembershipFunction("Decreasing");
        fn.addDataPoint(-100.,1.);
        fn.addDataPoint(0., 0.);
        amp_slope.addTerm(fn);
        
        fn = new MembershipFunction("Neutral");
        fn.addDataPoint(-10.,0.);
        fn.addDataPoint(0.,1.);
        fn.addDataPoint(10.,0.);
        amp_slope.addTerm(fn);
        
        fn = new MembershipFunction("Increasing");
        fn.addDataPoint(0.,0.);
        fn.addDataPoint(100.,1.);
        amp_slope.addTerm(fn);

	//	Add output variables
        OutputVariable roll_angle = new OutputVariable("Roll Angle");
        a.addOutputVariable(roll_angle);

	//	Create terms for the roll angle.
        fn = new MembershipFunction("Hard Left");
        fn.addDataPoint(-60.,1.);
        fn.addDataPoint(-30.,0.);
        roll_angle.addTerm(fn);
        
        fn = new MembershipFunction("Left");
        fn.addDataPoint(-45.,0.);
        fn.addDataPoint(-20.,1.);
        fn.addDataPoint(0.,0.);
        roll_angle.addTerm(fn);
        
        fn = new MembershipFunction("Neutral");
        fn.addDataPoint(-10.,0.);
        fn.addDataPoint(0.,1.);
        fn.addDataPoint(10.,0.);
        roll_angle.addTerm(fn);

        fn = new MembershipFunction("Right");
        fn.addDataPoint(0.,0.);
        fn.addDataPoint(20.,1.);
        fn.addDataPoint(45.,0.);
        roll_angle.addTerm(fn);
        
        fn = new MembershipFunction("Hard Right");
        fn.addDataPoint(30.,0.);
        fn.addDataPoint(60.,1.);
        roll_angle.addTerm(fn);

	//	Discretize the output variable
        roll_angle.setNumDiscretes(361);
        roll_angle.setDefaultValue(0.);
        roll_angle.setDefuzzificationMethod(OutputVariable.DefuzzificationMethod.COG);
        roll_angle.discretize();

	//	Create conditions, conclusions and rules.
        ICondition aoa1 = new SubCondition(aoa, aoa.getTermIndex("Rear Left"));
        
        ICondition aoa2 = new SubCondition(aoa, aoa.getTermIndex("Left"));
        
        ICondition aoa3 = new SubCondition(aoa, aoa.getTermIndex("Front Left"));
        
        ICondition aoa4 = new SubCondition(aoa, aoa.getTermIndex("Front"));

        ICondition aoa5 = new SubCondition(aoa, aoa.getTermIndex("Front Right"));
        
        ICondition aoa6 = new SubCondition(aoa, aoa.getTermIndex("Right"));

        ICondition aoa7 = new SubCondition(aoa, aoa.getTermIndex("Rear Right"));

        Conclusion conc1 = new Conclusion(roll_angle, roll_angle.getTermIndex("Hard Left"));
        
        Conclusion conc1b = new Conclusion(roll_angle, roll_angle.getTermIndex("Hard Left"), 0.5);
        
        Conclusion conc2 = new Conclusion(roll_angle, roll_angle.getTermIndex("Left"));
        
        Conclusion conc2b = new Conclusion(roll_angle, roll_angle.getTermIndex("Left"), 0.5);

        Conclusion conc3 = new Conclusion(roll_angle, roll_angle.getTermIndex("Neutral"));

        Conclusion conc4 = new Conclusion(roll_angle, roll_angle.getTermIndex("Right"));
        
        Conclusion conc4b = new Conclusion(roll_angle, roll_angle.getTermIndex("Right"), 0.5);

        Conclusion conc5 = new Conclusion(roll_angle, roll_angle.getTermIndex("Hard Right"));
        
        Conclusion conc5b = new Conclusion(roll_angle, roll_angle.getTermIndex("Hard Right"), 0.5);

	//	Assemble some rules.
        Rule rule = new Rule(aoa1, conc1);
        a.addRule(rule);
        
        rule = new Rule(aoa2, conc1b);
        a.addRule(rule);
        
        rule = new Rule(aoa2, conc2b);
        a.addRule(rule);

        rule = new Rule(aoa3, conc2);
        a.addRule(rule);

        rule = new Rule(aoa3, conc3);
        a.addRule(rule);

        rule = new Rule(aoa4, conc3);
        a.addRule(rule);

        rule = new Rule(aoa5, conc3);
        a.addRule(rule);
        
        rule = new Rule(aoa5, conc4);
        a.addRule(rule);

        rule = new Rule(aoa6, conc4b);
        a.addRule(rule);

        rule = new Rule(aoa6, conc5b);
        a.addRule(rule);

        rule = new Rule(aoa7, conc5);
        a.addRule(rule);

	//	Set some rule base params
        a.setAndOperator(And.FuzzyAndOperator.MIN);
	a.setActivationMethod(RuleBase.ActivationMethod.MIN);
	a.setAccumulationMethod(RuleBase.AccumulationMethod.MAX);

        File fcl = new File("fly.fcl");
        a.writeFCL(fcl);
        
	//	Enter some crisp variables.
	for ( int i = 0; i < 120; i++ )
	{
		double angle = -180+i*3.;

		a.fuzzifyVariable("AoA", angle);

		a.evaluateRules();

                System.out.println(angle+"\t"+a.getCrispOutput("Roll Angle"));
	}
    }
}
