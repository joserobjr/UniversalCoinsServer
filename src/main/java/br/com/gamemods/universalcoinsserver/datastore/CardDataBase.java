package br.com.gamemods.universalcoinsserver.datastore;

import java.util.UUID;

public interface CardDataBase
{
    UUID getAccountOwner(String account) throws DataBaseException;

    int getAccountBalance(String account) throws DataBaseException;

    boolean depositToAccount(String account, int depositAmount, Operator operator, TransactionType transaction, String product) throws DataBaseException;

    void saveNewMachine(Machine machine) throws DataBaseException;

    void saveTransaction(Transaction transaction) throws DataBaseException;
}
