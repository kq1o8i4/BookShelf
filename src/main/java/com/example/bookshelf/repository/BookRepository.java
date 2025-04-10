package com.example.bookshelf.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bookshelf.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
	List<Book> findByStatus(String status);
	
	boolean existsByTitleAndAuthorAndStatus(String title, String author, String status);
	
	List<Book> findByStatusAndReviewIsNotNullAndReviewNot(String status, String empty);

}
