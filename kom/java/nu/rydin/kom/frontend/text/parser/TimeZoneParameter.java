/*
 * Created on 2004-aug-19
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.util.TimeZone;

/**
 * @author Magnus Ihse
 */
public class TimeZoneParameter extends EnumParameter
{
    private static String[] m_javaTimeZones;

    private static String[] m_presentableTimeZones;

    static
    {
        m_javaTimeZones = TimeZone.getAvailableIDs();
        m_presentableTimeZones = convertToPresentable(m_javaTimeZones);
    }

    public TimeZoneParameter(String missingObjectQuestionKey, boolean isRequired)
    {
        super(missingObjectQuestionKey, "parser.parameter.timezone.header",
                missingObjectQuestionKey, m_presentableTimeZones, true,
                isRequired);
    }

    /**
     * @param timeZones
     * @return
     */
    private static String[] convertToPresentable(String[] timeZones)
    {
        String[] converted = new String[timeZones.length];

        for (int i = 0; i < timeZones.length; i++)
        {
            String original = timeZones[i];
            converted[i] = original.replace('/', ' ').replace('_', ' ');
        }

        return converted;
    }

    public static String getJavaNameForTimeZoneSelection(Integer selection)
    {
        return m_javaTimeZones[selection.intValue()];
    }

    public TimeZoneParameter(boolean isRequired)
    {
        this("parser.parameter.timezone.ask", isRequired);
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.timezone.description";
    }
}