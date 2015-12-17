package br.com.gamemods.universalcoinsserver.datastore;

public class OutOfCoinsException extends DataBaseException
{
    private final int lack;

    public OutOfCoinsException(int lack)
    {
        this.lack = lack;
    }

    public OutOfCoinsException(int lack, Throwable cause)
    {
        super(cause);
        this.lack = lack;
    }

    public OutOfCoinsException(int lack, String message)
    {
        super(message);
        this.lack = lack;
    }

    public OutOfCoinsException(int lack, String message, Throwable cause)
    {
        super(message, cause);
        this.lack = lack;
    }

    public OutOfCoinsException(int lack, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
        this.lack = lack;
    }

    public int getLack()
    {
        return lack;
    }
}
