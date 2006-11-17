package nu.rydin.kom.webui.beans;

import java.util.Arrays;
import java.util.Comparator;

import nu.rydin.kom.exceptions.UnexpectedException;

public abstract class ConferenceListBean
{
    public static final int SORT_ORDER      = 0;
    public static final int SORT_NUM_UNREAD = 1;
    public static final int SORT_NAME       = 2;
    
    private static Comparator[] comparators = new Comparator[] {
        new Comparator() // Sort order comparator
        {
            public int compare(Object o1, Object o2)
            {
                if(o1.equals(o2))
                    return 0;
                return ((ConferenceBean) o1).getOrder() < ((ConferenceBean) o2).getOrder() ? -1 : 1;
            }
        },
        new Comparator() // Unread comparator
        {
            public int compare(Object o1, Object o2)
            {
                if(o1.equals(o2))
                    return 0;
                return ((ConferenceBean) o1).getUnread() > ((ConferenceBean) o2).getUnread() ? -1 : 1;
            }
        },
        new Comparator() // Name comparator
        {
            public int compare(Object o1, Object o2)
            {
                return ((ConferenceBean) o1).getName().compareTo(((ConferenceBean) o2).getName());
            }
        }
    };
                                                 
    
    protected ConferenceBean[] news;
    private int sort;

    protected ConferenceListBean()
    throws UnexpectedException
    {
        this.initialize();
    }
    
    protected abstract void initialize()
    throws UnexpectedException;

    public ConferenceBean[] getNews()
    {
        return news;
    }

    public void setNews(ConferenceBean[] news)
    {
        this.news = news;
    }

    public int getSort()
    {
        return sort;
    }

    public void setSort(int sort)
    {
        this.sort = sort;
        
        // Re-sort array
        //
        Arrays.sort(news, comparators[sort]);
    }
}
