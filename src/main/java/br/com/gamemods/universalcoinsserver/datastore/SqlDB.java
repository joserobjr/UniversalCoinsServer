package br.com.gamemods.universalcoinsserver.datastore;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;
import java.util.*;

public class SqlDB extends AbstractSQL<AbstractSQL.SqlAccount>
{
    public SqlDB(Connection connection)
    {
        super(connection);
    }

    @Nullable
    @Override
    protected SqlAccount getAccount(@Nonnull String number) throws DataStoreException
    {
        try(PreparedStatement pst = connection.prepareStatement("SELECT `number`, `owner`, `balance`, `primary` FROM `accounts` WHERE `number`=? AND `terminated` IS NULL"))
        {
            pst.setString(1, number);
            ResultSet result = pst.executeQuery();
            if(!result.next())
                return null;

            return new SqlAccount(result.getString(1), UUID.fromString(result.getString(2)), result.getInt(3), result.getBoolean(4));
        }
        catch (SQLException|IllegalArgumentException e)
        {
            throw new DataStoreException(e);
        }
    }

    @Nullable
    @Override
    protected SqlAccount getCustomAccount(@Nonnull String name) throws DataStoreException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "SELECT ac.number, ac.owner, ac.balance, ac.primary FROM `custom_accounts` AS ca INNER JOIN `accounts` ON `number`=`account` WHERE ca.name=? AND `terminated` IS NULL"
        ))
        {
            pst.setString(1, name);
            ResultSet result = pst.executeQuery();
            if(!result.next())
                return null;

            return new SqlAccount(result.getString(1), UUID.fromString(result.getString(2)), result.getInt(3), result.getBoolean(4));
        }
        catch (SQLException e)
        {
            throw new DataStoreException(e);
        }
    }

    @Override
    protected void storeTrade(@Nonnull Transaction transaction, @Nullable AbstractSQL.SqlAccount ownerAccount, int ownerIncrement, @Nullable AbstractSQL.SqlAccount userAccount, int userIncrement)
            throws DataStoreException
    {
        try
        {
            connection.setAutoCommit(false);
            if(ownerAccount != null)
                ownerAccount.incrementBalance(ownerIncrement, null);

            if(userAccount != null)
                userAccount.incrementBalance(userIncrement, null);

            saveTransaction(transaction);
            connection.commit();
        }
        catch (Throwable e)
        {
            try
            {
                connection.rollback();
            } catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            throw new DataStoreException(e);
        }
        finally
        {
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

    @Override
    public void saveNewMachine(@Nonnull Machine machine) throws DataStoreException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO `machines`(`machine_id`,`dim`,`x`,`y`,`z`,`block`,`metadata`,`tile`,`owner`) " +
                              "VALUES  (     ?      ,  ?  , ? , ? , ? ,   ?   ,    ?     ,   ?  ,   ?   )"))
                                        //   1         2    3   4   5     6        7         8      9
        {
            pst.setString(1, machine.getMachineId().toString());
            TileEntity machineEntity = machine.getMachineEntity();
            pst.setInt(3, machineEntity.xCoord);
            //noinspection SuspiciousNameCombination
            pst.setInt(4, machineEntity.yCoord);
            pst.setInt(5, machineEntity.zCoord);
            if(!machineEntity.hasWorldObj())
            {
                pst.setNull(2, Types.INTEGER);
                pst.setNull(6, Types.VARCHAR);
                pst.setNull(7, Types.INTEGER);
            }
            else
            {
                pst.setInt(2, machineEntity.getWorldObj().provider.dimensionId);
                String block = GameData.getBlockRegistry().getNameForObject(machineEntity.getBlockType());

                if(block != null)
                    pst.setString(6, block);
                else
                    pst.setNull(6, Types.VARCHAR);

                pst.setInt(7, machineEntity.getBlockMetadata());
                pst.setString(8, machineEntity.getClass().getName());

                if(machineEntity instanceof PlayerOwned)
                {
                    UUID ownerId = ((PlayerOwned) machineEntity).getOwnerId();
                    if(ownerId != null)
                        pst.setString(9, ownerId.toString());
                    else
                        pst.setNull(9, Types.VARCHAR);
                }
                else
                    pst.setNull(9, Types.VARCHAR);

                pst.executeUpdate();
            }
        }
        catch (SQLException e)
        {
            throw new DataStoreException(e);
        }
    }

    private void updateMachine(@Nullable Machine machine) throws DataStoreException
    {
        if(machine == null) return;
        boolean found;
        try(PreparedStatement pst = connection.prepareStatement("SELECT `x` FROM `machines` WHERE `machine_id`=?"))
        {
            pst.setString(1, machine.getMachineId().toString());
            found = pst.executeQuery().next();
        }
        catch (SQLException e)
        {
            throw new DataStoreException(e);
        }

        if(!found)
            saveNewMachine(machine);
        else
        {
            TileEntity machineEntity = machine.getMachineEntity();
            boolean worldObj = machineEntity.hasWorldObj();
            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE `machines` SET `x`=?,`y`=?,`z`=?"+(worldObj?",`dim`=?,`block`=?,`metadata`=?":"")+",`tile`=?,`owner`=? WHERE `machine_id`=?"))
                                        //  1     2     3                       4         5            6           4/7      5/8                 6/9
            {
                int field = 1;
                pst.setNull(field++, machineEntity.xCoord);
                pst.setNull(field++, machineEntity.yCoord);
                pst.setNull(field++, machineEntity.zCoord);
                if(worldObj)
                {
                    pst.setInt(field++, machineEntity.getWorldObj().provider.dimensionId);
                    String block = GameData.getBlockRegistry().getNameForObject(machineEntity.getBlockType());

                    if (block != null)
                        pst.setString(field++, block);
                    else
                        pst.setNull(field++, Types.VARCHAR);

                    pst.setInt(field++, machineEntity.getBlockMetadata());
                }

                pst.setString(field++, machineEntity.getClass().getName());

                if(machineEntity instanceof PlayerOwned)
                {
                    UUID ownerId = ((PlayerOwned) machineEntity).getOwnerId();
                    if(ownerId != null)
                        pst.setString(field++, ownerId.toString());
                    else
                        pst.setNull(field++, Types.VARCHAR);
                }
                else
                    pst.setNull(field++, Types.VARCHAR);

                pst.setString(field, machine.getMachineId().toString());

                pst.executeUpdate();
            }
            catch (SQLException e)
            {
                throw new DataStoreException(e);
            }
        }
    }

    private void addBlockOperatorData(PreparedStatement pst, BlockOperator blockOperator) throws SQLException
    {
        int field = 1;
        pst.setInt(field++, blockOperator.getX());
        pst.setInt(field++, blockOperator.getY());
        pst.setInt(field++, blockOperator.getZ());
        Integer num = blockOperator.getDim();
        if(num == null)
            pst.setNull(field++, Types.INTEGER);
        else
            pst.setInt(field++, num);
        String blockId = blockOperator.getBlockId();
        if(blockId == null)
            pst.setNull(field++, Types.VARCHAR);
        else
            pst.setString(field++, blockId);
        num = blockOperator.getBlockMeta();
        if(null == num)
            pst.setNull(field++, Types.INTEGER);
        else
            pst.setInt(field++, num);
        UUID owner = blockOperator.getOwner();
        if(owner == null)
            pst.setNull(field++, Types.VARCHAR);
        else
            pst.setString(field++, owner.toString());
        if(blockOperator instanceof MachineOperator)
        {
            MachineOperator machine = (MachineOperator) blockOperator;
            pst.setString(field++, machine.getMachine().getMachineId().toString());
            pst.setString(field, machine.getMachine().getMachineEntity().getClass().getName());
        }
        else
        {
            pst.setNull(field++, Types.VARCHAR);
            pst.setNull(field, Types.VARCHAR);
        }
    }

    private int registerCoinSource(Transaction.CoinSource coinSource) throws SQLException, DataStoreException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO `coin_source`(`before`,`after`,`type`,`machine`,`account`,`card_item`,`card_damage`,`card_amount`," +
                                          //   1   ,   2   ,   3  ,   4     ,    5    ,      6    ,    7        ,     8
                        "`card_nbt`,`player_operator`,`block_operator`) " +
                        //    9    ,        10       ,      11
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        ))
        {
            pst.setInt(1, coinSource.getBalanceBefore());
            pst.setInt(2, coinSource.getBalanceAfter());
            if(coinSource instanceof Transaction.MachineCoinSource)
            {
                pst.setString(3, "machine");
                Machine machine = ((Transaction.MachineCoinSource) coinSource).getMachine();
                pst.setString(4, machine.getMachineId().toString());
                updateMachine(machine);
            }
            else
            {
                pst.setNull(4, Types.CHAR);
            }

            if(coinSource instanceof Transaction.CardCoinSource)
            {
                pst.setString(3, "card");
                Transaction.CardCoinSource card = (Transaction.CardCoinSource) coinSource;
                pst.setString(5, card.getAccountAddress().getNumber().toString());
                ItemStack stack = card.getCard();
                if(stack != null)
                {
                    String type = GameData.getItemRegistry().getNameForObject(stack.getItem());
                    if(type == null) type = stack.getItem().getClass().getName();
                    pst.setString(6, type);
                    pst.setInt(7, stack.getItemDamage());
                    pst.setInt(8, stack.stackSize);
                    if(stack.stackTagCompound != null)
                        pst.setString(9, stack.stackTagCompound.toString());
                    else
                        pst.setNull(9, Types.VARCHAR);
                }
                else
                {
                    pst.setNull(6, Types.VARCHAR);
                    pst.setNull(7, Types.INTEGER);
                    pst.setNull(8, Types.INTEGER);
                    pst.setNull(9, Types.VARCHAR);
                }
            }
            else
            {
                pst.setNull(5, Types.VARCHAR);
                pst.setNull(6, Types.VARCHAR);
                pst.setNull(7, Types.INTEGER);
                pst.setNull(8, Types.INTEGER);
                pst.setNull(9, Types.VARCHAR);
            }

            if(coinSource instanceof Transaction.InventoryCoinSource)
            {
                pst.setString(3, "inventory");
                Operator operator = ((Transaction.InventoryCoinSource) coinSource).getOperator();
                if(operator instanceof PlayerOperator)
                    pst.setString(10, ((PlayerOperator) operator).getPlayerId().toString());
                else
                    pst.setNull(10, Types.VARCHAR);

                if(operator instanceof BlockOperator)
                    pst.setInt(11, saveBlockOperator((BlockOperator) operator));
                else
                    pst.setNull(11, Types.INTEGER);
            }
            else
            {
                pst.setNull(10, Types.VARCHAR);
                pst.setNull(11, Types.INTEGER);
            }

            pst.executeUpdate();
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if(!generatedKeys.next())
                throw new DataStoreException("The coin_source ID wasnt returned for "+coinSource);
            return generatedKeys.getInt(1);
        }
    }

    private int saveBlockOperator(BlockOperator blockOperator) throws SQLException, DataStoreException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "SELECT `operator_id` FROM `block_operators` WHERE " +
                        "`x`=? AND `y`=? AND `z`=? AND `dim`=? AND `block_id`=? AND `block_meta` =? " +
                        "AND `owner`=? AND `machine_id`=? AND `machine_type`=?"
        ))
        {
            addBlockOperatorData(pst, blockOperator);
            ResultSet result = pst.executeQuery();
            if(result.next())
                return result.getInt(1);
        }

        try(PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO `block_operators`(`x`,`y`,`z`,`dim`,`block_id`,`block_meta`,`owner`,`machine_id`,`machine_type`) " +
                        "VALUES(?,?,?,?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        ))
        {
            addBlockOperatorData(pst, blockOperator);
            pst.executeUpdate();
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if(!generatedKeys.next())
                throw new DataStoreException("Failed to retrieve generated key for "+blockOperator);
            return generatedKeys.getInt(1);
        }
    }

    @Override
    public void saveTransaction(@Nonnull Transaction transaction) throws DataStoreException
    {
        boolean inTransaction;
        try
        {
            inTransaction = !connection.getAutoCommit();
        } catch (SQLException e)
        {
            throw new DataStoreException(e);
        }

        updateMachine(transaction.getMachine());
        try
        {
            if(!inTransaction)
                connection.setAutoCommit(false);

            Operator operator = transaction.getOperator();
            int blockOperatorId;
            if(operator instanceof BlockOperator)
                blockOperatorId = saveBlockOperator((BlockOperator) operator);
            else
                blockOperatorId = -1;

            Transaction.CoinSource coinSource = transaction.getUserCoinSource();
            int userCoinSource=-1, ownerCoinSource=-1;
            if(coinSource != null)
                userCoinSource = registerCoinSource(coinSource);
            coinSource = transaction.getOwnerCoinSource();
            if(coinSource != null)
                ownerCoinSource = registerCoinSource(coinSource);


            try(PreparedStatement pst = connection.prepareStatement(
                    "INSERT INTO `transactions`(`transaction_id`,`time`,`machine`,`player_operator`,`block_operator`," +
                                                //     1        ,   2  ,     3   ,         4       ,    5
                            "`product_item`,`product_damage`,`product_amount`,`product_nbt`,`trade_item`,`trade_damage`,`trade_amount`,`trade_nbt`," +
                            //     6       ,      7         ,        8       ,       9     ,      10    ,     11       ,      12      ,     13
                            "`operation`,`infinite`,`quantity`,`price`,`total_price`,`user_coinsource`,`owner_coinsource`) " +
                            //   14     ,     15   ,    16    ,   17  ,     18      ,     19          ,      20
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            ))
            {
                pst.setString(1, transaction.getId().toString());
                pst.setTimestamp(2, new Timestamp(transaction.getTime()));
                Machine machine = transaction.getMachine();
                if(machine != null)
                    pst.setString(3, machine.getMachineId().toString());
                else
                    pst.setNull(3, Types.CHAR);
                if(operator instanceof PlayerOperator)
                {
                    pst.setString(4, ((PlayerOperator) operator).getPlayerId().toString());
                    pst.setNull(5, Types.INTEGER);
                }
                else if(operator instanceof BlockOperator)
                {
                    pst.setNull(4, Types.CHAR);
                    pst.setInt(5, blockOperatorId);
                }
                ItemStack stack = transaction.getProduct();
                if(stack != null)
                {
                    String type = GameData.getItemRegistry().getNameForObject(stack.getItem());
                    if(type == null) type = stack.getItem().getClass().getName();
                    pst.setString(6, type);
                    pst.setInt(7, stack.getItemDamage());
                    pst.setInt(8, stack.stackSize);
                    if(stack.stackTagCompound != null)
                        pst.setString(9, stack.stackTagCompound.toString());
                    else
                        pst.setNull(9, Types.VARCHAR);
                }
                else
                {
                    pst.setNull(6, Types.VARCHAR);
                    pst.setNull(7, Types.INTEGER);
                    pst.setNull(8, Types.INTEGER);
                    pst.setNull(9, Types.VARCHAR);
                }

                stack = transaction.getTrade();
                if(stack != null)
                {
                    String type = GameData.getItemRegistry().getNameForObject(stack.getItem());
                    if(type == null) type = stack.getItem().getClass().getName();
                    pst.setString(10, type);
                    pst.setInt(11, stack.getItemDamage());
                    pst.setInt(12, stack.stackSize);
                    if(stack.stackTagCompound != null)
                        pst.setString(13, stack.stackTagCompound.toString());
                    else
                        pst.setNull(13, Types.VARCHAR);
                }
                else
                {
                    pst.setNull(10, Types.VARCHAR);
                    pst.setNull(11, Types.INTEGER);
                    pst.setNull(12, Types.INTEGER);
                    pst.setNull(13, Types.VARCHAR);
                }

                pst.setString(14, transaction.getOperation().name());
                pst.setBoolean(15, transaction.isInfiniteMachine());
                pst.setInt(16, transaction.getQuantity());
                pst.setInt(17, transaction.getPrice());
                pst.setInt(18, transaction.getTotalPrice());
                if(userCoinSource > -1)
                    pst.setInt(19, userCoinSource);
                else
                    pst.setNull(19, Types.INTEGER);

                if(ownerCoinSource > -1)
                    pst.setInt(20, ownerCoinSource);
                else
                    pst.setNull(20, Types.INTEGER);

                pst.executeUpdate();

                if(!inTransaction)
                    connection.commit();
            }

        }
        catch (Throwable e)
        {
            if(!inTransaction)
                try
                {
                    connection.rollback();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }

            throw new DataStoreException(e);
        }
        finally
        {
            if(!inTransaction)
                try
                {
                    connection.setAutoCommit(true);
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
        }
    }

    @Nonnull
    @Override
    public PlayerData getPlayerData(@Nonnull UUID playerUID) throws DataStoreException
    {
        try
        {
            AccountAddress primary;
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `primary_account`, ac.name,ac.owner " +
                        "FROM `user_data` LEFT JOIN `accounts` AS ac ON ac.number=`primary_account` " +
                        "WHERE `player_id`=?"
            ))
            {
                pst.setString(1, playerUID.toString());
                ResultSet result = pst.executeQuery();
                if(!result.next())
                    primary = null;
                else
                    primary = new AccountAddress(result.getString(1), result.getString(2), UUID.fromString(result.getString(3)));
            }

            ArrayList<AccountAddress> customAccounts = new ArrayList<>();
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT ac.number, ac.name, ac.owner " +
                        "FROM `custom_accounts` AS ca INNER JOIN `accounts` AS ac ON ac.number=ca.account " +
                        "WHERE ac.owner=? AND ac.terminated IS NULL AND ca.terminated IS NULL"
            ))
            {
                pst.setString(1, playerUID.toString());
                ResultSet result = pst.executeQuery();
                while (result.next())
                    customAccounts.add(new AccountAddress(result.getString(1), result.getString(2), UUID.fromString(result.getString(3))));
            }

            return new PlayerData(Integer.MIN_VALUE, playerUID, primary, customAccounts);
        }
        catch (SQLException|IllegalArgumentException e)
        {
            throw new DataStoreException(e);
        }
    }

    @Nonnull
    @Override
    public AccountAddress createPrimaryAccount(@Nonnull UUID playerUID, @Nonnull String name) throws DataStoreException, DuplicatedKeyException
    {
        return createAccount(playerUID, name, true, false);
    }

    @Nonnull
    private AccountAddress createAccount(@Nonnull UUID playerUID, @Nonnull String name, boolean primary, boolean transference) throws DataStoreException, DuplicatedKeyException
    {
        boolean inTransaction;
        boolean deleteOldReference = false;
        String playerId = playerUID.toString();
        try
        {
            inTransaction = !connection.getAutoCommit();

            if(primary)
            {
                if(!transference)
                {
                    try(PreparedStatement pst = connection.prepareStatement(
                            "SELECT `primary_account`, `terminated` " +
                                    "FROM `user_data` LEFT JOIN accounts ON `number`=`primary_account` " +
                                    "WHERE `player_id`=?"
                    ))
                    {
                        pst.setString(1, playerId);
                        ResultSet result = pst.executeQuery();
                        if(result.next())
                        {
                            String account = result.getString(1);
                            Timestamp terminated = result.getTimestamp(2);
                            if(account != null)
                            {
                                if(terminated == null)
                                    throw new DuplicatedKeyException(playerUID+" already have a primary account: "+account);
                                else
                                    deleteOldReference = true;
                            }
                        }
                    }
                }
            }
            else
            {
                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT ca.`account`, ac.`terminated` AS `account_terminated`, ca.`terminated` AS `name_terminated` " +
                            "FROM `custom_accounts` AS ca LEFT JOIN `accounts` AS ac ON `number`=`account` " +
                            "WHERE ca.name=?"
                ))
                {
                    pst.setString(1, name);
                    ResultSet resultSet = pst.executeQuery();
                    if(resultSet.next())
                    {
                        String account = resultSet.getString(1);
                        Timestamp terminated = resultSet.getTimestamp(2);
                        if(account != null)
                        {
                            if(terminated == null)
                                throw new DuplicatedKeyException(name + " is already registered for: " + account);
                            else
                                deleteOldReference = true;
                        }
                    }
                }
            }
        } catch (SQLException e)
        {
            throw new DataStoreException(e);
        }

        try
        {
            if(!inTransaction)
                connection.setAutoCommit(false);

            String number;
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `owner` FROM `accounts` WHERE `number`=?"
            ))
            {
                do
                {
                    number = generateAccountNumber();
                    pst.setString(1, number);
                } while (pst.executeQuery().next());
            }

            boolean registerUser;
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `primary_account` FROM `user_data` WHERE `player_id`=?"
            ))
            {
                pst.setString(1, playerId);
                ResultSet result = pst.executeQuery();
                registerUser = !result.next();
            }

            if(registerUser)
                try(PreparedStatement pst = connection.prepareStatement(
                        "INSERT INTO `user_data`(`player_id`) VALUES(?)"
                ))
                {
                    pst.setString(1, playerId);
                    pst.executeUpdate();
                }

            try (PreparedStatement pst = connection.prepareStatement(
                    "INSERT INTO `accounts`(`number`,`owner`,`name`,`primary`) VALUES(?,?,?,?)"
            ))
            {
                pst.setString(1, number);
                pst.setString(2, playerId);
                pst.setString(3, name);
                pst.setBoolean(4, primary);
                pst.executeUpdate();
            }

            if(primary)
            {
                try(PreparedStatement pst = connection.prepareStatement(
                        "UPDATE `user_data` SET `primary_account`=? WHERE `player_id`=?"
                ))
                {
                    pst.setString(1, number);
                    pst.setString(2, playerId);
                    pst.executeUpdate();
                }
            }
            else
            {
                if(deleteOldReference)
                    try(PreparedStatement pst = connection.prepareStatement(
                            "DELETE FROM `custom_accounts` WHERE `name`=?"
                    ))
                    {
                        pst.setString(1, name);
                        pst.executeUpdate();
                    }

                try(PreparedStatement pst = connection.prepareStatement(
                        "INSERT INTO `custom_accounts`(`name`,`account`) VALUES(?,?)"
                ))
                {
                    pst.setString(1, name);
                    pst.setString(2, number);
                    pst.executeUpdate();
                }
            }

            if(!inTransaction)
                connection.commit();
            return new AccountAddress(number, name, playerUID);
        }
        catch (Throwable e)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
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
                catch (Exception e)
                {
                    e.printStackTrace();
                }
        }
    }

    @Nullable
    @Override
    public AccountAddress getCustomAccountByName(@Nonnull String customAccountName) throws DataStoreException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "SELECT ac.number, ac.name, ac.owner " +
                    "FROM `custom_accounts` AS ca " +
                        "INNER JOIN `accounts` AS ac ON `number`=`account` AND ca.name=? " +
                    "WHERE ca.`terminated` IS NULL AND ac.`terminated` IS NULL"
        ))
        {
            pst.setString(1, customAccountName);
            ResultSet result = pst.executeQuery();
            if(!result.next())
                return null;
            return new AccountAddress(result.getString(1), result.getString(2), UUID.fromString(result.getString(3)));
        }
        catch (SQLException|IllegalArgumentException e)
        {
            throw new DataStoreException(e);
        }
    }

    @Nonnull
    @Override
    public AccountAddress createCustomAccount(@Nonnull UUID playerUID, @Nonnull String customAccountName) throws DataStoreException, DuplicatedKeyException
    {
        return createAccount(playerUID, customAccountName, false, false);
    }

    @Nonnull
    @Override
    public AccountAddress transferAccount(@Nonnull AccountAddress origin, @Nonnull String destiny, @Nullable Machine machine, @Nullable Operator operator)
            throws DataStoreException, AccountNotFoundException, DuplicatedKeyException
    {
        AccountAddress customAccountByName = getCustomAccountByName(origin.getName());
        if(customAccountByName == null)
            throw new AccountNotFoundException(origin);

        try
        {
            connection.setAutoCommit(false);

            AccountAddress newAccount = transfer(origin, destiny, machine, operator, false);

            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE `custom_accounts` SET `terminated`=?, `transferred`=?, `transferred_name`=? WHERE `name`=?"
                                                        //     1                2                     3              4
            ))
            {
                pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                pst.setString(2, newAccount.getNumber().toString());
                pst.setString(3, newAccount.getName());
                pst.setString(4, origin.getName());
                pst.executeUpdate();
            }

            return newAccount;
        }
        catch (Throwable e)
        {
            try
            {
                connection.rollback();
            } catch (SQLException e1)
            {
                e1.printStackTrace();
            }

            throw new DataStoreException(e);
        }
        finally
        {
            try
            {
                connection.setAutoCommit(true);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Nonnull
    @Override
    public AccountAddress transferPrimaryAccount(@Nonnull AccountAddress primaryAccount, @Nonnull String newName, @Nullable Machine machine, @Nullable Operator operator)
            throws DataStoreException, AccountNotFoundException
    {
        try
        {
            connection.setAutoCommit(false);
            AccountAddress newAccount = transfer(primaryAccount, newName, machine, operator, true);
            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE `user_data` SET `primary_account`=? WHERE `player_id`=?"
            ))
            {
                pst.setString(1, newAccount.getNumber().toString());
                pst.setString(2, newAccount.getOwner().toString());
                pst.executeUpdate();
            }

            connection.commit();
            return newAccount;
        }
        catch (Throwable e)
        {
            try
            {
                connection.rollback();
            } catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            throw new DataStoreException(e);
        }
        finally
        {
            try
            {
                connection.setAutoCommit(true);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Nonnull
    private AccountAddress transfer(@Nonnull AccountAddress oldAccount, @Nonnull String newName, @Nullable Machine machine, @Nullable Operator operator, boolean primary)
            throws DataStoreException, AccountNotFoundException
    {
        AbstractSQL.SqlAccount account = getAccount(oldAccount.getNumber());
        if(account == null) throw new AccountNotFoundException(oldAccount);
        int balance = account.getBalance();

        boolean inTransaction;
        try
        {
            inTransaction = !connection.getAutoCommit();
        } catch (SQLException e)
        {
            throw new DataStoreException(e);
        }

        try
        {
            if(!inTransaction)
                connection.setAutoCommit(false);

            AccountAddress newAddress = createAccount(oldAccount.getOwner(), newName, primary, true);
            AbstractSQL.SqlAccount newAccount = getAccount(newAddress.getNumber());
            assert newAccount != null;

            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE `accounts` SET `balance`=0, `terminated`=?, `transferred`=? WHERE `number`=?"
            ))
            {
                pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                pst.setString(2, newAddress.getNumber().toString());
                pst.setString(3, account.id);
                pst.executeUpdate();
            }

            newAccount.incrementBalance(balance, null);


            Transaction transaction = new Transaction(machine, Transaction.Operation.TRANSFER_ACCOUNT, operator,
                    new Transaction.CardCoinSource(null, oldAccount, balance, 0),
                    new Transaction.CardCoinSource(null, newAddress, 0, balance), null);


            saveTransaction(transaction);

            if(!inTransaction)
                connection.commit();
            return newAddress;
        }
        catch (Throwable e)
        {
            if(!inTransaction)
                try
                {
                    connection.rollback();
                } catch (SQLException e1)
                {
                    e1.printStackTrace();
                }
            throw new DataStoreException(e);
        }
        finally
        {
            if(!inTransaction)
                try
                {
                    connection.setAutoCommit(true);
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public Collection<PlayerData> getAllPlayerData() throws DataStoreException
    {
        try(Statement stm = connection.createStatement())
        {
            ResultSet result = stm.executeQuery("SELECT COUNT(*) FROM `user_data`");
            result.next();
            ArrayList<PlayerData> list = new ArrayList<>(result.getInt(1));

            result = stm.executeQuery("SELECT `player_id` FROM `user_data`");
            while (result.next())
                list.add(getPlayerData(UUID.fromString(result.getString(1))));

            return list;
        }
        catch (SQLException e)
        {
            throw new DataStoreException(e);
        }
    }

    @Override
    public Map<AccountAddress, Integer> getAllAccountsBalance() throws DataStoreException
    {
        try(Statement stm = connection.createStatement())
        {
            ResultSet result = stm.executeQuery("SELECT COUNT(*) FROM `accounts` WHERE `terminated` IS NULL");
            result.next();
            Map<AccountAddress, Integer> map = new HashMap<>(result.getInt(1));

            result = stm.executeQuery("SELECT `number`, `owner`, `name`, `balance` FROM `accounts` WHERE `terminated` IS NULL");
            while (result.next())
                map.put(new AccountAddress(result.getString(1), result.getString(3), UUID.fromString(result.getString(2))), result.getInt(4));

            return map;
        }
        catch (SQLException e)
        {
            throw new DataStoreException(e);
        }
    }

    @Override
    public void importData(CardDataBase original) throws DataStoreException
    {
        try
        {
            connection.setAutoCommit(false);

            Collection<PlayerData> allPlayerData = original.getAllPlayerData();
            Map<AccountAddress, Integer> allAccountsBalance = original.getAllAccountsBalance();
            Map<String, Map.Entry<AccountAddress, Integer>> accountMap = new HashMap<>(allAccountsBalance.size());
            for(Map.Entry<AccountAddress, Integer> entry: allAccountsBalance.entrySet())
                accountMap.put(entry.getKey().getNumber().toString(), entry);

            Logger logger = UniversalCoinsServer.logger;


            for(PlayerData otherPlayerData: allPlayerData)
            {
                PlayerData localPlayerData = getPlayerData(otherPlayerData.getPlayerId());
                AccountAddress otherPrimaryAccount = otherPlayerData.getPrimaryAccount();

                logger.info("");
                logger.info("Processing player "+localPlayerData.getPlayerId());

                if(otherPrimaryAccount != null)
                {
                    int otherBalance = accountMap.get(otherPrimaryAccount.getNumber().toString()).getValue();
                    if(otherBalance > 0)
                    {
                        if (localPlayerData.getPrimaryAccount() == null)
                        {
                            logger.info("Creating primary account for "+localPlayerData.getPlayerId()+" with name "+otherPrimaryAccount.getName());

                            AccountAddress localAddress = createPrimaryAccount(localPlayerData.getPlayerId(), otherPrimaryAccount.getName());
                            logger.info("Account created: "+localAddress);
                            try(PreparedStatement pst = connection.prepareStatement(
                                    "UPDATE `accounts` SET `number`=?, `balance`=? WHERE `number`=?"
                            ))
                            {
                                logger.info("Changing balance to "+otherBalance+" and number to "+otherPrimaryAccount.getNumber());

                                pst.setString(1, otherPrimaryAccount.getNumber().toString());
                                pst.setInt(2, otherBalance);
                                pst.setString(3, localAddress.getNumber().toString());
                                pst.executeUpdate();
                            }
                        }
                        else
                        {
                            AbstractSQL.SqlAccount account = getAccount(localPlayerData.getPrimaryAccount());
                            logger.info("Adding "+otherBalance+" to the account "+account.id);
                            account.incrementBalance(otherBalance, null);
                        }
                    }
                }

                if(otherPlayerData.getAlternativeAccounts().isEmpty())
                    continue;

                if(localPlayerData.getAlternativeAccounts().isEmpty())
                {
                    logger.info("The player doesn't have any alternative account, creating "+otherPlayerData.getAlternativeAccounts().size()+"...");
                    for(AccountAddress otherAccountAddress: otherPlayerData.getAlternativeAccounts())
                    {
                        int balance = accountMap.get(otherAccountAddress.getNumber().toString()).getValue();
                        if(balance <= 0) continue;
                        logger.info("Creating account "+otherAccountAddress.getName());
                        AccountAddress customAccount = createCustomAccount(localPlayerData.getPlayerId(), otherAccountAddress.getName());
                        logger.info("Account created with number "+customAccount.getNumber()+", changing to "+otherAccountAddress.getNumber()+" and setting balance to "+balance);
                        try(PreparedStatement pst = connection.prepareStatement(
                                "UPDATE `accounts` SET `number`=?, `balance`=? WHERE `number`=?"
                        ))
                        {
                            pst.setString(1, otherAccountAddress.getNumber().toString());
                            pst.setInt(2, balance);
                            pst.setString(3, customAccount.getNumber().toString());
                            pst.executeUpdate();
                        }
                    }
                }
                else if(localPlayerData.getAlternativeAccounts().size() == 1)
                {
                    logger.info("The player has one alternative account, merging "+otherPlayerData.getAlternativeAccounts().size()+" accounts...");
                    AbstractSQL.SqlAccount account = getAccount(localPlayerData.getAlternativeAccounts().iterator().next());
                    for(AccountAddress otherAccountAddress: otherPlayerData.getAlternativeAccounts())
                    {
                        int balance = accountMap.get(otherAccountAddress.getNumber().toString()).getValue();
                        if(balance <= 0) continue;
                        logger.info("Adding "+balance+" to the balance that came from "+otherAccountAddress);
                        account.incrementBalance(balance, null);
                    }
                }
                else
                {
                    logger.info("The player has multiple custom accounts, creating/merging "+otherPlayerData.getAlternativeAccounts().size()+" accounts...");
                    for(AccountAddress otherAccountAddress: otherPlayerData.getAlternativeAccounts())
                    {
                        int balance = accountMap.get(otherAccountAddress.getNumber().toString()).getValue();
                        if(balance <= 0) continue;

                        SqlAccount customAccount = getCustomAccount(otherAccountAddress.getName());
                        if(customAccount != null)
                        {
                            logger.info("Adding "+balance+" to the balance that came from "+otherAccountAddress);
                            customAccount.incrementBalance(balance, null);
                        }
                        else
                        {
                            logger.info("Creating account "+otherAccountAddress.getName());
                            AccountAddress createdAccount = createCustomAccount(localPlayerData.getPlayerId(), otherAccountAddress.getName());
                            logger.info("Account created with number "+createdAccount.getNumber()+", changing to "+otherAccountAddress.getNumber()+" and setting balance to "+balance);
                            try(PreparedStatement pst = connection.prepareStatement(
                                    "UPDATE `accounts` SET `number`=?, `balance`=? WHERE `number`=?"
                            ))
                            {
                                pst.setString(1, otherAccountAddress.getNumber().toString());
                                pst.setInt(2, balance);
                                pst.setString(3, createdAccount.getNumber().toString());
                                pst.executeUpdate();
                            }
                        }
                    }
                }
            }

            logger.info("");
            logger.info("Import finished, committing");
            connection.commit();
        }
        catch (Throwable e)
        {
            try
            {
                connection.rollback();
            } catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            throw new DataStoreException(e);
        }
        finally
        {
            try
            {
                connection.setAutoCommit(true);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
}
