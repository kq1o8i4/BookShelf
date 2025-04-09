package com.example.bookshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
	List<Book> findByStatus(String status);

	// タイトル、著者、ステータスが一致するか確認するメソッド
	boolean existsByTitleAndAuthorAndStatus(String title, String author, String status);

	// レビューが存在する「read」ステータスの本を取得（空文字やnullを除外）
	List<Book> findByStatusAndReviewIsNotNullAndReviewNot(String status, String empty);

}
