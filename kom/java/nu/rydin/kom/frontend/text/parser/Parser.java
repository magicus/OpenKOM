/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.exceptions.*;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Magnus Ihse
 * @author Henrik Schröder
 */
public class Parser
{
    private Command[] m_commands;

    /** Map[Command->CommandLinePart[]] */
    //	private Map m_commandToPartsMap = new HashMap();
    public static class ExecutableCommand
    {
        private Object[] m_parameterArray;

        private Command m_command;

        public ExecutableCommand(Command command, Object[] parameterArray)
        {
            m_command = command;
            m_parameterArray = parameterArray;
        }

        public Command getCommand()
        {
            return m_command;
        }

        public Object[] getParameterArray()
        {
            return m_parameterArray;
        }

        public void execute(Context context) throws KOMException, IOException,
                InterruptedException
        {
            m_command.printPreamble(context.getOut());
            m_command.execute(context, m_parameterArray);
            m_command.printPostamble(context.getOut());
        }

        public void executeBatch(Context context) throws KOMException,
                IOException, InterruptedException
        {
            m_command.execute(context, m_parameterArray);
        }
    }

    private class CommandToMatches
    {
        private Command m_command;

        /** List[CommandLinePart.Match] */
        private List m_matches = new LinkedList();

        /**
         * @param command
         * @param matches
         */
        public CommandToMatches(Command command)
        {
            m_command = command;
        }

        public Command getCommand()
        {
            return m_command;
        }

        public List getMatches()

        {
            return m_matches;
        }

        public void addMatch(Match match)
        {
            m_matches.add(match);
        }

        /**
         * @param level
         * @return
         */
        public Match getMatch(int level)
        {
            return (Match) m_matches.get(level);
        }

        public Match getLastMatch()
        {
            return (Match) m_matches.get(m_matches.size() - 1);
        }

        public int getLevel()
        {
            return m_matches.size();
        }

        /**
         * @param level
         * @return
         */
        public CommandLinePart getCommandLinePart(int level)
        {
            CommandLinePart[] parts = getCommand().getFullSignature();
            //CommandLinePart[] parts = (CommandLinePart[])
            // (m_commandToPartsMap.get(m_command));
            if (level >= parts.length)
            {
                return null;
            } else
            {
                return parts[level];
            }
        }

        public String toString()
        {
            return "CommandToMatches:[command=" + m_command + ", matches="
                    + m_matches + "]";
        }
    }

    /**
     * @param commands
     * @param primaryCommands
     */
    private Parser(List commands)
    {
        m_commands = new Command[commands.size()];
        commands.toArray(m_commands);
    }

    public ExecutableCommand parseCommandLine(Context context,
            String commandLine) throws IOException, InterruptedException,
            KOMException
    {
        CommandToMatches target = resolveMatchingCommand(context, commandLine);
        ExecutableCommand executableCommand = resolveParametersForTarget(
                context, target);
        return executableCommand;
    }

    private CommandToMatches resolveMatchingCommand(Context context,
            String commandLine) throws IOException, InterruptedException,
            KOMException, CommandNotFoundException
    {
        // Trim the commandline
        commandLine = commandLine.trim();

        List potentialTargets = getPotentialTargets(commandLine);

        potentialTargets = checkForExactMatch(potentialTargets);

        if (potentialTargets.size() > 1)
        {
            // Ambiguous matching command found. Try to resolve it.
            potentialTargets = resolveAmbiguousCommand(context,
                    potentialTargets);
        }

        // Now we either have one target candidate, or none.
        if (potentialTargets.size() == 0)
        {
            throw new CommandNotFoundException(new Object[] { commandLine } );
        }

        // We have one match, but it is not neccessarily correct: we might have
        // too few parameters, as well as too many. Let's find out, and
        // ask user about missing parameters.

        CommandToMatches target = (CommandToMatches) potentialTargets.get(0);
        return target;
    }

    public Command getMatchingCommand(Context context, String commandLine)
            throws IOException, InterruptedException, KOMException
    {
        return resolveMatchingCommand(context, commandLine).getCommand();
    }

    public static int askForResolution(Context context, List candidates,
            String promptKey, boolean printHeading, String headingKey,
            boolean allowPrefixes) throws IOException, InterruptedException,
            InvalidChoiceException, OperationInterruptedException
    {
        LineEditor in = context.getIn();
        PrintWriter out = context.getOut();
        MessageFormatter fmt = context.getMessageFormatter();

        // Ask user to chose
        //
        if (printHeading)
            out.println();
            out.println(fmt.format(headingKey));
        int top = candidates.size();
        for (int idx = 0; idx < top; ++idx)
        {
            String candidate = candidates.get(idx).toString();
            int printIndex = idx + 1;
            PrintUtils
                    .printRightJustified(out, Integer.toString(printIndex), 2);
            out.print(". ");
            out.println(candidate);
        }
        out.print(fmt.format(promptKey));
        out.flush();
        String input = in.readLine().trim();

        // Empty string given? Abort!
        //
        if (input.length() == 0)
        {
            throw new OperationInterruptedException();
        }

        try
        {
            // Is it a number the user entered?
            int selection = Integer.parseInt(input);
            if (selection < 1 || selection > top)
            {
                throw new InvalidChoiceException();
            }
            return selection - 1;
        } catch (NumberFormatException e)
        {
            return resolveString(context, input, candidates, headingKey,
                    promptKey, allowPrefixes);
        }
    }

    public static int resolveString(Context context, String input,
            List candidates, String headingKey, String promptKey,
            boolean allowPrefixes) throws InvalidChoiceException,
            OperationInterruptedException, IOException, InterruptedException
    {
        // Nope. Assume it is a name to be matched against the list.
        String cookedInput = NameUtils.normalizeName(input);
        List originalNumbers = new LinkedList();
        List matchingCandidates = new LinkedList();
        int i = 0;
        for (Iterator iter = candidates.iterator(); iter.hasNext();)
        {
            String candidate = (String) iter.next();
            String cookedCandidate = NameUtils.normalizeName(candidate);
            if (NameUtils.match(cookedInput, cookedCandidate, allowPrefixes))
            {
                originalNumbers.add(new Integer(i));
                matchingCandidates.add(candidate);
            }
            i++;
        }
        if (matchingCandidates.size() == 0)
        {
            throw new InvalidChoiceException();
        } else if (matchingCandidates.size() == 1)
        {
            // Yeah! We got it!
            Integer index = (Integer) originalNumbers.get(0);
            return index.intValue();
        } else
        {
            // Still ambiguous. Let the user chose again, recursively.
            int newIndex = askForResolution(context, matchingCandidates,
                    promptKey, true, headingKey, false);
            // This is the index in our (shorter) list of remaining candidates,
            // but we need to
            // return the index of the original list. Good thing we saved that
            // number! :-)
            Integer oldIndex = (Integer) originalNumbers.get(newIndex);
            return oldIndex.intValue();
        }
    }

    private List resolveAmbiguousCommand(Context context, List potentialTargets)
            throws IOException, InterruptedException, KOMException
    {
        List commandNames = new ArrayList();
        for (Iterator iter = potentialTargets.iterator(); iter.hasNext();)
        {
            CommandToMatches potentialTarget = (CommandToMatches) iter.next();
            commandNames.add(potentialTarget.getCommand().getFullName());
        }

        int selection = askForResolution(context, commandNames, "parser.choose",
                true, "parser.ambiguous", false);

        CommandToMatches potentialTarget = (CommandToMatches) potentialTargets
                .get(selection);

        // Just save the chosen one in our list for later processing
        potentialTargets = new LinkedList();
        potentialTargets.add(potentialTarget);
        return potentialTargets;
    }

    private ExecutableCommand resolveParametersForTarget(Context context,
            CommandToMatches target) throws InvalidParametersException,
            TooManyParametersException, IOException, InterruptedException,
            KOMException, OperationInterruptedException
    {
        CommandLinePart[] parts = target.getCommand().getFullSignature();
        //CommandLinePart[] parts = (CommandLinePart[])
        // m_commandToPartsMap.get(target.getCommand());
        int level = target.getLevel();
        Match lastMatch = target.getMatch(level - 1);

        // First, do we have more left on the command line to parse?
        // If so, match and put it in the targets match list.
        String remainder = lastMatch.getRemainder();
        while (remainder.length() > 0)
        {
            if (level < parts.length)
            {
                // We still have parts to match to
                Match match = parts[level].match(remainder);
                if (!match.isMatching())
                {
                    if (parts[level] instanceof CommandNamePart)
                    {
                        // User has entered enough to match one command
                        // uniquely,
                        // but the rest of the commandline does not match the
                        // rest of
                        // the command name parameters.
                        throw new CommandNotFoundException(
                                new Object[] { "ADD ORIGINAL COMMANDLINE HERE" });
                    } else
                    {
                        // User have entered an invalid parameter. This should
                        // be unlikely.
                        throw new InvalidParametersException(new Object[] { target.getCommand().getFullName() });
                    }
                }
                target.addMatch(match);
                remainder = match.getRemainder();
                level++;
            } else
            {
                throw new TooManyParametersException(new Object[] { target.getCommand().getFullName() });
            }
        }

        // Now, resolve the entered parts.
        List resolvedParameters = new LinkedList();
        for (int i = 0; i < target.getMatches().size(); i++)
        {
            // If this is a command name part, then it is part of the
            // signature. Add the resolved value of the match to our parameter
            // list.
            if (parts[i] instanceof CommandLineParameter)
            {
                Match match = target.getMatch(i);

                Object parameter = parts[i].resolveFoundObject(context, match);
                resolvedParameters.add(parameter);
            }
        }

        // If we still need more parameters, ask the user about them.
        while (level < parts.length)
        {
            Object parameter;
            CommandLinePart part = parts[level];

            if (part instanceof CommandNamePart)
            {
                // If we still have CommandNameParts unmatched, WE IGNORE THEM.
                // Since we've obviously matched one unique command, it's ok.
            } else
            {
                if (part.isRequired())
                {
                    // Not on command line and required, ask the user about it.
                    Match match = part.fillInMissingObject(context);
                    if (!match.isMatching())
                    {
                        // The user entered an invalid parameter, abort
                        throw new InvalidParametersException(new Object[] { target.getCommand().getFullName() });
                    }

                    // Resolve directly
                    parameter = part.resolveFoundObject(context, match);
                } else
                {
                    //Parameter was not required, just skip it and add null to
                    // the parameters
                    parameter = null;
                }
                resolvedParameters.add(parameter);
            }
            level++;
        }

        // Now we can execute the command with the resolved parameters.
        Object[] parameterArray = new Object[resolvedParameters.size()];
        resolvedParameters.toArray(parameterArray);

        Command command = target.getCommand();
        return new ExecutableCommand(command, parameterArray);
    }

    private List checkForExactMatch(List potentialTargets)
    {
        // Check if there is one and only one potential command that the user
        // wrote all parts of. If so, choose it.
        if (potentialTargets.size() > 1)
        {
            List newPotentialTargets = new LinkedList();
            for (Iterator iter = potentialTargets.iterator(); iter.hasNext();)
            {
                CommandToMatches each = (CommandToMatches) iter.next();
                List matches = each.getMatches();
                CommandNamePart[] words = each.getCommand().getNameSignature();
                boolean failedAtLeastOnce = false;

                //More words than matches, we have definitely not matched all
                // words.
                if (words.length > matches.size())
                {
                    failedAtLeastOnce = true;
                } else
                {
                    //Ok, let's check if all words in the command matches.
                    //If so, put it in the new list.
                    for (int i = 0; i < words.length; i++)
                    {
                        if (!((Match) matches.get(i)).isMatching())
                        {
                            failedAtLeastOnce = true;
                        }
                    }
                }

                if (!failedAtLeastOnce)
                {
                    newPotentialTargets.add(each);
                }
            }

            //If newPotentialTargets holds one and only one item, this means
            //that in the list of ambigous commands, one and only one matched
            //all of its words. SELECT IT!
            if (newPotentialTargets.size() == 1)
            {
                potentialTargets = newPotentialTargets;
            }
        }
        return potentialTargets;
    }

    private List getPotentialTargets(String commandLine)
    {
        int level = 0;

        // List[CommandToMatches]
        List potentialTargets = new LinkedList();

        // Build a copy of all commands first, to filter down later.
        for (int i = 0; i < m_commands.length; i++)
        {
            potentialTargets.add(new CommandToMatches(m_commands[i]));
        }

        boolean remaindersExist = true;
        while (remaindersExist && potentialTargets.size() > 1)
        {
            remaindersExist = false;
            for (Iterator iter = potentialTargets.iterator(); iter.hasNext();)
            {
                CommandToMatches potentialTarget = (CommandToMatches) iter
                        .next();
                CommandLinePart part = potentialTarget
                        .getCommandLinePart(level);
                if (part == null)
                {
                    if (potentialTarget.getLastMatch().getRemainder().length() > 0)
                    {
                        iter.remove();
                    }
                } else
                {
                    String commandLineToMatch;
                    if (level == 0)
                    {
                        commandLineToMatch = commandLine;
                    } else
                    {
                        commandLineToMatch = potentialTarget.getLastMatch()
                                .getRemainder();
                    }
                    Match match = part.match(commandLineToMatch);
                    if (!match.isMatching())
                    {
                        iter.remove();
                    } else
                    {
                        potentialTarget.addMatch(match);
                        if (match.getRemainder().length() > 0)
                        {
                            remaindersExist = true;
                        }
                    }
                }
            }
            level++;
        }
        return potentialTargets;
    }

    private static final Class[] s_commandCtorSignature = new Class[]
    { Context.class, String.class };

    /**
     * 
     * @param filename
     * @param context
     * @return @throws
     *         IOException
     * @throws UnexpectedException
     */
    public static Parser load(String filename, Context context)
            throws IOException, UnexpectedException
    {
        MessageFormatter formatter = context.getMessageFormatter();
        try
        {
            //List commandNames = new ArrayList();
            List list = new ArrayList();
            BufferedReader rdr = new BufferedReader(new InputStreamReader(
                    Parser.class.getResourceAsStream(filename)));

            // Read command list
            //
            String line;
            while ((line = rdr.readLine()) != null)
            {
                line = line.trim();
                if (!line.startsWith("#"))
                    list.add(line);
            }
            rdr.close();

            // Instantiate commands
            //
            int top = list.size();
            List commandList = new ArrayList();
            for (int idx = 0; idx < top; ++idx)
            {
                Class clazz = Class.forName((String) list.get(idx));
                Constructor ctor = clazz.getConstructor(s_commandCtorSignature);

                // Install primary command
                //
                String name = formatter.format(clazz.getName() + ".name");
                Command primaryCommand = (Command) ctor
                        .newInstance(new Object[]
                        { context, name });
                commandList.add(primaryCommand);
                //commandNames.add(name);

                // Install aliases
                //
                int aliasIdx = 1;
                for (;; ++aliasIdx)
                {
                    // Try alias key
                    //
                    String alias = formatter.getStringOrNull(clazz.getName()
                            + ".name." + aliasIdx);
                    if (alias == null)
                        break; // No more aliases

                    // We found an alias! Create command.
                    //
                    Command aliasCommand = (Command) ctor
                            .newInstance(new Object[]
                            { context, alias });
                    commandList.add(aliasCommand);
                    //commandNames.add(alias);
                }
            }

            // Copy to command array
            // 
            return new Parser(commandList);
        } catch (ClassNotFoundException e)
        {
            throw new UnexpectedException(-1, e);
        } catch (NoSuchMethodException e)
        {
            throw new UnexpectedException(-1, e);
        } catch (InstantiationException e)
        {
            throw new UnexpectedException(-1, e);
        } catch (IllegalAccessException e)
        {
            throw new UnexpectedException(-1, e);
        } catch (InvocationTargetException e)
        {
            throw new UnexpectedException(-1, e.getCause());
        }
    }

    /**
     * Returns an array of all Commands that are available to the user.
     * 
     * @return An Command[] of available commands.
     */
    public Command[] getCommandList()
    {
        return m_commands;
    }

    /**
     * @param class1
     * @return
     */
    public Command getCommand(Class class1)
    {
        //TODO: HOLY INEFFICIENT LOOKUP, BATMAN!
        for (int i = 0; i < m_commands.length; i++)
        {
            if (class1.isInstance(m_commands[i]))
            {
                return m_commands[i];
            }
        }
        return null;
    }
}