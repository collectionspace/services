package org.collectionspace.services.common.storage.spring;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.collectionspace.services.common.storage.JDBCTools;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfiguration {
	@Bean
	@Qualifier("cspaceDataSource")
	public DataSource cspaceDataSource() throws NamingException {
		return JDBCTools.getDataSource(JDBCTools.CSPACE_DATASOURCE_NAME);
	}
}
