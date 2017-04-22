package smpl.catalog;

import java.util.Iterator;

import org.apache.commons.configuration.AbstractConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

@Configuration
@PropertySources(
		{
			@PropertySource(value = "classpath:TestConfiguration.properties", ignoreResourceNotFound=true)
		
		}
	)
@EnableMongoRepositories(basePackages="smpl.quote")
public class SpringConfig extends AbstractConfiguration {
	@Autowired
	private Environment properties;
	
	@Override
	public String getProperty(String propname) {
		return properties.getProperty(propname);
    }
	
	@Bean
    protected String getZipkinURL() {
        return properties.getProperty("zipkin.mrpservice.uri");
    }
	
   	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<String> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addPropertyDirect(String key, Object value) {
		// TODO Auto-generated method stub
		
	}
}
