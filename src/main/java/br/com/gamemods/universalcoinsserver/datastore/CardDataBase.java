package br.com.gamemods.universalcoinsserver.datastore;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface CardDataBase
{
    UUID getAccountOwner(String account) throws DataBaseException;

    int getAccountBalance(Object account) throws DataBaseException;

    boolean depositToAccount(String account, int depositAmount, Operator operator, TransactionType transaction, String product) throws DataBaseException;

    void saveNewMachine(@Nonnull Machine machine) throws DataBaseException;

    void saveTransaction(@Nonnull Transaction transaction) throws DataBaseException;

    @Nonnull
    PlayerData getPlayerData(@Nonnull UUID playerUID) throws DataBaseException;

    @Nonnull
    AccountAddress createPrimaryAccount(@Nonnull UUID playerUID, @Nonnull String name) throws DataBaseException;
}
