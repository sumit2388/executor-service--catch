package net.one97.contest.audit.util;

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class PropertyLoader extends PropertyPlaceholderConfigurer{
	private static Properties prop;

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory,
			Properties props) throws BeansException {
		super.processProperties(beanFactory, props);
		prop=props;
	}

	public static String getProperty(String name) {
		return prop.getProperty(name);
	}
}
