package ru.svetlanailina.currencyservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.svetlanailina.currencyservice.entity.BankAccount;
import ru.svetlanailina.currencyservice.entity.User;
import ru.svetlanailina.currencyservice.repository.UserRepository;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User senderUser;
    private User receiverUser;
    private Authentication authenticationMock;


    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Создание пользователей напрямую без использования DTO
        senderUser = new User();
        senderUser.setId(1L); // Устанавливаем id пользователя
        senderUser.setUsername("senderUser");
        senderUser.setPassword("senderPassword");
        senderUser.setEmail("sender@example.com");
        senderUser.setPhone("+1234567890");
        senderUser.setFullName("Sender Full Name");
        senderUser.setBirthDate(LocalDate.of(1990, 1, 1));

        BankAccount senderAccount = new BankAccount();
        senderAccount.setInitialBalance(1000.0);
        senderAccount.setBalance(1000.0);
        senderAccount.setUser(senderUser);

        senderUser.setBankAccount(senderAccount);

        receiverUser = new User();
        receiverUser.setId(2L); // Устанавливаем id пользователя
        receiverUser.setUsername("receiverUser");
        receiverUser.setPassword("receiverPassword");
        receiverUser.setEmail("receiver@example.com");
        receiverUser.setPhone("+9876543210");
        receiverUser.setFullName("Receiver Full Name");
        receiverUser.setBirthDate(LocalDate.of(1995, 5, 5));

        BankAccount receiverAccount = new BankAccount();
        receiverAccount.setInitialBalance(500.0);
        receiverAccount.setBalance(500.0);
        receiverAccount.setUser(receiverUser);

        receiverUser.setBankAccount(receiverAccount);


        // Создание мок объекта аутентификации
        authenticationMock = mock(Authentication.class);
        when(authenticationMock.getName()).thenReturn(senderUser.getUsername());

        // Устанавливаем мок объект аутентификации в контекст безопасности
        SecurityContextHolder.getContext().setAuthentication(authenticationMock);


        // Устанавливаем мок объекты для userRepository
        when(userRepository.findByUsername(senderUser.getUsername())).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(receiverUser.getId())).thenReturn(Optional.of(receiverUser));
    }

    @AfterEach
    public void tearDown() {
        // Очищаем контекст безопасности после каждого теста
        SecurityContextHolder.clearContext();
    }


    @Test
    @WithMockUser(username = "senderUser", password = "senderPassword", roles = "USER")
    public void testTransferFunds() throws Exception {
        mockMvc.perform(get("/transfer"))
                .andExpect(status().isOk());

        // Вызываем метод transferFunds с мок объектами
        assertDoesNotThrow(() -> userService.transferFunds(receiverUser.getId(), 200.0), "Exception should not be thrown");

        // Проверяем, что балансы обновлены верно после перевода
        verify(userRepository).save(senderUser);
        verify(userRepository).save(receiverUser);
        assertEquals(800.0, senderUser.getBankAccount().getBalance()); // Проверка баланса отправителя после перевода
        assertEquals(700.0, receiverUser.getBankAccount().getBalance()); // Проверка баланса получателя после перевода
    }
}