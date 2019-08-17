package com.ascending.training.repository;

import com.ascending.training.model.Book;
import com.ascending.training.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BookDaoImpl implements BookDao{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public long save(Book book) {
        Transaction transaction = null;
        long bookId = 0;
        Boolean isSuccess = true;

        logger.warn("Before getSessionFactory.openSession().");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            logger.warn("Enter getSessionFactory.openSession().");
            transaction = session.beginTransaction();

            logger.warn("Enter beginTransaction().");
            bookId = (long) session.save(book);

            logger.warn("session.save() completes.");
            transaction.commit();
        } catch (Exception e) {
            isSuccess = false;
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(e.getMessage());
        }

        if (isSuccess) {
            logger.warn(String.format("The book %s has been inserted into the table.", book.toString()));
        }

        return bookId;
    }

    @Override
    public boolean update(Book book) {
        Transaction transaction = null;
        Boolean isSuccess = true;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(book);
            transaction.commit();
        } catch (Exception e) {
            isSuccess = false;
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(e.getMessage());
        }

        if (isSuccess) {
            logger.warn(String.format("The book %s has been updated.", book.toString()));
        }

        return isSuccess;
    }

    @Override
    public boolean delete(String bookTitle) {
        String hql = "delete Book where title = :bookTitle";         //  Placeholder with a name, not anonymous
        int deletedCount = 0;
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(hql);
            query.setParameter("bookTitle", bookTitle);
            transaction = session.beginTransaction();
            deletedCount = query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(e.getMessage());
        }

        logger.debug(String.format("The book %s has been deleted.", bookTitle));

        return deletedCount >= 1 ? true : false;
    }

    @Override
    public List<Book> getBooks() {
        String hql = "from Book";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(hql);
            return query.list();
        }
    }

    @Override
    public Book getBookByTitle(String bookTitle) {
        if (bookTitle == null) {
            return null;
        }

        String hql = "from Book as bk " +
                "left join fetch bk.issueStatuses as is " +
                "where lower(bk.title) = :bookTitle";

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(hql);
            query.setParameter("bookTitle", bookTitle.toLowerCase());

            Book book = query.uniqueResult();
            logger.debug(book.toString());

            return book;
        }
    }
}