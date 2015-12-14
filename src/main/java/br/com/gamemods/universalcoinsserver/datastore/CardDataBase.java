package br.com.gamemods.universalcoinsserver.datastore;

import java.util.UUID;

public interface CardDataBase
{
    UUID getAccountOwner(String account) throws DataBaseException;

    int getAccountBalance(String account) throws DataBaseException;

    boolean depositToAccount(String account, int depositAmount, CardOperator operator, TransactionType transaction, String product) throws DataBaseException;
}
