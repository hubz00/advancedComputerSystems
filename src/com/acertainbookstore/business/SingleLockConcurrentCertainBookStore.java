package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.acertainbookstore.client.tests.ConcurrencyTest;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/** {@link SingleLockConcurrentCertainBookStore} implements the {@link BookStore} and
 * {@link StockManager} functionalities.
 * 
 * @see BookStore
 * @see StockManager
 */
public class SingleLockConcurrentCertainBookStore implements BookStore, StockManager {

	/** The mapping of books from ISBN to {@link BookStoreBook}. */
	private Map<Integer, BookStoreBook> bookMap = null;

	/** The lock that will take care of the book map */
	ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Instantiates a new {@link CertainBookStore}.
	 */
	public SingleLockConcurrentCertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addBooks(java.util.Set)
	 */
	public void addBooks(Set<StockBook> bookSet) throws BookStoreException {
		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		lock.writeLock().lock();
		try {
			// Check if all are there
			for (StockBook book : bookSet) {
				int isbn = book.getISBN();
				String bookTitle = book.getTitle();
				String bookAuthor = book.getAuthor();
				int noCopies = book.getNumCopies();
				float bookPrice = book.getPrice();

				if (BookStoreUtility.isInvalidISBN(isbn)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isEmpty(bookTitle)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isEmpty(bookAuthor)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isInvalidNoCopies(noCopies)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (bookPrice < 0.0) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (bookMap.containsKey(isbn)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.DUPLICATED);
				}
			}

			for (StockBook book : bookSet) {
				int isbn = book.getISBN();
				bookMap.put(isbn, new BookStoreBook(book));
				
			}

<<<<<<< HEAD
		}finally {lock.writeLock().unlock();}
			
=======
>>>>>>> d2e61e511bacfe54c80ee09438961b49cfa6f209
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addCopies(java.util.Set)
	 */
	public void addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {
		int isbn;
		int numCopies;

		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		lock.writeLock().lock();
		try {
			for (BookCopy bookCopy : bookCopiesSet) {
				isbn = bookCopy.getISBN();
				numCopies = bookCopy.getNumCopies();

				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
				}

				if (BookStoreUtility.isInvalidNoCopies(numCopies)) {
					throw new BookStoreException(BookStoreConstants.NUM_COPIES + numCopies + BookStoreConstants.INVALID);
				}
			}

			BookStoreBook book;
			Random r = new Random();

			// Update the number of copies
			for (BookCopy bookCopy : bookCopiesSet) {
				try {
					Thread.sleep(Math.abs(r.nextInt())%ConcurrencyTest.SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				isbn = bookCopy.getISBN();
				numCopies = bookCopy.getNumCopies();
				book = bookMap.get(isbn);
				for (int j = 0; j < numCopies;j++)
					book.addCopies(1);
			}
		}finally {lock.writeLock().unlock();}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooks()
	 */
	public List<StockBook> getBooks() {
		List<StockBook> listBooks = new ArrayList<>();
		lock.readLock().lock();
		try {
			Collection<BookStoreBook> bookMapValues = bookMap.values();

			for (BookStoreBook book : bookMapValues) {
				listBooks.add(book.immutableStockBook());
			}
		} finally {lock.readLock().unlock();}
		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#updateEditorPicks(java.util
	 * .Set)
	 */
	public void updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException {
		// Check that all ISBNs that we add/remove are there first.
		if (editorPicks == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int isbnValue;
		lock.writeLock().lock();
		try {
			for (BookEditorPick editorPickArg : editorPicks) {
				isbnValue = editorPickArg.getISBN();

				if (BookStoreUtility.isInvalidISBN(isbnValue)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(isbnValue)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			for (BookEditorPick editorPickArg : editorPicks) {
				bookMap.get(editorPickArg.getISBN()).setEditorPick(editorPickArg.isEditorPick());
			}
		} finally {lock.readLock().unlock();}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#buyBooks(java.util.Set)
	 */
	public void buyBooks(Set<BookCopy> bookCopiesToBuy) throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int isbn;
		BookStoreBook book;
		Boolean saleMiss = false;

		Map<Integer, Integer> salesMisses = new HashMap<>();
		
		lock.writeLock().lock();
		try {
			for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
				isbn = bookCopyToBuy.getISBN();

				if (bookCopyToBuy.getNumCopies() < 0) {
					lock.writeLock().unlock();
					throw new BookStoreException(
							BookStoreConstants.NUM_COPIES + bookCopyToBuy.getNumCopies() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isInvalidISBN(isbn)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(isbn)) {
					lock.writeLock().unlock();
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
				}

				book = bookMap.get(isbn);
				Random r = new Random();

				if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
					try {
						Thread.sleep(Math.abs(r.nextInt())% ConcurrencyTest.SLEEP);
					} catch (InterruptedException e) {
						lock.writeLock().unlock();
						e.printStackTrace();
					}
					// If we cannot sell the copies of the book, it is a miss.
					salesMisses.put(isbn, bookCopyToBuy.getNumCopies() - book.getNumCopies());
					saleMiss = true;
				}
			}

			// We throw exception now since we want to see how many books in the
			// order incurred misses which is used by books in demand
			if (saleMiss) {
				for (Map.Entry<Integer, Integer> saleMissEntry : salesMisses.entrySet()) {
					book = bookMap.get(saleMissEntry.getKey());
					book.addSaleMiss(saleMissEntry.getValue());
				}
				lock.writeLock().unlock();
				throw new BookStoreException(BookStoreConstants.BOOK + BookStoreConstants.NOT_AVAILABLE);
			}

			// Then make the purchase.
			for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
				book = bookMap.get(bookCopyToBuy.getISBN());
				for (int j = 0; j < bookCopyToBuy.getNumCopies();j++)
					book.buyCopies(1);
			}
		} finally {lock.writeLock().unlock();}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#getBooksByISBN(java.util.
	 * Set)
	 */
	public List<StockBook> getBooksByISBN(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<StockBook> listBooks = new ArrayList<>();

		lock.readLock().lock();
		try {
			for (Integer isbn : isbnSet) {
				listBooks.add(bookMap.get(isbn).immutableStockBook());
			}
		} finally {lock.readLock().unlock();}
		

		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getBooks(java.util.Set)
	 */
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		lock.readLock().lock();
		List<Book> listBooks = new ArrayList<>();
		try {
			// Check that all ISBNs that we rate are there to start with.
			for (Integer ISBN : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			
			for (Integer isbn : isbnSet) {
				listBooks.add(bookMap.get(isbn).immutableBook());
			}

			
		} finally { lock.readLock().unlock(); }
		
		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getEditorPicks(int)
	 */
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks + ", but it must be positive");
		}
		List<BookStoreBook> listAllEditorPicks = new ArrayList<>();
		List<Book> listEditorPicks = new ArrayList<>();
		
		lock.readLock().lock();
		try {
			Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet().iterator();
			BookStoreBook book;

			// Get all books that are editor picks.
			while (it.hasNext()) {
				Entry<Integer, BookStoreBook> pair = it.next();
				book = pair.getValue();

				if (book.isEditorPick()) {
					listAllEditorPicks.add(book);
				}
			}

			// Find numBooks random indices of books that will be picked.
			Random rand = new Random();
			Set<Integer> tobePicked = new HashSet<>();
			int rangePicks = listAllEditorPicks.size();

			if (rangePicks <= numBooks) {

				// We need to add all books.
				for (int i = 0; i < listAllEditorPicks.size(); i++) {
					tobePicked.add(i);
				}
			} else {

				// We need to pick randomly the books that need to be returned.
				int randNum;

				while (tobePicked.size() < numBooks) {
					randNum = rand.nextInt(rangePicks);
					tobePicked.add(randNum);
				}
			}

			// Get the numBooks random books.
			for (Integer index : tobePicked) {
				book = listAllEditorPicks.get(index);
				listEditorPicks.add(book.immutableBook());
			}

		} finally {lock.readLock().unlock();}
		
		return listEditorPicks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getTopRatedBooks(int)
	 */
	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooksInDemand()
	 */
	@Override
	public List<StockBook> getBooksInDemand() throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#rateBooks(java.util.Set)
	 */
	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#removeAllBooks()
	 */
	public void removeAllBooks() throws BookStoreException {
		lock.writeLock().lock();
		try {
			bookMap.clear();
		} finally { lock.writeLock().unlock();}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#removeBooks(java.util.Set)
	 */
	public void removeBooks(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		lock.writeLock().lock();
		try {
			for (Integer ISBN : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			for (int isbn : isbnSet) {
				bookMap.remove(isbn);
			}
		} finally {lock.writeLock().unlock();}

	}
}