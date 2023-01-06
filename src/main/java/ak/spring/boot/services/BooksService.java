package ak.spring.boot.services;

import ak.spring.boot.models.Book;
import ak.spring.boot.models.Person;
import ak.spring.boot.repositories.BooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class BooksService {

    private final BooksRepository booksRepository;

    @Autowired
    public BooksService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public List<Book> findAll(boolean sortByYear) {
        if (sortByYear)
            return booksRepository.findAll(Sort.by("year"));
        else
            return booksRepository.findAll();
    }

    public List<Book> findWithPagination(Integer page, Integer booksPerPage, boolean sortByYear) {
        if (sortByYear)
            return booksRepository.findAll(PageRequest.of(page, booksPerPage, Sort.by("year"))).getContent();
        else
            return booksRepository.findAll(PageRequest.of(page, booksPerPage)).getContent();
    }

    public Book findOne(int id) {
        Optional<Book> foundBook = booksRepository.findById(id);
        return foundBook.orElse(null);
    }

    public List<Book> searchByTitle(String query) {
        return booksRepository.findByTitleStartingWith(query);
    }

    @Transactional
    public void save(Book book) {
        booksRepository.save(book);
    }


    @Transactional
    public void update(int id, Book updatedBook) {
        Book bookToBeUpdated = booksRepository.findById(id).get();

        // добавляем по сути новую книгу (которая не находится в Persistence context), поэтому нужен save()
        updatedBook.setId(id);
        updatedBook.setOwners(bookToBeUpdated.getOwners());// чтобы не терялась связь при обновлении

        booksRepository.save(updatedBook);
        System.out.println("check");
    }

    @Transactional
    public void buy(int id, Person owner) {
        System.out.println("OK" + booksRepository.findById(id).get().getTitle() + owner.getFullName());
        System.out.println(booksRepository.findById(id).get().getOwners().toString());

        booksRepository.findById(id).get().getOwners().add(owner);
        owner.getBooks().add(booksRepository.findById(id).orElse(null));
        booksRepository.save(findOne(id));

        System.out.println(booksRepository.findById(id).get().getOwners().toString());
    }

    @Transactional
    public void delete(int id) {
        booksRepository.deleteById(id);
    }

    // Returns null if book has no owner
    public List<Person> getBookOwners(int id) {
        // Здесь Hibernate.initialize() не нужен, так как владелец (сторона One) загружается не лениво
        return booksRepository.findById(id).map(Book::getOwners).orElse(null);
    }

    // Освбождает книгу (этот метод вызывается, когда человек возвращает книгу в библиотеку)
    @Transactional
    public void release(int id) {
        booksRepository.findById(id).ifPresent(
                book -> {
                    book.setOwners(null);
                });
    }

}
