package com.bionic.socialNetwork.dao.impl;

import com.bionic.socialNetwork.dao.PasswordDao;
import com.bionic.socialNetwork.dao.UserDao;
import com.bionic.socialNetwork.models.Group;
import com.bionic.socialNetwork.models.Interest;
import com.bionic.socialNetwork.models.Password;
import com.bionic.socialNetwork.models.User;
import com.bionic.socialNetwork.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dmytro Troshchuk, Denis Biyovskiy
 * @version 1.00  14.07.14.
 */
public class UserDaoImpl implements UserDao {
    @Override
    public void insert(User user, Password password) throws Exception {
        Session session;
        session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(user);
        password.setUserId(user.getId());
        session.save(password);
        session.getTransaction().commit();
        session.refresh(user);
        session.close();
    }

    @Override
    public User selectById(long id) throws Exception {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = (User) session.get(User.class, id);
            return user;
        }
        finally {
            if(session!= null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public User selectByLogin(String login) throws Exception {

        Session session = HibernateUtil.getSessionFactory().openSession();

        Query query = session.createQuery(
                "SELECT id FROM User where login = '" + login + "'");
        List<Long> list = query.list();
        session.close();
        if (list.size() != 0) {
            User user = selectById(list.get(0));
            return user;
        } else {
            return null;
        }


    }

    @Override
    public List<User> selectByName(String name, String surname, long beginId) throws Exception {
        int limit = 10;
        List<User> returnUser = new ArrayList<User>();
        Session session = HibernateUtil.getSessionFactory().openSession();

        Query query = session.createQuery(
                "FROM User WHERE name = '" + name + "' AND surname = '" + surname + "' AND id >= " + beginId);
        List<User> list = query.list();
        session.close();
        for (User user : list) {
            if (user.getName().equals(name)
                    && user.getSurname().equals(surname)
                    && returnUser.size() < limit
                    ) {
                returnUser.add(user);
            }
        }
        return returnUser;
    }

    @Override
    public List<User> selectNext(long beginId) throws Exception {
        int limit = 10;
        Session session = HibernateUtil.getSessionFactory().openSession();

        Query query = session.createQuery(
                "FROM User WHERE id >= " + beginId);
        query.setMaxResults(10);
        List<User> users = query.list();
        session.close();

        return users;
    }

    @Override
    public void delete(User user) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        PasswordDao passwordDao = new PasswordDaoImpl();
        Password password = passwordDao.selectById(user.getId());
        session.beginTransaction();
        session.delete(password);
        session.delete(user);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public void update(User user) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.update(user);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public void deleteInterests(Interest interest, User user) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        SQLQuery query = session.createSQLQuery(
                "DELETE FROM Users_Interests WHERE user_id = " + user.getId() +
                        " AND interest_id = " + interest.getInterests_id() + ";");

        query.executeUpdate();

        session.close();
    }

    @Override
    public List<User> selectFollowingsNext(int lot) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(User.class);
        criteria.createAlias("myFollowings", "followingsAlias");
        criteria.setMaxResults(10);
        criteria.addOrder(Order.asc("id"));
        criteria.setFirstResult(lot * 10);

        List<User> list = criteria.list();
        session.close();
        return list;
    }

    @Override
    public List<User> selectFollowingsByName(String name, String surname, long id, int lot) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(User.class);
        criteria.createAlias("myFollowings", "followingsAlias");
        criteria.setMaxResults(10);
        criteria.addOrder(Order.asc("id"));
        criteria.setFirstResult(lot * 10);

        List<User> list = criteria.list();
        List<User> resultList = new LinkedList<User>();
        session.close();
        for (User user : list) {
            if (user.getName().equals(name)
                    && user.getSurname().equals(surname)
                    && resultList.size() < 10) {
                resultList.add(user);
            }
        }
        return resultList;
    }

    @Override
    public void insertFollowing(User user, User hisFriend) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        SQLQuery query = session.createSQLQuery(
                "INSERT INTO Followings VALUES (" + user.getId() + ", " +
                        hisFriend.getId() + ");");

        query.executeUpdate();

        session.close();
    }

    @Override
    public void deleteFollowing(User user, User hisFollowing) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        SQLQuery query = session.createSQLQuery(
                "DELETE FROM Followings WHERE follower_id = " + user.getId() +
                        " AND following_id = " + hisFollowing.getId() + ";");

        query.executeUpdate();

        session.close();
    }

    @Override
    public List<Group> selectUserGroupsNext(long id, int lot) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Group.class);
        criteria.createAlias("followers", "followersAlias");
        criteria.setMaxResults(10);
        criteria.addOrder(Order.asc("id"));
        criteria.setFirstResult(lot * 10);

        List<Group> list = criteria.list();
        session.close();
        return list;
    }

    @Override
    public List<Group> selectUserGroupsByName(long id, int lot, String name) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Group.class);
        criteria.createAlias("followers", "followersAlias");
        criteria.addOrder(Order.asc("id"));
        criteria.setFirstResult(lot * 10);

        List<Group> list = criteria.list();
        List<Group> resultList = new LinkedList<Group>();
        session.close();
        for (Group group : list) {
            if (group.getName().equals(name)
                    && resultList.size() < 10) {
                resultList.add(group);
            }
        }
        return resultList;
    }

    @Override
    public List<Interest> selectAllInterests(long id) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Interest.class);
        criteria.createAlias("users", "usersAlias");
        criteria.add(Restrictions.eq("usersAlias.id", id));
        List<Interest> list = criteria.list();
        session.close();
        return list;
    }
}