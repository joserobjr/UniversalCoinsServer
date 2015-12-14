package br.com.gamemods.universalcoinsserver.datastore;

public class DataBaseException extends Exception
{
    public DataBaseException()
    {
    }

    public DataBaseException(Throwable cause)
    {
        super(cause);
    }

    public DataBaseException(String message)
    {
        super(message);
    }

    public DataBaseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
