package project;

public class QueryPositionHelper
{
    private int queryId;
    private int position;
    public QueryPositionHelper(int queryId, int position)
    {
        super();
        this.queryId = queryId;
        this.position = position;
    }
    public int getQueryId()
    {
        return queryId;
    }
    public void setQueryId(int queryId)
    {
        this.queryId = queryId;
    }
    public int getPosition()
    {
        return position;
    }
    public void setPosition(int position)
    {
        this.position = position;
    }


}
