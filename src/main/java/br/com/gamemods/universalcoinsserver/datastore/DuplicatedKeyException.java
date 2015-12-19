package br.com.gamemods.universalcoinsserver.datastore;

public class DuplicatedKeyException extends DataBaseException
{
    public DuplicatedKeyException(String message)
    {
        super(message);
    }
}
