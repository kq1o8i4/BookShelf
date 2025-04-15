package com.example.bookshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Book;

// Book エンティティに対するリポジトリインターフェース（データアクセス層）
// JpaRepository を継承することで基本的な CRUD 操作が自動で提供される
public interface BookRepository extends JpaRepository<Book, Long> {

	// 指定されたステータス（"reading" or "read"）の書籍を全件取得
	List<Book> findByStatus(String status);

	// 同じタイトル・著者・ステータスの書籍がすでに存在するか確認（重複登録防止）
	boolean existsByTitleAndAuthorAndStatus(String title, String author, String status);

	// レビューが空でない（null または "" でない）"read" ステータスの書籍を取得
	List<Book> findByStatusAndReviewIsNotNullAndReviewNot(String status, String empty);
}
