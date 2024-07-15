package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private static List<Account> accountList = new ArrayList<>();

    private int accountId;
    private int userId;
    private String accountNumber;
    private String accountType;
    private String currency;

    public Account(int accountId, int userId, String accountNumber, String accountType, String currency) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = currency;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // Static method to initialize account list with some hardcoded values
    public static void initializeAccountList() {
        accountList.add(new Account(1, 101, "ACC123", "DMAT", "USD"));
        accountList.add(new Account(2, 102, "ACC124", "FX", "EUR"));
        accountList.add(new Account(3, 103, "ACC125", "DMAT", "INR"));
    }

    // Static method to add a new account
    public static void addAccount(Account account) {
        accountList.add(account);
    }

    // Static method to get account by account ID
    public static Account getAccountById(int accountId) {
        return accountList.stream()
                .filter(account -> account.getAccountId() == accountId)
                .findFirst()
                .orElse(null);
    }

    // Static method to get all accounts
    public static List<Account> getAllAccounts() {
        return new ArrayList<>(accountList);
    }

    // Static method to delete an account by account ID
    public static boolean deleteAccount(int accountId) {
        return accountList.removeIf(account -> account.getAccountId() == accountId);
    }
}
