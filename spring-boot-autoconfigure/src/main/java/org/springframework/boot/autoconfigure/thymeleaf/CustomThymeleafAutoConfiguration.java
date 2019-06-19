/*
 * Copyright 2017-2018 the original author or authors.
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

package org.springframework.boot.autoconfigure.thymeleaf;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.extras.minify.dialect.MinifierDialect;
import org.thymeleaf.templatemode.TemplateMode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Custom {@link ThymeleafAutoConfiguration}
 */
@Configuration
@EnableConfigurationProperties(CustomThymeleafProperties.class)
@ConditionalOnClass(TemplateMode.class)
@AutoConfigureBefore(ThymeleafAutoConfiguration.class)
@AutoConfigureAfter({ WebMvcAutoConfiguration.class, WebFluxAutoConfiguration.class })
public class CustomThymeleafAutoConfiguration extends ThymeleafAutoConfiguration {
	
	/**
	 * {@link CustomThymeleafProperties}
	 * 
	 * @return {@link CustomThymeleafProperties}
	 */
	@Bean
	@Primary
	public CustomThymeleafProperties thymeleafProperties() {
		
		return new CustomThymeleafProperties();
	}
	
	/**
	 * {@link Configuration}: {@link MinifierDialect}
	 */
	@Configuration
	@ConditionalOnProperty(prefix = CustomThymeleafProperties.PREFIX, name = "minify.enabled", havingValue = "true")
	@ConditionalOnClass(MinifierDialect.class)
	@RequiredArgsConstructor
	protected static class ThymeleafMinifierDialectConfiguration {
		
		/**
		 * {@link CustomThymeleafProperties}
		 */
		@NonNull
		private final CustomThymeleafProperties properties;
		
		/**
		 * {@link Bean}: {@link MinifierDialect}
		 * 
		 * @return {@link MinifierDialect}
		 */
		@Bean
		@ConditionalOnMissingBean
		public MinifierDialect minifierDialect() {
			
			return new MinifierDialect(this.properties.getMinify().getHandlerClass());
		}
	}
}
