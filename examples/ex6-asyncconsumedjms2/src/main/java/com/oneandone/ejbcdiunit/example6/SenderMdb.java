package com.oneandone.ejbcdiunit.example6;

import java.util.HashMap;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author aschoerk
 */

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@MessageDriven(name = "SenderMdb", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/example6"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "destination='sender'")})
public class SenderMdb implements MessageListener {

    @EJB(name = "RemoteServiceIntf")
    RemoteServiceIntf remoteService;

    @Inject
    private JmsDispatcher jmsDispatcher;

    /**
     * Passes a message to the listener.
     *
     * @param message the message passed to the listener
     */
    @Override
    public void onMessage(Message message) {
        MapMessage mapMessage = (MapMessage)message;
        try {
            HashMap<String, Object> result;
            switch (mapMessage.getString("operation")) {
                case "newEntity1":
                    final long l = remoteService.newEntity1(mapMessage.getInt("intValue"),
                        mapMessage.getString("stringValue"));
                    result = jmsDispatcher.initializeMessageForCallback(mapMessage);
                    result.put("id", l);
                    jmsDispatcher.send(result);
                    break;
                case "getStringValueFor":
                    final String stringFor = remoteService.getStringValueFor(mapMessage.getLong("id"));
                    result = jmsDispatcher.initializeMessageForCallback(mapMessage);
                    result.put("stringFor", stringFor);
                    jmsDispatcher.send(result);
                    break;
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
