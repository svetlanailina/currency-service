package ru.svetlanailina.currencyservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import ru.svetlanailina.currencyservice.dto.TransferRequestDto;
import ru.svetlanailina.currencyservice.entity.BankAccount;
import ru.svetlanailina.currencyservice.entity.User;
import ru.svetlanailina.currencyservice.repository.BankAccountRepository;
import ru.svetlanailina.currencyservice.repository.UserRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserService userService;

    private User senderUser;
    private User receiverUser;

    @BeforeEach
    public void setup() {
        // Создание пользователей напрямую без использования DTO
        senderUser = new User();
        senderUser.setUsername("senderUser");
        senderUser.setPassword("senderPassword");
        senderUser.setEmail("sender@example.com");
        senderUser.setPhone("+12345678940");
        senderUser.setFullName("Sender Full Name");
        senderUser.setBirthDate(LocalDate.of(1990, 1, 1));

        BankAccount senderAccount = new BankAccount();
        senderAccount.setInitialBalance(1000.0);
        senderAccount.setBalance(1000.0);
        senderAccount.setUser(senderUser);

        senderUser.setBankAccount(senderAccount);

        receiverUser = new User();
        receiverUser.setUsername("receiverUser");
        receiverUser.setPassword("receiverPassword");
        receiverUser.setEmail("receiver@example.com");
        receiverUser.setPhone("+98765432110");
        receiverUser.setFullName("Receiver Full Name");
        receiverUser.setBirthDate(LocalDate.of(1995, 5, 5));

        BankAccount receiverAccount = new BankAccount();
        receiverAccount.setInitialBalance(500.0);
        receiverAccount.setBalance(500.0);
        receiverAccount.setUser(receiverUser);

        receiverUser.setBankAccount(receiverAccount);

        // Сохранение пользователей и их банковских счетов в базу данных
        userRepository.save(senderUser);
        userRepository.save(receiverUser);
        bankAccountRepository.save(senderAccount);
        bankAccountRepository.save(receiverAccount);
    }

    @AfterEach
    public void tearDown() {
        // Очистка данных после каждого теста
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "senderUser", password = "senderPassword", roles = "USER")
    public void testTransferFunds() throws Exception {
        TransferRequestDto transferRequestDto = new TransferRequestDto();
        transferRequestDto.setToUserId(receiverUser.getId());
        transferRequestDto.setAmount(200.0);
        userService.transferFunds(receiverUser.getId(), 200.0);

        // Проверяем, что балансы обновлены верно после перевода
        User updatedSenderUser = userRepository.findByUsername("senderUser").orElseThrow();
        User updatedReceiverUser = userRepository.findByUsername("receiverUser").orElseThrow();

        assertEquals(800.0, updatedSenderUser.getBankAccount().getBalance());
        assertEquals(700.0, updatedReceiverUser.getBankAccount().getBalance());
    }
}