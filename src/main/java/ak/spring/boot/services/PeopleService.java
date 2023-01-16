package ak.spring.boot.services;

import ak.spring.boot.models.Book;
import ak.spring.boot.models.Person;
import ak.spring.boot.repositories.PeopleRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class PeopleService {

    private final PeopleRepository peopleRepository;
    private final BooksService booksService;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository, BooksService booksService) {
        this.peopleRepository = peopleRepository;
        this.booksService = booksService;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public Person findOne(int id) {
        Optional<Person> foundPerson = peopleRepository.findById(id);
        return foundPerson.orElse(null);
    }

    @Transactional
    public void save(Person person) {
        peopleRepository.save(person);
    }

    @Transactional
    public void deleteBook(int id){
        Book book = booksService.findOne(id);
        peopleRepository.findAll().forEach(person -> person.getBooks().remove(book));
    }

    @Transactional
    public void buy(int id, Person owner) {
        getBooksByPersonId(owner.getId()).add(booksService.findOne(id));
        save(findOne(owner.getId()));
    }

    @Transactional
    public void update(int id, Person updatedPerson) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        updatedPerson.setId(id);
        updatedPerson.setPassword(encoder.encode(updatedPerson.getPassword()));
        if (updatedPerson.getRole() == null) updatedPerson.setRole(findOne(id).getRole());
        peopleRepository.save(updatedPerson);
    }

    @Transactional
    public void delete(int id) {
        peopleRepository.deleteById(id);
    }

    public Optional<Person> getPersonByFullName(String fullName) {
        return peopleRepository.findByFullName(fullName);
    }

    public List<Book> getBooksByPersonId(int id) {
        Optional<Person> person = peopleRepository.findById(id);

        if (person.isPresent()) {
            Hibernate.initialize(person.get().getBooks());
            // Мы внизу итерируемся по книгам, поэтому они точно будут загружены, но на всякий случай
            // не мешает всегда вызывать Hibernate.initialize()
            // (на случай, например, если код в дальнейшем поменяется и итерация по книгам удалится)

            return person.get().getBooks();
        }
        else {
            return Collections.emptyList();
        }
    }
}
