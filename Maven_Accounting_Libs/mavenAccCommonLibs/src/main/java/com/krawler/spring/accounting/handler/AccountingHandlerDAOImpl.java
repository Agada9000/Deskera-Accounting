/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.krawler.spring.accounting.handler;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
/**
 *
 * @author krawler
 */
public class AccountingHandlerDAOImpl implements AccountingHandlerDAO {
    private HibernateTemplate hibernateTemplate;
//    private BeanFactory factory;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
//        this.factory = getBeanFactory();
	}

    public KwlReturnObject getObject(String classpath, String id) throws ServiceException {
        List list = new ArrayList();
        try {
            Class cls = Class.forName(classpath);
            Object obj = hibernateTemplate.get(cls, id);
            list.add(obj);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public static Object getObject(HibernateTemplate hibernateTemplate, String classpath, String id) throws ServiceException {
        Object obj = null;
        try {
            Class cls = Class.forName(classpath);
            obj = hibernateTemplate.get(cls, id);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return obj;
    }

/*
    public Object invokeMethod(String modulename, String method, Object[] params) throws ServiceException {
        Object result = null;
        String beanid = "";
        try {
//            BeanFactory factory = getBeanFactory();
            beanid = ConfigReader.getinstance().get(modulename+"BeanId");

            Object beanobj = factory.getBean(beanid);
            Class cl1 = beanobj.getClass();
            Object invoker = beanobj;

            int len = params.length;
            Class[] arguments = new Class[len];
            for(int i=0; i<len ; i++){
                arguments[i] = params[i].getClass();
            }
            
            java.lang.reflect.Method objMethod1 = cl1.getMethod(method, arguments);
            result = objMethod1.invoke(invoker, params);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("invokeMethod.IllegalAccessException, MethodName="+method, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("invokeMethod.IllegalArgumentException, MethodName="+method, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("invokeMethod.TargetMethodInvocationException, MethodName="+method, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("invokeMethod.NoSuchMethodException, MethodName="+method, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("invokeMethod.SecurityException, MethodName="+method, ex);
        } catch (NoSuchBeanDefinitionException ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("invokeMethod.NoSuchBeanDefinitionException, BeanId="+beanid, ex);
        }
        return result;
    }

    private BeanFactory getBeanFactory() {
//        BeanFactory factory = new XmlBeanFactory(new FileSystemResource("classpath:../dispatcher-servlet.xml"));
//        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:../dispatcher-servlet.xml");
//        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] {"classpath:../applicationContext.xml", "classpath:../dispatcher-servlet.xml"});
//        BeanFactory factory = (BeanFactory) appContext;
        BeanFactory bfactory = new XmlBeanFactory(new ClassPathResource("../dispatcher-servlet.xml"));
        return bfactory;
    }
*/

}
