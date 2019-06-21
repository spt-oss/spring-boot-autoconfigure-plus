/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.orm.jpa;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.boot.model.naming.ImplicitNamingStrategy; // TODO @checkstyle:ignore
import org.hibernate.boot.model.naming.PhysicalNamingStrategy; // TODO @checkstyle:ignore
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl; // TODO @checkstyle:ignore
import org.hibernate.jpa.boot.spi.IntegratorProvider; // TODO @checkstyle:ignore
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.jdbc.SchemaManagementProvider;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.StringUtils;

import lombok.NonNull;

/**
 * Custom {@link HibernateJpaConfiguration}
 */
@Configuration
public class CustomHibernateJpaConfiguration extends HibernateJpaConfiguration {
	
	/**
	 * {@link CustomJpaProperties}
	 */
	private final CustomJpaProperties properties;
	
	/**
	 * {@link IntegratorProvider}
	 */
	private final IntegratorProvider integratorProvider;
	
	/**
	 * Constructor
	 * 
	 * @param dataSource {@link DataSource}
	 * @param properties {@link CustomJpaProperties}
	 * @param beanFactory {@link ConfigurableListableBeanFactory}
	 * @param jtaTransactionManager {@link JtaTransactionManager}
	 * @param transactionManagerCustomizers {@link TransactionManagerCustomizers}
	 * @param hibernateProperties {@link HibernateProperties}
	 * @param metadataProviders {@link DataSourcePoolMetadataProvider}
	 * @param schemaManagementProvider {@link SchemaManagementProvider}
	 * @param physicalNamingStrategy {@link PhysicalNamingStrategy}
	 * @param implicitNamingStrategy {@link ImplicitNamingStrategy}
	 * @param hibernatePropertiesCustomizer {@link HibernatePropertiesCustomizer}
	 * @param integratorProvider {@link IntegratorProvider}
	 */
	public CustomHibernateJpaConfiguration(
	/* @formatter:off */
		DataSource dataSource,
		@NonNull CustomJpaProperties properties,
		ConfigurableListableBeanFactory beanFactory,
		ObjectProvider<JtaTransactionManager> jtaTransactionManager,
		ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers,
		HibernateProperties hibernateProperties,
		ObjectProvider<Collection<DataSourcePoolMetadataProvider>> metadataProviders,
		ObjectProvider<SchemaManagementProvider> schemaManagementProvider,
		ObjectProvider<PhysicalNamingStrategy> physicalNamingStrategy,
		ObjectProvider<ImplicitNamingStrategy> implicitNamingStrategy,
		ObjectProvider<HibernatePropertiesCustomizer> hibernatePropertiesCustomizer,
		@NonNull ObjectProvider<IntegratorProvider> integratorProvider) {
		/* @formatter:on */
		
		super(
		/* @formatter:off */
			dataSource,
			properties,
			beanFactory,
			jtaTransactionManager,
			transactionManagerCustomizers,
			hibernateProperties,
			metadataProviders,
			schemaManagementProvider,
			physicalNamingStrategy,
			implicitNamingStrategy,
			hibernatePropertiesCustomizer
			/* @formatter:on */
		);
		
		this.properties = properties;
		this.integratorProvider = integratorProvider.getIfAvailable();
	}
	
	@Bean
	@Override
	public PlatformTransactionManager transactionManager() {
		
		JpaTransactionManager transactionManager = (JpaTransactionManager) super.transactionManager();
		
		String persistenceUnitName = this.properties.getPersistenceUnitName();
		
		if (StringUtils.hasText(persistenceUnitName)) {
			
			transactionManager.setPersistenceUnitName(persistenceUnitName);
		}
		
		return transactionManager;
	}
	
	@Bean
	@Override
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
		
		LocalContainerEntityManagerFactoryBean factory = super.entityManagerFactory(builder);
		
		String persistenceUnitName = this.properties.getPersistenceUnitName();
		
		if (StringUtils.hasText(persistenceUnitName)) {
			
			factory.setPersistenceUnitName(persistenceUnitName);
		}
		
		return factory;
	}
	
	@Override
	protected void customizeVendorProperties(@NonNull Map<String, Object> vendorProperties) {
		
		super.customizeVendorProperties(vendorProperties);
		
		if (this.integratorProvider != null) {
			
			vendorProperties.put(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER, this.integratorProvider);
		}
	}
}
