package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;
    private final AccountDao accountDao;
    private List<Account> accounts = new ArrayList<>();

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, AccountDao accountDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountDao = accountDao;
    }

    @Override
    public void transferFrom(int accountFrom, int accountTo, int amount) {
         double balanceFrom = accountDao.getBalance(accountFrom);
         double balanceTo = accountDao.getBalance(accountTo);
        if (balanceFrom >= amount) {
            String sql = "INSERT INTO tenmo_transfer (transfer_type_id, " +
                    "transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (2, 2, ?, ?, ?) " +
                    "RETURNING transfer_id;";
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountFrom, accountTo, amount);
            if (results.next()) {
                mapRowToTransfer(results);
            }
            sql = "UPDATE tenmo_account " +
                    "SET balance = ? + WHERE account_id = ?";
            jdbcTemplate.update(sql, (balanceFrom - amount), accountFrom);
            sql = "UPDATE tenmo_account " +
                    "SET balance = ? + WHERE account_id = ?";
            jdbcTemplate.update(sql, (balanceTo + amount), accountTo);
        } else {
            System.out.println("Sorry, insufficient funds");
        }
    }

    @Override
    public void transferTo(int accountTo, int accountFrom, int amount) {
        double balanceTo = accountDao.getBalance(accountTo);
        if (balanceTo >= amount) {
            String sql = "INSERT INTO tenmo_transfer (transfer_type_id, " +
                    "transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (1, 2, ?, ?, ?) " +
                    "RETURNING transfer_id;";
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountFrom, accountTo, amount);
            if (results.next()) {
                mapRowToTransfer(results);
            }
        } else {
            System.out.println("Sorry, insufficient funds");
        }
    }


    @Override
    public List<Transfer> findAll() {
        return null;
    }

    @Override
    public Transfer findByTransferId(int transferId) {
        return null;
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
        transfer.setAccountFrom(rs.getInt("account_from"));
        transfer.setAccountTo(rs.getInt("account_to"));
        transfer.setAmount(rs.getDouble("amount"));
        return transfer;
    }
}
