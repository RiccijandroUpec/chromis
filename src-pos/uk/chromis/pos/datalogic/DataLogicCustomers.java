/*
**    Chromis POS  - Open Source Point of Sale
**
**    This file is part of Chromis POS Version Chromis V1.5.4
**
**    Copyright (c) 2015-2023 Chromis & previous Openbravo POS related works   
**
**    https://www.chromis.co.uk
**   
**    Chromis POS is free software: you can redistribute it and/or modify
**    it under the terms of the GNU General Public License as published by
**    the Free Software Foundation, either version 3 of the License, or
**    (at your option) any later version.
**
**    Chromis POS is distributed in the hope that it will be useful,
**    but WITHOUT ANY WARRANTY; without even the implied warranty of
**    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
**    GNU General Public License for more details.
**
**    You should have received a copy of the GNU General Public License
**    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>
**
 */
package uk.chromis.pos.datalogic;

import java.util.List;
import uk.chromis.basic.BasicException;
import uk.chromis.data.loader.DataParams;
import uk.chromis.data.loader.DataRead;
import uk.chromis.data.loader.Datas;
import uk.chromis.data.loader.PreparedSentence;
import uk.chromis.data.loader.QBFBuilder;
import uk.chromis.data.loader.SentenceExec;
import uk.chromis.data.loader.SentenceExecTransaction;
import uk.chromis.data.loader.SentenceFind;
import uk.chromis.data.loader.SentenceList;
import uk.chromis.data.loader.SerializerRead;
import uk.chromis.data.loader.SerializerReadBasic;
import uk.chromis.data.loader.SerializerReadInteger;
import uk.chromis.data.loader.SerializerWriteBasic;
import uk.chromis.data.loader.SerializerWriteBasicExt;
import uk.chromis.data.loader.SerializerWriteParams;
import uk.chromis.data.loader.SerializerWriteString;
import uk.chromis.data.loader.Session;
import uk.chromis.data.loader.StaticSentence;
import uk.chromis.data.loader.TableDefinition;
import uk.chromis.pos.customers.CustomerInfo;
import uk.chromis.pos.customers.CustomerInfoExt;
import uk.chromis.pos.forms.BeanFactoryDataSingle;
import uk.chromis.pos.sales.CustomerDeliveryInfo;

public class DataLogicCustomers extends BeanFactoryDataSingle {

    private SentenceExec receivedCustomer;
    private SentenceFind m_CustomerPresent;
    protected Session s;
    private TableDefinition tcustomers;
    private static final Datas[] customerdatas = new Datas[]{
        Datas.STRING,
        Datas.TIMESTAMP,
        Datas.TIMESTAMP,
        Datas.STRING,
        Datas.STRING,
        Datas.STRING,
        Datas.INT,
        Datas.BOOLEAN,
        Datas.STRING};

    @Override
    public void init(Session s) {
        this.s = s;

        m_CustomerPresent = new PreparedSentence(s, "select count(*) from customers where customertype = 'account' and card = ? ", SerializerWriteString.INSTANCE, SerializerReadInteger.INSTANCE);
        receivedCustomer = new StaticSentence(s, "update reservations set isdone = true where id = ? ", SerializerWriteString.INSTANCE);
    }

    public SentenceList getCustomerList(String siteGuid) {
        if (siteGuid == null) {
            return new StaticSentence(s, new QBFBuilder("select id, taxid, name, postal, email, "
                    + "phone from customers where visible = " + s.DB.TRUE()
                    + " and ?(QBF_FILTER) order by lower (name)",
                    new String[]{"taxid", "name", "postal", "phone", "email"}), new SerializerWriteBasic(new Datas[]{
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING}), new SerializerRead() {
                @Override
                public Object readValues(DataRead dr) throws BasicException {
                    CustomerInfo c = new CustomerInfo(dr.getString(1));
                    c.setTaxid(dr.getString(2));
                    c.setName(dr.getString(3));
                    c.setPostal(dr.getString(4));
                    c.setPhone(dr.getString(5));
                    c.setEmail(dr.getString(6));

                    return c;
                }
            });
        } else {
            return new StaticSentence(s, new QBFBuilder("select id, taxid, name, postal, email, "
                    + "phone from customers where visible = " + s.DB.TRUE() + " and siteguid ='" + siteGuid + "'"
                    + " and ?(QBF_FILTER) order by lower (name)",
                    new String[]{"taxid", "name", "postal", "phone", "email"}), new SerializerWriteBasic(new Datas[]{
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.STRING}), new SerializerRead() {
                @Override
                public Object readValues(DataRead dr) throws BasicException {
                    CustomerInfo c = new CustomerInfo(dr.getString(1));
                    c.setTaxid(dr.getString(2));
                    c.setName(dr.getString(3));
                    c.setPostal(dr.getString(4));
                    c.setPhone(dr.getString(5));
                    c.setEmail(dr.getString(6));

                    return c;
                }
            });

        }

    }

    /**
     *
     * @param customer
     * @return
     * @throws BasicException
     */
    public int updateCustomerExt(final CustomerInfoExt customer) throws BasicException {

        return new PreparedSentence(s, "update customers set notes = ? where id = ?", SerializerWriteParams.INSTANCE
        ).exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setString(1, customer.getNotes());
                setString(2, customer.getId());
            }
        });
    }

    public final void receiveCustomer(String id) throws BasicException {
        receivedCustomer.exec(id);
    }

    /**
     *
     * @return customer's existing reservation (restaurant mode)
     */
    public final SentenceList getReservationsList() {
        return new PreparedSentence(s, "select r.id, r.created, r.datenew, c.customer, customers.taxid, coalesce(customers.name, r.title),  r.chairs, r.isdone, r.description "
                + "from reservations r left outer join reservation_customers c on r.id = c.id left outer join customers on c.customer = customers.id "
                + "where r.datenew >= ? and r.datenew < ?", new SerializerWriteBasic(new Datas[]{Datas.TIMESTAMP, Datas.TIMESTAMP}), new SerializerReadBasic(customerdatas));
    }

    /**
     *
     * @return create/update customer reservation (restaurant mode)
     */
    public final SentenceExec getReservationsUpdate() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {

                new PreparedSentence(s, "delete from reservation_customers where id = ?", new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
                if (((Object[]) params)[3] != null) {
                    new PreparedSentence(s, "insert into reservation_customers (id, customer) values (?, ?)", new SerializerWriteBasicExt(customerdatas, new int[]{0, 3})).exec(params);
                }
                return new PreparedSentence(s, "update reservations set id = ?, created = ?, datenew = ?, title = ?, chairs = ?, isdone = ?, description = ? where id = ?", new SerializerWriteBasicExt(customerdatas, new int[]{0, 1, 2, 5, 6, 7, 8, 0})).exec(params);
            }
        };
    }

    /**
     *
     * @return delete customer reservation (restaurant mode)
     */
    public final SentenceExec getReservationsDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s, "delete from reservation_customers where id = ?", new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
                return new PreparedSentence(s, "delete from reservations where id = ?", new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
            }
        };
    }

    /**
     *
     * @return insert a new customer reservation (restaurant mode)
     */
    public final SentenceExec getReservationsInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {

                int i = new PreparedSentence(s, 
                        "insert into reservations (id, created, datenew, title, chairs, isdone, description) values (?, ?, ?, ?, ?, ?, ?)", 
                        new SerializerWriteBasicExt(customerdatas, new int[]{0, 1, 2, 5, 6, 7, 8})).exec(params);

                if (((Object[]) params)[3] != null) {
                    new PreparedSentence(s, 
                            "insert into reservation_customers (id, customer) values (?, ?)", 
                            new SerializerWriteBasicExt(customerdatas, new int[]{0, 3})).exec(params);
                }
                return i;
            }
        };
    }

    /**
     *
     * @return assign a table to a customer reservation (restaurant mode)
     */
    public final TableDefinition getTableCustomers() {
        return tcustomers;
    }

    public final Boolean customerExists(String customerCard) {
        try {
            Integer i = (Integer) m_CustomerPresent.find(customerCard);
            return (i > 0);
        } catch (BasicException ex) {
            return false;
        }
    }

    public final Boolean customerExists(String customerCard, Boolean activeStatus) {
        try {
            Integer i = (Integer) m_CustomerPresent.find(customerCard);
            return (i > 0);
        } catch (BasicException ex) {
            return false;
        }
    }

    public final CustomerDeliveryInfo fetchCustomerDelivery(String id) throws BasicException {
        return (CustomerDeliveryInfo) new PreparedSentence(s, "select "
                + "id,"
                + "name, "
                + "addressline1, "
                + "addressline2, "
                + "addressline3, "
                + "postcode, "
                + "phone, "
                + " deliverydate, "
                + "delivered, "
                + "comments "
                + "from customer_delivery "
                + "where id = ? ",
                SerializerWriteString.INSTANCE,
                CustomerDeliveryInfo.getSerializerRead()).find(id);
    }

    public final List<CustomerDeliveryInfo> fetchWaitingCustomerDeliveries() throws BasicException {
        return new PreparedSentence(s, "select "
                + "id,"
                + "name, "
                + "addressline1, "
                + "addressline2, "
                + "addressline3, "
                + "postcode, "
                + "phone, "
                + " deliverydate, "
                + "delivered, "
                + "comments "
                + "from customer_delivery "
                + "where delivered = false ", SerializerWriteString.INSTANCE, CustomerDeliveryInfo.getSerializerRead()).list();
    }

    public final List<CustomerDeliveryInfo> fetchAllCustomerDeliveries() throws BasicException {
        return new PreparedSentence(s, "select "
                + "id,"
                + "name, "
                + "addressline1, "
                + "addressline2, "
                + "addressline3, "
                + "postcode, "
                + "phone, "
                + " deliverydate, "
                + "delivered, "
                + "comments "
                + "from customer_delivery "
                + "where delivered = false ", null, CustomerDeliveryInfo.getSerializerRead()).list();
    }

}
