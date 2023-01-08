package ak.spring.boot.controllers;


import ak.spring.boot.models.Book;
import ak.spring.boot.models.Person;
import ak.spring.boot.services.BooksService;
import ak.spring.boot.services.PeopleService;
import ak.spring.boot.security.PersonDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/books")
public class BooksController {

    private final BooksService booksService;
    private final PeopleService peopleService;

    @Autowired
    public BooksController(BooksService booksService, PeopleService peopleService) {
        this.booksService = booksService;
        this.peopleService = peopleService;
    }

    @GetMapping()
    public String index(Model model, @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "books_per_page", required = false) Integer booksPerPage,
                        @RequestParam(value = "sort_by_year", required = false) boolean sortByYear) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        model.addAttribute("person", personDetails.getPerson());

        System.out.println("here1");
        if (page == null || booksPerPage == null)
            model.addAttribute("books", booksService.findAll(sortByYear)); // выдача всех книг
        else
            model.addAttribute("books", booksService.findWithPagination(page, booksPerPage, sortByYear));

        return "books/index";
    }

    @GetMapping("/purchase")
    public String purchase(@ModelAttribute("book") Book Book, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        model.addAttribute("person", personDetails.getPerson());
        model.addAttribute("amount", personDetails.getPerson().getBooks().size());
        model.addAttribute("total", String.valueOf(booksService.findAll(false).stream().collect(Collectors.summarizingInt(book -> book.getPrice())).getSum()));
        System.out.println("here2");
        return "books/purchase";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id") int id, Model model, @ModelAttribute Person person) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        model.addAttribute("person", personDetails.getPerson());

        System.out.println("here3");
        model.addAttribute("book", booksService.findOne(id));

        List<Person> bookOwners = booksService.getBookOwners(id);

        if (bookOwners != null)
            model.addAttribute("owner", bookOwners);
        else
            model.addAttribute("people", peopleService.findAll());

        return "books/show";
    }

    @GetMapping("/new")
    public String newBook(@ModelAttribute("book") Book Book) {
        System.out.println("here4");
        return "books/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("book") @Valid Book Book,
                         BindingResult bindingResult) {
        System.out.println("here5");
        if (bindingResult.hasErrors())
            return "books/new";

        booksService.save(Book);
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") int id) {
        System.out.println("here6");
        model.addAttribute("book", booksService.findOne(id));
        return "books/edit";
    }

    @PatchMapping("/{id}")
    public String update(@ModelAttribute("book") @Valid Book book, BindingResult bindingResult,
                         @PathVariable("id") int id) {
        System.out.println("here7");
        if (bindingResult.hasErrors())
            return "books/edit";

        booksService.update(id, book);
        return "redirect:/books";
    }

    @PostMapping("/{id}")
    public String buy(@PathVariable("id") int id) {
        System.out.println("here8");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        booksService.buy(id, personDetails.getPerson());
        peopleService.save(personDetails.getPerson());
        return "redirect:/books";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        System.out.println("here9");
        peopleService.deleteBook(id);
        booksService.delete(id);
        return "redirect:/books";
    }

    @PatchMapping("/{id}/release")
    public String release(@PathVariable("id") int id) {
        System.out.println("here10");
        booksService.release(id);
        return "redirect:/books/" + id;
    }


    @GetMapping("/search")
    public String searchPage() {
        System.out.println("here11");
        return "books/search";
    }

    @PostMapping("/search")
    public String makeSearch(Model model, @RequestParam("query") String query) {
        System.out.println("here12");
        model.addAttribute("books", booksService.searchByTitle(query));
        return "books/search";
    }
}
