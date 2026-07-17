package com.expensetrackerweb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetrackerweb.entity.Expense;
import com.expensetrackerweb.entity.User;

public interface ExpenseRepository
        extends JpaRepository<Expense, Integer> {

    List<Expense> findByCategoryIgnoreCase(String category);

    List<Expense> findByUser(User user);
}