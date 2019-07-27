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

package com.ridderware.jfuzzy.examples.fclfly;

import com.ridderware.jfuzzy.*;
import java.io.File;
import org.apache.logging.log4j.*;

/**
 * The same airplane as in the "fly" example, but the controller is created from
 * an fcl (fuzzy control language) file.
 * @author Jeff Ridder
 */
public class flyMain
{
    private static final Logger logger = LogManager.getLogger(flyMain.class);
    
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
               
        logger.info("Reading file "+args[0]);
        
        File fcl = new File(args[0]);
        a.readFCL(fcl);

        fcl = new File("flyout.fcl");
        a.writeFCL(fcl);
        
        //  Get the output variable and discretize
        OutputVariable ovar = a.getOutputVariable("Roll Angle");
        ovar.setNumDiscretes(361);
        ovar.discretize();
        
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
