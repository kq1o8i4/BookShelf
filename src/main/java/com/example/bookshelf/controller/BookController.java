// ディレクトリ: controller/BookController.java
package com.example.bookshelf.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.repository.BookRepository;

@Controller
public class BookController {
	@Autowired
	private BookRepository bookRepository;

	@GetMapping("/mypage")
	public String showMyPage(Model model) {
		List<Book> readingBooks = bookRepository.findByStatus("reading");
		List<Book> readBooks = bookRepository.findByStatus("read");
		model.addAttribute("readingBooks", readingBooks);
		model.addAttribute("readBooks", readBooks);
		return "mypage";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("book", new Book());
		return "create";
	}

	@PostMapping("/create")
	public String createBook(@Valid @ModelAttribute Book book, BindingResult result, Model model) {
		if ("reading".equals(book.getStatus())) {
			book.setReview(null);
			book.setEndDate(null); // 読了日は不要
		} else if ("read".equals(book.getStatus())) {
			book.setStartDate(null); // 開始日は不要
		}

		if (result.hasErrors()) {
			return "create";
		}

		bookRepository.save(book);
		return "redirect:/mypage";
	}

	// /review に対する処理を追加
	@GetMapping("/review")
	public String showReviews(Model model) {
		List<Book> readBooks = bookRepository.findByStatus("read");
		model.addAttribute("readBooks", readBooks);
		return "review";
	}

	@GetMapping("/bookchoice")
	public String showBookChoice(Model model) {
		// すべての書籍を取得
		List<Book> books = bookRepository.findAll();

		// モデルに書籍リストを追加
		model.addAttribute("books", books);
		// 空のBookオブジェクトをidフィールド用に追加
		model.addAttribute("selectedBookId", new Book());

		return "bookchoice";
	}

	// /edit ページの表示
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
		model.addAttribute("book", book);
		return "edit";
	}

	// /edit ページでの更新処理
	@PostMapping("/edit/{id}")
	public String updateBook(@PathVariable Long id, Book updatedBook) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));

		// 変更内容を更新
		book.setTitle(updatedBook.getTitle());
		book.setAuthor(updatedBook.getAuthor());
		book.setStatus(updatedBook.getStatus());
		book.setStartDate(updatedBook.getStartDate());
		book.setEndDate(updatedBook.getEndDate());
		book.setReview(updatedBook.getReview());

		bookRepository.save(book);
		return "redirect:/mypage"; // マイページにリダイレクト
	}
	
	// 書籍削除処理
	@PostMapping("/delete/{id}")
	public String deleteBook(@PathVariable Long id) {
		bookRepository.deleteById(id);
		return "redirect:/delete";
	}

	// 削除後の確認ページ
	@GetMapping("/delete")
	public String showDeletePage() {
		return "delete";
	}

}