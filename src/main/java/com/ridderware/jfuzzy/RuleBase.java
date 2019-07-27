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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.logging.log4j.*;

/**
 * Class to represent a collection of rules (i.e., a rule base).
 *
 * @author Jeff Ridder
 */
public class RuleBase
{
    private static final Logger logger = LogManager.getLogger(RuleBase.class);

    /**
     * Enumeration of activation methods.
     */
    public enum ActivationMethod
    {
        /**
         * Min of activation level and output term.
         */
        MIN,
        /**
         * Product of activation level and output term.
         */
        PROD

    }

    /**
     * Enumeration of accumulation methods.
     */
    public enum AccumulationMethod
    {
        /** Max of all outputs at each point. */
        MAX,
        /** Min(1, sum of doms). */
        BSUM

    }
    private HashSet<Rule> rules = new HashSet<Rule>();

    private HashSet<OutputVariable> output_variables =
        new HashSet<OutputVariable>();

    private HashSet<Variable> input_variables = new HashSet<Variable>();

    private ActivationMethod activation_method;

    private AccumulationMethod accumulation_method;

    /** Creates a new instance of RuleBase */
    public RuleBase()
    {
        this.accumulation_method = AccumulationMethod.MAX;
        this.activation_method = ActivationMethod.MIN;
    }

    /**
     * Clears the rule-base of all rules.
     */
    public void clearRules()
    {
        this.rules.clear();
    }

    /**
     * Sets the activation method.
     * @param activation_method activation method.
     */
    public void setActivationMethod(ActivationMethod activation_method)
    {
        this.activation_method = activation_method;
    }

    /**
     * Sets the accumulation method.
     * @param accumulation_method accumulation method.
     */
    public void setAccumulationMethod(AccumulationMethod accumulation_method)
    {
        this.accumulation_method = accumulation_method;
    }

    /**
     * Returns a java array containing the input variables.
     * @return java array.
     */
    public Variable[] getInputVariables()
    {
        Variable[] ivars = new Variable[input_variables.size()];
        this.input_variables.toArray(ivars);

        return ivars;
    }

    /**
     * Returns a java array containing the output varaiables.
     * @return java array.
     */
    public OutputVariable[] getOutputVariables()
    {
        OutputVariable[] ovars = new OutputVariable[output_variables.size()];
        this.output_variables.toArray(ovars);

        return ovars;
    }

    /**
     * Sets the And operator for the rule base.
     * @param oper And operator.
     */
    public void setAndOperator(And.FuzzyAndOperator oper)
    {
        switch (oper)
        {
            case MIN:
            {
                And.setAndOperator(And.FuzzyAndOperator.MIN);
                Or.setOrOperator(Or.FuzzyOrOperator.MAX);
                break;
            }
            case PROD:
            {
                And.setAndOperator(And.FuzzyAndOperator.PROD);
                Or.setOrOperator(Or.FuzzyOrOperator.ASUM);
                break;
            }
            case BDIF:
            {
                And.setAndOperator(And.FuzzyAndOperator.BDIF);
                Or.setOrOperator(Or.FuzzyOrOperator.BSUM);
                break;
            }
            default:
            {
            }
        }
    }

    /**
     * Reads from an fcl file and creates the rule base from that.
     * @param fcl FCL file to read the rulebase from.
     */
    public void readFCL(File fcl)
    {
        ArrayList<String> lines = new ArrayList<String>();

        this.file2ArrayList(fcl, lines);

        //  Parse the arraylist.

        Variable ivar = null;
        OutputVariable ovar = null;
        boolean bivar = false;
        boolean bovar = false;
        boolean bfuzz = false;
        boolean bdefuzz = false;
        boolean brule = false;
        for (String l : lines)
        {
            if (l.contains("VAR_INPUT"))
            {
                bivar = true;
            }
            else if (l.contains("VAR_OUTPUT"))
            {
                bovar = true;
            }
            else if (l.contains("FUZZIFY") && !l.contains("DEFUZZIFY") &&
                !l.contains("END"))
            {
                bfuzz = true;
                String name = l.substring(l.indexOf("FUZZIFY") + 7, l.length()).
                    trim();
                ivar = this.getInputVariable(name);
                if (ivar == null)
                {
                    logger.error("No input variable of name " + name + ", length: " +
                        name.length());
                }
            }
            else if (l.contains("DEFUZZIFY") && !l.contains("END"))
            {
                bdefuzz = true;
                String name = l.substring(l.indexOf("DEFUZZIFY") + 9, l.length()).
                    trim();
                ovar = this.getOutputVariable(name);
                if (ovar == null)
                {
                    logger.error("No output variable of name " + name);
                }
            }
            else if (l.contains("RULEBLOCK"))
            {
                brule = true;
            }
            else if (bivar)
            {
                if (l.contains("END_VAR"))
                {
                    bivar = false;
                }
                else
                {
                    String sub = l.trim();
                    ivar =
                        new Variable(sub.substring(0, sub.indexOf(":")).trim());
                    this.addInputVariable(ivar);
                }
            }
            else if (bovar)
            {
                if (l.contains("END_VAR"))
                {
                    bovar = false;
                }
                else
                {
                    String sub = l.trim();
                    ovar = new OutputVariable(sub.substring(0, sub.indexOf(":")).
                        trim());
                    this.addOutputVariable(ovar);
                }
            }
            else if (bfuzz)
            {
                if (l.trim().startsWith("TERM"))
                {
                    String name = l.substring(l.indexOf("TERM") + 4,
                        l.indexOf(":=")).trim();
                    MembershipFunction term = new MembershipFunction(name);

                    //  Strip off datapoints and add to term.
                    int i = l.indexOf(":=") + 2;
                    do
                    {
                        String point = l.substring(l.indexOf("(", i) + 1,
                            l.indexOf(")", i)).trim();
                        String[] coords = point.split(",");
                        term.addDataPoint(Double.parseDouble(coords[0].trim()),
                            Double.parseDouble(coords[1].trim()));

                        int istart = l.indexOf(")", i) + 1;
                        int iend = l.length();
                        if (istart < 0 || istart > iend)
                        {
                            break;
                        }

                        l = l.substring(istart, iend);
                        i = 0;
                    }
                    while (!l.equals(";"));

                    if (ivar != null)
                    {
                        ivar.addTerm(term);
                    }
                }
                else if (l.startsWith("END_FUZZIFY"))
                {
                    bfuzz = false;
                    ivar = null;
                }
            }
            else if (bdefuzz)
            {
                if (l.trim().startsWith("TERM"))
                {
                    String name = l.substring(l.indexOf("TERM") + 4,
                        l.indexOf(":=")).trim();
                    MembershipFunction term = new MembershipFunction(name);

                    //  Strip off datapoints and add to term.
                    int i = l.indexOf(":=") + 2;
                    do
                    {
                        String point = l.substring(l.indexOf("(", i) + 1,
                            l.indexOf(")", i)).trim();
                        String[] coords = point.split(",");
                        term.addDataPoint(Double.parseDouble(coords[0].trim()),
                            Double.parseDouble(coords[1].trim()));

                        int istart = l.indexOf(")", i) + 1;
                        int iend = l.length();
                        if (istart < 0 || istart > iend)
                        {
                            break;
                        }

                        l = l.substring(istart, iend);
                        i = 0;
                    }
                    while (!l.equals(";"));

                    if (ovar != null)
                    {
                        ovar.addTerm(term);

                        //  Do default discretize
                        ovar.discretize();
                    }
                }
                else if (l.trim().startsWith("METHOD"))
                {
                    if (l.contains("COG") && !l.contains("COGS"))
                    {
                        ovar.setDefuzzificationMethod(OutputVariable.DefuzzificationMethod.COG);
                    }
                    else if (l.contains("COGS"))
                    {
                        ovar.setDefuzzificationMethod(OutputVariable.DefuzzificationMethod.COGS);
                    }
                    else if (l.contains("COA"))
                    {
                        ovar.setDefuzzificationMethod(OutputVariable.DefuzzificationMethod.COA);
                    }
                }
                else if (l.trim().startsWith("DEFAULT"))
                {
                    String value = l.substring(l.indexOf(":=") + 2,
                        l.indexOf(";")).trim();
                    ovar.setDefaultValue(Double.parseDouble(value));
                }
                else if (l.startsWith("END_DEFUZZIFY"))
                {
                    bdefuzz = false;
                    ovar = null;
                }
            }
            else if (brule)
            {
                if (l.trim().startsWith("AND"))
                {
                    if (l.contains("MIN"))
                    {
                        this.setAndOperator(And.FuzzyAndOperator.MIN);
                    }
                    else if (l.contains("PROD"))
                    {
                        this.setAndOperator(And.FuzzyAndOperator.PROD);
                    }
                    else if (l.contains("BDIF"))
                    {
                        this.setAndOperator(And.FuzzyAndOperator.BDIF);
                    }
                }
                else if (l.trim().startsWith("ACT"))
                {
                    if (l.contains("MIN"))
                    {
                        this.setActivationMethod(ActivationMethod.MIN);
                    }
                    else if (l.contains("PROD"))
                    {
                        this.setActivationMethod(ActivationMethod.PROD);
                    }
                }
                else if (l.trim().startsWith("ACCU"))
                {
                    if (l.contains("MAX"))
                    {
                        this.setAccumulationMethod(AccumulationMethod.MAX);
                    }
                    else if (l.contains("BSUM"))
                    {
                        this.setAccumulationMethod(AccumulationMethod.BSUM);
                    }
                }
                else if (l.trim().startsWith("RULE"))
                {
                    String condition = l.substring(l.indexOf("IF") + 2,
                        l.indexOf("THEN")).trim();
                    String conclusion = l.substring(l.indexOf("THEN") + 4,
                        l.indexOf(";")).trim();

                    //  Parse the condition string
                    String[] atoms = condition.split("AND");
                    ArrayList<SubCondition> simples =
                        new ArrayList<SubCondition>();
                    for (String atom : atoms)
                    {
                        String var_name = atom.substring(0, atom.indexOf("IS")).
                            trim();
                        ivar = this.getInputVariable(var_name);
                        if (ivar == null)
                        {
                            logger.error("No input variable of name " + var_name);
                        }

                        boolean bnot = false;
                        String term_name;
                        if (atom.contains("NOT"))
                        {
                            bnot = true;
                            term_name = atom.substring(atom.indexOf("NOT") + 3,
                                atom.length()).trim();
                        }
                        else
                        {
                            term_name = atom.substring(atom.indexOf("IS") + 2,
                                atom.length()).trim();
                        }

                        int term_index = ivar.getTermIndex(term_name);

                        SubCondition s =
                            new SubCondition(ivar, term_index, bnot);
                        simples.add(s);
                    }

                    ICondition root = null;
                    if (simples.size() == 1)
                    {
                        root = simples.get(0);
                    }
                    else
                    {
                        And leaf = new And(simples.get(simples.size() - 1),
                            simples.get(simples.size() - 2));
                        And and = leaf;

                        //  # of ands always # simples-1
                        for (int i = simples.size() - 3; i >= 0; i--)
                        {
                            and = new And(simples.get(i), and);
                        }
                        root = and;
                    }

                    //  Parse the conclusion string
                    String var_name = conclusion.substring(0,
                        conclusion.indexOf("IS")).trim();
                    ovar = this.getOutputVariable(var_name);
                    if (ovar == null)
                    {
                        logger.error("No output variable of name " + var_name);
                    }

                    String term_name;
                    double weight = 1.;

                    if (conclusion.contains("WITH"))
                    {
                        term_name = conclusion.substring(conclusion.indexOf("IS") + 2, conclusion.indexOf("WITH")).
                            trim();
                        String w = conclusion.substring(conclusion.indexOf("WITH") + 6, conclusion.length()).
                            trim();
                        weight = Double.parseDouble(w);
                    }
                    else
                    {
                        term_name = conclusion.substring(conclusion.indexOf("IS") + 2, conclusion.length()).
                            trim();
                    }

                    int term_index = ovar.getTermIndex(term_name);

                    Conclusion conc = new Conclusion(ovar, term_index, weight);

                    //  Make the rule and add to rule base.
                    Rule rule = new Rule(root, conc);
                    this.addRule(rule);
                }
                else if (l.startsWith("END_RULEBLOCK"))
                {
                    brule = false;
                }
            }
        }
    }

    /**
     * Writes the FCL file.
     * @param fcl a File object in which to write the fuzzy control language description of the rule base.
     */
    public void writeFCL(File fcl)
    {
        ArrayList<String> lines = new ArrayList<String>();

        //  Build up the array list here.
        lines.add("FUNCTION_BLOCK");

        //	Variables
        lines.add("VAR_INPUT");

        for (Variable ivar : input_variables)
        {
            String str = "\t" + ivar.getName();
            str += " :\tREAL;";
            lines.add(str);
        }

        lines.add("END_VAR");

        lines.add("VAR_OUTPUT");

        for (Variable ovar : output_variables)
        {
            String str = "\t" + ovar.getName();
            str += " :\tREAL;";
            lines.add(str);
        }

        lines.add("END_VAR");

        for (Variable ivar : input_variables)
        {
            String str = "FUZZIFY " + ivar.getName();

            lines.add(str);

            for (MembershipFunction term : ivar.getTerms())
            {
                str = "\tTERM " + term.getName() + " :=";

                for (int j = 0; j < term.getNumberOfDataPoints(); j++)
                {
                    DataPoint p = term.getDataPoint(j);

                    str += " (";
                    str += p.getX();
                    str += ", ";
                    str += p.getY();
                    str += ")";
                }

                str += ";";
                lines.add(str);
            }

            lines.add("END_FUZZIFY");
        }

        for (OutputVariable ovar : output_variables)
        {
            String str = "DEFUZZIFY " + ovar.getName();

            lines.add(str);

            for (MembershipFunction term : ovar.getTerms())
            {
                str = "\tTERM " + term.getName() + " :=";

                for (int j = 0; j < term.getNumberOfDataPoints(); j++)
                {
                    DataPoint p = term.getDataPoint(j);

                    str += " (";
                    str += p.getX();
                    str += ", ";
                    str += p.getY();
                    str += ")";
                }

                str += ";";
                lines.add(str);
            }

            switch (ovar.getDefuzzificationMethod())
            {
                case COG:
                    lines.add("\tMETHOD : COG;");
                    break;
                case COGS:
                    lines.add("\tMETHOD : COGS;");
                    break;
                case COA:
                    lines.add("\tMETHOD : COA;");
                    break;
                default:
            }

            str = "\tDEFAULT := ";
            str += ovar.getDefaultValue();
            str += ";";
            lines.add(str);

            lines.add("END_DEFUZZIFY");
        }

        //	Rule block
        lines.add("RULEBLOCK");

        switch (And.getAndOperator())
        {
            case MIN:
            {
                lines.add("\tAND : MIN;");
                break;
            }
            case PROD:
            {
                lines.add("\tAND : PROD;");
                break;
            }
            case BDIF:
            {
                lines.add("\tAND : BDIF;");
                break;
            }
            default:
            {
            }
        }

        switch (this.activation_method)
        {
            case MIN:
            {
                lines.add("\tACT : MIN;");
                break;
            }
            case PROD:
            {
                lines.add("\tACT : PROD;");
                break;
            }
            default:
            {
            }
        }

        switch (this.accumulation_method)
        {
            case MAX:
            {
                lines.add("\tACCU : MAX;");
                break;
            }
            case BSUM:
            {
                lines.add("\tACCU : BSUM;");
                break;
            }
            default:
            {
            }
        }

        int i = 1;
        for (Rule rule : rules)
        {
            lines.add(rule.writeFCL(i));
            i++;
        }

        lines.add("END_RULEBLOCK");
        lines.add("END_FUNCTION_BLOCK");

        this.ArrayList2file(fcl, lines);
    }

    /**
     * Method ArrayList2file
     *
     * @return a boolean, true iff there was an IOException.
     * @param file the file to write to
     * @param file_data an ArrayList
     */
    protected boolean ArrayList2file(File file, ArrayList<String> file_data)
    {
        boolean error = false;

        try
        {
            FileWriter fw = new FileWriter(file);
            PrintWriter outFile = new PrintWriter(fw);

            file_data.trimToSize();

            int size = file_data.size();
            for (int index = 0; index < size; index++)
            {
                outFile.println(file_data.get(index));
            }

            outFile.close();
        }
        catch (IOException e)
        {
            logger.error("ERROR, IOException - Could not write file: " + file);
            e.printStackTrace();
            error = true;
        }

        return error;
    }

    /**
     *  Method file2ArrayList
     * @param file File object to read from
     * @param  file_data  an ArrayList
     * @return            an ArrayList
     */
    public ArrayList<String> file2ArrayList(File file,
        ArrayList<String> file_data)
    {
        try
        {
            FileReader fr = new FileReader(file);
            BufferedReader inFile = new BufferedReader(fr);

            String line = inFile.readLine();

            while (line != null)
            {
                file_data.add(line);
                line = inFile.readLine();
            }

            inFile.close();
        }
        catch (FileNotFoundException e)
        {
            logger.error("ERROR, FileNotFoundException - Could not open file: " +
                file);
            e.printStackTrace();
            file_data = null;
        //errors are dealt with by checking for null
        }
        catch (IOException e)
        {
            logger.error("ERROR, IOException - Could not read from file: " +
                file);
            e.printStackTrace();
            file_data = null;
        //errors are dealt with by checking for null
        }

        return file_data;
    }

    /**
     * Adds an input variable to the rule base.
     * @param ivar Variable object.
     */
    public void addInputVariable(Variable ivar)
    {
        this.input_variables.add(ivar);
    }

    /**
     * Returns the input variable by name.
     * @param name name of the input variable to return.
     * @return the requested input variable (or null).
     */
    public Variable getInputVariable(String name)
    {
        for (Variable ivar : input_variables)
        {
            if (name.equals(ivar.getName()))
            {
                return ivar;
            }
        }

        return null;
    }

    /**
     * Returns the output variable by name.
     * @param name of the output variable to return.
     * @return the requested output variable (or null).
     */
    public OutputVariable getOutputVariable(String name)
    {
        for (OutputVariable ovar : output_variables)
        {
            if (name.equals(ovar.getName()))
            {
                return ovar;
            }
        }

        return null;
    }

    /**
     * Adds an output variable to the rule base.
     * @param ovar OutputVariable object.
     */
    public void addOutputVariable(OutputVariable ovar)
    {
        this.output_variables.add(ovar);
    }

    /**
     * Adds a rule to the rule base.
     * @param rule Rule object.
     */
    public void addRule(Rule rule)
    {
        this.rules.add(rule);
    }

    /**
     * Fires all rules.  The sequence of operations is:
     * Fuzzify(), Infer(), Defuzzify().
     */
    public void evaluateRules()
    {
        //	Reset the discretes of all output variables.
        for (OutputVariable ovar : output_variables)
        {
            ovar.resetDiscretes();
        }

        for (Rule rule : rules)
        {
            rule.infer(this.activation_method, this.accumulation_method);
        }

        for (OutputVariable ovar : output_variables)
        {
            ovar.defuzzify();
        }
    }

    /**
     * Fuzzifies the specified variable.
     * @param variable_name name of variable to fuzzify.
     * @param value crisp input value.
     */
    public void fuzzifyVariable(String variable_name, double value)
    {
        for (Variable var : input_variables)
        {
            if (var.getName().equals(variable_name))
            {
                var.fuzzify(value);
            }
        }
    }

    /**
     * Fuzzifies the specified variable.
     * @param variable Variable object.
     * @param value crisp input value.
     */
    public void fuzzifyVariable(Variable variable, double value)
    {
        if (variable != null)
        {
            variable.fuzzify(value);
        }
    }

    /**
     * Returns the crisp output for the specified variable resulting from evaluating the rule base.
     * @param variable OutputVariable object
     * @return crisp output value.
     */
    public double getCrispOutput(OutputVariable variable)
    {
        return variable.getCrispOutput();
    }

    /**
     * Returns the crisp output for the specified variable resulting from evaluating the rule base.
     * @param variable_name name of the output variable.
     * @return crisp output value.
     */
    public double getCrispOutput(String variable_name)
    {
        double crisp = 0.;

        for (OutputVariable var : output_variables)
        {
            if (var.getName().equals(variable_name))
            {
                crisp = var.getCrispOutput();
            }
        }

        return crisp;
    }
}
