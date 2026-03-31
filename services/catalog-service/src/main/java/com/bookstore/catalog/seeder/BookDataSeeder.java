package com.bookstore.catalog.seeder;
import com.bookstore.catalog.document.Book;
import com.bookstore.catalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component @RequiredArgsConstructor @Slf4j
public class BookDataSeeder implements ApplicationRunner {
    private final BookRepository bookRepository;

    @Override
    public void run(ApplicationArguments args) {
        deduplicateByIsbn();
        List<Book> books = List.of(
            book("The Great Gatsby","F. Scott Fitzgerald","Fiction","A story of wealth, love, and the American Dream in the 1920s.","12.99",4.2,1920,"https://covers.openlibrary.org/b/id/8739161-L.jpg","9780743273565",1925,192),
            book("To Kill a Mockingbird","Harper Lee","Classic","A profound story of racial injustice and loss of innocence in the American South.","14.99",4.8,1850,"https://covers.openlibrary.org/b/id/8228691-L.jpg","9780061935466",1960,281),
            book("1984","George Orwell","Science Fiction","A dystopian novel set in a totalitarian society ruled by Big Brother.","11.99",4.7,1600,"https://covers.openlibrary.org/b/id/8234383-L.jpg","9780451524935",1949,328),
            book("Brave New World","Aldous Huxley","Science Fiction","A dystopian vision of a future society conditioned for happiness through technology.","13.99",4.1,1400,"https://covers.openlibrary.org/b/id/8231856-L.jpg","9780060850524",1932,311),
            book("The Catcher in the Rye","J.D. Salinger","Fiction","The story of Holden Caulfield, a teenager navigating alienation and identity.","12.99",4.0,980,"https://covers.openlibrary.org/b/id/10527843-L.jpg","9780316769174",1951,224),
            book("Harry Potter and the Sorcerer's Stone","J.K. Rowling","Fantasy","The beginning of Harry Potter's journey into the magical world of Hogwarts.","15.99",4.9,5200,"https://covers.openlibrary.org/b/id/7984916-L.jpg","9780439708180",1997,309),
            book("The Lord of the Rings","J.R.R. Tolkien","Fantasy","The epic tale of the Fellowship of the Ring and their quest to destroy the One Ring.","24.99",4.9,4800,"https://covers.openlibrary.org/b/id/8091016-L.jpg","9780544003415",1954,1178),
            book("The Hobbit","J.R.R. Tolkien","Fantasy","Bilbo Baggins joins a group of dwarves on a quest to reclaim their mountain home.","14.99",4.7,3600,"https://covers.openlibrary.org/b/id/8478235-L.jpg","9780547928227",1937,310),
            book("Dune","Frank Herbert","Science Fiction","A complex saga of politics, religion, and ecology set on the desert planet Arrakis.","16.99",4.6,2800,"https://covers.openlibrary.org/b/id/8707710-L.jpg","9780441013593",1965,412),
            book("Foundation","Isaac Asimov","Science Fiction","Hari Seldon develops psychohistory to predict and guide the fall of a galactic empire.","13.99",4.5,1900,"https://covers.openlibrary.org/b/id/7964654-L.jpg","9780553293357",1951,244),
            book("The Da Vinci Code","Dan Brown","Thriller","Harvard symbologist Robert Langdon investigates a murder in the Louvre.","14.99",4.0,3100,"https://covers.openlibrary.org/b/id/6597283-L.jpg","9780307474278",2003,689),
            book("Gone Girl","Gillian Flynn","Thriller","When Amy Dunne disappears on her fifth wedding anniversary, suspicion falls on her husband Nick.","13.99",4.1,2400,"https://covers.openlibrary.org/b/id/8091124-L.jpg","9780307588371",2012,422),
            book("The Girl with the Dragon Tattoo","Stieg Larsson","Mystery","Journalist Mikael Blomkvist and hacker Lisbeth Salander investigate a decades-old disappearance.","15.99",4.3,2100,"https://covers.openlibrary.org/b/id/8739161-L.jpg","9780307454546",2005,644),
            book("In Cold Blood","Truman Capote","True Crime","The chilling account of the 1959 murders of the Clutter family in Holcomb, Kansas.","14.99",4.4,890,"https://covers.openlibrary.org/b/id/6979861-L.jpg","9780679745587",1966,343),
            book("The Silence of the Lambs","Thomas Harris","Thriller","FBI trainee Clarice Starling must seek the help of imprisoned cannibal Hannibal Lecter.","13.99",4.5,1780,"https://covers.openlibrary.org/b/id/8228691-L.jpg","9780312924584",1988,338),
            book("Pride and Prejudice","Jane Austen","Romance","The witty and beloved story of Elizabeth Bennet and Mr. Darcy.","9.99",4.7,3900,"https://covers.openlibrary.org/b/id/8234383-L.jpg","9780141439518",1813,432),
            book("Jane Eyre","Charlotte Bronte","Romance","The passionate story of Jane Eyre, an orphan who becomes a governess.","10.99",4.5,2100,"https://covers.openlibrary.org/b/id/8231856-L.jpg","9780141441146",1847,507),
            book("Wuthering Heights","Emily Bronte","Romance","The dark and passionate tale of Heathcliff and Catherine set on the Yorkshire moors.","9.99",4.2,1600,"https://covers.openlibrary.org/b/id/10527843-L.jpg","9780141439556",1847,342),
            book("Outlander","Diana Gabaldon","Romance","Claire Randall is transported back to 18th-century Scotland where she meets Jamie Fraser.","17.99",4.6,2800,"https://covers.openlibrary.org/b/id/7984916-L.jpg","9780440212560",1991,850),
            book("The Notebook","Nicholas Sparks","Romance","A touching love story about Noah and Allie, separated by circumstance.","12.99",4.2,2300,"https://covers.openlibrary.org/b/id/8091016-L.jpg","9781455582877",1996,214),
            book("Sapiens","Yuval Noah Harari","History","A brief history of humankind from the Stone Age to the twenty-first century.","18.99",4.5,4100,"https://covers.openlibrary.org/b/id/8478235-L.jpg","9780062316110",2011,443),
            book("A Brief History of Time","Stephen Hawking","Science","Hawking explores the cosmos, from the Big Bang to black holes.","16.99",4.4,2900,"https://covers.openlibrary.org/b/id/8707710-L.jpg","9780553380163",1988,212),
            book("Guns, Germs, and Steel","Jared Diamond","History","Diamond explores why Western civilizations came to dominate the world.","17.99",4.3,1800,"https://covers.openlibrary.org/b/id/7964654-L.jpg","9780393354324",1997,480),
            book("The Art of War","Sun Tzu","Self-Help","Ancient Chinese military treatise on strategy and tactics.","8.99",4.5,3200,"https://covers.openlibrary.org/b/id/6597283-L.jpg","9781599869773",-500,68),
            book("Atomic Habits","James Clear","Self-Help","Clear explains how tiny changes in behavior can lead to remarkable results.","19.99",4.8,6100,"https://covers.openlibrary.org/b/id/8091124-L.jpg","9780735211292",2018,320),
            book("The 7 Habits of Highly Effective People","Stephen Covey","Self-Help","Covey presents a principle-centered approach to personal and professional effectiveness.","16.99",4.5,3400,"https://covers.openlibrary.org/b/id/8739161-L.jpg","9781982137274",1989,432),
            book("How to Win Friends and Influence People","Dale Carnegie","Self-Help","Carnegie's timeless classic on human relations and communication.","14.99",4.6,4200,"https://covers.openlibrary.org/b/id/6979861-L.jpg","9780671027032",1936,291),
            book("Think and Grow Rich","Napoleon Hill","Self-Help","Based on interviews with successful people, Hill outlines 13 principles for achieving success.","12.99",4.3,2800,"https://covers.openlibrary.org/b/id/8228691-L.jpg","9781585424337",1937,320),
            book("Clean Code","Robert C. Martin","Technology","A handbook of agile software craftsmanship.","39.99",4.7,2100,"https://covers.openlibrary.org/b/id/8234383-L.jpg","9780132350884",2008,431),
            book("The Pragmatic Programmer","Andrew Hunt","Technology","Practical advice for software developers on how to become more effective.","44.99",4.8,1900,"https://covers.openlibrary.org/b/id/8231856-L.jpg","9780135957059",1999,352),
            book("Design Patterns","Gang of Four","Technology","The classic software engineering book presenting 23 design patterns.","49.99",4.6,1600,"https://covers.openlibrary.org/b/id/10527843-L.jpg","9780201633610",1994,395),
            book("The Lean Startup","Eric Ries","Technology","Ries introduces the lean startup methodology for building successful businesses.","18.99",4.4,2400,"https://covers.openlibrary.org/b/id/7984916-L.jpg","9780307887894",2011,336),
            book("Zero to One","Peter Thiel","Technology","Thiel shares his philosophy on building companies that create something new.","16.99",4.3,2000,"https://covers.openlibrary.org/b/id/8091016-L.jpg","9780804139021",2014,224),
            book("It","Stephen King","Horror","Seven children in Derry, Maine, encounter a shape-shifting evil entity.","19.99",4.6,2700,"https://covers.openlibrary.org/b/id/8478235-L.jpg","9781501156700",1986,1138),
            book("The Shining","Stephen King","Horror","Jack Torrance takes his family to the isolated Overlook Hotel for the winter.","14.99",4.5,2200,"https://covers.openlibrary.org/b/id/8707710-L.jpg","9780307743657",1977,447),
            book("Dracula","Bram Stoker","Horror","The original vampire novel follows Count Dracula's attempt to move from Transylvania to England.","10.99",4.4,1800,"https://covers.openlibrary.org/b/id/7964654-L.jpg","9780141439846",1897,418),
            book("Frankenstein","Mary Shelley","Horror","Victor Frankenstein creates a living being from dead matter, then abandons his creation.","9.99",4.3,1600,"https://covers.openlibrary.org/b/id/6597283-L.jpg","9780141439471",1818,288),
            book("The Adventures of Tom Sawyer","Mark Twain","Adventure","The classic story of a mischievous boy growing up along the Mississippi River.","9.99",4.2,1400,"https://covers.openlibrary.org/b/id/8091124-L.jpg","9780143039563",1876,274),
            book("Treasure Island","Robert Louis Stevenson","Adventure","Young Jim Hawkins discovers a treasure map and sets sail with Long John Silver.","8.99",4.4,1700,"https://covers.openlibrary.org/b/id/8739161-L.jpg","9780143039549",1883,311),
            book("The Count of Monte Cristo","Alexandre Dumas","Adventure","Edmond Dantes is unjustly imprisoned and escapes to exact an elaborate revenge.","22.99",4.8,2900,"https://covers.openlibrary.org/b/id/8140234-L.jpg","9780140449266",1844,1276),
            book("Around the World in Eighty Days","Jules Verne","Adventure","English gentleman Phileas Fogg bets he can circumnavigate the globe in just eighty days.","11.99",4.3,1500,"https://covers.openlibrary.org/b/id/8228691-L.jpg","9780140449068",1872,256),
            book("Steve Jobs","Walter Isaacson","Biography","The exclusive biography of Apple co-founder Steve Jobs.","19.99",4.4,3100,"https://covers.openlibrary.org/b/id/8234383-L.jpg","9781451648539",2011,656),
            book("Leonardo da Vinci","Walter Isaacson","Biography","Based on thousands of pages from Leonardo's notebooks.","21.99",4.6,2100,"https://covers.openlibrary.org/b/id/8231856-L.jpg","9781501139161",2017,576),
            book("The Diary of a Young Girl","Anne Frank","Biography","Anne Frank's remarkable diary written while hiding from the Nazis in Amsterdam.","12.99",4.8,4300,"https://covers.openlibrary.org/b/id/10527843-L.jpg","9780553577129",1947,283),
            book("Long Walk to Freedom","Nelson Mandela","Biography","Nelson Mandela's autobiography traces his journey from rural South Africa.","18.99",4.7,1900,"https://covers.openlibrary.org/b/id/7984916-L.jpg","9780316548182",1994,656),
            book("The Alchemist","Paulo Coelho","Fiction","A young shepherd's journey to find worldly treasure leads him on a spiritual journey.","13.99",4.6,5100,"https://covers.openlibrary.org/b/id/8091016-L.jpg","9780062315007",1988,197),
            book("One Hundred Years of Solitude","Gabriel Garcia Marquez","Fiction","The multi-generational saga of the Buendia family in the fictional town of Macondo.","15.99",4.7,2600,"https://covers.openlibrary.org/b/id/8478235-L.jpg","9780060883287",1967,417),
            book("The Hitchhiker's Guide to the Galaxy","Douglas Adams","Science Fiction","Arthur Dent is rescued from Earth moments before it is demolished.","13.99",4.7,3800,"https://covers.openlibrary.org/b/id/8707710-L.jpg","9780345391803",1979,224),
            book("Ender's Game","Orson Scott Card","Science Fiction","Gifted child Ender Wiggin is trained at a military school in space to fight alien invaders.","14.99",4.5,2800,"https://covers.openlibrary.org/b/id/7964654-L.jpg","9780812550702",1985,352),
            book("The Name of the Wind","Patrick Rothfuss","Fantasy","Kvothe tells the story of how he became the most notorious wizard his world has ever seen.","16.99",4.7,3200,"https://covers.openlibrary.org/b/id/6597283-L.jpg","9780756404079",2007,662)
        );

        int insertedCount = 0;
        int updatedCount = 0;
        for (Book seedBook : books) {
            Book existing = bookRepository.findByIsbn(seedBook.getIsbn()).orElse(null);
            if (existing == null) {
                bookRepository.save(seedBook);
                insertedCount++;
                continue;
            }

            updateMutableFields(existing, seedBook);
            bookRepository.save(existing);
            updatedCount++;
        }

        log.info("Catalog seed sync completed. inserted={}, updated={}", insertedCount, updatedCount);
    }

    private void deduplicateByIsbn() {
        Map<String, List<Book>> booksByIsbn = bookRepository.findAll().stream()
            .filter(book -> book.getIsbn() != null && !book.getIsbn().isBlank())
            .collect(Collectors.groupingBy(Book::getIsbn));

        int removed = 0;
        for (Map.Entry<String, List<Book>> entry : booksByIsbn.entrySet()) {
            List<Book> duplicates = entry.getValue();
            if (duplicates.size() <= 1) {
                continue;
            }

            duplicates.sort(Comparator.comparing(Book::getId, Comparator.nullsLast(String::compareTo)));
            Book canonical = duplicates.get(0);
            List<Book> booksToRemove = new ArrayList<>(duplicates.subList(1, duplicates.size()));

            Book mergedBook = mergeBooks(canonical, duplicates);
            bookRepository.save(mergedBook);
            bookRepository.deleteAll(booksToRemove);
            removed += booksToRemove.size();
        }

        if (removed > 0) {
            log.info("Removed {} duplicate book documents by ISBN", removed);
        }
    }

    private Book mergeBooks(Book canonical, List<Book> duplicates) {
        Book merged = Book.builder()
            .id(canonical.getId())
            .title(canonical.getTitle())
            .author(canonical.getAuthor())
            .genre(canonical.getGenre())
            .description(canonical.getDescription())
            .price(canonical.getPrice())
            .coverUrl(canonical.getCoverUrl())
            .stock(canonical.getStock())
            .averageRating(canonical.getAverageRating())
            .totalReviews(canonical.getTotalReviews())
            .isbn(canonical.getIsbn())
            .publishedYear(canonical.getPublishedYear())
            .language(canonical.getLanguage())
            .pages(canonical.getPages())
            .build();

        for (Book duplicate : duplicates) {
            if (Objects.equals(duplicate.getId(), canonical.getId())) {
                continue;
            }
            if (isBlank(merged.getTitle())) merged.setTitle(duplicate.getTitle());
            if (isBlank(merged.getAuthor())) merged.setAuthor(duplicate.getAuthor());
            if (isBlank(merged.getGenre())) merged.setGenre(duplicate.getGenre());
            if (isBlank(merged.getDescription())) merged.setDescription(duplicate.getDescription());
            if (merged.getPrice() == null) merged.setPrice(duplicate.getPrice());
            if (isBlank(merged.getCoverUrl())) merged.setCoverUrl(duplicate.getCoverUrl());
            if (merged.getStock() == 0) merged.setStock(duplicate.getStock());
            if (merged.getAverageRating() == 0) merged.setAverageRating(duplicate.getAverageRating());
            if (merged.getTotalReviews() == 0) merged.setTotalReviews(duplicate.getTotalReviews());
            if (merged.getPublishedYear() == 0) merged.setPublishedYear(duplicate.getPublishedYear());
            if (isBlank(merged.getLanguage())) merged.setLanguage(duplicate.getLanguage());
            if (merged.getPages() == 0) merged.setPages(duplicate.getPages());
        }

        return merged;
    }

    private void updateMutableFields(Book target, Book source) {
        target.setTitle(source.getTitle());
        target.setAuthor(source.getAuthor());
        target.setGenre(source.getGenre());
        target.setDescription(source.getDescription());
        target.setPrice(source.getPrice());
        target.setCoverUrl(source.getCoverUrl());
        target.setStock(source.getStock());
        target.setAverageRating(source.getAverageRating());
        target.setTotalReviews(source.getTotalReviews());
        target.setPublishedYear(source.getPublishedYear());
        target.setLanguage(source.getLanguage());
        target.setPages(source.getPages());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Book book(String title, String author, String genre, String desc, String price,
                      double rating, int stock, String cover, String isbn, int year, int pages) {
        return Book.builder().title(title).author(author).genre(genre).description(desc)
            .price(new BigDecimal(price)).averageRating(rating).stock(stock)
            .coverUrl(cover).isbn(isbn).publishedYear(year).pages(pages)
            .language("English").totalReviews(0).build();
    }
}
