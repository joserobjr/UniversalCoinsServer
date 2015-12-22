package br.com.gamemods.universalcoinsserver.datastore;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public abstract class AbstractSQL<A extends AbstractSQL.SqlAccount> extends AbstractDB<A>
{
    protected Connection connection;

    public AbstractSQL(Connection connection)
    {
        this.connection = connection;
    }

    class SqlAccount extends Account
    {
        final boolean primary;
        public SqlAccount(String id, UUID owner, int balance, boolean primary)
        {
            super(id, owner, balance);
            this.primary = primary;
        }

        @Override
        public void incrementBalance(int increment, @Nullable Transaction transaction) throws DataStoreException
        {
            boolean inTransaction;
            try
            {
                inTransaction = !connection.getAutoCommit();
            }
            catch (SQLException e)
            {
                throw new DataStoreException(e);
            }

            try
            {
                if(!inTransaction)
                    connection.setAutoCommit(false);

                try (PreparedStatement pst = connection.prepareStatement("UPDATE `accounts` SET `balance`=`balance`+? WHERE `number`=?"))
                {
                    pst.setInt(1, increment);
                    pst.setString(2, id);
                    pst.executeUpdate();
                }

                if(transaction != null)
                    saveTransaction(transaction);

                if(!inTransaction)
                    connection.commit();

                balance += increment;
            }
            catch (Throwable e)
            {
                if(!inTransaction)
                    try
                    {
                        connection.rollback();
                    }
                    catch (SQLException e2)
                    {
                        e2.printStackTrace();
                    }

                throw new DataStoreException(e);
            }
            finally
            {
                if(!inTransaction)
                    try
                    {
                        connection.setAutoCommit(true);
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
            }
        }
    }
}
