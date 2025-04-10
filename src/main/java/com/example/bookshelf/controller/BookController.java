// ディレクトリ: controller/BookController.java
package com.example.bookshelf.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.repository.BookRepository;

@Controller
public class BookController {
	@Autowired
	private BookRepository bookRepository;

	@GetMapping("/mypage")
	public String showMyPage(Model model) {
		List<Book> allReadingBooks = bookRepository.findByStatus("reading");
		List<Book> allReadBooks = bookRepository.findByStatus("read");

		// 最大7件に制限
		List<Book> readingBooks = allReadingBooks.stream().limit(3).toList();
		List<Book> readBooks = allReadBooks.stream().limit(3).toList();

		model.addAttribute("readingBooks", readingBooks);
		model.addAttribute("readBooks", readBooks);

		// 8件以上あるか判定
		model.addAttribute("hasMoreReading", allReadingBooks.size() > 3);
		model.addAttribute("hasMoreRead", allReadBooks.size() > 3);

		return "mypage";
	}

	@GetMapping("/readinglist")
	public String showReadingList(
			@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "titleKeyword", required = false) String titleKeyword,
			Model model) {

		// 「読み途中」の書籍を取得
		List<Book> readingBooks = bookRepository.findByStatus("reading");

		// 作者で絞り込み
		if (author != null && !author.isEmpty()) {
			readingBooks = readingBooks.stream()
					.filter(book -> book.getAuthor().equals(author))
					.collect(Collectors.toList());
		}

		// タイトルのキーワードで絞り込み（部分一致）
		if (titleKeyword != null && !titleKeyword.isEmpty()) {
			readingBooks = readingBooks.stream()
					.filter(book -> book.getTitle() != null && book.getTitle().contains(titleKeyword))
					.collect(Collectors.toList());
		}

		// 作者別にグループ化
		Map<String, List<Book>> booksByAuthor = readingBooks.stream()
				.collect(Collectors.groupingBy(Book::getAuthor));

		// 作者リスト（全体対象のため、最初に取得）
		List<String> authors = bookRepository.findByStatus("reading").stream()
				.map(Book::getAuthor)
				.distinct()
				.collect(Collectors.toList());

		model.addAttribute("booksByAuthor", booksByAuthor);
		model.addAttribute("authors", authors);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("titleKeyword", titleKeyword); // ← 新たに追加
		return "readinglist";
	}

	@GetMapping("/readlist")
	public String showReadList(
			@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "titleKeyword", required = false) String titleKeyword,
			Model model) {

		// 「読了済み」の書籍を取得
		List<Book> readBooks = bookRepository.findByStatus("read");

		// 作者で絞り込み
		if (author != null && !author.isEmpty()) {
			readBooks = readBooks.stream()
					.filter(book -> book.getAuthor().equals(author))
					.collect(Collectors.toList());
		}

		// タイトルのキーワードで絞り込み（部分一致）
		if (titleKeyword != null && !titleKeyword.isEmpty()) {
			readBooks = readBooks.stream()
					.filter(book -> book.getTitle() != null && book.getTitle().contains(titleKeyword))
					.collect(Collectors.toList());
		}

		// 作者別にグループ化
		Map<String, List<Book>> booksByAuthor = readBooks.stream()
				.collect(Collectors.groupingBy(Book::getAuthor));

		// 作者のリストを取得（元の全リストから抽出）
		List<String> authors = bookRepository.findByStatus("read").stream()
				.map(Book::getAuthor)
				.distinct()
				.collect(Collectors.toList());

		model.addAttribute("booksByAuthor", booksByAuthor);
		model.addAttribute("authors", authors);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("titleKeyword", titleKeyword); // ← ここが新規追加

		return "readlist";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("book", new Book());
		return "create";
	}

	@PostMapping("/create")
	public String createBook(@Valid @ModelAttribute Book book, BindingResult result, Model model) {
		// 既存の書籍情報と完全一致するデータが存在するかを確認
		if (bookRepository.existsByTitleAndAuthorAndStatus(book.getTitle(), book.getAuthor(), book.getStatus())) {
			result.rejectValue("title", "error.book", "この書籍は既に登録されています。");
			return "create"; // エラーを返し、再度入力フォームに戻る
		}

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

	@GetMapping("/review")
	public String showReviews(@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "title", required = false) String title,
			Model model) {
		// レビューが存在する「read」ステータスの本を取得
		List<Book> readBooksWithReview = bookRepository.findByStatusAndReviewIsNotNullAndReviewNot("read", "");

		// もし author が指定されていれば絞り込む
		if (author != null && !author.isEmpty()) {
			readBooksWithReview = readBooksWithReview.stream()
					.filter(book -> book.getAuthor().equals(author))
					.collect(Collectors.toList());
		}

		// もし title が指定されていれば絞り込む
		if (title != null && !title.isEmpty()) {
			readBooksWithReview = readBooksWithReview.stream()
					.filter(book -> book.getTitle().equals(title))
					.collect(Collectors.toList());
		}

		// 絞り込んだリストをモデルに追加
		model.addAttribute("readBooks", readBooksWithReview);

		// その他の情報（作者やタイトルのリストなど）もモデルに追加
		List<String> authors = readBooksWithReview.stream()
				.map(Book::getAuthor)
				.distinct()
				.collect(Collectors.toList());
		List<String> titles = readBooksWithReview.stream()
				.map(Book::getTitle)
				.distinct()
				.collect(Collectors.toList());

		model.addAttribute("authors", authors);
		model.addAttribute("titles", titles);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("selectedTitle", title);

		return "review";
	}

	@GetMapping("/bookchoice")
	public String showBookChoice(@RequestParam(required = false) String status, Model model) {
		List<Book> books;

		if (status != null && !status.isEmpty()) {
			books = bookRepository.findByStatus(status);
			model.addAttribute("selectedStatus", status); // 選択状態保持用
		} else {
			books = bookRepository.findAll();
		}

		model.addAttribute("books", books);
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
