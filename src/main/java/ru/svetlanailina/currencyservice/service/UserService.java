package ru.svetlanailina.currencyservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svetlanailina.currencyservice.dto.UserRegistrationDto;
import ru.svetlanailina.currencyservice.entity.BankAccount;
import ru.svetlanailina.currencyservice.entity.User;
import ru.svetlanailina.currencyservice.exception.CustomException;
import ru.svetlanailina.currencyservice.repository.BankAccountRepository;
import ru.svetlanailina.currencyservice.repository.UserRepository;
import ru.svetlanailina.currencyservice.util.DateUtils;
import java.util.Optional;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
    public User createUser(UserRegistrationDto dto) {
        //Реализация методов для проверки уникальности: логин, телефон и email не должны быть заняты
        logger.info("Registering user with username: {}", dto.getUsername());

        if (isUsernameTaken(dto.getUsername())) {
            logger.warn("Username is already taken: {}", dto.getUsername());
            throw new CustomException("Username is already taken");
        }
        if (isEmailTaken(dto.getEmail())) {
            logger.warn("Email is already taken: {}", dto.getEmail());
            throw new CustomException("Email is already taken");
        }
        if (isPhoneTaken(dto.getPhone())) {
            logger.warn("Phone number is already taken: {}", dto.getPhone());
            throw new CustomException("Phone number is already taken");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Шифрование пароля
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setFullName(dto.getFullName());
        user.setBirthDate(dto.getBirthDate());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setInitialBalance(dto.getInitialBalance());
        bankAccount.setBalance(dto.getInitialBalance());
        bankAccount.setUser(user);

        user.setBankAccount(bankAccount);

        userRepository.save(user);
        bankAccountRepository.save(bankAccount);

        return user;
    }
    //Пользователь может добавить/сменить свои номер телефона и/или email,
    //если они еще не заняты другими пользователями.
    @Transactional
    public User updateUserContact(Long userId, String email, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        // Если оба поля null или пусты, выбросить исключение
        if ((email == null || email.isEmpty()) && (phone == null || phone.isEmpty())) {
            throw new CustomException("At least one contact information (email or phone) must be provided.");
        }
        //Пользователь может добавить/сменить свои номер телефона и/или email,
        // если они еще не заняты другими пользователями.
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new CustomException("Email is already taken");
            }
            user.setEmail(email);
        }

        if (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone())) {
            if (userRepository.findByPhone(phone).isPresent()) {
                throw new CustomException("Phone number is already taken");
            }
            user.setPhone(phone);
        }
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void transferFunds(Long toUserId, Double amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String fromUserAuthentication = authentication.getName();
        User fromUser = userRepository.findByUsername(fromUserAuthentication)
                .orElseThrow(() -> new CustomException("Authenticated user not found"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new CustomException("Target user not found"));

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
            predicates.add(cb.greaterThan(user.get("birthDate"), birthDate));
        }

        query.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));

        Pageable pageable = PageRequest.of(page, size);
        List<User> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return resultList;
    }


    public boolean isUsernameTaken(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.isPresent();
    }

    public boolean isEmailTaken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.isPresent();
    }

    public boolean isPhoneTaken(String phone) {
        Optional<User> userOptional = userRepository.findByPhone(phone);
        return userOptional.isPresent();
    }
}

