package ru.svetlanailina.currencyservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svetlanailina.currencyservice.entity.BankAccount;
import ru.svetlanailina.currencyservice.repository.BankAccountRepository;

import java.util.List;

@Service
public class BalanceUpdateService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Scheduled(fixedRate = 60000) // Каждую минуту
    @Transactional
    public void updateBalances() {
        List<BankAccount> accounts = bankAccountRepository.findAll();

        for (BankAccount account : accounts) {
            double newBalance = account.getBalance() * 1.05;
            if (newBalance > account.getMaxBalance()) {
                newBalance = account.getMaxBalance();
            }
            account.setBalance(newBalance);
            bankAccountRepository.save(account);
        }
    }
}
