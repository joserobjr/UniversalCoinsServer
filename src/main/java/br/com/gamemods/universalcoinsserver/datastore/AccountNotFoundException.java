package br.com.gamemods.universalcoinsserver.datastore;

public class AccountNotFoundException extends DataBaseException
{
    private final Object account;

    public AccountNotFoundException(Object account)
    {
        this.account = account;
    }

    public AccountNotFoundException(Object account, Throwable cause)
    {
        super(cause);
        this.account = account;
    }

    public AccountNotFoundException(Object account, String message)
    {
        super(message);
        this.account = account;
    }

    public AccountNotFoundException(Object account, String message, Throwable cause)
    {
        super(message, cause);
        this.account = account;
    }

    public AccountNotFoundException(Object account, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
        this.account = account;
    }

    public Object getAccount()
    {
        return account;
    }
}
