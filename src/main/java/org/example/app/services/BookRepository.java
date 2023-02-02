package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class BookRepository implements ProjectRepository<Book>, ApplicationContextAware {

    private final Logger logger = Logger.getLogger(BookRepository.class);
    private ApplicationContext context;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public BookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Book> retreiveAll() {
        List<Book> books = jdbcTemplate.query("SELECT * FROM books", (ResultSet rs, int rowNum) -> {
            Book book = new Book();
            book.setId(rs.getInt("id"));
            book.setAuthor(rs.getString("author"));
            book.setTitle(rs.getString("title"));
            book.setSize(rs.getInt("size"));
            return book;
        });
        return new ArrayList<>(books);
    }

    @Override
    public void store(Book book) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("author", book.getAuthor());
        parameterSource.addValue("title", book.getTitle());
        parameterSource.addValue("size", book.getSize());
        jdbcTemplate.update("INSERT INTO books(author,title,size) VALUES (:author, :title, :size)", parameterSource);
        logger.info("store new book: " + book);
    }

    @Override
    public boolean removeItemById(Integer bookIdToRemove) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("id", bookIdToRemove);
        jdbcTemplate.update("DELETE FROM books WHERE id = :id", parameterSource);
        logger.info("remove book completed");
        return true;
    }

    @Override
    public boolean removeItemByRegex(String bookStringByRegex) {
        for (Book book : retreiveAll()) {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            Pattern pattern = Pattern.compile(bookStringByRegex, Pattern.CASE_INSENSITIVE);
            String author = book.getAuthor();
            String title = book.getTitle();
            Integer size = book.getSize();
            parameterSource.addValue("author", author);
            parameterSource.addValue("title", title);
            parameterSource.addValue("size", size);
            Matcher matcherAuthor = pattern.matcher(author);
            Matcher matcherTitle = pattern.matcher(title);
            Matcher matcherSize = pattern.matcher(size.toString());
            if (matcherAuthor.find()) {
                logger.info("remove book by title completed: " + book);
                jdbcTemplate.update("DELETE FROM books WHERE author = :author", parameterSource);
            } else if (matcherTitle.find()) {
                logger.info("remove book by author completed: " + book);
                jdbcTemplate.update("DELETE FROM books WHERE title = :title", parameterSource);
            } else if (matcherSize.find()) {
                jdbcTemplate.update("DELETE FROM books WHERE size = :size", parameterSource);
            } else {
                logger.info("book don't found");
            }
        }
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
