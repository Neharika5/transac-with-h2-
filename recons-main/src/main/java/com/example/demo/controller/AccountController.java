package com.example.demo.controller;

import com.example.demo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    public AccountController() {
        // Initialize account list with hardcoded values
        Account.initializeAccountList();
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return Account.getAllAccounts();
    }

    @GetMapping("/getAccount")
    public ResponseEntity<Account> getAccountById(@RequestParam int id) {
        Account account = Account.getAccountById(id);
        return account != null ? ResponseEntity.ok(account) : ResponseEntity.notFound().build();
    }

    @PostMapping("/createAccount")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account.addAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestParam int id) {
        boolean removed = Account.deleteAccount(id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
