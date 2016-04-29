package nl.rubix.eos.mqfabric.javaconsumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaConsumer {
	private static final Logger LOG = LoggerFactory
			.getLogger(JavaConsumer.class);

	private static final Boolean NON_TRANSACTED = false;
	private static final String CONNECTION_FACTORY_NAME = "myJmsFactory";
	private static final String DESTINATION_NAME = "queue/simple";
	private static final int MESSAGE_TIMEOUT_MILLISECONDS = 120000;

	public static void main(String args[]) {
		final String brokerUrl = System.getProperty("java.naming.provider.url");

		if (brokerUrl != null) {
			LOG.info("******************************");
			LOG.info("Overriding jndi brokerUrl, now using: {}", brokerUrl);
			LOG.info("******************************");
		}

		Connection connection = null;

		try {
			// JNDI lookup of JMS Connection Factory and JMS Destination
			Context context = new InitialContext();
			ConnectionFactory factory = (ConnectionFactory) context
					.lookup(CONNECTION_FACTORY_NAME);
			Destination destination = (Destination) context
					.lookup(DESTINATION_NAME);

			connection = factory.createConnection();
			connection.start();

			Session session = connection.createSession(NON_TRANSACTED,
					Session.AUTO_ACKNOWLEDGE);
			MessageConsumer consumer = session.createConsumer(destination);

			LOG.info("Start consuming messages from {} with {}ms timeout",
					destination, MESSAGE_TIMEOUT_MILLISECONDS);

			// Synchronous message consumer
			int i = 1;
			while (true) {
				Message message = consumer
						.receive(MESSAGE_TIMEOUT_MILLISECONDS);
				if (message != null) {
					if (message instanceof TextMessage) {
						String text = ((TextMessage) message).getText();
						LOG.info("Got {}. message: {}", i++, text);
					}
				} else {
					break;
				}
			}

			consumer.close();
			session.close();
		} catch (Throwable t) {
			LOG.error("Error receiving message", t);
		} finally {
			// Cleanup code
			// In general, you should always close producers, consumers,
			// sessions, and connections in reverse order of creation.
			// For this simple example, a JMS connection.close will
			// clean up all other resources.
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					LOG.error("Error closing connection", e);
				}
			}
		}
	}
}
