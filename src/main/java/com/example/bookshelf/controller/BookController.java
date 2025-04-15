package com.example.bookshelf.controller;

import java.util.List;
import java.util.Map;

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
import com.example.bookshelf.service.BookService;

@Controller
public class BookController {

	@Autowired
	private BookService bookService;

	@GetMapping("/mypage")
	public String showMyPage(Model model) {
		List<Book> allReadingBooks = bookService.findByStatus("reading");
		List<Book> allReadBooks = bookService.findByStatus("read");
		model.addAttribute("readingBooks", allReadingBooks.stream().limit(3).toList());
		model.addAttribute("readBooks", allReadBooks.stream().limit(3).toList());
		model.addAttribute("hasMoreReading", allReadingBooks.size() > 3);
		model.addAttribute("hasMoreRead", allReadBooks.size() > 3);
		return "mypage";
	}

	@GetMapping("/readinglist")
	public String showReadingList(
			@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "titleKeyword", required = false) String titleKeyword,
			Model model) {

		List<Book> books = bookService.findByStatus("reading");
		books = bookService.filterByAuthorAndTitle(books, author, titleKeyword);
		Map<String, List<Book>> booksByAuthor = bookService.groupByAuthor(books);
		List<String> authors = bookService.getDistinctAuthorsByStatus("reading");

		model.addAttribute("booksByAuthor", booksByAuthor);
		model.addAttribute("authors", authors);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("titleKeyword", titleKeyword);
		return "readinglist";
	}

	@GetMapping("/readlist")
	public String showReadList(
			@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "titleKeyword", required = false) String titleKeyword,
			Model model) {

		List<Book> books = bookService.findByStatus("read");
		books = bookService.filterByAuthorAndTitle(books, author, titleKeyword);
		Map<String, List<Book>> booksByAuthor = bookService.groupByAuthor(books);
		List<String> authors = bookService.getDistinctAuthorsByStatus("read");

		model.addAttribute("booksByAuthor", booksByAuthor);
		model.addAttribute("authors", authors);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("titleKeyword", titleKeyword);
		return "readlist";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("book", new Book());
		return "create";
	}

	@PostMapping("/create")
	public String createBook(@Valid @ModelAttribute Book book, BindingResult result, Model model) {
		if (bookService.existsByTitleAuthorStatus(book)) {
			result.rejectValue("title", "error.book", "この書籍は既に登録されています。");
			return "create";
		}
		bookService.prepareBookDates(book);
		if (result.hasErrors()) {
			return "create";
		}
		bookService.save(book);
		return "redirect:/mypage";
	}

	@GetMapping("/review")
	public String showReviews(
			@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "title", required = false) String title,
			Model model) {

		List<Book> readBooks = bookService.findReviewedBooks(author, title);
		List<String> authors = bookService.extractDistinctAuthors(readBooks);
		List<String> titles = bookService.extractDistinctTitles(readBooks);

		model.addAttribute("readBooks", readBooks);
		model.addAttribute("authors", authors);
		model.addAttribute("titles", titles);
		model.addAttribute("selectedAuthor", author);
		model.addAttribute("selectedTitle", title);

		return "review";
	}

	@GetMapping("/bookchoice")
	public String showBookChoice(@RequestParam(required = false) String status, Model model) {
		List<Book> books = (status != null && !status.isEmpty())
				? bookService.findByStatus(status)
				: bookService.findAll();
		model.addAttribute("books", books);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("selectedBookId", new Book());
		return "bookchoice";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Book book = bookService.findById(id);
		model.addAttribute("book", book);
		return "edit";
	}

	@PostMapping("/edit/{id}")
	public String updateBook(@PathVariable Long id, @Valid @ModelAttribute Book updatedBook, BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("book", updatedBook);
			return "edit";
		}
		bookService.updateBook(id, updatedBook);
		return "redirect:/mypage";
	}

	@GetMapping("/delete")
	public String showDeletePage() {
		return "delete";
	}

	@PostMapping("/delete/{id}")
	public String deleteBook(@PathVariable Long id) {
		bookService.deleteById(id);
		return "redirect:/delete";
	}
}
