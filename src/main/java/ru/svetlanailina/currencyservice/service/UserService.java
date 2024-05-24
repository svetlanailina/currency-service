package ru.svetlanailina.currencyservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svetlanailina.currencyservice.entity.BankAccount;
import ru.svetlanailina.currencyservice.entity.User;
import ru.svetlanailina.currencyservice.exception.CustomException;
import ru.svetlanailina.currencyservice.repository.BankAccountRepository;
import ru.svetlanailina.currencyservice.repository.UserRepository;
import ru.svetlanailina.currencyservice.util.DateUtils;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserService(UserRepository userRepository, BankAccountRepository bankAccountRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String password, String email, String phone, String fullName, LocalDate birthDate, Double initialBalance) {
        if (userRepository.findByUsername(username).isPresent() ||
                userRepository.findByEmail(email).isPresent() ||
                userRepository.findByPhone(phone).isPresent()) {
            throw new CustomException("Username, email, or phone already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Шифрование пароля
        user.setEmail(email);
        user.setPhone(phone);
        user.setFullName(fullName);
        user.setBirthDate(birthDate);

        BankAccount bankAccount = new BankAccount();
        bankAccount.setInitialBalance(initialBalance);
        bankAccount.setBalance(initialBalance);
        bankAccount.setUser(user);

        user.setBankAccount(bankAccount);

        // Устанавливаем дату создания пользователя
        user.setDateCreated(DateUtils.now());

        userRepository.save(user);
        bankAccountRepository.save(bankAccount);

        return user;
    }

    @Transactional
    public User updateUserContact(Long userId, String email, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        if (email != null && !email.isEmpty() && !email.equals(user.getEmail()) &&
                userRepository.findByEmail(email).isEmpty()) {
            user.setEmail(email);
        }

        if (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone()) &&
                userRepository.findByPhone(phone).isEmpty()) {
            user.setPhone(phone);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void transferFunds(Long fromUserId, Long toUserId, Double amount) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new CustomException("User not found"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new CustomException("User not found"));

        BankAccount fromAccount = fromUser.getBankAccount();
        BankAccount toAccount = toUser.getBankAccount();

        if (fromAccount.getBalance() < amount) {
            throw new CustomException("Insufficient funds");
        }

        synchronized (this) {
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);
            bankAccountRepository.save(fromAccount);
            bankAccountRepository.save(toAccount);
        }
    }

    public List<User> searchUsers(String fullName, String email, String phone, LocalDate birthDate, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        if (fullName != null && !fullName.isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("fullName")), "%" + fullName.toLowerCase() + "%"));
        }

        if (email != null && !email.isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%"));
        }

        if (phone != null && !phone.isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("phone")), "%" + phone.toLowerCase() + "%"));
        }

        if (birthDate != null) {
            predicates.add(cb.equal(user.get("birthDate"), birthDate));
        }

        query.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));

        Pageable pageable = PageRequest.of(page, size);
        List<User> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return resultList;
    }
}

