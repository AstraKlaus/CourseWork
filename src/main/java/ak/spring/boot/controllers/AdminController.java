package ak.spring.boot.controllers;


import ak.spring.boot.models.Person;
import ak.spring.boot.services.BooksService;
import ak.spring.boot.services.PeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        return "/admin";
    }

    @DeleteMapping()
    public String delete(@RequestParam(name = "person") String person_id, @RequestParam(name = "book_id") String book_id) {
        System.out.println("here9");
        peopleService.getBooksByPersonId(Integer.parseInt(person_id)).remove(Integer.parseInt(book_id));
        peopleService.save(peopleService.findOne(Integer.parseInt(person_id)));
        return "redirect:/books";
    }
}
