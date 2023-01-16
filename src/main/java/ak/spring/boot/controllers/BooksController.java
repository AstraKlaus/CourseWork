package ak.spring.boot.controllers;


import ak.spring.boot.models.Book;
import ak.spring.boot.models.Person;
import ak.spring.boot.services.BooksService;
import ak.spring.boot.services.PeopleService;
import ak.spring.boot.security.PersonDetails;
import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
        model.addAttribute("total", String.valueOf(peopleService.getBooksByPersonId(
                personDetails.getPerson().getId()).stream().collect(Collectors.summarizingInt(ak.spring.boot.models.Book::getPrice)).getSum()));
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

        model.addAttribute("inCart",
                peopleService.getBooksByPersonId(personDetails.getPerson().getId()).contains(booksService.findOne(id)));

        return "books/show";
    }

    @GetMapping("/new")
    public String newBook(@ModelAttribute("book") Book Book, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        model.addAttribute("person", personDetails.getPerson());
        System.out.println("here4");
        return "books/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("book") @Valid Book Book, BindingResult bindingResult) {
        System.out.println("here5");
        if (bindingResult.hasErrors())
            return "books/new";

        booksService.save(Book);
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        model.addAttribute("person", personDetails.getPerson());
        System.out.println("here6");
        model.addAttribute("book", booksService.findOne(id));
        return "books/edit";
    }

    @GetMapping("/successful")
    public String successful() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

        try (FileWriter writer = new FileWriter("/Users/andrewkasyanov/Documents/"+
                personDetails.getPerson().getId()+
                personDetails.getPerson().getUsername()+
                personDetails.getPerson().getBooks().get(0).getTitle()+".txt")) {
            writer.write("Спасибо за покупку!");
            writer.write("\nПокупатель: " + personDetails.getPerson().getFullName());
            writer.write("\nПочта: " + personDetails.getPerson().getEmail());
            writer.write("\nВы приобрели: ");
            personDetails.getPerson().getBooks().forEach(book -> {
                try {
                    writer.write(book.getTitle() + " ");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "/books/successful";
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

        peopleService.buy(id, personDetails.getPerson());
        booksService.findOne(id).setQuantity(booksService.findOne(id).getQuantity() - 1);
        booksService.save(booksService.findOne(id));

        return "redirect:/books";
    }

    @DeleteMapping("/{id}")
    public String delete(@RequestParam(name = "person_id") String person_id, @PathVariable int id) {
        System.out.println("here9");
        peopleService.getBooksByPersonId(Integer.parseInt(person_id)).remove(booksService.findOne(id));
        peopleService.save(peopleService.findOne(Integer.parseInt(person_id)));
        booksService.save(booksService.findOne(id));
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
