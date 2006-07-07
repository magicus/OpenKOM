package nu.rydin.kom.webui.beans;

import nu.rydin.kom.structs.ConferenceListItem;

public class ConferenceListBean
{
    private ConferenceListItem[] conferences;

    public ConferenceListItem[] getConferences()
    {
        return conferences;
    }

    public void setConferences(ConferenceListItem[] conferences)
    {
        this.conferences = conferences;
    } 
}
