package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.*;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import org.junit.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class ConcurrencyTest {


    /** The Constant TEST_ISBN. */
    private static final int TEST_ISBN = 3044560;

    /** The Constant NUM_COPIES. */
    private static final int NUM_COPIES = 5;

    /** The local test. */
    private static boolean localTest = true;

    /** Single lock test */
    private static boolean singleLock = true;

    private static final Integer test2Number = 1000;

    public static boolean test2Result = true;


    public final static Integer SLEEP = 5;



    /** The store manager. */
    public static StockManager storeManager;

    /** The client. */
    public static BookStore client;

    /**
     * Sets the up before class.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
            localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;

            String singleLockProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_SINGLE_LOCK);
            singleLock = (singleLockProperty != null) ? Boolean.parseBoolean(singleLockProperty) : singleLock;

            if (localTest) {
                if (singleLock) {
                    SingleLockConcurrentCertainBookStore store = new SingleLockConcurrentCertainBookStore();
                    storeManager = store;
                    client = store;
                } else {
                    TwoLevelLockingConcurrentCertainBookStore store = new TwoLevelLockingConcurrentCertainBookStore();
                    storeManager = store;
                    client = store;
                }
            } else {
                storeManager = new StockManagerHTTPProxy("http://localhost:8081/stock");
                client = new BookStoreHTTPProxy("http://localhost:8081");
            }

            storeManager.removeAllBooks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to get the default book used by initializeBooks.
     *
     * @return the default book
     */
    public StockBook getDefaultBook() {
        return new ImmutableStockBook(TEST_ISBN, "Harry Potter and JUnit", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,
                false);
    }

    /**
     * Method to add a book, executed before every test case is run.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @Before
    public void initializeBooks() throws BookStoreException {
        Set<StockBook> booksToAdd = new HashSet<StockBook>();
        booksToAdd.add(getDefaultBook());

    }

    /**
     *
     * @throws BookStoreException
     */
    @Test

        int copies = 1000;
        int numberOfOperations = 1000;

        // configure the initial state
        Set<BookCopy> bookCopiesSet1 = new HashSet<BookCopy>();
        bookCopiesSet1.add(new BookCopy(TEST_ISBN, numberOfOperations * copies));

        storeManager.addCopies(bookCopiesSet1);

        HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
        booksToBuy.add(new BookCopy(TEST_ISBN, copies)); // valid

        Set<BookCopy> bookCopiesSet = new HashSet<BookCopy>();
        bookCopiesSet.add(new BookCopy(TEST_ISBN, copies));

        // start client threads
        Test1Client1 client1 = new Test1Client1(numberOfOperations,booksToBuy);
        Test1Client2 client2 = new Test1Client2(numberOfOperations,bookCopiesSet);


        client1.start();
        client2.start();

        client1.join();
        client2.join();

        StockBook bookAfter  = storeManager.getBooks().get(0);

        assertTrue(1000005 == bookAfter.getNumCopies());
    }



    /**
     * Method to clean up the book store, execute after every test case is run.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @After
    public void cleanupBooks() throws BookStoreException {
        storeManager.removeAllBooks();
    }

    /**
     * Tear down after class.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws BookStoreException {
        storeManager.removeAllBooks();

        if (!localTest) {
            ((BookStoreHTTPProxy) client).stop();
            ((StockManagerHTTPProxy) storeManager).stop();
        }
    }

    @Test
    public void testAddBuyCopiesConcurrent() throws BookStoreException, InterruptedException {

        StockBook book1 = getDefaultBook();
        StockBook book2 = new ImmutableStockBook(TEST_ISBN + 1, "Harry Potter and JUnit2", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);
        StockBook book3 = new ImmutableStockBook(TEST_ISBN + 2, "Harry Potter and JUnit3", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);
        StockBook book4 = new ImmutableStockBook(TEST_ISBN + 3, "Harry Potter and JUnit4", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);
        StockBook book5 = new ImmutableStockBook(TEST_ISBN + 4, "Harry Potter and JUnit5", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);

        Set<StockBook> booksToAdd = new HashSet<>();
        booksToAdd.add(book2);
        booksToAdd.add(book3);
        booksToAdd.add(book4);
        booksToAdd.add(book5);

        storeManager.addBooks(booksToAdd);

        booksToAdd.add(book1);

        Set<BookCopy> addCopies = new HashSet<>();
        addCopies.add(new BookCopy(book1.getISBN(), 5));
        addCopies.add(new BookCopy(book2.getISBN(), 5));
        addCopies.add(new BookCopy(book3.getISBN(), 5));
        addCopies.add(new BookCopy(book4.getISBN(), 5));
        addCopies.add(new BookCopy(book5.getISBN(), 5));

        Set<BookCopy> booksToRemove = new HashSet<>();
        booksToRemove.add(new BookCopy(book1.getISBN(), 5));
        booksToRemove.add(new BookCopy(book2.getISBN(), 5));
        booksToRemove.add(new BookCopy(book3.getISBN(), 5));
        booksToRemove.add(new BookCopy(book4.getISBN(), 5));
        booksToRemove.add(new BookCopy(book5.getISBN(), 5));


        Test2Client1 client1 = new Test2Client1(test2Number, addCopies, booksToRemove);
        Test2Client2 client2 = new Test2Client2(test2Number);

        client1.start();
        client2.start();

        client1.join();
        client2.join();

        assertTrue(test2Result);
    }

    @Test
    public void testAddRemoveBooksConcurrent() throws BookStoreException, InterruptedException {
        StockBook book1 = getDefaultBook();
        StockBook book2 = new ImmutableStockBook(TEST_ISBN + 1, "Harry Potter and JUnit2", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);
        StockBook book3 = new ImmutableStockBook(TEST_ISBN + 2, "Harry Potter and JUnit3", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);
        StockBook book4 = new ImmutableStockBook(TEST_ISBN + 3, "Harry Potter and JUnit4", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);
        StockBook book5 = new ImmutableStockBook(TEST_ISBN + 4, "Harry Potter and JUnit5", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,  false);

        Set<StockBook> booksToAdd = new HashSet<>();
        booksToAdd.add(book2);
        booksToAdd.add(book3);
        booksToAdd.add(book4);
        booksToAdd.add(book5);

        storeManager.addBooks(booksToAdd);

        booksToAdd.add(book1);

        Set<Integer> booksToRemove = booksToAdd.stream().map(Book::getISBN).collect(Collectors.toSet());

        Test4Client1 client1 = new Test4Client1(test2Number, booksToAdd,booksToRemove);
        Test4Client2 client2 = new Test4Client2(test2Number, booksToAdd.size());

        client1.start();
        client2.start();

        client1.join();
        client2.join();

        assertTrue(testAddRemoveBooksConcurrentResult);
    }

}