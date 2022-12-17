package ak.spring.boot.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;


@Entity
@Table(name = "Person")
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов длиной")
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    @Email
    private String email;

    @Min(value = 1900, message = "Год рождения должен быть больше, чем 1900")
    @Max(value = 2020, message = "Год рождения должен быть меньше, чем 2020")
    @Column(name = "year_of_birth")
    private int yearOfBirth;

    @OneToMany(mappedBy = "owner")
    private List<Book> books;


    public Person() {}

    public Person(String fullName, int yearOfBirth, String email) {
        this.fullName = fullName;
        this.yearOfBirth = yearOfBirth;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }
}
