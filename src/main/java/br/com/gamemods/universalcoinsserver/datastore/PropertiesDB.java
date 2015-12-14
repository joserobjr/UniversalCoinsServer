package br.com.gamemods.universalcoinsserver.datastore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class PropertiesDB implements CardDataBase
{
    private final File baseDir, accounts, players;

    public PropertiesDB(File baseDir) throws IOException
    {
        this.baseDir = baseDir;
        if(!baseDir.isDirectory() && !baseDir.mkdirs())
            throw new IOException(baseDir.getAbsolutePath()+" is not a directory");

        accounts = new File(baseDir, "accounts");
        if(!accounts.isDirectory() && !accounts.mkdirs())
            throw new IOException("Failed to create "+accounts.getAbsolutePath());

        players = new File(baseDir, "players");
        if(!players.isDirectory() && !players.mkdirs())
            throw new IOException("Failed to create "+players.getAbsolutePath());
    }

    private Properties loadAccount(String account) throws DataBaseException
    {
        File accountFile = new File(accounts, account+".properties");
        if(!accountFile.isFile())
            return null;

        Properties properties = new Properties();
        try(FileReader reader = new FileReader(accountFile))
        {
            properties.load(reader);
        }
        catch (Exception e)
        {
            throw new DataBaseException(e);
        }

        return properties;
    }

    private void saveAccount(String account, Properties properties) throws DataBaseException
    {
        File accountFile = new File(accounts, account+".properties");
        try(FileWriter writer = new FileWriter(accountFile))
        {
            properties.store(writer, "Account: "+account);
        }
        catch (Exception e)
        {
            throw new DataBaseException(e);
        }
    }


    @Override
    public UUID getAccountOwner(String account) throws DataBaseException
    {
        Properties properties = loadAccount(account);
        if (properties == null)
            return null;
        try
        {
            return UUID.fromString(properties.getProperty("owner.id"));
        }
        catch (Exception e)
        {
            throw new DataBaseException(e);
        }
    }

    @Override
    public int getAccountBalance(String account) throws DataBaseException
    {
        Properties properties = loadAccount(account);
        if(properties == null)
            return -1;

        try
        {
            return Integer.parseInt(properties.getProperty("balance"));
        }
        catch (Exception e)
        {
            throw new DataBaseException(e);
        }
    }

    @Override
    public boolean depositToAccount(String account, int depositAmount, CardOperator operator, TransactionType transaction, String product) throws DataBaseException
    {
        Properties properties = loadAccount(account);
        if(properties == null)
            return false;

        try
        {
            long balance = Integer.parseInt(properties.getProperty("balance"));
            if(balance < 0)
                return false;

            balance += depositAmount;
            if(balance > Integer.MAX_VALUE)
                return false;

            properties.put("balance", balance);
        }
        catch (Exception e)
        {
            throw new DataBaseException(e);
        }

        saveAccount(account, properties);
        return true;
    }
}
