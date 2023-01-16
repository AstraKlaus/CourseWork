package ak.spring.boot.controllers;


import ak.spring.boot.models.Book;
import ak.spring.boot.models.Person;
import ak.spring.boot.services.BooksService;
import ak.spring.boot.services.PeopleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BooksService booksService;
    private final PeopleService peopleService;

    @Autowired
    public AdminController(BooksService booksService, PeopleService peopleService) {
        this.booksService = booksService;
        this.peopleService = peopleService;
    }

    @GetMapping()
    public String index(Model model){
        model.addAttribute("books", booksService.findAll(false));
        model.addAttribute("people", peopleService.findAll());
        return "/admin/index";
    }

    @DeleteMapping()
    public String delete(@RequestParam(name = "person") String person_id, @RequestParam(name = "book_id") String book_id) {
        System.out.println("here9");
        peopleService.getBooksByPersonId(Integer.parseInt(person_id)).remove(booksService.findOne(Integer.parseInt(book_id)));
        peopleService.save(peopleService.findOne(Integer.parseInt(person_id)));
        booksService.save(booksService.findOne(Integer.parseInt(book_id)));
        return "redirect:/admin";
    }

    @DeleteMapping("/books")
    public String deleteBook(@RequestParam(name = "book_id") String book_id) {
        System.out.println("here9");
        peopleService.deleteBook(Integer.parseInt(book_id));
        booksService.delete(Integer.parseInt(book_id));
        return "redirect:/admin/books";
    }

    @GetMapping("/people")
    public String people(Model model){
        model.addAttribute("people", peopleService.findAll());
        return "admin/people";
    }

    @DeleteMapping("/people")
    public String deletePerson(@RequestParam(name = "person_id") String person_id) {
        System.out.println("here9");
        peopleService.delete(Integer.parseInt(person_id));
        return "redirect:/admin/people";
    }

    @GetMapping("/books")
    public String books(Model model){
        model.addAttribute("books", booksService.findAll(true));
        return "/admin/books";
    }

    @GetMapping("/people/{id}")
    public String person(Model model, @PathVariable int id){
        model.addAttribute("person", peopleService.findOne(id));
        return "admin/edit";
    }

    @PatchMapping("/people/{id}")
    public String update(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult,
                         @PathVariable("id") int id) {
        System.out.println("here7");
        if (bindingResult.hasErrors())
            return "/admin/edit";

        peopleService.update(id, person);
        return "redirect:/admin/people";
    }
}
