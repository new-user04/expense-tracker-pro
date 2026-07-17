package com.expensetrackerweb.controller;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.expensetrackerweb.entity.Expense;
import com.expensetrackerweb.entity.User;
import com.expensetrackerweb.repository.ExpenseRepository;
import com.expensetrackerweb.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    private final ExpenseRepository expenseRepository;
private final UserRepository userRepository;

public HomeController(
        ExpenseRepository expenseRepository,
        UserRepository userRepository) {

    this.expenseRepository = expenseRepository;
    this.userRepository = userRepository;
}
@GetMapping("/login")
public String login() {
    return "login";
}
    @GetMapping("/")
public String home(
        Model model,
        Authentication authentication) {

    String username =
            authentication.getName();

    User user =
            userRepository
                    .findByUsername(username)
                    .orElseThrow();

    List<Expense> expenses =
            expenseRepository.findByUser(user);

model.addAttribute("username", username);

    loadDashboardData(model, expenses);

    

    return "index";
}

    @PostMapping("/save")
public String saveExpense(
        Expense expense,
        Authentication authentication) {

    String username =
            authentication.getName();

    User user =
            userRepository
                    .findByUsername(username)
                    .orElseThrow();

    expense.setUser(user);

    expenseRepository.save(expense);

    return "redirect:/";
}

   @GetMapping("/expenses")
public String expenses(
        Model model,
        Authentication authentication) {

    String username =
            authentication.getName();

    User user =
            userRepository
                    .findByUsername(username)
                    .orElseThrow();

    model.addAttribute(
            "expenses",
            expenseRepository.findByUser(user));

    return "expenses";
}
@GetMapping("/analytics")
public String analytics(
        Model model,
        Authentication authentication) {

    String username =
            authentication.getName();

    User user =
            userRepository
                    .findByUsername(username)
                    .orElseThrow();

    List<Expense> expenses =
            expenseRepository.findByUser(user);

    double food = 0;
    double travel = 0;
    double shopping = 0;
    double bills = 0;
    double education = 0;
    double entertainment = 0;
    double other = 0;

    for (Expense expense : expenses) {

        switch (expense.getCategory().trim().toLowerCase()) {

            case "food":
                food += expense.getAmount();
                break;

            case "travel":
                travel += expense.getAmount();
                break;

            case "shopping":
                shopping += expense.getAmount();
                break;

            case "bills":
                bills += expense.getAmount();
                break;

            case "education":
                education += expense.getAmount();
                break;

            case "entertainment":
                entertainment += expense.getAmount();
                break;

            default:
                other += expense.getAmount();
        }
    }

    model.addAttribute("food", food);
    model.addAttribute("travel", travel);
    model.addAttribute("shopping", shopping);
    model.addAttribute("bills", bills);
    model.addAttribute("education", education);
    model.addAttribute("entertainment", entertainment);
    model.addAttribute("other", other);

    return "analytics";
}

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Integer id) {

        expenseRepository.deleteById(id);

        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchExpense(
            @RequestParam String category,
            Model model) {

        List<Expense> expenses =
                expenseRepository.findByCategoryIgnoreCase(category);

        loadDashboardData(model, expenses);

        return "index";
    }

    @GetMapping("/edit/{id}")
    public String editExpense(
            @PathVariable Integer id,
            Model model) {

        Optional<Expense> expense =
                expenseRepository.findById(id);

        if (expense.isPresent()) {

            model.addAttribute(
                    "expense",
                    expense.get());

            return "edit";
        }

        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateExpense(
            Expense expense) {

        expenseRepository.save(expense);

        return "redirect:/";
    }

    @GetMapping("/export")
    public void exportCsv(
            HttpServletResponse response)
            throws Exception {

        response.setContentType("text/csv");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=expenses.csv");

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "ID,Title,Amount,Category,Date");

        for (Expense expense :
                expenseRepository.findAll()) {

            writer.printf(
                    "%d,%s,%.2f,%s,%s%n",
                    expense.getId(),
                    expense.getTitle(),
                    expense.getAmount(),
                    expense.getCategory(),
                    expense.getExpenseDate());
        }

        writer.flush();
    }

    private void loadDashboardData(
            Model model,
            List<Expense> expenses) {

        double food = 0;
        double travel = 0;
        double shopping = 0;
        double bills = 0;
        double education = 0;
        double entertainment = 0;
        double other = 0;

        for (Expense expense : expenses) {

            String category =
                    expense.getCategory().toLowerCase();

            switch (category) {

                case "food":
                    food += expense.getAmount();
                    break;

                case "travel":
                    travel += expense.getAmount();
                    break;

                case "shopping":
                    shopping += expense.getAmount();
                    break;

                case "bills":
                    bills += expense.getAmount();
                    break;

                case "education":
                    education += expense.getAmount();
                    break;

                case "entertainment":
                    entertainment += expense.getAmount();
                    break;

                default:
                    other += expense.getAmount();
            }
        }

        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double highestExpense = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .max()
                .orElse(0);

        double averageExpense = expenses.isEmpty()
                ? 0
                : total / expenses.size();

        double budget = 10000;

        double remaining =
                budget - total;

        double percentage =
                (total / budget) * 100;

        if (percentage > 100) {
            percentage = 100;
        }

        model.addAttribute("expenses", expenses);

        model.addAttribute("total", total);
        model.addAttribute("transactions",
                expenses.size());

        model.addAttribute("budget", budget);
        model.addAttribute("remaining", remaining);
        model.addAttribute("percentage", percentage);

        model.addAttribute("highestExpense",
                highestExpense);

        model.addAttribute("averageExpense",
                averageExpense);

        model.addAttribute("food", food);
        model.addAttribute("travel", travel);
        model.addAttribute("shopping", shopping);
        model.addAttribute("bills", bills);
        model.addAttribute("education", education);
        model.addAttribute("entertainment",
                entertainment);
        model.addAttribute("other", other);
    }
}